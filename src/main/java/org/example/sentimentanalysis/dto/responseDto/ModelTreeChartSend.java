package org.example.sentimentanalysis.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//要求
//你需要使用数据库中的models和domains表完成这个树形结果，数据库中的baseModelId对应的就是子节点，使用这个baseModelId的就是父节点。没有baseModelId的模型就是根节点。
// 你需要对每个领域都生成一张图，领域遍历名字，放到domainNameList中，然后每张图放到modelTreeNodeList中。每个modelTreeNodeList根节点为领域名字+模型树状图。然后才是哪些没有baseModelId的model 然后是以他们为baseModelId的模型，依次类推。
// 数据库已经严格保证 图中没有环形结构，只是简单的树形结构，无需任何对其的额外冗余处理。
//
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelTreeChartSend {
    private List<ModelTreeNode> modelTreeNodeList;
    private List<String> domainNameList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelTreeNode {

        // 对应显示在图表上的名称
        @JsonProperty("name")
        private String modelVersion; // 模型版本

        // 是否活跃，用于控制颜色
        private boolean active; // 活跃由models中的deleted字段控制

        // 精度，用于控制大小 (对应 ECharts 的 value)
        @JsonProperty("value")
        private BigDecimal accuracy;// 对应models表中的accuracy字段

        // 子节点递归
        private List<ModelTreeNode> children;//子节点
    }
}
