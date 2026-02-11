package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.InferenceTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InferenceController {
    @Autowired
    private InferenceTasksService inferenceTasksService;

    @Operation(summary = "列出推理任务列表")
    @GetMapping("/InferenceTasksList")
    public Response<List<InferenceTasksDto>> InferenceTasksList() {

        return Response.data(inferenceTasksService.listAllTasks());
    }
}
