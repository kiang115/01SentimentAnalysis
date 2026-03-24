# Repository Guidelines

## Project Structure & Module Organization
本仓库是情感分析系统的 Spring Boot 后端。核心代码位于 `src/main/java/org/example/sentimentanalysis`，按 `controller`、`service`、`service/impl`、`mapper`、`model`、`dto`、`config` 分层。MyBatis XML 位于 `src/main/resources/mapper`，运行配置在 `src/main/resources/application.properties`，补充文档与 SQL 示例在 `src/main/resources/doc`。测试代码放在 `src/test/java`，包路径应与生产代码保持镜像。

## Build, Test, and Development Commands
优先使用 Maven Wrapper，避免本地 Maven 版本漂移。

- `mvnw.cmd spring-boot:run`：本地启动后端，默认端口 `8888`
- `mvnw.cmd test`：运行 JUnit 5 / Spring Boot 测试
- `mvnw.cmd clean package`：清理并打包可执行 Jar

启动前确认 MySQL、Redis、FastAPI 服务可用，并核对 `application.properties` 中的连接配置。

## Coding Style & Naming Conventions
Java 使用 4 空格缩进，包名全小写，类名使用 PascalCase。遵循现有分层职责：Controller 只做请求编排，业务逻辑放在 Service，数据库访问通过 MyBatis-Plus Service/Mapper 完成。请求 DTO 放在 `dto/requestDto`，统一命名为 `*Rec`；响应 DTO 放在 `dto/responseDto`，统一命名为 `*Send`。接口返回统一使用 `Response<T>`，`@RequestBody` 入参默认配合 `@Valid`。

## Testing Guidelines
当前测试框架为 Spring Boot Test + JUnit 5，测试类命名使用 `*Tests`，例如 `SpringbootApplicationTests`。新增业务时，至少补齐对应 Service 或接口的基本通过路径测试；若涉及 SQL、分页、鉴权或 SSE，请覆盖关键分支。运行命令：`mvnw.cmd test`。

## Commit & Pull Request Guidelines
现有提交信息以简短中文说明为主，例如 `商品增删完成`、`bug fix`。继续保持单次提交只表达一个明确改动，避免混入无关格式化。PR 应说明变更目的、影响模块、配置或表结构变更；若修改接口，附上示例请求或返回；若影响联调，说明对 Vue/FastAPI 的依赖。

## Security & Configuration Tips
不要把真实密钥、数据库密码或第三方 Token 提交到仓库。新增配置优先写入 `application.properties` 的可替换项，并在 PR 中注明默认值与依赖服务。涉及表结构调整时，同时更新 `src/main/resources/doc/sql.txt` 或相关说明文档。
