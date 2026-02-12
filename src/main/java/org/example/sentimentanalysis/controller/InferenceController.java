package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InferenceController {
    @Autowired
    private InferenceTasksService inferenceTasksService;
    @Autowired
    private DomainsService domainsService;

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

    @Operation(summary = "使用推理配置")
    @PostMapping("/InferenceDataCheck")
    public Response<InferenceDataDto> InferenceDataCheck(@RequestBody @Valid InferenceParaDto inferenceParaDto) {
//        1.根据参数配置找到对应的InferenceDataDto
        InferenceDataDto inferenceDataDto = inferenceTasksService.getInferecneData(inferenceParaDto);
//        2. 更新推理任务表
        Long inferenceTaskId = inferenceTasksService.addInferenceTasks(inferenceDataDto);
//        3. 返回数据给fastapi

        return Response.data(inferenceDataDto);
    }
}
