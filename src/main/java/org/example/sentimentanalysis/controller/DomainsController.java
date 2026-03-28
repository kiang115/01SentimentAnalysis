package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.dto.requestDto.DomainAddRec;
import org.example.sentimentanalysis.dto.responseDto.DomainDataListSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DomainsController {
    @Autowired
    private DomainsService  domainsService;

    @Operation(summary = "查询领域基础信息")
    @GetMapping("/queryDomainsInfo")
    public Response<List<DomainsInfo>> getDomainsInfo() {
        return Response.data(domainsService.listAllDomainsInfo());
    }

    @Operation(summary = "查询领域详细列表信息")
    @GetMapping("/domainDataList")
    public Response<DomainDataListSend> domainDataList() {
        return Response.data(domainsService.listDomainDataList());
    }

    @Operation(summary = "增加领域")
    @PostMapping("/addDomain")
    public Response<String> addDomain(@ModelAttribute @Valid DomainAddRec domainAddRec) {
        return Response.data(domainsService.addDomain(domainAddRec));
    }
}
