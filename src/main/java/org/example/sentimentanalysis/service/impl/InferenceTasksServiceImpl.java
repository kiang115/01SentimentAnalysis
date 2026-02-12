package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.assembler.InferenceDataAssembler;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.SortEnum;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        List<Comments> comments = commentsService.list();
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getStatus, PENDING.getCode())
                .orderByDesc(inferenceParaDto.getSort().equals(SortEnum.NEWEST_SORT.getKey()), Comments::getPublishTime)
                .orderByAsc(inferenceParaDto.getSort().equals(SortEnum.LATEST_SORT.getKey()), Comments::getPublishTime);
        List<Comments> sortedCommentsList = commentsService.list(wrapper);

        Map<Long, List<Comments>> domainsToCommentsList = sortedCommentsList.stream().collect(Collectors.groupingBy(Comments::getDomainId));
        List<InferenceParaDto.InferenceDomainPara> inferenceDomainParaList = inferenceParaDto.getInferenceDomainPara();
        return inferenceDataAssembler.toDto(inferenceDomainParaList,domainsToCommentsList);
    }
}
