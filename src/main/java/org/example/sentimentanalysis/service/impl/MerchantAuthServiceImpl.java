package org.example.sentimentanalysis.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.example.sentimentanalysis.enums.UserTypeEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.ProductsMapper;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.service.MerchantAuthService;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MerchantAuthServiceImpl implements MerchantAuthService {

    @Autowired
    private UsersService usersService;
    @Autowired
    private MerchantsService merchantsService;
    @Autowired
    private ProductsMapper productsMapper;

    private Users getCurrentUserOrThrow() {
        Long userId = StpUtil.getLoginIdAsLong();
        Users currentUser = usersService.getById(userId);
        if (currentUser == null) {
            throw new CustomBusinessException("当前登录用户不存在 userId=" + userId);
        }
        return currentUser;
    }

    @Override
    public Long getCurrentMerchantIdOrThrow() {
        Users currentUser = getCurrentUserOrThrow();
        if (!Objects.equals(currentUser.getUserType(), UserTypeEnum.MERCHANT.getCode())) {
            throw new CustomBusinessException("当前用户不是商家用户");
        }
        Long merchantId = currentUser.getMerchantId();
        if (merchantId == null || merchantId <= 0) {
            throw new CustomBusinessException("当前商家用户未绑定商铺");
        }
        return merchantId;
    }

    @Override
    public Merchants getCurrentMerchantOrThrow() {
        Long merchantId = getCurrentMerchantIdOrThrow();
        Merchants merchant = merchantsService.getById(merchantId);
        if (merchant == null) {
            throw new CustomBusinessException("当前商铺不存在 merchantId=" + merchantId);
        }
        return merchant;
    }

    @Override
    public Products getOwnedProductOrThrow(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = productsMapper.selectById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        Long currentMerchantId = getCurrentMerchantIdOrThrow();
        if (!Objects.equals(product.getMerchantId(), currentMerchantId)) {
            throw new CustomBusinessException("无权操作非本人商铺商品 productId=" + productId);
        }
        return product;
    }

    @Override
    public Products getTagAnalyzableProductOrThrow(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = productsMapper.selectById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        Users currentUser = getCurrentUserOrThrow();
        if (Objects.equals(currentUser.getUserType(), UserTypeEnum.ADMIN.getCode())) {
            return product;
        }

        Long currentMerchantId = getCurrentMerchantIdOrThrow();
        if (!Objects.equals(product.getMerchantId(), currentMerchantId)) {
            throw new CustomBusinessException("无权分析非本人商铺商品 productId=" + productId);
        }
        return product;
    }
}
