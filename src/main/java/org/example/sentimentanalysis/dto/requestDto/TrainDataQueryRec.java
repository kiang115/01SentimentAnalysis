package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainDataQueryRec {
//    以下数据，为null表示不筛选
    @Builder.Default
    private Integer pageNum=1;//页码
    @Builder.Default
    private Integer pageSize=8;//每页数量
    private String content;//搜索content内容 仅在content不为空或者非全空格时生效
    private Integer label;//按标签筛选 仅在label=0|1 时生效
    private String source;//按来源筛选 仅在source=original|upload|corrected时生效
    private Long domainId;//按照domainId筛选
    private String orderName;//time 创建时间 count 训练次数 默认按照id由小到大排序
    private String order;//参数 desc表示降序，asc表示升序 仅在orderName为time或count时+自己为desc|asc时生效
}
