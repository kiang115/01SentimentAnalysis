package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailSend {
    private Long productId;//商品id
    private String name;//商品名字
    private String imageUrl;//商品图片url
    private String details;//商品详情
    private BigDecimal price;//商品价格 单位￥
    private BigDecimal rating;//综合得分
    private Long commentCount;//评论数
    private Long merchantId;//商铺id
    private String merchantName;//商铺名字
    private BigDecimal positiveRate;//好评率
    private Long domainId;//领域id
}
