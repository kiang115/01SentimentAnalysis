package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.TrainParaListSend;
import org.example.sentimentanalysis.model.TrainPara;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TrainParaAssembler {

    public List<TrainParaListSend.TrainParaInfo> toTrainParaList(List<TrainPara> rawList, Map<Long, String> domainIdToName) {
        if (rawList == null || domainIdToName == null) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .map(raw -> TrainParaListSend.TrainParaInfo.builder()
                        .paraId(raw.getParaId())
                        .domainName(domainIdToName.get(raw.getDomainId()))
                        .domainId(raw.getDomainId())
                        .loraR(raw.getLoraR())
                        .loraAlpha(raw.getLoraAlpha())
                        .epochs(raw.getEpochs())
                        .batchSize(raw.getBatchSize())
                        .learningRate(raw.getLearningRate())
                        .randomSeed(raw.getRandomSeed())
                        .loraModules(raw.getLoraModules())
                        .trainSplitRatio(raw.getTrainSplitRatio())
                        .build()
                ).toList();
    }
}
