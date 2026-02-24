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
 * 领域信息表
 * </p>
 *
 * @author kiang
 * @since 2026-02-24
 */
@Getter
@Setter
@ToString
@TableName("domains")
@Accessors(chain = true)
public class Domains implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 领域id
     */
    @TableId(value = "domain_id", type = IdType.AUTO)
    private Long domainId;

    /**
     * 领域名字
     */
    @TableField("domain_name")
    private String domainName;

    /**
     * 领域模型路径名字, 如: waimai, hotel, shop
     */
    @TableField("domain_url")
    private String domainUrl;

    /**
     * 领域创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
