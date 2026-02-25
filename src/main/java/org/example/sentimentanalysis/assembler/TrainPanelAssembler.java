package org.example.sentimentanalysis.assembler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.dto.responseDto.TrainPanelSend;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainPara;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrainPanelAssembler {

    /**
     * 将领域列表、训练数据来源统计、训练参数配置组装为训练面板DTO
     *
     * @param domains                领域列表
     * @param sourceCountMap         训练数据来源统计（domainId -> source -> count）
     * @param trainParaMap           训练参数列表（domainId -> TrainPara列表）
     * @param domainMajorVersionList
     * @return TrainPanelSend
     */
    public TrainPanelSend toDto(List<Domains> domains,
                                Map<Long, Map<String, Long>> sourceCountMap,
                                Map<Long, List<TrainPara>> trainParaMap, Map<Long, List<Integer>> domainMajorVersionList) {
        // 1. 边界处理：无领域直接返回空列表
        if (CollectionUtils.isEmpty(domains)) {
            return TrainPanelSend.builder()
                    .trainParaList(Collections.emptyList())
                    .build();
        }

        // 2. 逐领域组装训练面板节点
        List<TrainPanelSend.TrainPanelDomainDTO> trainPanelDomainDTOList = domains.stream()
                .map(domain -> {
                    Long domainId = domain.getDomainId();
                    Map<String, Long> domainSourceCount = sourceCountMap.getOrDefault(domainId, Collections.emptyMap());
                    List<TrainPara> domainTrainParas = trainParaMap.getOrDefault(domainId, Collections.emptyList());

                    List<TrainPanelSend.TrainParaDTO> trainParaDTOList = domainTrainParas.stream()
                            .map(this::toTrainParaDto)
                            .collect(Collectors.toList());

                    return TrainPanelSend.TrainPanelDomainDTO.builder()
                            .majorVersionList(domainMajorVersionList.getOrDefault(domainId, Collections.emptyList()))
                            .domainId(domainId)
                            .domainName(domain.getDomainName())
                            .correctedNum(domainSourceCount.getOrDefault("corrected", 0L))
                            .uploadNum(domainSourceCount.getOrDefault("upload", 0L))
                            .originalNum(domainSourceCount.getOrDefault("original", 0L))
                            .defaultParaList(trainParaDTOList)
                            .build();
                })
                .collect(Collectors.toList());

        // 3. 返回顶层DTO
        return TrainPanelSend.builder()
                .trainParaList(trainPanelDomainDTOList)
                .build();
    }

    /**
     * 训练参数实体转响应DTO
     */
    private TrainPanelSend.TrainParaDTO toTrainParaDto(TrainPara trainPara) {

        return TrainPanelSend.TrainParaDTO.builder()
                .paraId(trainPara.getParaId())
                .loraR(trainPara.getLoraR())
                .loraAlpha(trainPara.getLoraAlpha())
                .epochs(trainPara.getEpochs())
                .batchSize(trainPara.getBatchSize())
                .learningRate(trainPara.getLearningRate())
                .randomSeed(trainPara.getRandomSeed())
                .loraModules(trainPara.getLoraModules())
                .trainSplitRatio(trainPara.getTrainSplitRatio())
                .build();
    }
}
