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
 * 模型表
 * </p>
 *
 * @author kiang
 * @since 2026-02-19
 */
@Getter
@Setter
@ToString
@TableName("models")
@Accessors(chain = true)
public class Models implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(value = "model_id", type = IdType.AUTO)
    private Long modelId;

    /**
     * 领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 模型版本号(两位小数, 如1.12)
     */
    @TableField("model_version")
    private String modelVersion;

    /**
     * 创建时间(年月日时分秒)
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
