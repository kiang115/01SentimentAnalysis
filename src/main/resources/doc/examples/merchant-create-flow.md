# 新增商家业务流示例

## 1. 接口定义（示例）

- 路径：`POST /addMerchant`
- 入参：`MerchantAddRec`
- 出参：`Response<Void>`

请求示例：

```json
{
  "name": "示例商家",
  "domainId": 1,
  "description": "主营家居用品"
}
```

成功响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

失败响应示例：

```json
{
  "code": 500,
  "message": "商家名称不能为空",
  "data": null
}
```

## 2. Controller 职责

- 使用 `@Valid` 校验入参。
- 不写复杂业务逻辑，只转发到 Service。
- 统一返回 `Response.success()` 或 `Response.data(...)`。

## 3. Service 职责

- 校验业务规则（如名称重复、领域是否存在）。
- 组装实体并调用 MyBatis-Plus 保存。
- 不符合规则时抛 `CustomBusinessException`。

## 4. 数据流（推荐）

1. Controller 接收 `MerchantAddRec`。
2. Service 校验 `domainId` 和名称唯一性。
3. Service 转换为 `Merchants` 实体并 `save`。
4. 返回统一 `Response<Void>`。

## 5. 常见错误场景

- `domainId` 不存在：抛业务异常。
- 名称为空：通过校验注解拦截。
- 名称重复：Service 查询后抛业务异常。
