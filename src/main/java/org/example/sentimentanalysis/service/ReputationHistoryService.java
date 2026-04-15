package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.ReputationHistoryQueryRec;
import org.example.sentimentanalysis.dto.responseDto.ReputationHistoryChangeSend;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.ReputationHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 口碑历史记录表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-04-15
 */
public interface ReputationHistoryService extends IService<ReputationHistory> {
    void recordProductReputation(List<Products> updatedProducts);
    void recordMerchantReputation(List<Merchants> updatedMerchants);

    /**
     * 查询指定商铺/商品的口碑变化差额（当前值与最新历史快照之差）。
     * 无历史记录时返回 hasHistory=false、所有差值为 0 的结果。
     *
     * @param rec 包含 targetId（商铺或商品ID）和 type（0=商铺, 1=商品）
     */
    ReputationHistoryChangeSend getReputationChange(ReputationHistoryQueryRec rec);
}
