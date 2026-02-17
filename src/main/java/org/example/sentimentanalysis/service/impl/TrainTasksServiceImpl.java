package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.mapper.TrainTasksMapper;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import org.example.sentimentanalysis.service.TrainDataService;
import org.example.sentimentanalysis.service.TrainTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 训练任务管理表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainTasksServiceImpl extends ServiceImpl<TrainTasksMapper, TrainTasks> implements TrainTasksService {
    private static final String SOURCE_CORRECTED = "corrected";
    private static final String SOURCE_UPLOAD = "upload";
    private static final String SOURCE_ORIGINAL = "original";
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private TrainDataService trainDataService;

    @Override
    public TrainDataSend getTrainData(TrainPanelRec trainPanelRec) {
        return null;
    }

    /**
     * 随机筛选指定数量训练数据，不足直接抛业务异常
     */
    private List<TrainData> pickTrainDataBySource(List<TrainData> sourceDataList,
                                                  Long requiredCount,
                                                  String source,
                                                  String domainName,
                                                  Random random) {
        if (requiredCount <= 0) {
            throw new CustomBusinessException("训练数据装配失败：领域[" + domainName + "] source[" + source + "] 目标数量必须大于0");
        }
        if (sourceDataList.size() < requiredCount) {
            throw new CustomBusinessException(
                    "训练数据装配失败：领域[" + domainName + "] source[" + source + "] 数据不足，目标数量="
                            + requiredCount + "，可用数量=" + sourceDataList.size()
            );
        }
        List<TrainData> copiedList = new ArrayList<>(sourceDataList);
        Collections.shuffle(copiedList, random);
        return copiedList.subList(0, Math.toIntExact(requiredCount));
    }

}
