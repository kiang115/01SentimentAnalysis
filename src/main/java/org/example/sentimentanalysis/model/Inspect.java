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
 * 产品-标签统计表
 * </p>
 *
 * @author kiang
 * @since 2026-03-23
 */
@Getter
@Setter
@ToString
@TableName("inspect")
@Accessors(chain = true)
public class Inspect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 正向（好评）计数
     */
    @TableField("positive_count")
    private Integer positiveCount;

    /**
     * 负向（差评）计数
     */
    @TableField("negative_count")
    private Integer negativeCount;

    /**
     * 总计数
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
