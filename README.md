# 项目说明与开发规范

本项目是基于 Vue 3 + TypeScript + Vite 的前端管理系统，使用 Element Plus 作为 UI 组件库，Pinia 进行状态管理，Axios 统一处理网络请求。

## 技术栈

- Vue `3.5.x`
- TypeScript `5.9.x`
- Vite `7.x`
- Element Plus `2.13.x`
- Pinia `3.x`
- Vue Router `5.x`
- Axios `1.x`
- Less `4.x`

## 目录结构约定

- `src/api/`：接口调用层，集中封装所有后端 API。
- `src/Dto/SendDto/`：前端发送给后端的请求参数类型定义。
- `src/Dto/ReceiveDto/`：后端返回给前端的数据类型定义。
- `src/Dto/SseSnapshot/`：SSE 场景下的快照数据结构定义。
- `src/views/`：页面级视图组件。
- `src/components/`：可复用业务组件。
- `src/router/`：路由配置与路由守卫。
- `src/stores/`：Pinia 状态管理。
- `src/utils/`：工具方法、请求封装、常量定义。
- `src/styles/`：全局样式与 Less 变量。

## 核心开发规范（必须遵守）

### 1. DTO 使用规范

- 前端发送的数据，统一在 `src/Dto/SendDto/` 中定义类型。
- 组件中使用请求参数时，必须直接使用已定义的 SendDto 类型，不允许在组件内临时重建“匿名结构体”。
- 后端返回的数据类型统一在 `src/Dto/ReceiveDto/` 中定义。

### 2. API 返回结构规范

- 标准返回类型定义在 `src/api/model-api.ts` 中：

  - `code: number`
  - `message: string`
  - `data: T`

- 项目约定后端成功返回 `code = 200`。
- 非 `200` 的响应会在 `src/utils/request.ts` 的响应拦截器中统一处理并中断流程，业务代码通常只处理成功态数据。

### 3. 请求封装规范

- 禁止在业务组件内直接使用原生 `axios` 发请求。
- 所有 HTTP 调用必须通过 `src/utils/request.ts` 中的 `get/post/http` 封装进行。
- 所有业务接口统一收口到 `src/api/*.ts` 中，由组件按需调用。

### 4. 路由与鉴权规范

- 路由统一维护在 `src/router/index.ts`。
- 除登录页外，页面访问默认经过 token 校验。
- token 读取优先使用 `userStore().getToken`，并与 localStorage 保持一致。
- 未登录或 token 缺失时，统一跳转 `/login`。

### 5. 状态管理规范

- 全局状态统一使用 Pinia（`src/stores/`）。
- 登录态相关信息由 `user` store 维护，不在组件内重复维护同类全局状态。

### 6. 样式规范

- 全局样式集中在 `src/styles/` 维护。
- 主题变量优先通过 `var.less` 管理，避免硬编码重复颜色与尺寸。
- 页面/组件样式默认使用 `lang="less"`，并遵循现有命名风格。

### 7. 代码协作规范

- 保证类型完整，不绕过 TypeScript 类型检查。
- 新增接口时，按顺序补齐：`SendDto/ReceiveDto -> api 封装 -> 页面调用`。
- 遇到业务语义不明确的情况，禁止擅自猜测实现，必须先与需求方确认。

## 开发与构建命令

```bash
npm install
npm run dev
npm run type-check
npm run build
npm run preview
```

## 环境配置

- API 地址通过环境变量 `VITE_APP_API_URL` 注入（见 `src/utils/constants.ts` 中 `BASE_URL`）。
- 本地开发请在 `.env.development` 中配置对应地址。

## 提交流程建议

- 提交前至少执行一次：`npm run type-check`。
- 涉及接口变更时，优先检查 DTO 与 API 封装是否同步更新。
- 避免在组件内出现未类型化的 `any` 请求参数或响应体。
