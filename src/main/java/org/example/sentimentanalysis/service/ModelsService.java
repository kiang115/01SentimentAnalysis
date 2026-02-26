package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.ModelAddRec;
import org.example.sentimentanalysis.dto.requestDto.ModelQueryRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.dto.commonDto.ModelInfo;
import org.example.sentimentanalysis.dto.responseDto.ModelLineChartSend;
import org.example.sentimentanalysis.dto.responseDto.ModelPieChartSend;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.model.Models;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.sentimentanalysis.service.impl.ModelsServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 模型表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface ModelsService extends IService<Models> {
    @Transactional(rollbackFor = Exception.class)
    List<ModelInfo> deleteAndGetModels(Long[] ids);

    Long addModelByTrainResult(TrainResultRec trainRec);

    void updateInferredNum(InferResultRec inferResultRec);

    ModelDetailListSend listModels(ModelQueryRec modelQueryRec);

    void addModel(ModelAddRec modelAddRec, ModelSourceEnum modelSource);

//    验证逻辑
    void checkIdExist(Long modelId);
    void checkIdsExist(Long[] modelIds);

    ModelInfo getModelInfoById(Long modelId);

    String getMaxVersion(List<Models> domainModels);

    String getNextModelVersion(String maxVersionStr, Boolean isOverTrain);

    ModelsServiceImpl.VersionParts parseVersion(String version);

    List<Integer> getMajorVersionList(List<Models> models);
    List<Integer> getSmallVersionList(List<Models> models);

    ModelLineChartSend getModelLineChart();

    ModelPieChartSend getModelPieChart();
}
