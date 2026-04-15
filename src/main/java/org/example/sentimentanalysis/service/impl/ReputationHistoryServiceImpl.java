package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.sentimentanalysis.dto.requestDto.ReputationHistoryQueryRec;
import org.example.sentimentanalysis.dto.responseDto.ReputationHistoryChangeSend;
import org.example.sentimentanalysis.dto.responseDto.ReputationHistoryLineChartSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.ReputationHistory;
import org.example.sentimentanalysis.mapper.ReputationHistoryMapper;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.example.sentimentanalysis.service.ReputationHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final DateTimeFormatter HISTORY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Lazy
    @Autowired
    private MerchantsService merchantsService;

    @Lazy
    @Autowired
    private ProductsService productsService;

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
                Long ranking = productsService.getProductDomainRanking(product);
                ReputationHistory newHistory = new ReputationHistory()
                        .setTargetId(product.getProductsId())
                        .setType((byte) 1)
                        .setInferredCount(currentInferredCount)
                        .setCommentCount(product.getCommentCount()) // 使用商品表当前总评论数
                        .setRating(product.getRating())
                        .setPositiveRate(product.getPositiveRate())
                        .setRanking(ranking);
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
                Long ranking = merchantsService.getMerchantDomainRanking(merchant);
                ReputationHistory history = new ReputationHistory()
                        .setTargetId(merchant.getMerchantsId())
                        .setType((byte) 0) // 商家类型
                        .setInferredCount(currentInferredCount)
                        .setCommentCount(merchant.getCommentCount()) // 商家总评论数
                        .setRating(merchant.getRating())
                        .setPositiveRate(merchant.getPositiveRate())
                        .setRanking(ranking);
                historiesToSave.add(history);
            }
        }

        // 2. 批量保存
        if (!historiesToSave.isEmpty()) {
            this.saveBatch(historiesToSave);
        }
    }

    @Override
    public ReputationHistoryChangeSend getReputationChange(ReputationHistoryQueryRec rec) {
        // 1. 先获取当前实际值
        BigDecimal currentRating;
        BigDecimal currentPositiveRate;
        Long currentCommentCount;
        Long currentRanking;

        if (rec.getType() == 0) {
            // 商铺
            Merchants merchant = merchantsService.getById(rec.getTargetId());
            if (merchant == null) {
                throw new CustomBusinessException("商铺不存在, merchantId=" + rec.getTargetId());
            }
            currentRating = merchant.getRating();
            currentPositiveRate = merchant.getPositiveRate();
            currentCommentCount = merchant.getCommentCount();
            currentRanking = merchantsService.getMerchantDomainRanking(merchant);
        } else {
            // 商品
            Products product = productsService.getById(rec.getTargetId());
            if (product == null) {
                throw new CustomBusinessException("商品不存在, productId=" + rec.getTargetId());
            }
            currentRating = product.getRating();
            currentPositiveRate = product.getPositiveRate();
            currentCommentCount = product.getCommentCount();
            currentRanking = productsService.getProductDomainRanking(product);
        }

        BigDecimal safeCurrentRating = currentRating == null ? BigDecimal.ZERO : currentRating;
        BigDecimal safeCurrentPositiveRate = currentPositiveRate == null ? BigDecimal.ZERO : currentPositiveRate;
        Long safeCurrentCommentCount = currentCommentCount == null ? 0L : currentCommentCount;

        // 2. 查询最新两条历史记录
        List<ReputationHistory> latestTwoHistoryList = this.lambdaQuery()
                .eq(ReputationHistory::getTargetId, rec.getTargetId())
                .eq(ReputationHistory::getType, rec.getType())
                .orderByDesc(ReputationHistory::getId)
                .last("LIMIT 2")
                .list();

        // 3. 无历史记录 → 历史值按 0 处理，rankingDiff 固定返回 0
        if (latestTwoHistoryList.isEmpty()) {
            return ReputationHistoryChangeSend.builder()
                    .ratingDiff(safeCurrentRating)
                    .positiveRateDiff(safeCurrentPositiveRate)
                    .commentCountDiff(safeCurrentCommentCount)
                    .rankingDiff(0L)
                    .build();
        }

        ReputationHistory baseline = latestTwoHistoryList.get(0);
        BigDecimal baselineRating = baseline.getRating() == null ? BigDecimal.ZERO : baseline.getRating();
        BigDecimal baselinePositiveRate = baseline.getPositiveRate() == null ? BigDecimal.ZERO : baseline.getPositiveRate();
        Long baselineCommentCount = baseline.getCommentCount() == null ? 0L : baseline.getCommentCount();

        boolean sameAsLatest = safeCurrentRating.compareTo(baselineRating) == 0
                && safeCurrentPositiveRate.compareTo(baselinePositiveRate) == 0
                && safeCurrentCommentCount.equals(baselineCommentCount);

        if (sameAsLatest) {
            if (latestTwoHistoryList.size() == 1) {
                return ReputationHistoryChangeSend.builder()
                        .ratingDiff(safeCurrentRating)
                        .positiveRateDiff(safeCurrentPositiveRate)
                        .commentCountDiff(safeCurrentCommentCount)
                        .rankingDiff(0L)
                        .build();
            }
            baseline = latestTwoHistoryList.get(1);
        }

        // 4. 计算差值（当前 - 基线历史快照），任一字段为 null 时差值取 0
        BigDecimal baselineHistoryRating = baseline.getRating();
        BigDecimal baselineHistoryPositiveRate = baseline.getPositiveRate();
        Long baselineHistoryCommentCount = baseline.getCommentCount();
        Long baselineHistoryRanking = baseline.getRanking();

        BigDecimal ratingDiff = (currentRating != null && baselineHistoryRating != null)
                ? currentRating.subtract(baselineHistoryRating)
                : BigDecimal.ZERO;

        BigDecimal positiveRateDiff = (currentPositiveRate != null && baselineHistoryPositiveRate != null)
                ? currentPositiveRate.subtract(baselineHistoryPositiveRate)
                : BigDecimal.ZERO;

        Long commentCountDiff = (currentCommentCount != null && baselineHistoryCommentCount != null)
                ? currentCommentCount - baselineHistoryCommentCount
                : 0L;

        Long rankingDiff = (currentRanking != null && baselineHistoryRanking != null)
                ? currentRanking - baselineHistoryRanking
                : 0L;

        return ReputationHistoryChangeSend.builder()
                .ratingDiff(ratingDiff)
                .positiveRateDiff(positiveRateDiff)
                .commentCountDiff(commentCountDiff)
                .rankingDiff(rankingDiff)
                .build();
    }

    @Override
    public ReputationHistoryLineChartSend getReputationLineChart(ReputationHistoryQueryRec rec) {
        BigDecimal currentRating;
        BigDecimal currentPositiveRate;
        Long currentCommentCount;
        Long currentRanking;

        if (rec.getType() == 0) {
            Merchants merchant = merchantsService.getById(rec.getTargetId());
            if (merchant == null) {
                throw new CustomBusinessException("商铺不存在, merchantId=" + rec.getTargetId());
            }
            currentRating = merchant.getRating();
            currentPositiveRate = merchant.getPositiveRate();
            currentCommentCount = merchant.getCommentCount();
            currentRanking = merchantsService.getMerchantDomainRanking(merchant);
        } else {
            Products product = productsService.getById(rec.getTargetId());
            if (product == null) {
                throw new CustomBusinessException("商品不存在, productId=" + rec.getTargetId());
            }
            currentRating = product.getRating();
            currentPositiveRate = product.getPositiveRate();
            currentCommentCount = product.getCommentCount();
            currentRanking = productsService.getProductDomainRanking(product);
        }

        List<ReputationHistory> historyList = this.lambdaQuery()
                .eq(ReputationHistory::getTargetId, rec.getTargetId())
                .eq(ReputationHistory::getType, rec.getType())
                .orderByAsc(ReputationHistory::getCreateTime)
                .orderByAsc(ReputationHistory::getId)
                .list();

        List<String> timeList = new ArrayList<>(historyList.stream()
                .map(history -> Optional.ofNullable(history.getCreateTime())
                        .map(time -> time.format(HISTORY_TIME_FORMATTER))
                        .orElse(""))
                .toList());
        List<BigDecimal> ratingList = new ArrayList<>(historyList.stream()
                .map(history -> Optional.ofNullable(history.getRating()).orElse(BigDecimal.ZERO))
                .toList());
        List<BigDecimal> positiveRateList = new ArrayList<>(historyList.stream()
                .map(history -> Optional.ofNullable(history.getPositiveRate()).orElse(BigDecimal.ZERO))
                .toList());
        List<Long> commentCountList = new ArrayList<>(historyList.stream()
                .map(history -> Optional.ofNullable(history.getCommentCount()).orElse(0L))
                .toList());
        List<Long> rankingList = new ArrayList<>(historyList.stream()
                .map(history -> Optional.ofNullable(history.getRanking()).orElse(0L))
                .toList());

        timeList.add("当前");
        ratingList.add(Optional.ofNullable(currentRating).orElse(BigDecimal.ZERO));
        positiveRateList.add(Optional.ofNullable(currentPositiveRate).orElse(BigDecimal.ZERO));
        commentCountList.add(Optional.ofNullable(currentCommentCount).orElse(0L));
        rankingList.add(Optional.ofNullable(currentRanking).orElse(0L));

        return ReputationHistoryLineChartSend.builder()
                .timeList(timeList)
                .ratingList(ratingList)
                .positiveRateList(positiveRateList)
                .commentCountList(commentCountList)
                .rankingList(rankingList)
                .build();
    }
}
