package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainParaAddRec {
    //    添加一个参数配置时使用，必须每个参数都非null才行添加成功
    @NotNull(message = "domainId不能为空")
    private Long domainId;
    @NotNull(message = "loraR不能为空")
    private Integer loraR;
    @NotNull(message = "loraAlpha不能为空")
    private Integer loraAlpha;
    @NotNull(message = "epochs不能为空")
    private Integer epochs;
    @NotNull(message = "batchSize不能为空")
    private Integer batchSize;
    @NotNull(message = "learningRate不能为空")
    private BigDecimal learningRate;
    @NotNull(message = "randomSeed不能为null")
    private Integer randomSeed;

    /**
     * 核心校验逻辑：
     * 1. @NotEmpty: 确保列表不为 null 且 size > 0 (满足“至少一个”)
     * 2. @UniqueElements: 确保 List 内部元素不重复 (满足“不能重复”)
     * 3. @Pattern: 确保列表中的每个 String 都在范围内 (满足“指定组合”)
     */
    @NotEmpty(message = "loraModules不能为空，且至少包含一个模块")
    @UniqueElements(message = "loraModules不能包含重复的模块")
    private List<@Pattern(regexp = "^(query|key|value|dense)$", message = "loraModules只能包含: query, key, value, dense") String> loraModules;

    @NotNull(message = "trainSplitRatio不能为空")
    private BigDecimal trainSplitRatio;
}
