# 项目规范总览（SpringBoot 后端）

> 本文档基于以下已给材料整理：
> - `src/main/resources/doc/example.txt`
> - `src/main/resources/doc/sql.txt`

## 1. 项目定位与系统边界

根据 `src/main/resources/doc/example.txt`：

- 本项目是面向商户的评论情感分析系统。
- 系统由三端组成：
  - SpringBoot 后台端（本仓库）
  - FastAPI 模型服务端
  - Vue 前端
- 本规范仅约束 SpringBoot 后台端。
``
## 2. 技术与数据访问规范

根据 `src/main/resources/doc/example.txt`：

- 持久层框架：MyBatis-Plus。
- 表实体通过 MyBatis-Plus Generator 生成，位于 `model` 包。
- Service 层继承 `IService`，通过注入 Service 进行数据库操作。

规范要求：

- 数据库访问优先使用 MyBatis-Plus 提供的能力（Service + Wrapper）。
- 自动生成实体后，业务代码应避免重复手写基础 CRUD。``

## 3. 分层职责规范

根据 `src/main/resources/doc/example.txt`：

- Controller 层：实现接口，负责接收请求与返回响应。
- Service 层：承担业务逻辑与数据处理。

规范要求：

- Controller 只做接口编排，不承载复杂业务逻辑。
- Service 负责核心业务流程、参数校验后的业务规则处理与持久化。

## 4. 接口入参与出参规范（DTO）

根据 `src/main/resources/doc/example.txt`：

- 请求参数 DTO 必须定义在 `dto/requestDto`。
- 请求 DTO 命名规范：`XXRec.java`。
- 返回参数 DTO 必须定义在 `dto/responseDto`。
- 返回 DTO 命名规范：`XXSend.java`。

规范要求：

- 所有业务接口入参、出参都应采用 DTO，不直接暴露数据库实体。
- 命名必须遵守 `Rec/Send` 规则，便于统一识别。

## 5. 参数校验规范

根据 `src/main/resources/doc/example.txt`：

- 如果接口接收 body，需要使用 `@Valid` 做参数校验。

规范要求：

- 所有需要校验的请求对象必须声明校验注解并在 Controller 参数处使用 `@Valid`。
- 未通过校验的请求应在统一异常处理链路中返回标准错误结构。

## 6. 统一响应规范

根据 `src/main/resources/doc/example.txt`：

- 所有接口统一使用 `response/Response.java` 的结构体返回。

规范要求：

- 接口返回保持统一 envelope 结构。
- 禁止出现同一系统内多套返回体格式。

## 7. 数据库建模规范（已知部分）

根据 `src/main/resources/doc/sql.txt`：

- 表引擎：`InnoDB`
- 字符集：`utf8mb4`
- 字段应带 `COMMENT`，用于说明业务语义。
- 主键采用 `BIGINT AUTO_INCREMENT`（如 `users.user_id`、`merchants.merchants_id`）。
- 时间字段采用 `create_time` / `update_time`，并配置默认值与自动更新时间。

已示例表：

- `users`
  - 用户基础信息表
  - 包含索引 `idx_username (user_name)`
- `merchants`
  - 商家信息表
  - 包含领域标识与业务描述字段

## 8. 当前材料尚未覆盖的规范项

以下规范在已提供的 `example.txt` 与 `sql.txt` 中未明确，需要补充后再固化到本文件：

- 代码格式规范（缩进、行宽、IDE 统一规则）
- 命令规范（本地启动、测试、构建、发布命令）
- Git/分支/提交规范（commit message、PR 模板）
- 测试规范（单测/集成测试覆盖要求）
- 安全规范（配置脱敏、密钥管理）
- 日志与可观测性规范

建议优先查看并补充来源：

- `pom.xml`（构建、插件、依赖规范）
- `mvnw` / `mvnw.cmd`（命令入口）
- `src/main/resources/application.properties`（运行配置约定）
- `controller`、`service`、`response`、`exception` 包（实际编码约束）

## 9. 统一执行要求（落地）

- 新增接口必须遵守：`Rec/Send` DTO 命名 + `@Valid` 校验 + 统一 `Response` 返回。
- 新增数据表必须遵守：字段注释、时间字段规范、主键与字符集规范。
- 业务逻辑必须放在 Service 层，Controller 不得直接处理复杂业务。
- 当前开发协作默认不新增测试代码，除非用户明确要求。
- 默认不运行本机测试命令，不尝试启动或测试项目，因为本机不具备完整运行环境。
- 如需验证实现，仅进行静态代码检查与链路自查，不将“本地跑通”作为默认动作。

## 10. 示例参考文件（新增）

为便于后续开发者上手，已新增示例目录：`src/main/resources/doc/examples`。

- `src/main/resources/doc/examples/README.md`：示例索引说明。
- `src/main/resources/doc/examples/merchant-create-flow.md`：常规“新增商家”端到端业务流。
- `src/main/resources/doc/examples/code-template-controller-service.md`：DTO、Controller、Service、ServiceImpl 模板。
- `src/main/resources/doc/examples/sql-template.md`：常见业务表建表模板。
- `src/main/resources/doc/examples/error-handling-template.md`：统一异常和返回结构示例。
- `src/main/resources/doc/examples/api-debug.http`：本地联调 HTTP 请求示例。

---
