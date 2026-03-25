package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCreateRec {

    @NotBlank(message = "商铺名称不能为空")
    private String name;

    @NotNull(message = "领域id不能为空")
    private Long domainId;

    @NotBlank(message = "商铺描述不能为空")
    private String description;

    @NotBlank(message = "商铺头像url不能为空")
    private String avatarUrl;
}
