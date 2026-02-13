package org.example.sentimentanalysis.config;

import org.example.sentimentanalysis.dto.requestDto.InferenceResultDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fastapiClient", url = "${remote.fastapi.url}")
public interface FastApiClient {

    @PostMapping("/predict")
    Response<InferenceResultDto> sendInferenceData(@RequestBody InferenceDataDto data);

//    @PostMapping("/train")
//    String startTraining(@RequestBody TrainDto data);
//
//    @GetMapping("/status/{id}")
//    String getStatus(@PathVariable("id") String id);
}

