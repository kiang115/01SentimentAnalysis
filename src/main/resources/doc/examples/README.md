# 示例文件目录

这些示例用于给后续开发者快速参考，全部围绕本项目既有规范：

- 请求 DTO 使用 `XXRec`
- 响应 DTO 使用 `XXSend`
- Controller 使用统一 `Response<T>` 返回
- 业务逻辑放在 Service/ServiceImpl
- 数据库访问使用 MyBatis-Plus

## 文件说明

- `merchant-create-flow.md`：新增商家的端到端业务示例（接口 + 数据流 + 错误处理）
- `code-template-controller-service.md`：Controller/DTO/Service/Impl 代码模板
- `sql-template.md`：常规业务表建表示例和字段约定
- `error-handling-template.md`：统一异常与返回规范示例
- `api-debug.http`：本地联调请求示例（可直接在 IDE HTTP Client 使用）
