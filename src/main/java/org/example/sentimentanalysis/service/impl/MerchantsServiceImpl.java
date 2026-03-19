package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.dto.requestDto.MerchantsQueryRec;
import org.example.sentimentanalysis.dto.responseDto.MerchantDetailSend;
import org.example.sentimentanalysis.dto.responseDto.MerchantsSend;
import org.example.sentimentanalysis.enums.MerchantsOrderTypeEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.mapper.MerchantsMapper;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商家信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Service
public class MerchantsServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantsService {

    @Autowired
    private DomainsService domainsService;
    @Lazy
    @Autowired
    private ProductsService productsService;

    @Override
    public MerchantsSend listMerchants(MerchantsQueryRec queryRec) {
        int pageNum = queryRec.getPageNum();
        int pageSize = queryRec.getPageSize();

        // 构建排序方式
        MerchantsOrderTypeEnum orderType = MerchantsOrderTypeEnum.fromCode(queryRec.getOrderName());

        LambdaQueryWrapper<Merchants> wrapper = new LambdaQueryWrapper<>();

        if (queryRec.getDomainId() != null) {
            wrapper.eq(Merchants::getDomainId, queryRec.getDomainId());
        }

        if (queryRec.getSearchText() != null && !queryRec.getSearchText().isEmpty()) {
            String keyword = "%" + queryRec.getSearchText().trim() + "%";
            wrapper.like(Merchants::getName, keyword)
                    .or()
                    .like(Merchants::getDescription, keyword);
        }

        // 排序逻辑
        switch (orderType) {
            case POSITIVE_RATE:
                wrapper.orderByDesc(Merchants::getPositiveRate);
                break;
            case COMMENT_COUNT:
                wrapper.orderByDesc(Merchants::getCommentCount);
                break;
            case RATING:
            default:
                wrapper.orderByDesc(Merchants::getRating);
                break;
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Merchants> merchantsList = this.list(wrapper);
        PageInfo<Merchants> pageInfo = new PageInfo<>(merchantsList);

        PageInfo<MerchantsSend.MerchantInfo> merchantInfoPageInfo = new PageInfo<>();
        merchantInfoPageInfo.setPageNum(pageInfo.getPageNum());
        merchantInfoPageInfo.setPageSize(pageInfo.getPageSize());
        merchantInfoPageInfo.setTotal(pageInfo.getTotal());
        merchantInfoPageInfo.setPages(pageInfo.getPages());
        merchantInfoPageInfo.setList(
                merchantsList.stream().map(m -> MerchantsSend.MerchantInfo.builder()
                                .merchantId(m.getMerchantsId())
                                .name(m.getName())
                                .domainName(m.getDomainName())
                                .description(m.getDescription())
                                .rating(m.getRating())
                                .commentCount(m.getCommentCount())
                                .positiveRate(m.getPositiveRate())
                                .avatarUrl(m.getAvatarUrl())
                                .build())
                        .collect(Collectors.toList())
        );

        List<DomainsInfo> domainsInfos = domainsService.list().stream()
                .map(domain -> DomainsInfo.builder()
                        .domainId(domain.getDomainId())
                        .domainName(domain.getDomainName())
                        .build())
                .collect(Collectors.toList());

        return MerchantsSend.builder()
                .pageInfo(merchantInfoPageInfo)
                .domains(domainsInfos)
                .build();
    }

    @Override
    public MerchantDetailSend getMerchantDetail(Long merchantId) {
        if (merchantId == null || merchantId <= 0) {
            throw new CustomBusinessException("商铺ID不合法");
        }

        Merchants merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new CustomBusinessException("商铺不存在, merchantId=" + merchantId);
        }

        List<Products> products = productsService.list(
                new LambdaQueryWrapper<Products>().eq(Products::getMerchantId, merchantId)
        );

        List<MerchantDetailSend.ProductBriefSend> productSends = products.stream()
                .map(product -> MerchantDetailSend.ProductBriefSend.builder()
                        .productId(product.getProductsId())
                        .name(product.getName())
                        .details(product.getDetails())
                        .rating(product.getRating())
                        .commentCount(product.getCommentCount())
                        .price(product.getPrice())
                        .imageUrl(product.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return MerchantDetailSend.builder()
                .merchantId(merchant.getMerchantsId())
                .name(merchant.getName())
                .domainId(merchant.getDomainId())
                .domainName(merchant.getDomainName())
                .description(merchant.getDescription())
                .rating(merchant.getRating())
                .commentCount(merchant.getCommentCount())
                .positiveRate(merchant.getPositiveRate())
                .avatarUrl(merchant.getAvatarUrl())
                .products(productSends)
                .build();
    }

}
