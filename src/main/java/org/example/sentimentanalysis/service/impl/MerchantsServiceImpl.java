package org.example.sentimentanalysis.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.dto.requestDto.MerchantCreateRec;
import org.example.sentimentanalysis.dto.requestDto.MerchantsQueryRec;
import org.example.sentimentanalysis.dto.responseDto.MerchantDetailSend;
import org.example.sentimentanalysis.dto.responseDto.MerchantsSend;
import org.example.sentimentanalysis.enums.MerchantsOrderTypeEnum;
import org.example.sentimentanalysis.enums.UserTypeEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.mapper.MerchantsMapper;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.example.sentimentanalysis.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
    @Autowired
    private UsersService usersService;
    @Lazy
    @Autowired
    private ProductsService productsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMerchant(MerchantCreateRec rec) {
        // 1. 从 Sa-Token 会话中提取当前登录用户ID，后续所有校验都以数据库真实用户为准
        Long userId = StpUtil.getLoginIdAsLong();
        Users currentUser = usersService.getById(userId);
        if (currentUser == null) {
            throw new CustomBusinessException("当前登录用户不存在 userId=" + userId);
        }

        // 2. 校验当前登录用户必须是商家角色，且此前尚未绑定过商铺
        if (!Objects.equals(currentUser.getUserType(), UserTypeEnum.MERCHANT.getCode())) {
            throw new CustomBusinessException("当前用户不是商家用户，不能创建商铺");
        }
        if (currentUser.getMerchantId() != null) {
            throw new CustomBusinessException("当前用户已创建过商铺，不能重复创建");
        }

        // 3. 校验领域存在，并取出领域中文名写入 merchants 表
        domainsService.checkIdExist(rec.getDomainId());
        Domains domain = domainsService.getById(rec.getDomainId());
        if (domain == null || domain.getDomainName() == null || domain.getDomainName().isBlank()) {
            throw new CustomBusinessException("领域信息不存在或不完整 domainId=" + rec.getDomainId());
        }

        // 4. 组装商铺实体并入库，商铺基础统计字段使用数据库默认值
        Merchants merchant = new Merchants()
                .setName(rec.getName().trim())
                .setDomainId(rec.getDomainId())
                .setDomainName(domain.getDomainName())
                .setDescription(rec.getDescription().trim())
                .setAvatarUrl(rec.getAvatarUrl().trim());
        boolean saveSuccess = this.save(merchant);
        if (!saveSuccess || merchant.getMerchantsId() == null) {
            throw new CustomBusinessException("创建商铺失败");
        }

        // 5. 将当前用户与新建商铺绑定，保证 users.merchant_id 与 merchants 主键建立联系
        boolean bindSuccess = usersService.updateById(new Users()
                .setUserId(userId)
                .setMerchantId(merchant.getMerchantsId()));
        if (!bindSuccess) {
            throw new CustomBusinessException("绑定用户商铺关系失败");
        }
        return merchant.getMerchantsId();
    }

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

        List<DomainsInfo> domainsInfos = domainsService.listAllDomainsInfo();

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
