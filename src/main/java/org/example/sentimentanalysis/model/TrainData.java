package org.example.sentimentanalysis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 训练数据表
 * </p>
 *
 * @author kiang
 * @since 2026-02-20
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("train_data")
public class TrainData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 训练数据id (主键)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 训练数据内容
     */
    @TableField("content")
    private String content;

    /**
     * 训练数据标签
     */
    @TableField("label")
    private Integer label;

    /**
     * 训练数据来源 (corrected/upload/original)
     */
    @TableField("source")
    private String source;

    /**
     * 数据被训练次数
     */
    @TableField("train_count")
    private Integer trainCount;

    /**
     * 数据创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
