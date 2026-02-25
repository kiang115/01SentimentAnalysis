package org.example.sentimentanalysis.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.assembler.TrainDataAssembler;
import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.dto.responseDto.TrainLineChartSend;
import org.example.sentimentanalysis.dto.responseDto.TrainTasksSend;
import org.example.sentimentanalysis.enums.TaskDataSourceEnum;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

//        得到大版本号前缀
        String majorPrefix = trainPanelRec.getMajorVersion()+".";
//        2. 查询当前领域或者当前领域+大版本 的模型列表
        List<Models> domainModels = modelsService.list(
                new LambdaQueryWrapper<Models>()
                        .eq(Models::getDomainId, trainPanelRec.getDomainId())
                        .like(trainPanelRec.getIsOverTrain()==false,Models::getModelVersion,majorPrefix)
        );

//      1. 获取最大版本号
        String latestVersionStr = modelsService.getMaxVersion(domainModels);

//          2. 生成下一个版本号
        String nextModelVersion = modelsService.getNextModelVersion(latestVersionStr, trainPanelRec.getIsOverTrain());

//          3. 计算回填的基准版本号 (只有非全量训练才需要回填)
        String baseModelVersion = Boolean.FALSE.equals(trainPanelRec.getIsOverTrain()) ? latestVersionStr : null;

//        4. 按领域批量查询训练数据
        List<TrainData> allTrainDataList = trainDataService.list(
                new LambdaQueryWrapper<TrainData>()
                        .eq(TrainData::getDomainId, trainPanelRec.getDomainId()));
//        将查询结果按数据来源(source字段)进行分组，形成 Map<数据来源, 数据列表> 的结构
        Map<String, List<TrainData>> sourceDataMap = allTrainDataList.stream()
                .collect(Collectors.groupingBy(TrainData::getSource));

//        5. 按前端配置数量随机筛选；不足直接抛异常
        Random random = new Random(trainPanelRec.getRandomSeed());
//       6. 根据sourceMap中遍历，对每个数据来源进行筛选 sourceDataMap可能只包含某两个或者一个领域，并非全部领域

        List<TrainData> correctedDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TaskDataSourceEnum.CORRECTED.getCode(), Collections.emptyList()),
                trainPanelRec.getCorrectedNum(),
                TaskDataSourceEnum.CORRECTED.getCode(),
                domain.getDomainName(),
                random
        );
        List<TrainData> uploadDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TaskDataSourceEnum.UPLOAD.getCode(), Collections.emptyList()),
                trainPanelRec.getUploadNum(),
                TaskDataSourceEnum.UPLOAD.getCode(),
                domain.getDomainName(),
                random
        );
        List<TrainData> originalDataList = pickTrainDataBySource(
                sourceDataMap.getOrDefault(TaskDataSourceEnum.ORIGINAL.getCode(), Collections.emptyList()),
                trainPanelRec.getOriginalNum(),
                TaskDataSourceEnum.ORIGINAL.getCode(),
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

        Domains domain = domainsService.getById(trainPanelRec.getDomainId());
        if (domain == null) {
            throw new CustomBusinessException("未找到ID为[" + trainPanelRec.getDomainId() + "]的Domain数据");
        }

        long totalData = trainPanelRec.getCorrectedNum()
                + trainPanelRec.getUploadNum()
                + trainPanelRec.getOriginalNum();

        TrainTasks trainTask = new TrainTasks()
                .setCreatorId(1L)
                .setStatus(TaskStatusEnum.PROCESSING.getCode())
                .setDomainId(trainPanelRec.getDomainId())
                .setDomainName(domain.getDomainName())
                .setCorrectedNum(trainPanelRec.getCorrectedNum())
                .setUploadNum(trainPanelRec.getUploadNum())
                .setOriginalNum(trainPanelRec.getOriginalNum())
                .setTotalData(totalData)
                .setLoraR(trainPanelRec.getLoraR())
                .setLoraAlpha(trainPanelRec.getLoraAlpha())
                .setEpochs(trainPanelRec.getEpochs())
                .setBatchSize(trainPanelRec.getBatchSize())
                .setLearningRate(trainPanelRec.getLearningRate())
                .setRandomSeed(trainPanelRec.getRandomSeed())
                .setLoraModules(trainPanelRec.getLoraModules())
                .setTrainSplitRatio(trainPanelRec.getTrainSplitRatio())
                .setIfOverTrain(trainPanelRec.getIsOverTrain() == true ? 1 : 0);

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
                .setModelVersion(trainRec.getModelVersion())
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

    @Override
    public TrainTasksSend listTrainTask() {
//        是否是否有进行中状态的task
        LambdaQueryWrapper<TrainTasks> queryWrapper = new LambdaQueryWrapper<TrainTasks>()
                .eq(TrainTasks::getStatus, TaskStatusEnum.PROCESSING.getCode());
        Integer ifAllFinished = count(queryWrapper) > 0 ? 0 : 1;
        TrainTasksSend trainTasksSend = new TrainTasksSend();
        trainTasksSend.setIfAllFinished(ifAllFinished);
        trainTasksSend.setTrainTasksList(list());
        return trainTasksSend;
    }

    @Override
    public TrainLineChartSend getTrainLineChart() {
        // 1. 数据来源：domains 表一次查询，train_tasks 表一次查询（仅 status=2 已完成任务）
        List<Domains> domains = domainsService.list();
        Map<Long, String> domainIdToName = domains.stream()
                .collect(Collectors.toMap(Domains::getDomainId, Domains::getDomainName, (a, b) -> a));
        List<TrainTasks> successTasks = list(new LambdaQueryWrapper<TrainTasks>()
                .eq(TrainTasks::getStatus, TaskStatusEnum.SUCCESS.getCode()));
        Map<Long, List<TrainTasks>> tasksByDomain = successTasks.stream()
                .collect(Collectors.groupingBy(TrainTasks::getDomainId));

        List<String> domainNameList = new ArrayList<>();
        List<TrainLineChartSend.DomainLinesData> domainLinesDataList = new ArrayList<>();

        // 2. 按领域顺序遍历，保证 domainNameList 与 domainLinesDataList 一一对应
        for (Domains domain : domains) {
            Long domainId = domain.getDomainId();
            String domainName = domainIdToName.get(domainId);
            List<TrainTasks> tasks = tasksByDomain.getOrDefault(domainId, Collections.emptyList());

            // 3. 版本解析与范围：解析 model_version 得到 maxMajor、maxMinor；无任务时均为 0（解析失败由 parseVersion 直接抛异常）
            List<ModelsServiceImpl.VersionParts> parts = tasks.stream()
                    .map(t -> modelsService.parseVersion(t.getModelVersion()))
                    .toList();
            int maxMajor = parts.isEmpty() ? 0 : parts.stream().mapToInt(ModelsServiceImpl.VersionParts::major).max().orElse(0);
            int maxMinor = parts.isEmpty() ? 0 : parts.stream().mapToInt(ModelsServiceImpl.VersionParts::minor).max().orElse(0);

            // 4. version -> accuracy 映射，便于 O(1) 查找（同上，非法 version 已在步骤 3 抛异常）
            Map<String, BigDecimal> versionToAccuracy = tasks.stream()
                    .filter(t -> t.getModelVersion() != null && t.getAccuracy() != null)
                    .collect(Collectors.toMap(TrainTasks::getModelVersion, TrainTasks::getAccuracy, (a, b) -> b));

            // 5. 组装 DTO：横坐标小版本号 X.0、X.1、X.2…；大版本号 0.X、1.X、2.X
            List<String> allSmallVersions = IntStream.rangeClosed(0, maxMinor)
                    .mapToObj(m -> "X." + m)
                    .toList();
            List<TrainLineChartSend.MajorVersionData> majorVersionDataList = new ArrayList<>();
            List<String> allMajorVersions = new ArrayList<>();
            for (int major = 0; major <= maxMajor; major++) {
                final int majorVal = major;
                List<BigDecimal> versionAccuracyList = IntStream.rangeClosed(0, maxMinor)
                        .mapToObj(minor -> versionToAccuracy.get(majorVal + "." + minor))
                        .toList();
                majorVersionDataList.add(TrainLineChartSend.MajorVersionData.builder()
                        .majorVersion(majorVal + ".X")
                        .versionAccuracyList(versionAccuracyList)
                        .build());
                allMajorVersions.add(majorVal + ".X");
            }
            domainLinesDataList.add(TrainLineChartSend.DomainLinesData.builder()
                    .domainName(domainName)
                    .allSmallVersions(allSmallVersions)
                    .allMajorVersions(allMajorVersions)
                    .majorVersionDataList(majorVersionDataList)
                    .build());
            domainNameList.add(domainName);
        }

        return TrainLineChartSend.builder()
                .domainNameList(domainNameList)
                .domainLinesDataList(domainLinesDataList)
                .build();
    }

    /**
     * 随机筛选指定数量训练数据，不足直接抛业务异常
     */
    private List<TrainData> pickTrainDataBySource(List<TrainData> sourceDataList,
                                                  Long requiredCount,
                                                  String source,
                                                  String domainName,
                                                  Random random) {
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
}
