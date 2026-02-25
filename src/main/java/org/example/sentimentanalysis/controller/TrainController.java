package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.config.FastApiClient;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.dto.requestDto.TrainPanelRec;
import org.example.sentimentanalysis.dto.responseDto.*;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TrainController {
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private TrainTasksService trainTasksService;
    @Autowired
    private FastApiClient fastApiClient;
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private TrainDataService trainDataService;
    @Autowired
    private CommentsService commentsService;

    @Operation(summary = "列出训练任务列表")
    @GetMapping("/TrainTaskList")
    public Response<TrainTasksSend> listTrainTask() {
        TrainTasksSend trainTasksSend = trainTasksService.listTrainTask();
        return Response.data(trainTasksSend);
    }

    /**
     * 列出训练配置面板数据（开始训练前初始化）
     */
    @Operation(summary = "列出训练参数配置面板")
    @GetMapping("/TrainPanel")
    public Response<TrainPanelSend> listTrainPanel() {
        TrainPanelSend trainPanelSend = domainsService.listTrainPanel();
        return Response.data(trainPanelSend);
    }

    @Operation(summary = "使用配置开始训练")
    @PostMapping("/TrainDataCheck")
    public Response<TrainDataSend> trainDataCheck(@RequestBody @Valid TrainPanelRec trainPanelRec) {
//      得到发送给fastapi的数据
        TrainDataSend trainDataSend = trainTasksService.getTrainData(trainPanelRec);
//        todo 模型界面需要增加一个字段 叫做使用的基础模型的版本号
        Long taskId = trainTasksService.addTrainTask(trainPanelRec);
        trainDataSend.setTaskId(taskId);

        try {
            Response<InferResultRec> response = fastApiClient.sendTrainData(trainDataSend);
            if (response.getCode() != 200) {
                trainTasksService.updateById(new TrainTasks().setId(taskId).setStatus(TaskStatusEnum.FAILED.getCode()));
                throw new CustomBusinessException("error-模型训练失败:"+response.getMessage());
            }
        } catch (Exception e) {
            trainTasksService.updateById(new TrainTasks().setId(taskId).setStatus(TaskStatusEnum.FAILED.getCode()));
            throw new CustomBusinessException("error-模型服务未启动:"+e.getMessage());
        }
//        要解决并行同时对同一个大版本进行训练的问题+(模型大版本可选化,可以选择在那个基线版本上进行训练)
        return Response.success();
    }

    @Operation(summary = "训练结果解析和处理")
    @PostMapping("/TrainResultProcess")
    public Response<Void> trainResultProcess(@RequestBody @Valid Response<TrainResultRec> trainDataRec) {
        TrainResultRec trainRec = trainDataRec.getData();
        if (trainDataRec.getCode() != 200) {
            trainTasksService.updateById(new TrainTasks().setId(trainRec.getTaskId()).setStatus(TaskStatusEnum.FAILED.getCode()));
            throw new CustomBusinessException("训练任务失败");
        }
//        添加训练得到的新模型
        Long modelId = modelsService.addModelByTrainResult(trainRec);
        System.out.println("添加训练得到的新模型");
        if (modelId == -1) {
            throw new CustomBusinessException("训练数据装配失败：模型已存在");
        }
//        对训练任务表(end_time,duration,model_id,accuracy,precision_rate,recall_rate,f1_score，train_loss_list，train_acc_list,val_loss_list，val_acc_list)进行更新
        trainTasksService.updateByTrainRec(trainRec, modelId);
        System.out.println("对训练任务表进行更新");
//        训练数据表，进行更新(trainCount++)。
        trainDataService.updateTrainCount(trainRec.getResults());
        System.out.println("训练数据表进行更新");
        return Response.success();
    }

    @Operation(summary = "训练准确率折线图")
    @GetMapping("/TrainLineChart")
    public Response<TrainLineChartSend> InferLineChart() {
        TrainLineChartSend trainLineChartSend = trainTasksService.getTrainLineChart();
        return Response.data(trainLineChartSend);
    }
}
