package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.sentimentanalysis.assembler.InferenceDataAssembler;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.requestDto.InferenceResultDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.InferenceTaskStatusEnum;
import org.example.sentimentanalysis.enums.SortEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceRecordsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.sentimentanalysis.enums.CommentStatusEnum.INFERRED;
import static org.example.sentimentanalysis.enums.CommentStatusEnum.PENDING;

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

    @Override
    public List<InferenceTasksDto> listAllTasks(List<InferenceTasks> tasks) {
//         加载所有的modelId->model映射
        Map<Long, Models> modelMap = modelsService.list().stream().collect(Collectors.toMap(Models::getModelId, model -> model));
//        加载所有domainId->domain映射
        Map<Long, Domains> domainMap = domainsService.list().stream().collect(Collectors.toMap(Domains::getDomainId, domain -> domain));

//
        return inferenceTasksAssembler.toDtoList(tasks, modelMap, domainMap);
    }

    @Override
    public InferenceDataDto getInferenceData(InferenceParaDto inferenceParaDto) {
        List<InferenceParaDto.InferenceDomainPara> domainParas = inferenceParaDto.getInferenceDomainPara();

        // 1. 批量数据预取 (准备原材料)
        Set<Long> domainIds = domainParas.stream().map(InferenceParaDto.InferenceDomainPara::getDomainId).collect(Collectors.toSet());
        Set<Long> modelIds = domainParas.stream().map(InferenceParaDto.InferenceDomainPara::getModelId).collect(Collectors.toSet());

        Map<Long, Domains> domainMap = domainsService.listByIds(domainIds).stream()
                .collect(Collectors.toMap(Domains::getDomainId, d -> d));
        Map<Long, Models> modelMap = modelsService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(Models::getModelId, m -> m));

        // 2. 批量查询并分组评论
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getStatus, PENDING.getCode())
                .in(Comments::getDomainId, domainIds)
                .orderByDesc(SortEnum.NEWEST_SORT.getKey().equals(inferenceParaDto.getSort()), Comments::getPublishTime)
                .orderByAsc(SortEnum.LATEST_SORT.getKey().equals(inferenceParaDto.getSort()), Comments::getPublishTime);

        Map<Long, List<Comments>> domainsToCommentsMap = commentsService.list(wrapper).stream()
                .collect(Collectors.groupingBy(Comments::getDomainId));

        // 3. 核心业务处理与转换
        List<InferenceDataDto.InferenceDomainData> domainDataList = domainParas.stream().map(para -> {
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
        return InferenceDataDto.builder().inferenceDomainDataList(domainDataList).build();
    }

    //将推理任务插入到任务表中
    @Override
    @Transactional
    public Long addInferenceTasks(InferenceDataDto inferenceDataDto) {
//构造一个要插入的task数据
        InferenceTasks task = new InferenceTasks();
        List<InferenceDataDto.InferenceDomainData> domainDataList = inferenceDataDto.getInferenceDomainDataList();

        // 1. 获取 ModelId 列表 (去重)
        List<Long> modelIds = domainDataList.stream()
                .map(InferenceDataDto.InferenceDomainData::getModelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2. 获取 DomainId 列表 (不去重)
        List<Long> domainIds = domainDataList.stream()
                .map(InferenceDataDto.InferenceDomainData::getDomainId)
                .filter(Objects::nonNull)
                .toList();

        // 3. 计算评论总数 (注意处理 null 对象或 null 字段)
        long totalComments = domainDataList.stream()
                .filter(Objects::nonNull)
                .mapToLong(item -> item.getInferenceCommentNums() == null ? 0L : item.getInferenceCommentNums())
                .sum();
//        设置默认的发起人id为1
        task.setDomainIds(domainIds).setUsedModelIds(modelIds).setProcessedCount(totalComments).setInitiatorId(1L);
        save(task);
        if (task.getTaskId() == null) {
            throw new CustomBusinessException("添加推理任务失败，未生成主键！");
        }
        return task.getTaskId();
    }

    @Override
    public Long setInferenceTaskStatus(Long taskId, Integer status) {
        if (InferenceTaskStatusEnum.existsByCode(status)) {
            updateById(new InferenceTasks().setTaskId(taskId).setProcessStatus(status));
            return taskId;
        }
        return -1L;
    }

    @Override
    @Transactional
    public void setInferenceTaskSuccess(InferenceResultDto inferenceResultDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime taskEndTime = LocalDateTime.parse(inferenceResultDto.getTaskEndTime(), formatter);

        Long taskId = inferenceResultDto.getTaskId();
        Float taskDuration = inferenceResultDto.getTaskDuration();
        Long processCount = inferenceResultDto.getProcessCount();

        long inferenceDurationMs = (long) (taskDuration * 1000);
        BigDecimal avgProcessSpeed = BigDecimal.valueOf(processCount).divide(BigDecimal.valueOf(taskDuration), 2, RoundingMode.HALF_UP);

        this.updateById(new InferenceTasks()
                .setTaskId(taskId)
                .setInferenceEndTime(taskEndTime)
                .setInferenceDuration(inferenceDurationMs)
                .setAvgProcessSpeed(avgProcessSpeed));

        List<InferenceResultDto.CommentResultList> results = inferenceResultDto.getResults();
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
}
