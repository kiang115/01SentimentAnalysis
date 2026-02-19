package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.model.TrainData;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
