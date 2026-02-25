package org.example.sentimentanalysis.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import net.sf.jsqlparser.Model;
import org.example.sentimentanalysis.config.FastApiClient;
import org.example.sentimentanalysis.dto.requestDto.ModelAddRec;
import org.example.sentimentanalysis.dto.requestDto.ModelFileRec;
import org.example.sentimentanalysis.dto.requestDto.ModelQueryRec;
import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.dto.commonDto.ModelInfo;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@RestController
public class ModelController {
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private FastApiClient fastApiClient;
    @Autowired
    private DomainsService domainsService;

    @Operation(summary = "模型列表")
    @PostMapping("/listModels")
    public Response<ModelDetailListSend> listModels(@RequestBody ModelQueryRec modelQueryRec) {
        return Response.data(modelsService.listModels(modelQueryRec));
    }

    @Operation(summary = "添加模型")
    @PostMapping("/addModel")
    public Response<Void> addModel(@RequestBody @Valid ModelAddRec modelAddRec) {
        modelsService.addModel(modelAddRec, ModelSourceEnum.TRAIN);
        return Response.success();
    }

    @Operation(summary = "删除模型")
    @PostMapping("/deleteModels")
    public Response<Void> deleteModels(@RequestBody Long[] ids) {
        List<ModelInfo> modelsInfo = modelsService.deleteAndGetModels(ids);
//        发送给fastapi删除模型
        return fastApiClient.deleteModels(modelsInfo);
    }

    @Operation(summary = "下载模型")
    @GetMapping("/download-model/{modelId}")
    public ResponseEntity<StreamingResponseBody> downLoadModel(@PathVariable Long modelId) {
        // 1. 取出模型信息
        ModelInfo modelInfo = modelsService.getModelInfoById(modelId);
        if (modelInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 调用 FastAPI
        feign.Response feignResponse = fastApiClient.downLoadModel(modelInfo);

        if (feignResponse.status() != 200) {
            return ResponseEntity.status(feignResponse.status()).build();
        }

        // 获取文件名
        String fileName = modelInfo.getDomainUrl() + "_v" + modelInfo.getModelVersion() + ".zip";

        // 使用 StreamingResponseBody 进行流式写出
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = feignResponse.body().asInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(responseBody);
    }

    @Operation(summary = "上传模型文件")
    @PostMapping("/upload-model")
    public Response<Void> uploadModel(@ModelAttribute ModelFileRec modelFileRec) {
        MultipartFile[] files = modelFileRec.getFiles();
        Long domainId = modelFileRec.getDomainId();
//    文件名和文件数量严格限制
        // 1. 严格校验：必须是 2 个文件
        if (files == null || files.length != 2) {
            throw new CustomBusinessException("模型文件解析失败");
        }

        // 2. 严格校验：文件名
        boolean hasConfig = false;
        boolean hasBin = false;
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            if ("adapter_config.json".equals(name)) hasConfig = true;
            if ("adapter_model.bin".equals(name)) hasBin = true;
        }

        if (!hasConfig || !hasBin) {
            throw new CustomBusinessException("模型文件解析失败");
        }

        // 上传的模型相当于一个新的基础模型-
//        如果领域中没有任何模型（包括被删除的模型）->版本就是0.0
//        如果有模型(包括被删除的模型)->版本就是{最大版本+1}.0
        domainsService.checkIdExist(domainId);
        List<Models> domainModels = modelsService.list(new LambdaQueryWrapper<Models>().eq(Models::getDomainId, domainId));

        String maxVersion = modelsService.getMaxVersion(domainModels);
        String nextVersion = modelsService.getNextModelVersion(maxVersion, true);
        // 3. 组装转发给 FastAPI
        ModelInfo modelInfo = ModelInfo.builder()
                .domainUrl(domainsService.getDomainUrlById(domainId))
                .modelVersion(nextVersion)
                .build();
        // 调用 Feign 接口转发
        Response<Void> response = fastApiClient.uploadModel(
                files,
                modelInfo.getDomainUrl(),
                modelInfo.getModelVersion()
        );
        if (response.getCode() != 200) {
            throw new CustomBusinessException(response.getMessage());
        }
//        将模型加入表中
        modelsService.addModel(ModelAddRec.builder().modelVersion(nextVersion).description(modelFileRec.getDescription()).domainId(domainId).build(), ModelSourceEnum.UPLOAD);

        return Response.success();
    }

}
