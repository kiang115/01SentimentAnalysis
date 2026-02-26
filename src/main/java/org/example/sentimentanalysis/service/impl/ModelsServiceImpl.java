package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.assembler.ModelsAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.ModelAddRec;
import org.example.sentimentanalysis.dto.requestDto.ModelQueryRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.dto.responseDto.ModelDetailListSend;
import org.example.sentimentanalysis.dto.commonDto.ModelInfo;
import org.example.sentimentanalysis.dto.responseDto.ModelLineChartSend;
import org.example.sentimentanalysis.dto.responseDto.ModelPieChartSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.mapper.ModelsMapper;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 模型表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class ModelsServiceImpl extends ServiceImpl<ModelsMapper, Models> implements ModelsService {

    @Autowired
    @Lazy
    private DomainsService domainsService;
    @Autowired
    private ModelsAssembler modelsAssembler;
    @Autowired
    private CommentsService commentsService;

    @Override
    public ModelDetailListSend listModels(ModelQueryRec modelQueryRec) {
        int pageNum = modelQueryRec.getPageNum() != null ? modelQueryRec.getPageNum() : 1;
        int pageSize = modelQueryRec.getPageSize() != null ? modelQueryRec.getPageSize() : 8;
        if (modelQueryRec.getDomainId() != null) {
            domainsService.checkIdExist(modelQueryRec.getDomainId());
        }

        LambdaQueryWrapper<Models> wrapper = new LambdaQueryWrapper<>();
        Long domainId = modelQueryRec.getDomainId();
        Long modelId = modelQueryRec.getModelId();
        String source = modelQueryRec.getSource();

        if (source != null && !ModelSourceEnum.isCodeExist(source)) {
            throw new CustomBusinessException("模型来源不存在");
        }
        wrapper.eq(domainId != null, Models::getDomainId, domainId)
                .eq(modelId != null, Models::getModelId, modelId)
                .eq(source != null, Models::getSource, source)
                .eq(Models::getDeleted, 0);

        PageHelper.startPage(pageNum, pageSize);
        List<Models> rawList = this.list(wrapper);

        Map<Long, String> domainIdToName = domainsService.getDomainIdToName();

        List<ModelDetailListSend.ModelDetailInfo> infoList = modelsAssembler.toModelDetailInfoList(rawList, domainIdToName);
        @SuppressWarnings("unchecked")
        PageInfo<ModelDetailListSend.ModelDetailInfo> pageInfo = (PageInfo<ModelDetailListSend.ModelDetailInfo>) (PageInfo<?>) new PageInfo<>(rawList);
        pageInfo.setList(infoList);
        return ModelDetailListSend.builder()
                .pageInfo(pageInfo)
                .domains(domainsService.listAllDomainsInfo())
                .build();
    }

    @Override
    public void addModel(ModelAddRec modelAddRec, ModelSourceEnum sourceEnum) {
        domainsService.checkIdExist(modelAddRec.getDomainId());

        Models newModel = new Models();
        newModel.setDomainId(modelAddRec.getDomainId())
                .setModelVersion(modelAddRec.getModelVersion())
                .setSource(sourceEnum.getCode())
                .setDescription(modelAddRec.getDescription());
        this.save(newModel);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<ModelInfo> deleteAndGetModels(Long[] ids) {
        // 1. 基础校验与去重
        if (ids == null || ids.length == 0) {
            throw new CustomBusinessException("操作失败：请选择要删除的数据");
        }
        List<Long> distinctIds = Arrays.stream(ids).distinct().collect(Collectors.toList());

        // 2. 查询待删除的模型详情 (涉及 domainId 和 modelVersion)
        List<Models> modelList = this.list(new LambdaQueryWrapper<Models>()
                .select(Models::getModelId, Models::getDomainId, Models::getModelVersion)
                .eq(Models::getDeleted, 0)
                .in(Models::getModelId, distinctIds));

        // 3. 严格校验数量一致性
        if (modelList.size() != distinctIds.size()) {
            throw new CustomBusinessException("操作失败：待删除的模型id不存在或者已经被删除");
        }

        // 4. 提取所有的 domainId 并查询对应的 domainUrl
        Set<Long> domainIds = modelList.stream()
                .map(Models::getDomainId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> domainUrlMap = new HashMap<>();
        if (!domainIds.isEmpty()) {
            // 假设域名的 Service 名为 domainService，实体类为 Domains
            List<Domains> domainList = domainsService.list(new LambdaQueryWrapper<Domains>()
                    .select(Domains::getDomainId, Domains::getDomainUrl)
                    .in(Domains::getDomainId, domainIds));

            // 生成 domainId -> domainUrl 的映射 Map
            domainUrlMap = domainList.stream().collect(Collectors.toMap(
                    Domains::getDomainId,
                    Domains::getDomainUrl,
                    (v1, v2) -> v1 // 防止重复 key
            ));
        }

        // 5. 组装返回列表
        Map<Long, String> finalDomainUrlMap = domainUrlMap; // 用于 lambda
        List<ModelInfo> deleteInfoList = modelList.stream().map(m -> ModelInfo.builder()
                .domainUrl(finalDomainUrlMap.get(m.getDomainId()))
                .modelVersion(m.getModelVersion())
                .build()).collect(Collectors.toList());

        // 6. 执行逻辑删除
        this.update(new LambdaUpdateWrapper<Models>()
                .set(Models::getDeleted, 1)
                .in(Models::getModelId, distinctIds));

        // 7. 返回组装好的信息
        return deleteInfoList;
    }


    @Override
    public Long addModelByTrainResult(TrainResultRec trainRec) {
        if (trainRec.getModelVersion() == null || trainRec.getDomainId() == null) {
            throw new CustomBusinessException("训练数据装配失败：无法获取模型版本或者领域信息");
        }
//        防止重复添加模型
        LambdaQueryWrapper<Models> wrapper = new LambdaQueryWrapper<Models>()
                .eq(Models::getModelVersion, trainRec.getModelVersion())
                .eq(Models::getDomainId, trainRec.getDomainId());

        if (count(wrapper) == 0) {
            Models newModel = new Models().setModelVersion(trainRec.getModelVersion()).setDomainId(trainRec.getDomainId()).setSource(ModelSourceEnum.TRAIN.getCode()).setTrainTaskId(trainRec.getTaskId());
            save(newModel);
            return newModel.getModelId();
        }
        return -1L;
    }

    @Override
    @Transactional
    public void updateInferredNum(InferResultRec inferResultRec) {
        List<InferResultRec.CommentResultList> results = inferResultRec.getResults();

        // 1. 分组计数 (得到每个 modelId 需要增加的量)
        Map<Long, Long> modelCountMap = results.stream()
                .collect(Collectors.groupingBy(
                        InferResultRec.CommentResultList::getModelId,
                        Collectors.counting()
                ));

        // 2. 检测 模型ID 是否存在
        checkIdsExist(modelCountMap.keySet().toArray(new Long[0]));

        // 3. 循环执行累加更新
        modelCountMap.forEach((modelId, increment) -> this.update(null, new LambdaUpdateWrapper<Models>()
                        .eq(Models::getModelId, modelId)
                        .setSql("inferred_num = inferred_num + " + increment)
                )
        );
    }

    @Override
    public void checkIdExist(Long modelId) {
        boolean exist = this.exists(
                Wrappers.<Models>lambdaQuery().eq(Models::getModelId, modelId)
        );
        if (!exist) {
            throw new CustomBusinessException("模型id" + modelId + "不存在");
        }
    }

    @Override
    public void checkIdsExist(Long[] modelIds) {
        if (modelIds == null) {
            throw new CustomBusinessException("请选择要删除的领域");
        }
        // 1. 去重，防止前端传了重复的ID导致数量对不上
        List<Long> distinctIds = Arrays.stream(modelIds).distinct().collect(Collectors.toList());

        // 2. 查询数据库里实际存在的数量
        long count = this.count(new LambdaQueryWrapper<Models>()
                .in(Models::getModelId, distinctIds));

        // 3. 对比数量
        if (count != distinctIds.size()) {
            throw new CustomBusinessException("操作失败：模型id部分不匹配");
        }
    }

    @Override
    public ModelInfo getModelInfoById(Long modelId) {
        LambdaQueryWrapper<Models> wrapper = new LambdaQueryWrapper<Models>().eq(Models::getModelId, modelId).eq(Models::getDeleted, 0);
        Models model = getOne(wrapper);
        if (model != null) {
            return ModelInfo.builder()
                    .domainUrl(domainsService.getDomainUrlById(model.getDomainId()))
                    .modelVersion(model.getModelVersion())
                    .build();
        }
        return null;
    }

    @Override
//    输入为空返回null
    public String getMaxVersion(List<Models> domainModels) {
        return domainModels.stream()
                // 提取版本号字符串
                .map(Models::getModelVersion)
                // 使用自定义比较逻辑：先比 major，再比 minor
                .max(Comparator.comparing(this::parseVersion,
                        Comparator.comparingInt(VersionParts::major)
                                .thenComparingInt(VersionParts::minor)))
                // 如果列表为空，返回null
//                .orElseThrow(() -> new CustomBusinessException("训练数据装配失败：无法获取领域最新版本"));
                .orElse(null);
    }

    @Override
    public String getNextModelVersion(String maxVersionStr, Boolean isOverTrain) {
        if (maxVersionStr == null) {
            return "0.0";
        }
        // 解析字符串为结构化对象
        VersionParts latest = parseVersion(maxVersionStr);

        // 执行版本递增逻辑
        if (isOverTrain) {
            // 全量训练：大版本+1，小版本归零 (1.5 -> 2.0)
            return (latest.major() + 1) + ".0";
        } else {
            // 增量训练：小版本+1 (1.5 -> 1.6)
            return latest.major() + "." + (latest.minor() + 1);
        }
    }

    /**
     * 解析版本号，格式为 x.y（仅一个点分隔）；解析出现任何问题均抛 CustomBusinessException，并表述异常信息。
     */
    @Override
    public VersionParts parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }
        String[] parts = version.split("\\.");
        if (parts.length != 2) {
            throw new CustomBusinessException("训练数据装配失败：模型版本格式非法，应为 x.y，version=" + version);
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return new VersionParts(major, minor);
        } catch (NumberFormatException e) {
            throw new CustomBusinessException("训练数据装配失败：模型版本格式非法，非数字，version=" + version);
        }
    }


    @Override
    public List<Integer> getMajorVersionList(List<Models> models) {
        if (models == null || models.isEmpty()) {
            return List.of();
        }
        return models.stream().map(m -> {
            ModelsServiceImpl.VersionParts versionParts = this.parseVersion(m.getModelVersion());
            return versionParts.major();
        }).distinct().toList();
    }

    @Override
    public List<Integer> getSmallVersionList(List<Models> models) {
        return models.stream().map(m -> {
            ModelsServiceImpl.VersionParts versionParts = this.parseVersion(m.getModelVersion());
            return versionParts.minor();
        }).distinct().toList();
    }

    /**
     * 获取模型折线图数据。
     * 逻辑：按领域(Domain)分组，以小版本(Minor)为横坐标，大版本(Major)为不同折线，展示模型准确率(Accuracy)走势。
     * 数据源为 models 表全部记录（不按 deleted 过滤），准确率来自表虚拟列 (1 - corrected_num/inferred_num)*100。
     */
    @Override
    public ModelLineChartSend getModelLineChart() {
        // --- 1. 数据准备阶段 ---

        // 查询所有领域信息，用于后续构建「领域ID -> 领域名称」的映射，避免在循环中重复查库
        List<Domains> domains = domainsService.list();
        Map<Long, String> domainIdToName = domainsService.getDomainIdToName();

        // 查询全部模型（不按 deleted 过滤），用于按领域统计各版本准确率
        List<Models> allModels = this.list();

        // 将所有模型按 domainId 分组，形成 Map<领域ID, 模型列表>，便于 O(1) 获取某领域下的所有模型
        Map<Long, List<Models>> modelsByDomain = allModels.stream()
                .collect(Collectors.groupingBy(Models::getDomainId));

        // 用于存储最终返回前端的：领域名称列表、各领域的折线数据详情
        List<String> domainNameList = new ArrayList<>();
        List<ModelLineChartSend.DomainLinesData> domainLinesDataList = new ArrayList<>();

        // --- 2. 核心逻辑：按领域遍历 ---
        for (Domains domain : domains) {
            Long domainId = domain.getDomainId();
            String domainName = domainIdToName.get(domainId);
            // 获取当前领域下的所有模型，若没有则使用空列表
            List<Models> domainModels = modelsByDomain.getOrDefault(domainId, Collections.emptyList());

            // --- 3. 版本号解析与范围确定 ---
            // 将版本号字符串（如 "1.2"）解析为 VersionParts（major=1, minor=2），便于比较与计算
            List<VersionParts> parts = domainModels.stream()
                    .map(m -> parseVersion(m.getModelVersion()))
                    .filter(Objects::nonNull)
                    .toList();

            // 计算当前领域内出现的最大大版本号（Major）和最大小版本号（Minor）
            // 二者决定图表的 X 轴长度（maxMinor）以及折线数量（maxMajor）
            int maxMajor = parts.isEmpty() ? 0 : parts.stream().mapToInt(VersionParts::major).max().orElse(0);
            int maxMinor = parts.isEmpty() ? 0 : parts.stream().mapToInt(VersionParts::minor).max().orElse(0);

            // --- 4. 建立「版本号 -> 准确率」映射 ---
            // 将当前领域下的模型列表转为 Map：Key 为 modelVersion，Value 为 accuracy（表虚拟列）
            // 用于在组装折线时快速按版本取准确率；同版本取后者 (a, b) -> b
            Map<String, BigDecimal> versionToAccuracy = domainModels.stream()
                    .filter(m -> m.getModelVersion() != null && m.getAccuracy() != null)
                    .collect(Collectors.toMap(Models::getModelVersion, Models::getAccuracy, (a, b) -> b));

            // --- 5. 组装前端绘图所需的数据结构 ---

            // 生成横坐标（X 轴）标签：如 maxMinor=2 则 ["X.0", "X.1", "X.2"]
            List<String> allSmallVersions = IntStream.rangeClosed(0, maxMinor)
                    .mapToObj(m -> "X." + m)
                    .toList();

            List<ModelLineChartSend.MajorVersionData> majorVersionDataList = new ArrayList<>();
            List<String> allMajorVersions = new ArrayList<>();

            // 遍历从 0 到 maxMajor 的每个大版本（每条折线）
            for (int major = 0; major <= maxMajor; major++) {
                final int majorVal = major;

                // 补齐数据：对 0..maxMinor 的每个小版本，若存在该版本则取准确率，否则为 null，保证每条折线在小版本维度上对齐，便于前端绘制
                List<BigDecimal> versionAccuracyList = IntStream.rangeClosed(0, maxMinor)
                        .mapToObj(minor -> versionToAccuracy.get(majorVal + "." + minor))
                        .toList();

                // 封装单条折线数据（如 "1.X" 下所有小版本对应的准确率点）
                majorVersionDataList.add(ModelLineChartSend.MajorVersionData.builder()
                        .majorVersion(majorVal + ".X")
                        .versionAccuracyList(versionAccuracyList)
                        .build());

                allMajorVersions.add(majorVal + ".X");
            }

            // --- 6. 汇总当前领域数据并加入结果集 ---
            domainLinesDataList.add(ModelLineChartSend.DomainLinesData.builder()
                    .domainName(domainName)
                    .allSmallVersions(allSmallVersions)
                    .allMajorVersions(allMajorVersions)
                    .majorVersionDataList(majorVersionDataList)
                    .build());
            domainNameList.add(domainName);
        }

        // --- 7. 返回最终 DTO ---
        return ModelLineChartSend.builder()
                .domainNameList(domainNameList)
                .domainLinesDataList(domainLinesDataList)
                .build();
    }

    @Override
    public ModelPieChartSend getModelPieChart() {
        ModelPieChartSend modelPieChartSend = new ModelPieChartSend();
        // 1. 从数据库统计各状态的数量
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status", "count(*) as count_num")
                .groupBy("status");

        List<Map<String, Object>> resultList = commentsService.listMaps(queryWrapper);

        Map<Integer, Long> dbCountMap = resultList.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("status")).intValue(),
                        row -> ((Number) row.get("count_num")).longValue(),
                        (v1, v2) -> v1 // 防重
                ));

        List<String> messageList = new ArrayList<>();
        List<Long> countList = new ArrayList<>();
        for (CommentStatusEnum statusEnum : CommentStatusEnum.values()) {
            // 将枚举的描述信息放入列表
            messageList.add(statusEnum.getMessage());
            // 从 Map 中获取对应的数量，如果没查到则补 0
            Long count = dbCountMap.getOrDefault(statusEnum.getCode(), 0L);
            countList.add(count);
        }
        modelPieChartSend.setStatusNameList(messageList);
        modelPieChartSend.setStatusCountList(countList);

//        统计不同领域整体准确度
        List<Models> models = this.list();
        List<Domains> domains = domainsService.list();
        Map<Long, List<Models>> modelListByDomainId = models.stream().collect(Collectors.groupingBy(Models::getDomainId));
        List<String> domainNameList = new ArrayList<>();
        List<Long> inferredNumList = new ArrayList<>();
        List<Long> rightNumList = new ArrayList<>();
        List<BigDecimal> domainAccuracyList = domains.stream().map(domain -> {
                    domainNameList.add(domain.getDomainName());
                    List<Models> domainModels = modelListByDomainId.get(domain.getDomainId());
//                    统计domainModels的准确率
                    return getTotalAccuracy(domainModels,inferredNumList,rightNumList);
                }
        ).toList();


        return ModelPieChartSend.builder()
                .statusNameList(messageList)
                .statusCountList(countList)
                .domainNameList(domainNameList)
                .domainAccuracyList(domainAccuracyList)
                .inferredNumList(inferredNumList)
                .rightNumList(rightNumList)
                .build();
    }

    public BigDecimal getTotalAccuracy(List<Models> models,List<Long> inferredNumList,List<Long> rightNumList) {

        long totalInferredNum = models == null ? 0 :models.stream().mapToLong(Models::getInferredNum).sum();
        inferredNumList.add(totalInferredNum);
        long totalCorrectedNum =models==null?0: models.stream().mapToLong(Models::getCorrectedNum).sum();
        rightNumList.add(totalInferredNum-totalCorrectedNum);

        // 返回百分比形式，保留2位小数，和SQL的DECIMAL(5,2)一致
        return totalInferredNum == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(totalInferredNum - totalCorrectedNum)
                        .multiply(BigDecimal.valueOf(100))  // 转换为百分比
                        .divide(BigDecimal.valueOf(totalInferredNum), 2, RoundingMode.HALF_UP);  // 保留2位小数
    }


    public record VersionParts(int major, int minor) {
    }
}
