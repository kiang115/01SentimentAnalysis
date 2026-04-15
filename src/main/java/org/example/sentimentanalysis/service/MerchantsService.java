package org.example.sentimentanalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.sentimentanalysis.dto.requestDto.MerchantCreateRec;
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

    Long addMerchant(MerchantCreateRec rec);

    MerchantsSend listMerchants(MerchantsQueryRec queryRec);

    MerchantDetailSend getMerchantDetail(Long merchantId);

    Long getMerchantDomainRanking(Merchants merchant);
}
