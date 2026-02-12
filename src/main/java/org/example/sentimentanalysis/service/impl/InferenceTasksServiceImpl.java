package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.model.Models;
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
//    @Autowired
//    private InferenceTasksService inferenceTasksService;
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private InferenceTasksAssembler inferenceTasksAssembler;

    @Override
    public List<InferenceTasksDto> listAllTasks() {
        List<InferenceTasks> tasks = this.list();
//         加载所有的modelId->model映射
        Map<Long, Models> modelMap = modelsService.list().stream().collect(Collectors.toMap(Models::getModelId, model -> model));
//        加载所有domainId->domain映射
        Map<Long, Domains> domainMap = domainsService.list().stream().collect(Collectors.toMap(Domains::getDomainId, domain -> domain));

        // 2. 调用装配器做转换（无需在Service写转换逻辑）
        return inferenceTasksAssembler.toDtoList(tasks, modelMap,domainMap);
    }
}
