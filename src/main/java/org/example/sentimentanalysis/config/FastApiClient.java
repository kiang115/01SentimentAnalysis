package org.example.sentimentanalysis.config;

import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fastapiClient", url = "${remote.fastapi.url}")
public interface FastApiClient {

    @PostMapping("/predict")
    Response<InferResultRec> sendInferenceData(@RequestBody InferDataSend data);

//    @PostMapping("/train")
//    String startTraining(@RequestBody TrainDto data);
//
//    @GetMapping("/status/{id}")
//    String getStatus(@PathVariable("id") String id);
}

