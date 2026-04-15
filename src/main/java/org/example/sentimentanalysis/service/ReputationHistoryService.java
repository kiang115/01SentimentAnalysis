package org.example.sentimentanalysis.service;

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
}
