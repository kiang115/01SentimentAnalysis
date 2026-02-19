package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.mapper.ModelsMapper;
import org.example.sentimentanalysis.service.ModelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
            Models newModel = new Models().setModelVersion(trainRec.getModelVersion()).setDomainId(trainRec.getDomainId());
            save(newModel);
            return  newModel.getModelId();
        }
        return -1L;
    }
}
