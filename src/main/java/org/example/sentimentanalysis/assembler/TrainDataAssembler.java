package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.enums.TrainDataSourceEnum;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainDataAssembler {

    /**
     * 将训练面板请求参数、领域信息、训练数据组装为训练发送DTO
     *
     * @param baseModelVersion 非空时写入（一般为 isOverTrain=false 时的当前最新版本）
     */
    public TrainDataSend toTrainDataSend(TrainPanelRec rec, Domains domain, String nextModelVersion, String baseModelVersion,
                                         List<TrainData> corrected, List<TrainData> upload, List<TrainData> original) {
        return TrainDataSend.builder()
                // 基础信息
                .domainId(domain.getDomainId())
                .domainUrl(domain.getDomainUrl())
                .modelVersion(nextModelVersion)
                .baseModelVersion(baseModelVersion)
                // 配置参数 (来自 TrainPanelRec)
                .loraR(rec.getLoraR())
                .loraAlpha(rec.getLoraAlpha())
                .epochs(rec.getEpochs())
                .batchSize(rec.getBatchSize())
                .learningRate(rec.getLearningRate())
                .randomSeed(rec.getRandomSeed())
                .loraModules(rec.getLoraModules())
                .trainSplitRatio(rec.getTrainSplitRatio())
                .ifOverTrain(rec.getIsOverTrain())
                .baseModelId(rec.getBaseModelId())
                // 训练数据
                .trainDataList(List.of(
                        toTrainSourceData(TrainDataSourceEnum.CORRECTED.getCode(), corrected),
                        toTrainSourceData(TrainDataSourceEnum.UPLOAD.getCode(), upload),
                        toTrainSourceData(TrainDataSourceEnum.ORIGINAL.getCode(), original)
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
