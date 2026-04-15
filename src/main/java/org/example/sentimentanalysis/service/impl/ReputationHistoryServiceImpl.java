package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.ReputationHistory;
import org.example.sentimentanalysis.mapper.ReputationHistoryMapper;
import org.example.sentimentanalysis.service.ReputationHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 口碑历史记录表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-04-15
 */
@Service
public class ReputationHistoryServiceImpl extends ServiceImpl<ReputationHistoryMapper, ReputationHistory> implements ReputationHistoryService {
    // 定义增量阈值，例如每增加 100 条评论记录一次历史
    private static final int HISTORY_THRESHOLD = 3;

    @Override
    public void recordProductReputation(List<Products> updatedProducts) {
        if (CollectionUtils.isEmpty(updatedProducts)) return;

        List<Long> productIds = updatedProducts.stream()
                .map(Products::getProductsId)
                .collect(Collectors.toList());

        // 1. 批量查询这些商品在历史表中的“最新一条”记录
        // 为了性能，建议先查出每个商品最新的一条，这里使用 MP 的 list 配合简单的逻辑处理
        Map<Long, ReputationHistory> latestHistoryMap = this.list(
                new LambdaQueryWrapper<ReputationHistory>()
                        .in(ReputationHistory::getTargetId, productIds)
                        .eq(ReputationHistory::getType, (byte) 1) // 1 代表商品
                        .orderByDesc(ReputationHistory::getId)
        ).stream().collect(Collectors.toMap(
                ReputationHistory::getTargetId,
                h -> h,
                (existing, replacement) -> existing // 每组只保留第一条（即最新的）
        ));

        List<ReputationHistory> historiesToSave = new ArrayList<>();

        for (Products product : updatedProducts) {
            long currentInferredCount = product.getInferredCount() == null ? 0L : product.getInferredCount();
            ReputationHistory latestHistory = latestHistoryMap.get(product.getProductsId());

            boolean shouldRecord = false;
            if (latestHistory == null) {
                // 情况A：历史表没数据，且当前已有推理数据，记录第一条
                if (currentInferredCount > 0) {
                    shouldRecord = true;
                }
            } else {
                // 情况B：计算当前数量与历史最新数量的差值
                long diff = currentInferredCount - latestHistory.getInferredCount();
                if (diff >= HISTORY_THRESHOLD) {
                    shouldRecord = true;
                }
            }

            if (shouldRecord) {
                ReputationHistory newHistory = new ReputationHistory()
                        .setTargetId(product.getProductsId())
                        .setType((byte) 1)
                        .setInferredCount(currentInferredCount)
                        .setCommentCount(product.getCommentCount()) // 使用商品表当前总评论数
                        .setRating(product.getRating())
                        .setPositiveRate(product.getPositiveRate());
                // rank 暂不处理
                historiesToSave.add(newHistory);
            }
        }

        // 2. 批量保存历史记录
        if (!historiesToSave.isEmpty()) {
            this.saveBatch(historiesToSave);
        }
    }

    @Override
    public void recordMerchantReputation(List<Merchants> updatedMerchants) {
        if (CollectionUtils.isEmpty(updatedMerchants)) return;

        List<Long> merchantIds = updatedMerchants.stream()
                .map(Merchants::getMerchantsId)
                .collect(Collectors.toList());

        // 1. 获取这些商家在历史表中的最新一条记录 (type = 0)
        // 逻辑：按 targetId 分组并取 createTime 最晚的
        Map<Long, ReputationHistory> latestHistoryMap = this.list(
                new LambdaQueryWrapper<ReputationHistory>()
                        .in(ReputationHistory::getTargetId, merchantIds)
                        .eq(ReputationHistory::getType, (byte) 0) // 0 代表商家
                        .orderByDesc(ReputationHistory::getId)
        ).stream().collect(Collectors.toMap(
                ReputationHistory::getTargetId,
                h -> h,
                (existing, replacement) -> existing // 保持最新的一条
        ));

        List<ReputationHistory> historiesToSave = new ArrayList<>();

        for (Merchants merchant : updatedMerchants) {
            long currentInferredCount = merchant.getInferredCount() == null ? 0L : merchant.getInferredCount();
            ReputationHistory latestHistory = latestHistoryMap.get(merchant.getMerchantsId());

            boolean shouldRecord = false;
            if (latestHistory == null) {
                // 首次有推理数据时记录
                if (currentInferredCount > 0) shouldRecord = true;
            } else {
                // 差值达到阈值时记录
                long diff = currentInferredCount - latestHistory.getInferredCount();
                if (diff >= HISTORY_THRESHOLD) {
                    shouldRecord = true;
                }
            }

            if (shouldRecord) {
                ReputationHistory history = new ReputationHistory()
                        .setTargetId(merchant.getMerchantsId())
                        .setType((byte) 0) // 商家类型
                        .setInferredCount(currentInferredCount)
                        .setCommentCount(merchant.getCommentCount()) // 商家总评论数
                        .setRating(merchant.getRating())
                        .setPositiveRate(merchant.getPositiveRate());
                historiesToSave.add(history);
            }
        }

        // 2. 批量保存
        if (!historiesToSave.isEmpty()) {
            this.saveBatch(historiesToSave);
        }
    }
}
