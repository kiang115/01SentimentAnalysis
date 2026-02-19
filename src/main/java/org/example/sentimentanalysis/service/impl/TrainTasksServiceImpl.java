package org.example.sentimentanalysis.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.assembler.TrainDataAssembler;
import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
import org.example.sentimentanalysis.enums.TrainDataSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.TrainTasksMapper;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import org.example.sentimentanalysis.service.TrainDataService;
import org.example.sentimentanalysis.service.TrainTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

//        2. 查询领域模型并找到最新版本（版本号格式为 x.y）
        List<Models> domainModels = modelsService.list(
                new LambdaQueryWrapper<Models>()
                        .eq(Models::getDomainId, trainPanelRec.getDomainId())
        );
        if (domainModels.isEmpty()) {
            throw new CustomBusinessException("训练数据装配失败：领域[" + domain.getDomainName() + "]没有可用模型版本");
        }

        Models latestModel = domainModels.stream()
                .max(Comparator.comparing(m -> parseVersion(m.getModelVersion()),
                        Comparator.comparingInt(VersionParts::major).thenComparingInt(VersionParts::minor)))
                .orElseThrow(() -> new CustomBusinessException("训练数据装配失败：无法获取领域最新版本"));
        String latestVersionStr = latestModel.getModelVersion();
        VersionParts latestVersionParts = parseVersion(latestVersionStr);

//        3. 根据 isOverTrain 规则生成下一个版本号；isOverTrain=false 时需回填 baseModelVersion 为当前最新版本
        String nextModelVersion = nextVersion(latestVersionParts, trainPanelRec.getIsOverTrain());
        String baseModelVersion = Boolean.FALSE.equals(trainPanelRec.getIsOverTrain()) ? latestVersionStr : null;

//        4. 按领域+来源批量查询训练数据（source固定三类）
        List<TrainData> allTrainDataList = trainDataService.list(
                new LambdaQueryWrapper<TrainData>()
                        .eq(TrainData::getDomainId, trainPanelRec.getDomainId())
                        .in(TrainData::getSource, TrainDataSourceEnum.allCodes())
        );
        Map<String, List<TrainData>> sourceDataMap = allTrainDataList.stream()
                .collect(Collectors.groupingBy(TrainData::getSource));

//        5. 按前端配置数量随机筛选；不足直接抛异常
        Random random = new Random(trainPanelRec.getRandomSeed());
        List<TrainData> correctedDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TrainDataSourceEnum.CORRECTED.getCode(), Collections.emptyList()),
                trainPanelRec.getCorrectedNum(),
                TrainDataSourceEnum.CORRECTED.getCode(),
                domain.getDomainName(),
                random
        );
        List<TrainData> uploadDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TrainDataSourceEnum.UPLOAD.getCode(), Collections.emptyList()),
                trainPanelRec.getUploadNum(),
                TrainDataSourceEnum.UPLOAD.getCode(),
                domain.getDomainName(),
                random
        );
        List<TrainData> originalDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TrainDataSourceEnum.ORIGINAL.getCode(), Collections.emptyList()),
                trainPanelRec.getOriginalNum(),
                TrainDataSourceEnum.ORIGINAL.getCode(),
                domain.getDomainName(),
                random
        );

//        6. 委托 assembler 装配发送给模型服务的数据
        return trainDataAssembler.toTrainDataSend(
                trainPanelRec,
                domain,
                nextModelVersion,
                baseModelVersion,
                correctedDataList,
                uploadDataList,
                originalDataList
        );
    }

    @Override
    public Long addTrainTask(TrainPanelRec trainPanelRec) {
//        将trainPanelRec中对应值插入到trainTasks表中
//        trainTasks中 creatorId先写死为1，状态使用待处理，其他训练参数保持一致
        long totalData = trainPanelRec.getCorrectedNum()
                + trainPanelRec.getUploadNum()
                + trainPanelRec.getOriginalNum();

        TrainTasks trainTask = new TrainTasks()
                .setCreatorId(1L)
                .setStatus(TaskStatusEnum.PROCESSING.getCode())
                .setDomainId(trainPanelRec.getDomainId())
                .setTotalData(totalData)
                .setLoraR(trainPanelRec.getLoraR())
                .setLoraAlpha(trainPanelRec.getLoraAlpha())
                .setEpochs(trainPanelRec.getEpochs())
                .setBatchSize(trainPanelRec.getBatchSize())
                .setLearningRate(trainPanelRec.getLearningRate())
                .setRandomSeed(trainPanelRec.getRandomSeed())
                .setLoraModules(trainPanelRec.getLoraModules())
                .setTrainSplitRatio(trainPanelRec.getTrainSplitRatio())
                .setIfOverTrain(trainPanelRec.getIsOverTrain()==true?1:0);

//        保存并返回任务id
        boolean saveSuccess = save(trainTask);
        if (!saveSuccess || trainTask.getId() == null) {
            throw new CustomBusinessException("添加训练任务失败，未生成主键");
        }
        return trainTask.getId();
    }

    @Override
    public void updateByTrainRec(TrainResultRec trainRec, Long modelId) {
        TrainTasks trainTasks = new TrainTasks();
//        (end_time,duration,model_id,accuracy,precision_rate,recall_rate,f1_score，train_loss_list，train_acc_list,val_loss_list，val_acc_list)进行更新
        trainTasks.setId(trainRec.getTaskId())
                .setModelId(modelId)
                .setEndTime(trainRec.getEndTime())
                .setDuration(trainRec.getDuration().longValue())
                .setAccuracy(trainRec.getAccuracy())
                .setPrecisionRate(trainRec.getPrecisionRate())
                .setRecallRate(trainRec.getRecallRate())
                .setF1Score(trainRec.getF1Score())
                .setTrainLossList(trainRec.getTrainLossList())
                .setTrainAccList(trainRec.getTrainAccList())
                .setValLossList(trainRec.getValLossList())
                .setValAccList(trainRec.getValAccList())
                .setStatus(TaskStatusEnum.SUCCESS.getCode());
                updateById(trainTasks);
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

    /** 解析版本号，格式为 x.y（仅一个点分隔） */
    private VersionParts parseVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 2) {
            throw new CustomBusinessException("训练数据装配失败：模型版本格式非法，version=" + version);
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        return new VersionParts(major, minor);
    }

    /** 生成下一版本号：isOverTrain 为大版本+1.0，否则为小版本+1 */
    private String nextVersion(VersionParts latest, Boolean isOverTrain) {
        if (Boolean.TRUE.equals(isOverTrain)) {
            return (latest.major() + 1) + ".0";
        }
        return latest.major() + "." + (latest.minor() + 1);
    }

    private record VersionParts(int major, int minor) {
    }
}
