package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.model.TrainTasks;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 训练任务管理表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
public interface TrainTasksService extends IService<TrainTasks> {
    TrainDataSend getTrainData(TrainPanelRec trainPanelRec);
}
