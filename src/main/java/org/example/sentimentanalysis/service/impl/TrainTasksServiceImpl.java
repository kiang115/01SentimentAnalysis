package org.example.sentimentanalysis.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bouncycastle.math.raw.Mod;
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

        if (trainPanelRec.getIsOverTrain() == false && trainPanelRec.getMajorVersion() == null) {
            throw new CustomBusinessException("增量训练需要选择大版本号");
        }
//        得到大版本号前缀
        String majorPrefix = trainPanelRec.getMajorVersion() + ".";
//        2. 查询当前领域或者当前领域+大版本 的模型列表
        List<Models> domainModels = modelsService.list(
                new LambdaQueryWrapper<Models>()
                        .eq(Models::getDomainId, trainPanelRec.getDomainId())
                        .like(trainPanelRec.getIsOverTrain() == false, Models::getModelVersion, majorPrefix)
        );

//      1. 获取全部的最大版本号
        String latestAllVersionStr = modelsService.getMaxVersion(domainModels);

//          2. 生成下一个版本号(只能使用历史历史全部模型生成唯一的版本号,无论是否被删除了)
        String nextModelVersion = modelsService.getNextModelVersion(latestAllVersionStr, trainPanelRec.getIsOverTrain());

//          3. 计算回填的基准版本号 (如果需要增量训练,用现存版本号回填,不存在抛异常)
        String baseModelVersion = null;
        if (trainPanelRec.getIsOverTrain() == false) {
            List<Models> domainExistModels = domainModels.stream().filter(model->model.getDeleted()==0).toList();
            if (domainExistModels.isEmpty()) {
                throw new CustomBusinessException("选择增量训练,但是本大模型版本已无可用基础版本");
            }
            baseModelVersion = modelsService.getMaxVersion(domainExistModels);
        }


//        4. 按领域批量查询训练数据
        List<TrainData> allTrainDataList = trainDataService.list(
                new LambdaQueryWrapper<TrainData>()
                        .eq(TrainData::getDomainId, trainPanelRec.getDomainId()));
//        将查询结果按数据来源(source字段)进行分组，形成 Map<数据来源, 数据列表> 的结构
        Map<String, List<TrainData>> sourceDataMap = allTrainDataList.stream()
                .collect(Collectors.groupingBy(TrainData::getSource));

//        5. 按前端配置数量随机筛选；不足直接抛异常
//      todo  需要根据训练数据标签 每个标签各一半筛选,否者导致标签分布不均匀
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

    /**
     * 获取训练任务折线图数据
     * 逻辑：按领域(Domain)分组，以小版本(Minor)为横坐标，大版本(Major)为不同的折线，展示准确率(Accuracy)走势。
     */
    @Override
    public TrainLineChartSend getTrainLineChart() {
        // --- 1. 数据准备阶段 ---

        // 查询所有领域信息，用于后续构建 ID 与 名称 的映射，避免在循环中重复查库
        List<Domains> domains = domainsService.list();
        Map<Long, String> domainIdToName = domainsService.getDomainIdToName();

        // 只查询状态为“成功(SUCCESS)”的训练任务，失败的任务不计入准确率统计
        List<TrainTasks> successTasks = list(new LambdaQueryWrapper<TrainTasks>()
                .eq(TrainTasks::getStatus, TaskStatusEnum.SUCCESS.getCode()));

        // 将所有成功的任务按 domainId 进行分组，形成 Map<领域ID, 任务列表>，方便 O(1) 时间复杂度提取特定领域的任务
        Map<Long, List<TrainTasks>> tasksByDomain = successTasks.stream()
                .collect(Collectors.groupingBy(TrainTasks::getDomainId));

        // 用于存储最终返回给前端的领域名称列表和各领域的数据详情
        List<String> domainNameList = new ArrayList<>();
        List<TrainLineChartSend.DomainLinesData> domainLinesDataList = new ArrayList<>();

        // --- 2. 核心逻辑处理阶段：按领域遍历 ---
        for (Domains domain : domains) {
            Long domainId = domain.getDomainId();
            String domainName = domainIdToName.get(domainId);
            // 获取当前领域下所有的成功任务，如果没有则返回空列表
            List<TrainTasks> tasks = tasksByDomain.getOrDefault(domainId, Collections.emptyList());

            // --- 3. 版本号解析与范围确定 ---
            // 将版本号字符串（如 "1.2"）解析成对象（major=1, minor=2），便于比较和计算
            List<ModelsServiceImpl.VersionParts> parts = tasks.stream()
                    .map(t -> modelsService.parseVersion(t.getModelVersion()))
                    .toList();

            // 计算当前领域内出现的最大大版本号（Major）和最大小版本号（Minor）
            // 这决定了图表的 X 轴长度（由 maxMinor 决定）和折线的条数（由 maxMajor 决定）
            int maxMajor = parts.isEmpty() ? 0 : parts.stream().mapToInt(ModelsServiceImpl.VersionParts::major).max().orElse(0);
            int maxMinor = parts.isEmpty() ? 0 : parts.stream().mapToInt(ModelsServiceImpl.VersionParts::minor).max().orElse(0);

            // --- 4. 建立“版本->准确率”映射表 ---
            // 将任务列表转换为 Map，Key 是版本号字符串，Value 是准确率。
            // 目的是为了在构建数据矩阵时，能快速找到某个特定版本对应的准确率。
            Map<String, BigDecimal> versionToAccuracy = tasks.stream()
                    .filter(t -> t.getModelVersion() != null && t.getAccuracy() != null)
                    .collect(Collectors.toMap(TrainTasks::getModelVersion, TrainTasks::getAccuracy, (a, b) -> b));

            // --- 5. 组装前端绘图所需的数据结构 ---

            // 生成横坐标（X轴）标签：例如 maxMinor 为 2，则生成 ["X.0", "X.1", "X.2"]
            List<String> allSmallVersions = IntStream.rangeClosed(0, maxMinor)
                    .mapToObj(m -> "X." + m)
                    .toList();

            List<TrainLineChartSend.MajorVersionData> majorVersionDataList = new ArrayList<>();
            List<String> allMajorVersions = new ArrayList<>();

            // 遍历从 0 到 maxMajor 的每一个大版本（每一条折线）
            for (int major = 0; major <= maxMajor; major++) {
                final int majorVal = major;

                // 关键逻辑：补齐数据。
                // 遍历从 0 到 maxMinor 的每一个小版本，如果数据库中存在该版本（如 1.1），则取准确率；
                // 如果不存在（如 1.2），则放入 null。这保证了每条折线在同一小版本位置都有对应点，方便前端渲染。
                List<BigDecimal> versionAccuracyList = IntStream.rangeClosed(0, maxMinor)
                        .mapToObj(minor -> versionToAccuracy.get(majorVal + "." + minor))
                        .toList();

                // 封装单条折线的数据（例如 "1.X" 这一层级的所有准确率点）
                majorVersionDataList.add(TrainLineChartSend.MajorVersionData.builder()
                        .majorVersion(majorVal + ".X") // 折线名称，如 "1.X"
                        .versionAccuracyList(versionAccuracyList) // 该折线的纵坐标数值列表
                        .build());

                // 记录所有的大版本名称，通常用于图例显示
                allMajorVersions.add(majorVal + ".X");
            }

            // --- 6. 汇总当前领域的所有数据 ---
            domainLinesDataList.add(TrainLineChartSend.DomainLinesData.builder()
                    .domainName(domainName)
                    .allSmallVersions(allSmallVersions) // 统一的 X 轴
                    .allMajorVersions(allMajorVersions)   // 所有的折线名
                    .majorVersionDataList(majorVersionDataList) // 详细的折线数据
                    .build());

            // 记录领域名称，用于外层 Tab 或 菜单 切换
            domainNameList.add(domainName);
        }

        // --- 7. 返回最终的 DTO 结果 ---
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
