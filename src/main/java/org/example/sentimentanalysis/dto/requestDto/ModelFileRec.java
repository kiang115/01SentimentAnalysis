package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelFileRec {
    private Long domainId;
    private MultipartFile[] files;
    private String description;
}
