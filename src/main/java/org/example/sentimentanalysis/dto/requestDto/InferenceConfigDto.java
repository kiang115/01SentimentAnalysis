package org.example.sentimentanalysis.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data // 建议使用 Lombok 简化代码
public class InferenceConfigDto {
    private List<InferenceDomainConfig> inferenceDomainConfig;
    private String sort;
    // 使用 LocalDate 接收 YYYY-MM-DD 格式
//    前端选择器必须指定 format="YYYY/MM/DD"
    @JsonFormat(pattern = "YYYY-MM-DD")
    private LocalDate firstDate;

    @JsonFormat(pattern = "YYYY-MM-DD")
    private LocalDate lastDate;
}
//点击推理按钮，后端接受的部分模型参数
@Data
class InferenceDomainConfig {
    //领域id
    private Long domainId;
    //模型id
    private Long modelId;
    private Long inferenceReviewNums;
}
