package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.ProductsMapper;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

    @Autowired
    private MerchantsService merchantsService;

    @Override
    public ProductDetailSend getProductDetail(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = this.getById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        ProductDetailSend.ProductDetailSendBuilder builder = ProductDetailSend.builder()
                .productId(product.getProductsId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .details(product.getDetails())
                .price(product.getPrice())
                .rating(product.getRating())
                .commentCount(product.getCommentCount())
                .positiveRate(product.getPositiveRate())
                .merchantId(product.getMerchantId());

        if (product.getMerchantId() != null) {
            Merchants merchant = merchantsService.getById(product.getMerchantId());
            if (merchant != null) {
                builder.merchantName(merchant.getName());
            }
        }

        return builder.build();
    }

    @Override
    public void addProduct(ProductAddRec productAddRec) {
        if (productAddRec.getMerchantId() <= 0) {
            throw new CustomBusinessException("商铺ID不合法");
        }

        Merchants merchant = merchantsService.getById(productAddRec.getMerchantId());
        if (merchant == null) {
            throw new CustomBusinessException("商铺不存在 merchantId=" + productAddRec.getMerchantId());
        }

        Products product = new Products()
                .setMerchantId(productAddRec.getMerchantId())
                .setName(productAddRec.getProductName())
                .setDetails(productAddRec.getProductDetail())
                .setImageUrl(productAddRec.getImageUrl())
                .setPrice(productAddRec.getPrice())
                .setDomainId(merchant.getDomainId());

        this.save(product);
    }
}
