package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.sentimentanalysis.dto.requestDto.TrainDataAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainDataQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataListSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.TrainDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainDataController {

    @Autowired
    private TrainDataService trainDataService;

    //    列出训练数据面板
    @Operation(summary = "列出分页训练数据列表")
    @PostMapping("/TrainDataQuery")
    public Response<TrainDataListSend> listTrainDataPanel(@RequestBody TrainDataQueryRec queryRec) {
        return Response.data(trainDataService.listTrainDataList(queryRec));
    }

//    单个上传训练数据
    @Operation(summary = "上传单个训练数据")
    @PostMapping("/TrainDataAdd")
    public Response<Void> addTrainData(@RequestBody TrainDataAddRec trainData) {
        trainDataService.addBySingleData(trainData);
        return Response.success();
    }
}

