package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantsQueryRec {
    @Builder.Default
    private Integer pageNum = 1;//页码
    @Builder.Default
    private Integer pageSize = 9;//每页数量
    private Long domainId;//领域id 为空表示不筛选领域
    private String orderName;//排序方式 对应关系为 rating->综合得分  positive->好评率 commentNum -> 好评人数 (单独使用enums类型定义)| 其他或者为空时 按照默认的综合得分排序
    //     排序是降序排序
    private String searchText;//关键字筛选，需要同时筛选 商家名-商品信息 //筛选商品名字需要将商家表和商品表join查询 最后仍然只返回包含这个商品的商家即可|为空表示不筛选
//    此筛选条件与上面的筛选条件共同生效
}
