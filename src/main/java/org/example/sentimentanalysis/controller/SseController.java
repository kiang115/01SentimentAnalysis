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

// todo 推理端需要统一statuscode 训练+推理 需要增加错误原因字段 训练需要将字段传入vue中,并让vue根据传入的信息写错误原因 推理服务需要增加判断是否无需要建立连接的isfinished字段,并应用于vue中