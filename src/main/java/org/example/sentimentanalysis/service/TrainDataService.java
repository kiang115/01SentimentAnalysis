package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.TrainDataAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainDataQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataListSend;
import org.example.sentimentanalysis.model.TrainData;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 训练数据表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
public interface TrainDataService extends IService<TrainData> {

    void updateTrainCount(List<Long> ids);

    TrainDataListSend listTrainDataList(TrainDataQueryRec queryRec);

    void addBySingleData(TrainDataAddRec trainData);

    void importCsv(MultipartFile file, Long domainId) throws IOException;
}
