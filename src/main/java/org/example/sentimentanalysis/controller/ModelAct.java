package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name="后端模型管理接口")
@Controller
public class ModelAct {
    @PostMapping("/train")
    @Operation(summary = "前端点击训练按钮")
    public Boolean train(String action) {
//        if (action.equals("train")) {
////            开始训练
//        }
        return true;
    }
}
