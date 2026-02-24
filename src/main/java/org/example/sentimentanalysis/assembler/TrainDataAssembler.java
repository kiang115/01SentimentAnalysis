package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.enums.TaskDataSourceEnum;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainDataAssembler {

    /**
     * 将训练面板请求参数、领域信息、训练数据组装为训练发送DTO
     * @param baseModelVersion 非空时写入（一般为 isOverTrain=false 时的当前最新版本）
     */
    public TrainDataSend toTrainDataSend(TrainPanelRec trainPanelRec,
                                         Domains domain,
                                         String nextModelVersion,
                                         String baseModelVersion,
                                         List<TrainData> correctedDataList,
                                         List<TrainData> uploadDataList,
                                         List<TrainData> originalDataList) {
        TrainDataSend.TrainDataSendBuilder b = TrainDataSend.builder()
                .domainId(domain.getDomainId())
                .domainUrl(domain.getDomainUrl())
                .modelVersion(nextModelVersion)
                .loraR(trainPanelRec.getLoraR());
        if (baseModelVersion != null) {
            b.baseModelVersion(baseModelVersion);
        }
        return b
                .loraAlpha(trainPanelRec.getLoraAlpha())
                .epochs(trainPanelRec.getEpochs())
                .batchSize(trainPanelRec.getBatchSize())
                .learningRate(trainPanelRec.getLearningRate())
                .randomSeed(trainPanelRec.getRandomSeed())
                .loraModules(trainPanelRec.getLoraModules())
                .trainSplitRatio(trainPanelRec.getTrainSplitRatio())
                .ifOverTrain(trainPanelRec.getIsOverTrain())
                .trainDataList(List.of(
                        toTrainSourceData(TaskDataSourceEnum.CORRECTED.getCode(), correctedDataList),
                        toTrainSourceData(TaskDataSourceEnum.UPLOAD.getCode(), uploadDataList),
                        toTrainSourceData(TaskDataSourceEnum.ORIGINAL.getCode(), originalDataList)
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
                        .label(trainData.getLabel())
                        .build())
                .collect(Collectors.toList());

        return TrainDataSend.TrainSourceData.builder()
                .source(source)
                .trainDataList(trainBaseDataList)
                .build();
    }
}
