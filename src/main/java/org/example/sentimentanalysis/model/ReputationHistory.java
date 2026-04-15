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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 口碑历史记录表
 * </p>
 *
 * @author kiang
 * @since 2026-04-15
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("reputation_history")
public class ReputationHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史记录唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺或者商品的ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 类型: 0-商铺表, 1-商品表
     */
    @TableField("type")
    private Byte type;

    /**
     * 当前已经推理的评论数
     */
    @TableField("inferred_count")
    private Long inferredCount;

    /**
     * 总评论数
     */
    @TableField("comment_count")
    private Long commentCount;

    /**
     * 综合得分
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 好评率
     */
    @TableField("positive_rate")
    private BigDecimal positiveRate;

    /**
     * 综合得分在本领域的排名
     */
    @TableField("ranking")
    private Long ranking;

    /**
     * 创建时间 (自动添加)
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
