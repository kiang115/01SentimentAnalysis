package org.example.sentimentanalysis.controller;

import org.example.sentimentanalysis.dto.redisDto.InferTaskSnapshot;
import org.example.sentimentanalysis.dto.redisDto.TrainTaskSnapshot;
import org.example.sentimentanalysis.service.impl.GenericSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@CrossOrigin
public class SseController {

    @Autowired
    private GenericSseService<InferTaskSnapshot> infersSseService;
    @Autowired
    private GenericSseService<TrainTaskSnapshot> trainSseService;

    @GetMapping(value = "/infer/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter inferSubscribe() {
        return infersSseService.addConnection();
    }
    @GetMapping(value = "/train/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter trainSubscribe() {
        return trainSseService.addConnection();
    }
}
