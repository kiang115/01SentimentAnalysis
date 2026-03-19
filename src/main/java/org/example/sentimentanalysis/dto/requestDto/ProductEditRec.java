package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEditRec {
    @NotNull(message = "商品id非空")
    private Long productId;
    @NotNull(message = "商品名字非空")
    private String productName;
    @NotNull(message = "商品详情非空")
    private String productDetail;
    @NotNull(message = "商品图片地址非空")
    private String imageUrl;
    @NotNull(message = "商品价格非空")
    private BigDecimal price;
}
