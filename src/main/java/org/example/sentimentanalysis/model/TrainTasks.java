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
 * 训练任务管理表
 * </p>
 *
 * @author kiang
 * @since 2026-02-20
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName(value = "train_tasks", autoResultMap = true)
public class TrainTasks implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 训练任务id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发起人id
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 领域中文名字
     */
    @TableField("domain_name")
    private String domainName;

    /**
     * 训练状态 (0:待处理, 1:处理中, 2:成功完成, 3:失败完成)
     */
    @TableField("status")
    private Integer status;

    /**
     * 训练开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 训练结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 训练持续时间(秒)
     */
    @TableField("duration")
    private Long duration;

    /**
     * 训练总评论数
     */
    @TableField("total_data")
    private Long totalData;

    /**
     * 来源于评论审核的数据数量
     */
    @TableField("corrected_num")
    private Long correctedNum;

    /**
     * 来源于手动上传的数据数量
     */
    @TableField("upload_num")
    private Long uploadNum;

    /**
     * 来源于初始数据集的数据数量
     */
    @TableField("original_num")
    private Long originalNum;

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
     * Lora模块列表, eg: ["query", "key", "value", "dense"]
     */
    @TableField(value = "lora_modules", typeHandler = Fastjson2TypeHandler.class)
    private List<String> loraModules;

    /**
     * 训练集比例, eg: 0.80
     */
    @TableField("train_split_ratio")
    private BigDecimal trainSplitRatio;

    /**
     * 是否重新训练，是=1 否=0
     */
    @TableField("if_over_train")
    private Integer ifOverTrain;

    /**
     * 训练得到的模型id
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 模型版本号(两位小数, 如1.12)
     */
    @TableField("model_version")
    private String modelVersion;

    /**
     * 总体准确率(%)
     */
    @TableField("accuracy")
    private BigDecimal accuracy;

    /**
     * 精确率(准确率)
     */
    @TableField("precision_rate")
    private BigDecimal precisionRate;

    /**
     * 召回率
     */
    @TableField("recall_rate")
    private BigDecimal recallRate;

    /**
     * f1分数
     */
    @TableField("f1_score")
    private BigDecimal f1Score;

     /**
     * 训练集loss变化趋势数组
     */
    @TableField(value = "train_loss_list", typeHandler = Fastjson2TypeHandler.class)
    private List<BigDecimal> trainLossList;

    /**
     * 训练集准确率变化趋势数组
     */
    @TableField(value = "train_acc_list", typeHandler = Fastjson2TypeHandler.class)
    private List<BigDecimal> trainAccList;

    /**
     * 验证集loss变化趋势数组
     */
    @TableField(value = "val_loss_list", typeHandler = Fastjson2TypeHandler.class)
    private List<BigDecimal> valLossList;

    /**
     * 验证集准确率变化趋势数组
     */
    @TableField(value = "val_acc_list", typeHandler = Fastjson2TypeHandler.class)
    private List<BigDecimal> valAccList;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
