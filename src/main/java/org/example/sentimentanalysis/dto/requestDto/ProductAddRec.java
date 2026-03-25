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
public class ProductAddRec {
    @NotNull(message = "产品名不能为null")
    private String productName;
    @NotNull(message = "产品详情不能为null")
    private String productDetail;
    @NotNull(message = "图片url不能为null")
    private String imageUrl;
    @NotNull(message = "价格不能为null")
    private BigDecimal price;
}
