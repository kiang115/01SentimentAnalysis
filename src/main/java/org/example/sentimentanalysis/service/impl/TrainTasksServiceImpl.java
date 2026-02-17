package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.sentimentanalysis.assembler.TrainDataAssembler;
import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.mapper.TrainTasksMapper;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import org.example.sentimentanalysis.service.TrainDataService;
import org.example.sentimentanalysis.service.TrainTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>
 * 训练任务管理表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainTasksServiceImpl extends ServiceImpl<TrainTasksMapper, TrainTasks> implements TrainTasksService {
    private static final String SOURCE_CORRECTED = "corrected";
    private static final String SOURCE_UPLOAD = "upload";
    private static final String SOURCE_ORIGINAL = "original";
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private TrainDataService trainDataService;
    @Autowired
    private TrainDataAssembler trainDataAssembler;

    @Override
    public TrainDataSend getTrainData(TrainPanelRec trainPanelRec) {
//        1. 校验领域存在并拿到领域信息
        Domains domain = Optional.ofNullable(domainsService.getById(trainPanelRec.getDomainId()))
                .orElseThrow(() -> new CustomBusinessException("训练数据装配失败：领域不存在，domainId=" + trainPanelRec.getDomainId()));

//        2. 查询该领域最新模型版本并计算新版本
        Models latestModel = modelsService.getOne(
                new LambdaQueryWrapper<Models>()
                        .eq(Models::getDomainId, trainPanelRec.getDomainId())
                        .orderByDesc(Models::getModelVersion)
                        .last("limit 1")
        );
        if (latestModel == null || latestModel.getModelVersion() == null) {
            throw new CustomBusinessException("训练数据装配失败：领域[" + domain.getDomainName() + "]没有可用模型版本");
        }
        BigDecimal nextModelVersion = calculateNextModelVersion(latestModel.getModelVersion());

//        3. 按领域+来源批量查询训练数据（source固定三类）
        List<String> sourceList = List.of(SOURCE_CORRECTED, SOURCE_UPLOAD, SOURCE_ORIGINAL);
        List<TrainData> allTrainDataList = trainDataService.list(
                new LambdaQueryWrapper<TrainData>()
                        .eq(TrainData::getDomainId, trainPanelRec.getDomainId())
                        .in(TrainData::getSource, sourceList)
        );
        Map<String, List<TrainData>> sourceDataMap = allTrainDataList.stream()
                .collect(Collectors.groupingBy(TrainData::getSource));

//        4. 按前端配置数量随机筛选；不足直接抛异常
        Random random = new Random(trainPanelRec.getRandomSeed());
        List<TrainData> correctedDataList = pickTrainDataBySource(sourceDataMap.getOrDefault(SOURCE_CORRECTED, Collections.emptyList()),
                trainPanelRec.getCorrectedNum(), SOURCE_CORRECTED, domain.getDomainName(), random);
        List<TrainData> uploadDataList = pickTrainDataBySource(sourceDataMap.getOrDefault(SOURCE_UPLOAD, Collections.emptyList()),
                trainPanelRec.getUploadNum(), SOURCE_UPLOAD, domain.getDomainName(), random);
        List<TrainData> originalDataList = pickTrainDataBySource(sourceDataMap.getOrDefault(SOURCE_ORIGINAL, Collections.emptyList()),
                trainPanelRec.getOriginalNum(), SOURCE_ORIGINAL, domain.getDomainName(), random);

//        5. 委托 assembler 装配发送给模型服务的数据
        return trainDataAssembler.toTrainDataSend(
                trainPanelRec,
                domain,
                nextModelVersion,
                correctedDataList,
                uploadDataList,
                originalDataList
        );
    }

    /**
     * 随机筛选指定数量训练数据，不足直接抛业务异常
     */
    private List<TrainData> pickTrainDataBySource(List<TrainData> sourceDataList,
                                                  Long requiredCount,
                                                  String source,
                                                  String domainName,
                                                  Random random) {
        if (requiredCount <= 0) {
            throw new CustomBusinessException("训练数据装配失败：领域[" + domainName + "] source[" + source + "] 目标数量必须大于0");
        }
        if (sourceDataList.size() < requiredCount) {
            throw new CustomBusinessException(
                    "训练数据装配失败：领域[" + domainName + "] source[" + source + "] 数据不足，目标数量="
                            + requiredCount + "，可用数量=" + sourceDataList.size()
            );
        }
        List<TrainData> copiedList = new ArrayList<>(sourceDataList);
        Collections.shuffle(copiedList, random);
        return copiedList.subList(0, Math.toIntExact(requiredCount));
    }

    /**
     * 模型版本规则：
     * 0.1 -> 0.2
     * 0.14 -> 0.15
     */
    private BigDecimal calculateNextModelVersion(BigDecimal latestModelVersion) {
        BigDecimal normalizedVersion = latestModelVersion.stripTrailingZeros();
        BigDecimal increment = normalizedVersion.scale() <= 1
                ? new BigDecimal("0.1")
                : new BigDecimal("0.01");
        return normalizedVersion.add(increment).stripTrailingZeros();
    }
}
