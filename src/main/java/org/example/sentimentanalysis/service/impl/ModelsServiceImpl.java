package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.enums.ModelSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.mapper.ModelsMapper;
import org.example.sentimentanalysis.service.ModelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
}
