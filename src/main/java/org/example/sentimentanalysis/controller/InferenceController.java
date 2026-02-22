package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.config.FastApiClient;
import org.example.sentimentanalysis.dto.requestDto.InferPanelRec;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.*;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.example.sentimentanalysis.enums.TaskStatusEnum.*;

@RestController
public class InferenceController {
    @Autowired
    private InferenceTasksService inferenceTasksService;
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private FastApiClient fastApiClient; // 注入 Feign 客户端
    @Autowired
    private CommentsService commentsService;

    @Operation(summary = "列出推理任务列表")
    @GetMapping("/InferenceTasksList")
    public Response<List<InferTasksDetailSend>> InferenceTasksList() {

        List<InferenceTasks> tasks = inferenceTasksService.list();
//          在这里查找tasks中是否有任务状态为待处理的，如果有返回false 否者true
        boolean hasProcessingTask = tasks.stream().anyMatch(task ->
                Objects.equals(task.getProcessStatus(), TaskStatusEnum.PROCESSING.getCode()));
        return Response.success(inferenceTasksService.listAllTasks(tasks), hasProcessingTask ? "processing" : "completed");
    }

    @Operation(summary = "列出推理数据配置面板")
    @GetMapping("/InferencePanel")
    public Response<InferPanelSend> InferenceDataConfig() {
        return Response.data(domainsService.ListInferencePanelDto());
    }

    @Operation(summary = "使用配置开始推理")
    @PostMapping("/InferenceDataCheck")
    public Response InferenceDataCheck(@RequestBody @Valid InferPanelRec inferPanelRec) {
//        1.根据参数配置找到对应的inferDataSend
        InferDataSend inferDataSend = inferenceTasksService.getInferenceData(inferPanelRec);
//        2. 增加推理任务表
        Long inferenceTaskId = inferenceTasksService.addInferenceTasks(inferDataSend);
        inferDataSend.setTaskId(inferenceTaskId);
//        3. 发送数据给fastapi
        try {
            Response<InferResultRec> response = fastApiClient.sendInferenceData(inferDataSend);
//        4. 异常处理
            if (response.getCode() != 200) {
//            推理任务状态设置为failed
                inferenceTasksService.setInferenceTaskStatus(inferenceTaskId, TaskStatusEnum.FAILED.getCode());
                throw new CustomBusinessException("error-模型推理失败:" + response.getMessage());
            }
        } catch (Exception e) {
            inferenceTasksService.setInferenceTaskStatus(inferenceTaskId, TaskStatusEnum.FAILED.getCode());
            throw new CustomBusinessException("error-模型服务未启动:" + e.getMessage());
        }
//        5.更新评论状态 为推理中
        commentsService.updateCommentStatus(inferDataSend);
        return Response.success();
    }

    @Operation(summary = "推理结果解析和处理")
    @PostMapping("/InferenceResultProcess")
    public Response<InferDataSend> InferenceResultProcess(@RequestBody @Valid Response<InferResultRec> inferenceResultDtoResponse) {
//        接收推理结果
        InferResultRec inferResultRec = inferenceResultDtoResponse.getData();

        if (inferenceResultDtoResponse.getCode() != 200) {
            inferenceTasksService.setInferenceTaskStatus(inferResultRec.getTaskId(), FAILED.getCode());
//            设置评论状态重新为待处理
            commentsService.updateCommentStatus(inferResultRec, CommentStatusEnum.PENDING.getCode());
            throw new CustomBusinessException("error:" + inferenceResultDtoResponse.getMessage());
        }
//        设置评论状态为已处理
        commentsService.updateCommentStatus(inferResultRec, CommentStatusEnum.INFERRED.getCode());
//       更新推理任务表
        inferenceTasksService.setInferenceTaskSuccess(inferResultRec);
        return Response.success();
    }

    @Operation(summary = "推理数据饼状图")
    @GetMapping("/InferPieChart")
    public Response<InferPieChartSend> InferPieChart() {
        InferPieChartSend inferPieChartSend = inferenceTasksService.getInferPieChart();
        return Response.data(inferPieChartSend);
    }
//    月度日历热力图
    @Operation(summary = "数据月度日历热力图")
    @GetMapping("/TasksHotChart")
    public Response<TasksHotChartSend> TasksHotChart() {
        TasksHotChartSend tasksHotChartSend = inferenceTasksService.getTasksHotChart();
        return Response.data(tasksHotChartSend);
    }
}
