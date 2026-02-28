package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
}
