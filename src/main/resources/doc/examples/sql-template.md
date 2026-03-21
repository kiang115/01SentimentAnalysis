# SQL 建模模板（常规业务）

## 1. 建表模板

```sql
CREATE TABLE `sample_entity` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用,1启用',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0否,1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例业务表';
```

## 2. 字段约定建议

- 主键统一 `BIGINT AUTO_INCREMENT`。
- 时间字段统一 `create_time`、`update_time`。
- 字符串字段使用 `utf8mb4`。
- 常用筛选字段必须建索引。
- 每个字段必须带 `COMMENT`。
