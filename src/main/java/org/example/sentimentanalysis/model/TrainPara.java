package org.example.sentimentanalysis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 训练参数配置模板表
 * </p>
 *
 * @author kiang
 * @since 2026-02-26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName(value = "train_para", autoResultMap = true)
public class TrainPara implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数配置Id
     */
    @TableId(value = "para_id", type = IdType.AUTO)
    private Long paraId;

    /**
     * 领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * LORA_R
     */
    @TableField("lora_r")
    private Integer loraR;

    /**
     * lora_alpha
     */
    @TableField("lora_alpha")
    private Integer loraAlpha;

    /**
     * 训练轮数
     */
    @TableField("epochs")
    private Integer epochs;

    /**
     * 训练批次大小
     */
    @TableField("batch_size")
    private Integer batchSize;

    /**
     * 学习率(1e-5)
     */
    @TableField("learning_rate")
    private BigDecimal learningRate;

    /**
     * 随机种子
     */
    @TableField("random_seed")
    private Integer randomSeed;

    /**
     * Lora模块列表, 可能的值: ["query", "key", "value", "dense"]
     */
    @TableField(value = "lora_modules", typeHandler = Fastjson2TypeHandler.class)
    private List<String> loraModules;

    /**
     * 训练集比例, eg: 0.80
     */
    @TableField("train_split_ratio")
    private BigDecimal trainSplitRatio;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
