package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.mapper.ModelsMapper;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.ModelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        if(maxVersionStr==null){
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
        if(models==null || models.isEmpty()){
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

    public record VersionParts(int major, int minor) {
    }
}
