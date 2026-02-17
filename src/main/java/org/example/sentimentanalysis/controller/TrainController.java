package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.dto.responseDto.TrainPanelSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.TrainTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TrainController {
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private TrainTasksService trainTasksService;

    /**
     * 列出训练配置面板数据（开始训练前初始化）
     */
    @Operation(summary = "列出训练参数配置面板")
    @GetMapping("/TrainPanel")
    public Response<TrainPanelSend> listTrainPanel() {
        TrainPanelSend trainPanelSend = domainsService.listTrainPanel();
        return Response.data(trainPanelSend);
    }

    @Operation(summary = "开始训练")
    @PostMapping("/TrainDataCheck")
    public Response<String> trainDataCheck(@RequestBody @Valid  TrainPanelRec trainPanelRec) {
//        System.out.println(trainPanelRec);

        TrainDataSend trainDataSend = trainTasksService.getTrainData(trainPanelRec);
        return Response.data("开始训练");
    }
}
