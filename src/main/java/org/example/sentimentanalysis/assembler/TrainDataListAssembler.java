package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.TrainDataListSend;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrainDataListAssembler {

    /**
     * 将训练数据列表 + 领域 id->name 映射转换为 TrainDataInfo 列表（用于分页列表面板）
     */
    public List<TrainDataListSend.TrainDataInfo> toTrainDataInfoList(List<TrainData> trainDataList, Map<Long, String> domainIdToName) {
        if (trainDataList == null || domainIdToName == null) {
            return Collections.emptyList();
        }
        return trainDataList.stream()
                .map(d -> TrainDataListSend.TrainDataInfo.builder()
                        .id(d.getId())
                        .content(d.getContent())
                        .trainCount(d.getTrainCount())
                        .domainId(d.getDomainId())
                        .domainName(domainIdToName.get(d.getDomainId()))
                        .label(d.getLabel())
                        .source(d.getSource())
                        .createdAt(d.getCreatedAt())
                        .build())
                .toList();
    }
}
