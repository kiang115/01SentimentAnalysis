package org.example.sentimentanalysis.assembler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.InferenceTaskStatusEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.model.Models;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InferenceTasksAssembler {

    /**
     * 批量将 InferenceTasks 转换为 InferenceTasksDto
     *
     * @param tasks     推理任务实体列表
     * @param modelMap  模型ID->模型实体的映射（提前批量查询，避免N+1）
     * @param domainMap 领域ID->领域实体的映射（提前批量查询，避免N+1）
     * @return 转换后的DTO列表
     */
    public List<InferenceTasksDto> toDtoList(List<InferenceTasks> tasks, Map<Long, Models> modelMap, Map<Long, Domains> domainMap) {
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        return tasks.stream()
                .map(task -> toDto(task, modelMap, domainMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个将 InferenceTasks 转换为 InferenceTasksDto
     *
     * @param task     推理任务实体
     * @param modelMap 模型ID->模型实体的映射
     * @return 转换后的DTO
     */
    public InferenceTasksDto toDto(InferenceTasks task, Map<Long, Models> modelMap, Map<Long, Domains> domainMap) {

        InferenceTasksDto dto = InferenceTasksDto.builder()
                .taskId(task.getTaskId())
                .inferenceStartTime(task.getInferenceStartTime())
                .inferenceEndTime(task.getInferenceEndTime())
                .inferenceDuration(task.getInferenceDuration())
                .processedCount(task.getProcessedCount())
                .avgProcessSpeed(task.getAvgProcessSpeed())
                .processStatusName(InferenceTaskStatusEnum.getStatusNameByCode(task.getProcessStatus()))
                .build();

        // 2. 模型信息字段赋值（含空指针+业务校验）
        dto.setModelInfoList(buildModelInfoList(task.getUsedModelIds(), modelMap,domainMap));

        return dto;
    }

    /**
     * 构建模型信息列表（抽离独立方法，提高可读性）
     *
     * @param usedModelIds 任务关联的模型ID列表
     * @param modelMap     模型ID->模型实体的映射
     * @return 模型信息DTO列表
     */
    private List<InferenceTasksDto.ModelInfo> buildModelInfoList(List<Long> usedModelIds, Map<Long, Models> modelMap,Map<Long, Domains> domainMap) {
        List<InferenceTasksDto.ModelInfo> modelInfoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(usedModelIds)) {
            return modelInfoList;
        }

        for (Long modelId : usedModelIds) {
            Models model = modelMap.get(modelId);
            if (model == null) {
                // 保持原业务的异常逻辑，也可改为日志提醒（根据业务需求调整）
                throw new CustomBusinessException("任务关联的模型ID[" + modelId + "]不存在");
            }
            Domains domain = domainMap.get(model.getDomainId());
            if (domain == null) {
                // 保持原业务的异常逻辑，也可改为日志提醒（根据业务需求调整）
                throw new CustomBusinessException("模型对应的领域ID[" + model.getDomainId() + "]不存在");
            }
            InferenceTasksDto.ModelInfo modelInfo = new InferenceTasksDto.ModelInfo();
            modelInfo.setDomainName(domain.getDomainName());
            modelInfo.setModelId(model.getModelId());
            modelInfo.setModelVersion(model.getModelVersion());
            modelInfoList.add(modelInfo);
        }
        return modelInfoList;
    }
}
