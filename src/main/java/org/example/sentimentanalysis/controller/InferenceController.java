package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.config.FastApiClient;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.requestDto.InferenceResultDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.InferenceTaskStatusEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.example.sentimentanalysis.enums.InferenceTaskStatusEnum.*;

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
    public Response<List<InferenceTasksDto>> InferenceTasksList() {

        List<InferenceTasks> tasks = inferenceTasksService.list();
//          在这里查找tasks中是否有任务状态为待处理的，如果有返回false 否者true
        boolean hasProcessingTask = tasks.stream().anyMatch(task ->
                Objects.equals(task.getProcessStatus(), InferenceTaskStatusEnum.PROCESSING.getCode()));
        return Response.success(inferenceTasksService.listAllTasks(tasks), hasProcessingTask ? "processing" : "completed");
    }

    @Operation(summary = "列出推理数据配置面板")
    @GetMapping("/InferencePanel")
    public Response<InferencePanelDto> InferenceDataConfig() {
        return Response.data(domainsService.ListInferencePanelDto());
    }

    @Operation(summary = "使用配置开始推理")
    @PostMapping("/InferenceDataCheck")
    public Response<InferenceResultDto> InferenceDataCheck(@RequestBody @Valid InferenceParaDto inferenceParaDto) {
//        1.根据参数配置找到对应的InferenceDataDto
        InferenceDataDto inferenceDataDto = inferenceTasksService.getInferenceData(inferenceParaDto);
//        2. 更新推理任务表
        Long inferenceTaskId = inferenceTasksService.addInferenceTasks(inferenceDataDto);
        inferenceDataDto.setTaskId(inferenceTaskId);
//        3. 更新评论状态 为推理中
        commentsService.updateCommentStatus(inferenceDataDto);
//        4. 发送数据给fastapi
        Response<InferenceResultDto> response = fastApiClient.sendInferenceData(inferenceDataDto);
//     更新状态
        if (inferenceTasksService.setInferenceTaskStatus(
                inferenceTaskId,
                response.getCode() != 200 ? FAILED.getCode() : PROCESSING.getCode()
        ) == -1L) {
            throw new CustomBusinessException("error-更新推理任务:任务状态更新失败");
        }

        if (response.getCode() != 200) {
            throw new CustomBusinessException("error-推理处理:解析线程中失败");
        }
        return response;
    }

    @Operation(summary = "推理结果解析和处理")
    @PostMapping("/InferenceResultProcess")
    public Response<InferenceDataDto> InferenceResultProcess(@RequestBody @Valid Response<InferenceResultDto> inferenceResultDtoResponse) {
//        接收推理结果
        InferenceResultDto inferenceResultDto = inferenceResultDtoResponse.getData();

        if (inferenceResultDtoResponse.getCode() != 200) {
            inferenceTasksService.setInferenceTaskStatus(inferenceResultDto.getTaskId(), FAILED.getCode());
//            设置评论状态重新为待处理
            commentsService.updateCommentStatus(inferenceResultDto, CommentStatusEnum.PENDING.getCode());
            return Response.fail(inferenceResultDtoResponse.getMessage());
        }
//        设置评论状态为已处理
        commentsService.updateCommentStatus(inferenceResultDto, CommentStatusEnum.INFERRED.getCode());

        inferenceTasksService.setInferenceTaskStatus(inferenceResultDto.getTaskId(), SUCCESS.getCode());
//       设置成功需要更新的东西，状态上面已经更新了
        inferenceTasksService.setInferenceTaskSuccess(inferenceResultDto);
        return Response.success();
    }
}
