package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.InferPanelRec;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.*;
import org.example.sentimentanalysis.model.InferenceTasks;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 推理任务详情表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface InferenceTasksService extends IService<InferenceTasks> {

    List<InferTasksSend.InferTasksDetailSend> listAllTasks(List<InferenceTasks> tasks);

    InferDataSend getInferenceData(InferPanelRec inferPanelRec);

    Long addInferenceTasks(InferDataSend inferDataSend);

    void setInferenceTaskStatus(Long taskId, Integer status, String statusMsg);

    void setInferenceTaskSuccess(InferResultRec inferResultRec);

    InferPieChartSend getInferPieChart();

    TasksHotChartSend getTasksHotChart();
}
