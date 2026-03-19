package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.MerchantsQueryRec;
import org.example.sentimentanalysis.dto.responseDto.MerchantDetailSend;
import org.example.sentimentanalysis.dto.responseDto.MerchantsSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.MerchantsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerchantsController {

    @Autowired
    private MerchantsService merchantsService;

    @Operation(summary = "列出商家列表")
    @PostMapping("/ListMerchants")
    public Response<MerchantsSend> listMerchants(@RequestBody @Valid MerchantsQueryRec merchantsQueryRec) {
        return Response.data(merchantsService.listMerchants(merchantsQueryRec));
    }

    @Operation(summary = "查询商铺详情")
    @GetMapping("/MerchantDetail/{merchantId}")
    public Response<MerchantDetailSend> getMerchantDetail(@PathVariable Long merchantId) {
        return Response.data(merchantsService.getMerchantDetail(merchantId));
    }
}
