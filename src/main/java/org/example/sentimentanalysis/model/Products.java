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
 * 商品信息表
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Getter
@Setter
@ToString
@TableName("products")
@Accessors(chain = true)
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品唯一标识
     */
    @TableId(value = "products_id", type = IdType.AUTO)
    private Long productsId;

    /**
     * 所属商家id
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品详情
     */
    @TableField("details")
    private String details;

    /**
     * 所属领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 综合得分(小数两位)
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Long commentCount;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 商品头像url
     */
    @TableField("image_url")
    private String imageUrl;

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
//   好评率
    @TableField("positive_rate")
    private BigDecimal positiveRate;
}
