package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.mapper.TrainDataMapper;
import org.example.sentimentanalysis.service.TrainDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 训练数据表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainDataServiceImpl extends ServiceImpl<TrainDataMapper, TrainData> implements TrainDataService {

    @Override
    public void updateTrainCount(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> distinctIds = ids.stream().distinct().toList();
        // 一次查询，并校验“任意一个找不到抛异常”
        List<TrainData> existingList = listByIds(distinctIds);
        Set<Long> existingIds = existingList.stream().map(TrainData::getId).collect(Collectors.toSet());
        List<Long> missingIds = distinctIds.stream().filter(id -> !existingIds.contains(id)).toList();
        if (!missingIds.isEmpty()) {
            throw new CustomBusinessException("训练数据不存在或已删除，id=" + missingIds);
        }
        // 原子自增，一条 SQL
        LambdaUpdateWrapper<TrainData> wrapper = new LambdaUpdateWrapper<TrainData>()
                .setSql("train_count = train_count + 1")
                .in(TrainData::getId, distinctIds);
        update(wrapper);
    }
}
