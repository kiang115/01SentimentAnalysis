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
 * 商家信息表
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Getter
@Setter
@ToString
@TableName("merchants")
@Accessors(chain = true)
public class Merchants implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家唯一标识
     */
    @TableId(value = "merchants_id", type = IdType.AUTO)
    private Long merchantsId;

    /**
     * 商家名称
     */
    @TableField("name")
    private String name;

    /**
     * 领域id: 0=外卖, 1=电商, 2=酒店
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 领域中文名字
     */
    @TableField("domain_name")
    private String domainName;

    /**
     * 商家描述
     */
    @TableField("description")
    private String description;

    /**
     * 综合得分
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 总评论数
     */
    @TableField("comment_count")
    private Long commentCount;

    /**
     * 好评率
     */
    @TableField("positive_rate")
    private BigDecimal positiveRate;

    /**
     * 商家头像url
     */
    @TableField("avatar_url")
    private String avatarUrl;

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
