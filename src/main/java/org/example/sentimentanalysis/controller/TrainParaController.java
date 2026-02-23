package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.TrainParaAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainParaQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainParaListSend;
import org.example.sentimentanalysis.model.TrainPara;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.TrainParaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainParaController {
    @Autowired
    private TrainParaService trainParaService;

//    展示现在已经有的模型参数模板
    @Operation(summary = "模型参数模板列表")
    @PostMapping("/listTrainPara")
    public Response<TrainParaListSend> listTrainPara(@RequestBody TrainParaQueryRec trainParaQueryRec) {
        return Response.data(trainParaService.listTrainPara(trainParaQueryRec));
    }

    @Operation(summary = "添加模型参数模板")
    @PostMapping("/addTrainPara")
    public Response<Void> addTrainPara(@RequestBody @Valid TrainParaAddRec trainPara) {
        trainParaService.addTrainPara(trainPara);
        return Response.success();
    }

    @Operation(summary = "删除模型参数模板")
    @PostMapping("/deleteTrainPara")
    public Response<Void> deleteTrainPara(@RequestBody Long[] ids) {
        trainParaService.deleteByIds(ids);
        return Response.success();
    }
}
