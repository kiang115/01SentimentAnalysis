package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainParaQueryRec {
    //    以下数据，为null表示不筛选
    private Integer pageNum=1;//页码
    private Integer pageSize=8;//每页数量
    private Long paraId;//输入筛选特定id的参数模板
    private Long domainId;//按照domainId筛选
}
