package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantsSend {
    private PageInfo<MerchantInfo> pageInfo;//拿到需要展示的商铺信息
    private List<DomainsInfo> domains;//取出数据库中全部领域信息

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
//        商铺信息
    public static class MerchantInfo {
        private Long merchantId;//商铺id
        private String name;//商铺名字
        private String domainName;//商铺领域名字
        private String description;// 商品描述
        private BigDecimal rating;//商铺综合得分
        private Long commentCount;// 商铺评论总人数
        private BigDecimal positiveRate;//商铺好评率
        private String avatarUrl;//商铺展示大图
    }
}
