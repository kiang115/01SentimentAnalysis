# 异常与返回模板

## 1. 业务异常使用方式

```java
if (domain == null) {
    throw new CustomBusinessException("领域不存在");
}
```

## 2. 全局异常处理目标

- Controller 不写重复 try/catch。
- 业务异常输出业务可读信息。
- 未知异常统一转为失败响应。

## 3. 统一返回示例

成功：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001
  }
}
```

失败：

```json
{
  "code": 500,
  "message": "操作失败",
  "data": null
}
```
