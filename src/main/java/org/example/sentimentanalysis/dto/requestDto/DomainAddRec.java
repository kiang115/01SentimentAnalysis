package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainAddRec {

    @NotBlank(message = "领域名称不能为空")
    private String domainName;

    @NotBlank(message = "领域地址不能为空")
    private String domainUrl;

    @NotBlank(message = "领域头像必须非空")
    private String domainImageUrl;

    private String domainDescription;

    private MultipartFile file;

    private MultipartFile goldTestFile;
}
