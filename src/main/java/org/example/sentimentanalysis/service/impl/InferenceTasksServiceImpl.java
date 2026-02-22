package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.sentimentanalysis.assembler.InferenceDataAssembler;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferPanelRec;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.dto.responseDto.InferPieChartSend;
import org.example.sentimentanalysis.dto.responseDto.InferTasksDetailSend;
import org.example.sentimentanalysis.dto.responseDto.TasksHotChartSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
import org.example.sentimentanalysis.enums.SortEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceRecordsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.example.sentimentanalysis.service.TrainTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.sentimentanalysis.enums.CommentStatusEnum.INFERRED;
import static org.example.sentimentanalysis.enums.CommentStatusEnum.PENDING;
import static org.example.sentimentanalysis.enums.TaskStatusEnum.SUCCESS;

/**
 * <p>
 * 推理任务详情表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class InferenceTasksServiceImpl extends ServiceImpl<InferenceTasksMapper, InferenceTasks> implements InferenceTasksService {
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private InferenceTasksAssembler inferenceTasksAssembler;
    @Autowired
    private InferenceDataAssembler inferenceDataAssembler;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private InferenceRecordsService inferenceRecordsService;
    @Autowired
    private TrainTasksService trainTasksService;

    @Override
    public List<InferTasksDetailSend> listAllTasks(List<InferenceTasks> tasks) {
//         加载所有的modelId->model映射
        Map<Long, Models> modelMap = modelsService.list().stream().collect(Collectors.toMap(Models::getModelId, model -> model));
//        加载所有domainId->domain映射
        Map<Long, Domains> domainMap = domainsService.list().stream().collect(Collectors.toMap(Domains::getDomainId, domain -> domain));

//
        return inferenceTasksAssembler.toDtoList(tasks, modelMap, domainMap);
    }

    @Override
    public InferDataSend getInferenceData(InferPanelRec inferPanelRec) {
        List<InferPanelRec.InferenceDomainPara> domainParas = inferPanelRec.getInferenceDomainPara();

        // 1. 批量数据预取 (准备原材料)
        Set<Long> domainIds = domainParas.stream().map(InferPanelRec.InferenceDomainPara::getDomainId).collect(Collectors.toSet());
        Set<Long> modelIds = domainParas.stream().map(InferPanelRec.InferenceDomainPara::getModelId).collect(Collectors.toSet());

        Map<Long, Domains> domainMap = domainsService.listByIds(domainIds).stream()
                .collect(Collectors.toMap(Domains::getDomainId, d -> d));
        Map<Long, Models> modelMap = modelsService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(Models::getModelId, m -> m));

        // 2. 批量查询并分组评论
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getStatus, PENDING.getCode())
                .in(Comments::getDomainId, domainIds)
                .orderByDesc(SortEnum.NEWEST_SORT.getKey().equals(inferPanelRec.getSort()), Comments::getPublishTime)
                .orderByAsc(SortEnum.LATEST_SORT.getKey().equals(inferPanelRec.getSort()), Comments::getPublishTime);

        Map<Long, List<Comments>> domainsToCommentsMap = commentsService.list(wrapper).stream()
                .collect(Collectors.groupingBy(Comments::getDomainId));

        // 3. 核心业务处理与转换
        List<InferDataSend.InferenceDomainData> domainDataList = domainParas.stream().map(para -> {
            // 获取并校验
            Domains domain = Optional.ofNullable(domainMap.get(para.getDomainId()))
                    .orElseThrow(() -> new CustomBusinessException("领域ID不存在: " + para.getDomainId()));

            Models model = Optional.ofNullable(modelMap.get(para.getModelId()))
                    .orElseThrow(() -> new CustomBusinessException("模型ID不存在: " + para.getModelId()));

            List<Comments> allComments = domainsToCommentsMap.getOrDefault(para.getDomainId(), Collections.emptyList());

            if (para.getInferenceReviewNums() <= 0) {
                throw new CustomBusinessException("领域 [" + domain.getDomainName() + "] 待处理评论数不能小于等于0");
            }
            // 校验业务规则：评论数是否足够
            if (allComments.size() < para.getInferenceReviewNums()) {
                throw new CustomBusinessException("领域 [" + domain.getDomainName() + "] 待处理评论不足");
            }

            // 截取业务需要的数量
            List<Comments> limitedComments = allComments.stream()
                    .limit(para.getInferenceReviewNums())
                    .toList();

            // 交给 Assembler 组装单条领域数据
            return inferenceDataAssembler.toInferenceDomainData(para, model, domain, limitedComments);
        }).toList();

        // 组装最终结果
        return InferDataSend.builder().inferenceDomainDataList(domainDataList).build();
    }

    //将推理任务插入到任务表中
    @Override
    @Transactional
    public Long addInferenceTasks(InferDataSend inferDataSend) {
//构造一个要插入的task数据
        InferenceTasks task = new InferenceTasks();
        List<InferDataSend.InferenceDomainData> domainDataList = inferDataSend.getInferenceDomainDataList();

        // 1. 获取 ModelId 列表 (去重)
        List<Long> modelIds = domainDataList.stream()
                .map(InferDataSend.InferenceDomainData::getModelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2. 获取 DomainId 列表 (不去重)
        List<Long> domainIds = domainDataList.stream()
                .map(InferDataSend.InferenceDomainData::getDomainId)
                .filter(Objects::nonNull)
                .toList();

        // 3. 计算评论总数 (注意处理 null 对象或 null 字段)
        long totalComments = domainDataList.stream()
                .filter(Objects::nonNull)
                .mapToLong(item -> item.getInferenceCommentNums() == null ? 0L : item.getInferenceCommentNums())
                .sum();
//        设置默认的发起人id为1 处理中状态默认为0，无需设置
        task.setDomainIds(domainIds).setUsedModelIds(modelIds).setProcessedCount(totalComments).setInitiatorId(1L);
        save(task);
        if (task.getTaskId() == null) {
            throw new CustomBusinessException("添加推理任务失败，未生成主键！");
        }
        return task.getTaskId();
    }

    @Override
    public void setInferenceTaskStatus(Long taskId, Integer status) {
        if (TaskStatusEnum.existsByCode(status)) {
            updateById(new InferenceTasks().setTaskId(taskId).setProcessStatus(status));
        }
        return;
    }

    @Override
    @Transactional
    public void setInferenceTaskSuccess(InferResultRec inferResultRec) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime taskEndTime = LocalDateTime.parse(inferResultRec.getTaskEndTime(), formatter);

        Long taskId = inferResultRec.getTaskId();
        Float taskDuration = inferResultRec.getTaskDuration();
        Long processCount = inferResultRec.getProcessCount();

        long inferenceDurationMs = (long) (taskDuration * 1000);
        BigDecimal avgProcessSpeed = BigDecimal.valueOf(processCount).divide(BigDecimal.valueOf(taskDuration), 2, RoundingMode.HALF_UP);

        this.updateById(new InferenceTasks()
                .setTaskId(taskId)
                .setInferenceEndTime(taskEndTime)
                .setInferenceDuration(inferenceDurationMs)
                .setAvgProcessSpeed(avgProcessSpeed)
                .setProcessStatus(SUCCESS.getCode()));

        List<InferResultRec.CommentResultList> results = inferResultRec.getResults();
        List<Comments> commentsToUpdate = results.stream()
                .map(r -> new Comments()
                        .setCommentId(r.getCommentId())
                        .setFinalSentiment(r.getModelSentiment())
                        .setStatus(INFERRED.getCode()))
                .toList();
        commentsService.updateBatchById(commentsToUpdate);

        List<InferenceRecords> recordsToSave = results.stream()
                .map(r -> new InferenceRecords()
                        .setInferenceTaskId(taskId)
                        .setCommentId(r.getCommentId())
                        .setModelId(r.getModelId())
                        .setDomainId(r.getDomainId())
                        .setPositiveProb(r.getPositiveProb())
                        .setNegativeProb(r.getNegativeProb())
                        .setModelSentiment(r.getModelSentiment())
                        .setInferenceTime(taskEndTime)
                        .setConfidence(r.getConfidence()))
                .toList();
        inferenceRecordsService.saveBatch(recordsToSave);
    }

    @Override
    public InferPieChartSend getInferPieChart() {
        // 1. 数据来源：领域列表决定饼图扇区顺序与领域名
        List<Domains> domains = domainsService.list();
        if (domains == null || domains.isEmpty()) {
            return InferPieChartSend.builder()
                    .pieNameList(List.of("已完成推理评论分布", "未完成推理评论分布"))
                    .domainNameList(Collections.emptyList())
                    .inferredPie(Collections.emptyList())
                    .unInferredPie(Collections.emptyList())
                    .build();
        }
        List<String> domainNameList = domains.stream()
                .map(Domains::getDomainName)
                .collect(Collectors.toList());

        // 2. 根据 comments.status=2/3/4 + domain_id 分组统计，得到已推理评论数量
        QueryWrapper<Comments> inferredWrapper = new QueryWrapper<>();
        inferredWrapper.select("domain_id", "count(*) as count_num")
                .lambda()
                .in(Comments::getStatus,
                        CommentStatusEnum.INFERRED.getCode(),
                        CommentStatusEnum.REVIEWING.getCode(),
                        CommentStatusEnum.CORRECTED.getCode())
                .groupBy(Comments::getDomainId);
        List<Map<String, Object>> inferredRows = commentsService.listMaps(inferredWrapper);
        Map<Long, Long> inferredCountByDomainId = inferredRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("domain_id")).longValue(),
                        row -> ((Number) row.get("count_num")).longValue()));

        // 3. 根据 comments.status=0/1 + domain_id 分组统计，得到未推理评论数量
        QueryWrapper<Comments> unInferredWrapper = new QueryWrapper<>();
        unInferredWrapper.select("domain_id", "count(*) as count_num")
                .lambda()
                .in(Comments::getStatus,
                        CommentStatusEnum.PENDING.getCode(),
                        CommentStatusEnum.INFERRING.getCode())
                .groupBy(Comments::getDomainId);
        List<Map<String, Object>> unInferredRows = commentsService.listMaps(unInferredWrapper);
        Map<Long, Long> unInferredCountByDomainId = unInferredRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("domain_id")).longValue(),
                        row -> ((Number) row.get("count_num")).longValue()));

        // 4. 按领域顺序组装 inferredPie、unInferredPie（无数据的领域数量为 0）
        List<InferPieChartSend.PieData> inferredPie = new ArrayList<>();
        List<InferPieChartSend.PieData> unInferredPie = new ArrayList<>();
        for (Domains domain : domains) {
            Long domainId = domain.getDomainId();
            String domainName = domain.getDomainName();
            Long inferredNum = inferredCountByDomainId.getOrDefault(domainId, 0L);
            Long unInferredNum = unInferredCountByDomainId.getOrDefault(domainId, 0L);
            inferredPie.add(InferPieChartSend.PieData.builder()
                    .domainName(domainName)
                    .commentNum(inferredNum)
                    .build());
            unInferredPie.add(InferPieChartSend.PieData.builder()
                    .domainName(domainName)
                    .commentNum(unInferredNum)
                    .build());
        }

        return InferPieChartSend.builder()
                .pieNameList(List.of("已完成推理评论分布", "未完成推理评论分布"))
                .domainNameList(domainNameList)
                .inferredPie(inferredPie)
                .unInferredPie(unInferredPie)
                .build();
    }

    @Override
    public TasksHotChartSend getTasksHotChart() {
        // 1. 确定统计范围：当前年月，当月第一天 00:00 至最后一天 23:59:59
        LocalDate now = LocalDate.now();
        int yearVal = now.getYear();
        int monthVal = now.getMonthValue();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59, 999_999_999);

        // 2. 推理任务按日期统计：inference_tasks.inference_start_time 落在当月，按日期分组计数
        LambdaQueryWrapper<InferenceTasks> inferWrapper = new LambdaQueryWrapper<>();
        inferWrapper.between(InferenceTasks::getInferenceStartTime, monthStart, monthEnd);
        List<InferenceTasks> inferTasks = this.list(inferWrapper);
        Map<LocalDate, Long> inferCountByDate = inferTasks.stream()
                .filter(t -> t.getInferenceStartTime() != null)
                .collect(Collectors.groupingBy(t -> t.getInferenceStartTime().toLocalDate(), Collectors.counting()));

        // 3. 训练任务按日期统计：train_tasks.start_time 落在当月，按日期分组计数
        LambdaQueryWrapper<TrainTasks> trainWrapper = new LambdaQueryWrapper<>();
        trainWrapper.between(TrainTasks::getStartTime, monthStart, monthEnd);
        List<TrainTasks> trainTasks = trainTasksService.list(trainWrapper);
        Map<LocalDate, Long> trainCountByDate = trainTasks.stream()
                .filter(t -> t.getStartTime() != null)
                .collect(Collectors.groupingBy(t -> t.getStartTime().toLocalDate(), Collectors.counting()));

        // 4. 生成本月每一天的 DailyData（无任务的日期 count 均为 0）
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<TasksHotChartSend.DailyData> dailyDataList = new ArrayList<>();
        int lastDay = now.lengthOfMonth();
        for (int day = 1; day <= lastDay; day++) {
            LocalDate date = now.withDayOfMonth(day);
            String dateStr = date.format(dateFmt);
            int inferCount = inferCountByDate.getOrDefault(date, 0L).intValue();
            int trainCount = trainCountByDate.getOrDefault(date, 0L).intValue();
            int totalCount = inferCount + trainCount;
            dailyDataList.add(TasksHotChartSend.DailyData.builder()
                    .date(dateStr)
                    .inferCount(inferCount)
                    .trainCount(trainCount)
                    .totalCount(totalCount)
                    .build());
        }

        // 5. 热力图颜色最大值：当月单日 (推理+训练) 次数最大值
        int maxTotalCount = dailyDataList.stream()
                .mapToInt(TasksHotChartSend.DailyData::getTotalCount)
                .max()
                .orElse(0);

        return TasksHotChartSend.builder()
                .year(String.valueOf(yearVal))
                .month(String.valueOf(monthVal))
                .maxTotalCount(maxTotalCount)
                .dailyDataList(dailyDataList)
                .build();
    }
}
