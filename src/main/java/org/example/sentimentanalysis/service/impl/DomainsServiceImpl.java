package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.sentimentanalysis.assembler.InferencePanelAssembler;
import org.example.sentimentanalysis.assembler.TrainPanelAssembler;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.dto.requestDto.DomainAddRec;
import org.example.sentimentanalysis.dto.responseDto.DomainDataListSend;
import org.example.sentimentanalysis.dto.responseDto.InferPanelSend;
import org.example.sentimentanalysis.dto.responseDto.TrainPanelSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.TrainDataSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.mapper.DomainsMapper;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.model.TrainPara;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ModelsService;
import org.example.sentimentanalysis.service.TrainDataService;
import org.example.sentimentanalysis.service.TrainParaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
    @Autowired
    @Lazy
    private MerchantsService merchantsService;

    @Override
    public InferPanelSend ListInferencePanelDto() {
//        列举领域map
        List<Domains> domains = list();
//        列举所有没被删除的modelmap
        List<Models> models = modelsService.list(new LambdaQueryWrapper<Models>().eq(Models::getDeleted, 0));
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
            return trainPanelAssembler.toDto(domains, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
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

        // 1. 查询所有现存模型
        List<Models> allExistModels = modelsService.list(new LambdaQueryWrapper<Models>().eq(Models::getDeleted, 0));

        // 2. 分组
        Map<Long, List<Models>> domainModelsMap = allExistModels.stream()
                .collect(Collectors.groupingBy(Models::getDomainId));

//        找到领域id->modelInfo列表的映射
        Map<Long, List<TrainPanelSend.ModelVersionAndId>> domainModelsInfoMap = new HashMap<>();

        domainModelsMap.forEach((domainId, models) -> {
            domainModelsInfoMap.put(domainId, modelsService.getModelVersionAndIdList(models));
        });
//        6. 委托 assembler 进行 DTO 组装
        return trainPanelAssembler.toDto(domains, sourceCountMap, trainParaMap, domainModelsInfoMap);
    }

    @Override
    public List<DomainsInfo> listAllDomainsInfo() {
        List<Domains> domains = this.list();
        if (domains == null || domains.isEmpty()) {
            return Collections.emptyList();
        }
        return domains.stream()
                .map(d -> DomainsInfo.builder()
                        .domainId(d.getDomainId())
                        .domainName(d.getDomainName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DomainDataListSend listDomainDataList() {
        List<Domains> domains = this.list(new LambdaQueryWrapper<Domains>().orderByAsc(Domains::getDomainId));
        List<String> sourceNameList = Arrays.stream(TrainDataSourceEnum.values())
                .map(TrainDataSourceEnum::getDesc)
                .collect(Collectors.toList());
        if (domains == null || domains.isEmpty()) {
            return DomainDataListSend.builder()
                    .sourceNameList(sourceNameList)
                    .domainDataList(Collections.emptyList())
                    .build();
        }

        List<Long> domainIds = domains.stream().map(Domains::getDomainId).collect(Collectors.toList());

        QueryWrapper<TrainData> trainDataWrapper = new QueryWrapper<>();
        trainDataWrapper.select("domain_id", "source", "count(*) as count_num")
                .in("domain_id", domainIds)
                .groupBy("domain_id", "source");
        List<Map<String, Object>> trainDataResultList = trainDataService.listMaps(trainDataWrapper);
        Map<Long, Map<String, Long>> trainDataCountMap = trainDataResultList.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row.get("domain_id")).longValue(),
                        Collectors.toMap(
                                row -> Objects.toString(row.get("source"), ""),
                                row -> ((Number) row.get("count_num")).longValue(),
                                Long::sum
                        )
                ));

        Map<Long, Long> commentCountMap = toCountMap(commentsService.listMaps(
                new QueryWrapper<Comments>()
                        .select("domain_id", "count(*) as count_num")
                        .in("domain_id", domainIds)
                        .groupBy("domain_id")
        ));

        Map<Long, Long> modelCountMap = toCountMap(modelsService.listMaps(
                new QueryWrapper<Models>()
                        .select("domain_id", "count(*) as count_num")
                        .eq("deleted", 0)
                        .in("domain_id", domainIds)
                        .groupBy("domain_id")
        ));

        Map<Long, Long> merchantCountMap = toCountMap(merchantsService.listMaps(
                new QueryWrapper<Merchants>()
                        .select("domain_id", "count(*) as count_num")
                        .in("domain_id", domainIds)
                        .groupBy("domain_id")
        ));

        List<DomainDataListSend.DomainDataInfo> domainDataList = domains.stream()
                .map(domain -> {
                    Long domainId = domain.getDomainId();
                    Map<String, Long> sourceCount = trainDataCountMap.getOrDefault(domainId, Collections.emptyMap());
                    List<Long> trainDataCountList = Arrays.stream(TrainDataSourceEnum.values())
                            .map(source -> sourceCount.getOrDefault(source.getCode(), 0L))
                            .collect(Collectors.toList());
                    return DomainDataListSend.DomainDataInfo.builder()
                            .domainId(domainId)
                            .domainName(domain.getDomainName())
                            .domainUrl(domain.getDomainUrl())
                            .createdAt(domain.getCreatedAt())
                            .domainImageUrl(domain.getDomainImageUrl())
                            .domainDescription(domain.getDomainDescription())
                            .trainDataCountList(trainDataCountList)
                            .commentTotal(commentCountMap.getOrDefault(domainId, 0L))
                            .modelTotal(modelCountMap.getOrDefault(domainId, 0L))
                            .merchantTotal(merchantCountMap.getOrDefault(domainId, 0L))
                            .build();
                })
                .collect(Collectors.toList());

        return DomainDataListSend.builder()
                .sourceNameList(sourceNameList)
                .domainDataList(domainDataList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addDomain(DomainAddRec domainAddRec) {
        MultipartFile file = domainAddRec.getFile();
        if (file == null || file.isEmpty()) {
            throw new CustomBusinessException("原始训练数据csv文件不能为空");
        }

        boolean domainNameExists = this.exists(
                new LambdaQueryWrapper<Domains>().eq(Domains::getDomainName, domainAddRec.getDomainName())
        );
        if (domainNameExists) {
            throw new CustomBusinessException("领域名称已存在");
        }

        boolean domainUrlExists = this.exists(
                new LambdaQueryWrapper<Domains>().eq(Domains::getDomainUrl, domainAddRec.getDomainUrl())
        );
        if (domainUrlExists) {
            throw new CustomBusinessException("领域地址已存在");
        }

        Domains domain = new Domains()
                .setDomainName(domainAddRec.getDomainName())
                .setDomainUrl(domainAddRec.getDomainUrl())
                .setDomainImageUrl(domainAddRec.getDomainImageUrl())
                .setDomainDescription(domainAddRec.getDomainDescription());
        this.save(domain);

        try {
            trainDataService.importCsv(file, domain.getDomainId(), TrainDataSourceEnum.ORIGINAL.getCode());
        } catch (Exception e) {
            throw new CustomBusinessException("原始训练数据导入失败:" + e.getMessage());
        }

        return "是否立即为" + domain.getDomainName() + "训练第一个模型？";
    }

    @Override
    public Map<Long, String> getDomainIdToName() {
        List<Domains> domains = this.list();
        return domains.stream()
                .collect(Collectors.toMap(Domains::getDomainId, Domains::getDomainName, (a, b) -> a));
    }

    @Override
    public void checkIdExist(Long domainId) {
        boolean exist = this.exists(
                Wrappers.<Domains>lambdaQuery().eq(Domains::getDomainId, domainId)
        );
        if (!exist) {
            throw new CustomBusinessException("领域id" + domainId + "不存在");
        }
    }

    @Override
    public void checkIdsExist(Long[] domainIds) {
        if (domainIds == null) {
            throw new CustomBusinessException("请选择要删除的领域");
        }
        // 1. 去重，防止前端传了重复的ID导致数量对不上
        List<Long> distinctIds = Arrays.stream(domainIds).distinct().collect(Collectors.toList());

        // 2. 查询数据库里实际存在的数量
        long count = this.count(new LambdaQueryWrapper<Domains>()
                .in(Domains::getDomainId, distinctIds));

        // 3. 对比数量
        if (count != distinctIds.size()) {
            throw new CustomBusinessException("操作失败：领域id部分不匹配");
        }
    }

    @Override
    public String getDomainUrlById(Long domainId) {
        Domains domain = this.getOne(new LambdaQueryWrapper<Domains>().eq(Domains::getDomainId, domainId));
        if (domain == null) {
            throw new CustomBusinessException("领域id" + domainId + "不存在");
        }
        return domain.getDomainUrl();
    }

    private Map<Long, Long> toCountMap(List<Map<String, Object>> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return Collections.emptyMap();
        }
        return resultList.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("domain_id")).longValue(),
                        row -> ((Number) row.get("count_num")).longValue(),
                        Long::sum
                ));
    }

}
