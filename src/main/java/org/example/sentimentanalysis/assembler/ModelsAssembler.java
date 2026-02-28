package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.dto.responseDto.ModelTreeChartSend;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ModelsAssembler {
    @Autowired
    @Lazy
    private ModelsService modelsService;
    public List<ModelDetailListSend.ModelDetailInfo> toModelDetailInfoList(List<Models> rawList, Map<Long, String> domainIdToName) {
        if (rawList == null || domainIdToName == null) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .map(raw -> ModelDetailListSend.ModelDetailInfo.builder()
                        .modelId(raw.getModelId())
                        .domainId(raw.getDomainId())
                        .domainName(domainIdToName.get(raw.getDomainId()))
                        .modelVersion(raw.getModelVersion())
                        .sourceName(ModelSourceEnum.getDescByCode(raw.getSource()))
                        .inferredNum(raw.getInferredNum())
                        .correctedNum(raw.getCorrectedNum())
                        .accuracy(raw.getAccuracy())
                        .description(raw.getDescription())
                        .createdAt(raw.getCreatedAt())
                        .baseModelVersion(raw.getBaseModelId()==null?"-":modelsService.getModelVersionById(raw.getBaseModelId()))
                        .build()
                ).toList();
    }

    /**
     * 将 domains 与按领域分组的 models 组装为模型树状图 DTO。
     * 每个领域一棵树，虚拟根为「领域名字+模型树状图」，子节点按 base_model_id 递归组装。
     *
     * @param domains           已按 domain_id 升序的领域列表（可为 null，则返回空结构）
     * @param modelsByDomainId domainId -> 该领域下所有 Models，可为空列表
     */
    public ModelTreeChartSend toModelTreeChartSend(List<Domains> domains, Map<Long, List<Models>> modelsByDomainId) {
        List<String> domainNameList = new ArrayList<>();
        List<ModelTreeChartSend.ModelTreeNode> modelTreeNodeList = new ArrayList<>();
        if (domains == null) {
            return ModelTreeChartSend.builder()
                    .domainNameList(domainNameList)
                    .modelTreeNodeList(modelTreeNodeList)
                    .build();
        }
        for (Domains domain : domains) {
            domainNameList.add(domain.getDomainName());
            List<Models> domainModels = modelsByDomainId != null
                    ? modelsByDomainId.getOrDefault(domain.getDomainId(), Collections.emptyList())
                    : Collections.emptyList();
            ModelTreeChartSend.ModelTreeNode virtualRoot = ModelTreeChartSend.ModelTreeNode.builder()
                    .modelVersion(domain.getDomainName() + "模型树状图")
                    .active(true)
                    .accuracy(null)
                    .children(buildChildren(domainModels, null))
                    .build();
            modelTreeNodeList.add(virtualRoot);
        }
        return ModelTreeChartSend.builder()
                .domainNameList(domainNameList)
                .modelTreeNodeList(modelTreeNodeList)
                .build();
    }

    /**
     * 从当前领域的模型列表中，筛出 base_model_id 等于 parentBaseModelId 的模型，构建为树节点并递归子节点。
     *
     * @param domainModels       该领域下全部模型
     * @param parentBaseModelId  父节点对应的 modelId，null 表示取根级（base_model_id 为空的模型）
     * @return 子节点列表，无则返回 null
     */
    private List<ModelTreeChartSend.ModelTreeNode> buildChildren(List<Models> domainModels, Long parentBaseModelId) {
        List<ModelTreeChartSend.ModelTreeNode> list = new ArrayList<>();
        for (Models m : domainModels) {
            boolean isParentMatch = (parentBaseModelId == null && m.getBaseModelId() == null)
                    || (parentBaseModelId != null && parentBaseModelId.equals(m.getBaseModelId()));
            if (!isParentMatch) {
                continue;
            }
            ModelTreeChartSend.ModelTreeNode node = ModelTreeChartSend.ModelTreeNode.builder()
                    .modelVersion(m.getModelVersion())
                    .active(m.getDeleted() != null && m.getDeleted() == 0)
                    .accuracy(m.getAccuracy())
                    .children(buildChildren(domainModels, m.getModelId()))
                    .build();
            list.add(node);
        }
        return list.isEmpty() ? null : list;
    }
}
