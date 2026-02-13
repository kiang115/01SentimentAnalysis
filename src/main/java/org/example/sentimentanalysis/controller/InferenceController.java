package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.config.FastApiClient;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.requestDto.InferenceResultDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.example.sentimentanalysis.enums.InferenceTaskStatusEnum.FAILED;
import static org.example.sentimentanalysis.enums.InferenceTaskStatusEnum.PROCESSING;

@RestController
public class InferenceController {
    @Autowired
    private InferenceTasksService inferenceTasksService;
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private FastApiClient fastApiClient; // 注入 Feign 客户端

    @Operation(summary = "列出推理任务列表")
    @GetMapping("/InferenceTasksList")
    public Response<List<InferenceTasksDto>> InferenceTasksList() {

        return Response.data(inferenceTasksService.listAllTasks());
    }

    @Operation(summary = "列出推理数据配置面板")
    @GetMapping("/InferencePanel")
    public Response<InferencePanelDto> InferenceDataConfig() {

        return Response.data(domainsService.ListInferencePanelDto());
    }

    @Operation(summary = "使用配置进行推理")
    @PostMapping("/InferenceDataCheck")
    public Response<InferenceResultDto> InferenceDataCheck(@RequestBody @Valid InferenceParaDto inferenceParaDto) {
//        1.根据参数配置找到对应的InferenceDataDto
        InferenceDataDto inferenceDataDto = inferenceTasksService.getInferenceData(inferenceParaDto);
//        2. 更新推理任务表
        Long inferenceTaskId = inferenceTasksService.addInferenceTasks(inferenceDataDto);
        inferenceDataDto.setTaskId(inferenceTaskId);
//        3. 发送数据给fastapi
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
    public Response<InferenceDataDto> InferenceResultProcess(@RequestBody @Valid InferenceResultDto inferenceResultDto) {

        return Response.success();
    }
}
