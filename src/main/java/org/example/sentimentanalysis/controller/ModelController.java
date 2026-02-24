package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.ModelAddRec;
import org.example.sentimentanalysis.dto.requestDto.ModelQueryRec;
import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModelController {
    @Autowired
    private ModelsService modelsService;

    @Operation(summary = "模型列表")
    @PostMapping("/listModels")
    public Response<ModelDetailListSend> listModels(@RequestBody ModelQueryRec modelQueryRec) {
        return Response.data(modelsService.listModels(modelQueryRec));
    }

    @Operation(summary = "添加模型")
    @PostMapping("/addModel")
    public Response<Void> addModel(@RequestBody @Valid ModelAddRec modelAddRec) {
        modelsService.addModel(modelAddRec);
        return Response.success();
    }

//    todo 删除模型将导致任务详情将导致模型列表和推理列表无法显示，需要进行处理，暂时不进行 要删除只能标记模型为禁用状态，而且需要重构发送到推理面板的数据（筛选非禁用状态的模型）
    @Operation(summary = "删除模型")
    @PostMapping("/deleteModels")
    public Response<Void> deleteModels(@RequestBody Long[] ids) {
        modelsService.deleteByIds(ids);
        return Response.success();
    }
}
