package org.example.sentimentanalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.sentimentanalysis.dto.requestDto.MerchantsQueryRec;
import org.example.sentimentanalysis.dto.responseDto.MerchantDetailSend;
import org.example.sentimentanalysis.dto.responseDto.MerchantsSend;
import org.example.sentimentanalysis.model.Merchants;

/**
 * <p>
 * 商家信息表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
public interface MerchantsService extends IService<Merchants> {

    /**
     * 分页查询商家列表，支持领域筛选、关键字搜索与多种排序方式。
     *
     * @param queryRec 查询与分页参数
     * @return 商家列表分页数据及领域信息
     */
    MerchantsSend listMerchants(MerchantsQueryRec queryRec);

    MerchantDetailSend getMerchantDetail(Long merchantId);
}
