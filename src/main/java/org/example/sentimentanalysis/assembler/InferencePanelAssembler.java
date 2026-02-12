package org.example.sentimentanalysis.assembler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InferencePanelAssembler {

    /**
     * 将领域列表、未推理计数、模型Map转换为前端所需的面板DTO
     *
     * @param domains                          领域列表
     * @param domainsUninferencedReviewNumsMap domainId -> 未推理评论数
     * @param models                           模型实体列表 (实体包含 modelId, modelVersion, domainId)
     * @return InferencePanelDto
     */
    public InferencePanelDto toDto(List<Domains> domains,
                                   Map<Long, Long> domainsUninferencedReviewNumsMap,
                                   List<Models> models) {

        // 1. 边界条件校验
        if (CollectionUtils.isEmpty(domains)) {
            return InferencePanelDto.builder()
                    .inferenceConfigDataList(Collections.emptyList())
                    .build();
        }

        // 2. 预处理模型数据：将 Map<ModelId, Model> 转换为 Map<DomainId, List<Model>>
        Map<Long, List<Models>> modelsGroupedByDomainId = Collections.emptyMap();
        // 领域id->模型列表
        if (!CollectionUtils.isEmpty(models)) {
            modelsGroupedByDomainId = models.stream().collect(Collectors.groupingBy(Models::getDomainId));
        }

        // 为了在lambda中使用，创建一个实际上final的引用（虽然Java8+会自动识别，但显式声明更好理解逻辑）
        Map<Long, List<Models>> finalModelsGroupedByDomainId = modelsGroupedByDomainId;

        // 3. 核心转换逻辑
        List<InferencePanelDto.InferencePanelDomainsDataDto> inferenceConfigDataList = domains.stream()
                .map(domain -> {
                    Long domainId = domain.getDomainId();

                    // 3.1 获取该领域下的模型列表并转换为DTO
                    List<Models> domainModels = finalModelsGroupedByDomainId.getOrDefault(domainId, Collections.emptyList());

//                    3.1.1对domainModels按照版本大小排序
                    domainModels.sort((v1, v2) -> v2.getModelVersion().compareTo(v1.getModelVersion()));

                    List<InferencePanelDto.DomainsModelDataDto> modelDtos = domainModels.stream()
                            .map(model -> InferencePanelDto.DomainsModelDataDto.builder()
                                    .modelId(model.getModelId())
                                    .modelVersion(model.getModelVersion())
                                    .build())
                            .collect(Collectors.toList());

                    // 3.2 获取未推理数量 (处理空指针安全)
                    Long uninferencedCount = 0L;
                    if (domainsUninferencedReviewNumsMap != null) {
                        uninferencedCount = domainsUninferencedReviewNumsMap.getOrDefault(domainId, 0L);
                    }

                    // 3.3 构建单个领域的DTO
                    return InferencePanelDto.InferencePanelDomainsDataDto.builder()
                            .domainId(domainId)
                            .domainName(domain.getDomainName())
                            .uninferencedCommentNums(uninferencedCount)
                            .domainModelsDataList(modelDtos)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 返回最终结果
        return InferencePanelDto.builder()
                .inferenceConfigDataList(inferenceConfigDataList)
                .build();
    }
}
