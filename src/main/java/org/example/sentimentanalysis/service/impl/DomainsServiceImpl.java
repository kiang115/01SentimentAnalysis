package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.sentimentanalysis.assembler.InferencePanelAssembler;
import org.example.sentimentanalysis.assembler.TrainPanelAssembler;
import org.example.sentimentanalysis.dto.responseDto.InferPanelSend;
import org.example.sentimentanalysis.dto.responseDto.TrainPanelSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.mapper.DomainsMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.model.TrainPara;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.example.sentimentanalysis.service.TrainDataService;
import org.example.sentimentanalysis.service.TrainParaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 领域信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class DomainsServiceImpl extends ServiceImpl<DomainsMapper, Domains> implements DomainsService {
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private InferencePanelAssembler inferencePanelAssembler;
    @Autowired
    private TrainPanelAssembler trainPanelAssembler;
    @Autowired
    private TrainDataService trainDataService;
    @Autowired
    private TrainParaService trainParaService;

    @Override
    public InferPanelSend ListInferencePanelDto() {
//        列举领域map
        List<Domains> domains = list();
//        列举所有modelmap
        List<Models> models = modelsService.list();
//     计算comment表中未推理的评论数
        QueryWrapper<Comments> wrapper = new QueryWrapper<>();

        wrapper.select("domain_id", "count(*) as count_num") // 指定查询列：ID 和 数量
                .lambda() // 切换为 Lambda 模式，下面可以使用方法引用，防手误
                .eq(Comments::getStatus, CommentStatusEnum.PENDING.getCode()) // status = 0
                .groupBy(Comments::getDomainId); // GROUP BY domain_id
        // 2. 执行查询，返回 List<Map<String, Object>>
        // listMaps 是 MP 提供的专门用于返回 Map 的方法
        List<Map<String, Object>> resultList = commentsService.listMaps(wrapper);
        //每一个map是一行数据 的所有字段名->字段值映射
        // 3. 转换结果为 Map<Long, Long>
        Map<Long, Long> resultMap = resultList.stream()
                .collect(Collectors.toMap(
                        // Key: domain_id
                        map -> ((Number) map.get("domain_id")).longValue(),
                        // Value: count_num
                        map -> ((Number) map.get("count_num")).longValue()
                ));

        return inferencePanelAssembler.toDto(domains, resultMap, models);

    }

    @Override
    public TrainPanelSend listTrainPanel() {
//        1. 查询全部领域（面板按领域展示）
        List<Domains> domains = list();
        if (domains.isEmpty()) {
            return trainPanelAssembler.toDto(domains, Collections.emptyMap(), Collections.emptyMap());
        }

//        2. 提取领域id，供批量查询参数使用
        List<Long> domainIds = domains.stream()
                .map(Domains::getDomainId)
                .collect(Collectors.toList());

//        3. 按领域+来源统计训练数据数量（corrected/upload/original）
        QueryWrapper<TrainData> trainDataWrapper = new QueryWrapper<>();
        trainDataWrapper.select("domain_id", "source", "count(*) as count_num")
                .groupBy("domain_id", "source");
        List<Map<String, Object>> sourceResultList = trainDataService.listMaps(trainDataWrapper);

//        4. 转换为 domainId -> (source -> count) 供 assembler 直接使用
        Map<Long, Map<String, Long>> sourceCountMap = sourceResultList.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row.get("domain_id")).longValue(),
                        Collectors.toMap(
                                row -> Objects.toString(row.get("source"), ""),
                                row -> ((Number) row.get("count_num")).longValue(),
                                Long::sum
                        )
                ));

//        5. 按领域批量查询默认参数配置，避免N+1
        List<TrainPara> trainParas = trainParaService.list(
                new LambdaQueryWrapper<TrainPara>()
                        .in(TrainPara::getDomainId, domainIds)
        );
        Map<Long, List<TrainPara>> trainParaMap = trainParas.stream()
                .collect(Collectors.groupingBy(TrainPara::getDomainId));

//        6. 委托 assembler 进行 DTO 组装
        return trainPanelAssembler.toDto(domains, sourceCountMap, trainParaMap);
    }
}
