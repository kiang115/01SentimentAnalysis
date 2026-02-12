package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.catalina.User;
import org.example.sentimentanalysis.assembler.InferenceDataAssembler;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.SortEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.sentimentanalysis.enums.CommentStatusEnum.PENDING;
import static org.example.sentimentanalysis.enums.SortEnum.NEWEST_SORT;

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

    @Override
    public List<InferenceTasksDto> listAllTasks() {
        List<InferenceTasks> tasks = this.list();
//         加载所有的modelId->model映射
        Map<Long, Models> modelMap = modelsService.list().stream().collect(Collectors.toMap(Models::getModelId, model -> model));
//        加载所有domainId->domain映射
        Map<Long, Domains> domainMap = domainsService.list().stream().collect(Collectors.toMap(Domains::getDomainId, domain -> domain));
        return inferenceTasksAssembler.toDtoList(tasks, modelMap, domainMap);
    }

    @Override
    public InferenceDataDto getInferecneData(InferenceParaDto inferenceParaDto) {
//        根据sort排序，然后获取对应数量的推理表格，然后传入toDto装配
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getStatus, PENDING.getCode())
                .orderByDesc(inferenceParaDto.getSort().equals(SortEnum.NEWEST_SORT.getKey()), Comments::getPublishTime)
                .orderByAsc(inferenceParaDto.getSort().equals(SortEnum.LATEST_SORT.getKey()), Comments::getPublishTime);
        List<Comments> sortedCommentsList = commentsService.list(wrapper);

        Map<Long, List<Comments>> domainsToCommentsList = sortedCommentsList.stream().collect(Collectors.groupingBy(Comments::getDomainId));
        List<InferenceParaDto.InferenceDomainPara> inferenceDomainParaList = inferenceParaDto.getInferenceDomainPara();

        //    检测模型id+version是否存在
        //   检测领域id+name是否存在
        for (InferenceParaDto.InferenceDomainPara p : inferenceDomainParaList) {
            boolean exist = modelsService.exists(new LambdaQueryWrapper<Models>().eq(Models::getModelId, p.getModelId()).eq(Models::getModelVersion, p.getModelVersion())) &&
                    domainsService.exists(new LambdaQueryWrapper<Domains>().eq(Domains::getDomainId, p.getDomainId()).eq(Domains::getDomainName, p.getDomainName()));
            if (!exist) {
                throw new CustomBusinessException("模型或者领域信息错误！请检查");
            }
        }
        return inferenceDataAssembler.toDto(inferenceDomainParaList, domainsToCommentsList);
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

        task.setDomainIds(domainIds).setUsedModelIds(modelIds).setProcessedCount(totalComments);
        save(task);
        if (task.getTaskId() == null) {
            throw new CustomBusinessException("添加推理任务失败，未生成主键！");
        }
        return task.getTaskId();
    }
}
