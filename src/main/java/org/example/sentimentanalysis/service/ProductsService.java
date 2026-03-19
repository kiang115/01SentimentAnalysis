package org.example.sentimentanalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
import org.example.sentimentanalysis.model.Products;

/**
 * <p>
 * 商品信息表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
public interface ProductsService extends IService<Products> {

    ProductDetailSend getProductDetail(Long productId);

    void addProduct(ProductAddRec productAddRec);
}
