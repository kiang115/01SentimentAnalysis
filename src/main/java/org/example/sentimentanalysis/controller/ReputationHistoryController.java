package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.ReputationHistoryQueryRec;
import org.example.sentimentanalysis.dto.responseDto.ReputationHistoryChangeSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.ReputationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReputationHistoryController {

    @Autowired
    private ReputationHistoryService reputationHistoryService;

    @Operation(summary = "查询口碑数据变化信息")
    @SaCheckRole(value = {"consumer", "merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/ReputationHistory/Change")
    public Response<ReputationHistoryChangeSend> getReputationChange(
            @RequestBody @Valid ReputationHistoryQueryRec rec) {
        return Response.data(reputationHistoryService.getReputationChange(rec));
    }
}
