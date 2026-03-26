package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.TrainDataAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainDataQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataListSend;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.TrainDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@SaCheckRole(value = {"admin"}, mode = SaMode.OR)
public class TrainDataController {

    @Autowired
    private TrainDataService trainDataService;

    //    列出训练数据面板
    @Operation(summary = "列出分页训练数据列表")
    @PostMapping("/TrainDataQuery")
    public Response<TrainDataListSend> listTrainDataPanel(@RequestBody TrainDataQueryRec queryRec) {
        return Response.data(trainDataService.listTrainDataList(queryRec));
    }

    //    单个上传训练数据
    @Operation(summary = "上传单个训练数据")
    @PostMapping("/TrainDataAdd")
    public Response<Void> addTrainData(@RequestBody @Valid TrainDataAddRec trainData) {
        trainDataService.addBySingleData(trainData);
        return Response.success();
    }

    //    上传训练数据
    @Operation(summary = "上传训练数据csv文件")
    @PostMapping("/uploadTrainDataCsv")
    public Response<Void> uploadTrainData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("domainId") Long domainId) {

        if (file.isEmpty()) {
            return Response.fail("文件为空");
        }

        try {
            trainDataService.importCsv(file, domainId);
        } catch (IOException e) {
            return Response.fail("数据导入失败:" + e.getMessage());
        }
        return Response.success("数据导入成功");
    }

    //    删除训练数据
    @Operation(summary = "删除训练数据")
    @PostMapping("/deleteTrainData")
    public Response<Void> deleteTrainData(@RequestBody Long[] ids) {
        trainDataService.deleteByIds(ids);
        return Response.success();
    }
}

