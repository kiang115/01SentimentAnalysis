package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.utils.OssUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class DomainsController {
    @Autowired
    private DomainsService  domainsService;

    @Operation(summary = "查询全部领域信息")
    @GetMapping("/queryDomainsInfo")
    public Response<List<DomainsInfo>> getDomainsInfo() {
        return Response.data(domainsService.listAllDomainsInfo());
    }
}
