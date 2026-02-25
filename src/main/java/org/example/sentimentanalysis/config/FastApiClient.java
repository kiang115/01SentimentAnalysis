package org.example.sentimentanalysis.config;

import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.dto.commonDto.ModelInfo;
import org.example.sentimentanalysis.dto.responseDto.TrainDataSend;
import org.example.sentimentanalysis.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@FeignClient(name = "fastapiClient", url = "${remote.fastapi.url}")
public interface FastApiClient {

    @PostMapping("/predict")
    Response<InferResultRec> sendInferenceData(@RequestBody InferDataSend data);

    @PostMapping("/train")
    Response<InferResultRec> sendTrainData(@RequestBody TrainDataSend data);

    @PostMapping("/deleteModels")
    Response<Void> deleteModels(@RequestBody List<ModelInfo> models);


    @PostMapping(value = "/downLoadModel")
    feign.Response downLoadModel(@RequestBody ModelInfo modelInfo);

    @PostMapping(value = "/uploadModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<Void> uploadModel(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("domainUrl") String domainUrl,   // 拆开传
            @RequestPart("modelVersion") String modelVersion // 拆开传
    );
//
//    @GetMapping("/status/{id}")
//    String getStatus(@PathVariable("id") String id);
}

