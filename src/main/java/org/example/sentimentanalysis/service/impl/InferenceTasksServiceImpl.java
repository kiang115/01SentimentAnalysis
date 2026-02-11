package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.assembler.InferenceTasksAssembler;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.InferenceTaskStatusEnum;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.InferenceTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private InferenceTasksAssembler inferenceTasksAssembler; // 注入装配器

    @Override
    public List<InferenceTasksDto> listAllTasks() {
        // 1. 业务逻辑：查询任务+批量查询模型（Service 专注业务）
        List<InferenceTasks> tasks = this.list();
//        提取modelIds列表(删除重复的列表)
        List<Long> allModelIds = tasks.stream()
                .filter(task -> !CollectionUtils.isEmpty(task.getUsedModelIds()))
                .flatMap(task -> task.getUsedModelIds().stream())
                .distinct()
                .collect(Collectors.toList());
// 将modelIds列表映射为   modelid+model对象
        Map<Long, Models> modelMap = CollectionUtils.isEmpty(allModelIds)
                ? Collections.emptyMap()
                : modelsService.listByIds(allModelIds).stream()
                        .collect(Collectors.toMap(Models::getModelId, model -> model));

        // 2. 调用装配器做转换（无需在Service写转换逻辑）
        return inferenceTasksAssembler.toDtoList(tasks, modelMap);
    }
}
