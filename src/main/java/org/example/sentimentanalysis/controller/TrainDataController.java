package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainDataController {
//    列出训练数据面板
    @Operation(summary = "列出分页训练数据面板")
    @GetMapping
    public String listTrainDataPanel() {

    }
}
