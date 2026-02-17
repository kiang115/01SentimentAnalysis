package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainDataAssembler {

    /**
     * 将训练面板请求、领域信息和筛选后的训练数据组装为训练发送DTO
     */
    public TrainDataSend toTrainDataSend(TrainPanelRec trainPanelRec,
                                         Domains domain,
                                         BigDecimal nextModelVersion,
                                         List<TrainData> correctedDataList,
                                         List<TrainData> uploadDataList,
                                         List<TrainData> originalDataList) {
        return TrainDataSend.builder()
                .domainId(domain.getDomainId())
                .domainUrl(domain.getDomainUrl())
                .modelVersion(nextModelVersion)
                .loraR(trainPanelRec.getLoraR())
                .loraAlpha(trainPanelRec.getLoraAlpha())
                .epochs(trainPanelRec.getEpochs())
                .batchSize(trainPanelRec.getBatchSize())
                .learningRate(trainPanelRec.getLearningRate())
                .randomSeed(trainPanelRec.getRandomSeed())
                .loraModules(trainPanelRec.getLoraModules())
                .trainSplitRatio(trainPanelRec.getTrainSplitRatio())
                .isOverTrain(trainPanelRec.getIsOverTrain())
                .trainDataList(List.of(
                        toTrainSourceData("corrected", correctedDataList),
                        toTrainSourceData("upload", uploadDataList),
                        toTrainSourceData("original", originalDataList)
                ))
                .build();
    }

    /**
     * 单个source数据转换
     */
    private TrainDataSend.TrainSourceData toTrainSourceData(String source, List<TrainData> trainDataList) {
        List<TrainDataSend.TrainBaseData> trainBaseDataList = trainDataList.stream()
                .map(trainData -> TrainDataSend.TrainBaseData.builder()
                        .id(trainData.getId())
                        .content(trainData.getContent())
                        .build())
                .collect(Collectors.toList());

        return TrainDataSend.TrainSourceData.builder()
                .source(source)
                .trainDataList(trainBaseDataList)
                .build();
    }
}
