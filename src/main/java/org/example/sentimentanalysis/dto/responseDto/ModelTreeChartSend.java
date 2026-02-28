package org.example.sentimentanalysis.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelTreeChartSend {
    private List<ModelTreeNode> modelTreeNodeList;
    private List<String> domainNameList;

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
