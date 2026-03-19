package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetailSend {
    private Long merchantId;//商铺id
    private String name;//商铺名称
    private Long domainId;//所属领域id
    private String domainName;//所属领域名称
    private String description;//商铺详情描述
    private BigDecimal rating;//商铺综合评分
    private Long commentCount;//商铺总评论人数
    private BigDecimal positiveRate;//商铺好评率
    private String avatarUrl;//商铺头像/封面图url
    private List<ProductBriefSend> products;//该商铺下的商品列表

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBriefSend {
        private Long productId;//商品id
        private String name;//商品名称
        private String details;//商品详情描述
        private BigDecimal rating;//商品综合评分
        private Long commentCount;//商品评论人数
        private BigDecimal price;//商品价格
        private String imageUrl;//商品图片url
    }
}
