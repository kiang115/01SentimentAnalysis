# 项目说明与开发规范

本项目是基于 **Vue 3 + TypeScript + Vite** 的前端管理系统，使用 **Element Plus** 作为 UI 组件库，**Pinia** 管理全局状态，**Axios** 统一处理网络请求，包含训练/推理任务看板、模型管理、商铺与商品详情等业务模块。

---

## 技术栈（与当前仓库保持一致）

- Vue `3.5.x`
- TypeScript `5.9.x`
- Vite `7.x`
- Element Plus `2.13.x`
- Pinia `3.x`
- Vue Router `5.x`
- Axios `1.x`
- ECharts `6.x`
- Less `4.x`

> Node 版本要求（`package.json#engines`）：`^20.19.0 || >=22.12.0`

---

## 快速开始

```bash
npm install
npm run dev
npm run type-check
npm run build
npm run preview
```

说明：

- `npm run build` 实际执行：先 `type-check` 再 `vite build`。
- 提交前至少执行一次 `npm run type-check`。

---

## 目录结构（当前项目）

```text
.
├─ public/
├─ src/
│  ├─ api/                # 接口调用层（model-api/common-api）
│  ├─ assets/             # 静态资源（含登录页图片）
│  ├─ components/         # 业务组件（Train/Infer/ModelTable 等）
│  ├─ Dto/
│  │  ├─ SendDto/         # 请求参数类型
│  │  ├─ ReceiveDto/      # 响应数据类型
│  │  └─ SseSnapshot/     # SSE 快照类型
│  ├─ router/             # 路由配置与前置守卫
│  ├─ stores/             # Pinia（user 等）
│  ├─ styles/             # less 变量与全局样式
│  ├─ utils/              # request 封装、常量、工具函数
│  ├─ views/              # 页面级视图
│  ├─ App.vue
│  └─ main.ts
├─ .env.development
├─ vite.config.ts
└─ README.md
```

---

## 页面与路由结构（实际定义）

路由统一维护在 `src/router/index.ts`：

- `/login`：登录页
- `/merchants`：商铺列表
- `/merchant/:merchantId`：商铺详情
- `/product/:productId`：商品详情
- `/testVue`：测试页面
- `/model`：模型管理父路由，默认重定向到 `/model/trainData`
  - `/model/trainData`：训练数据管理
  - `/model/modelData`：模型数据管理
  - `/model/Task`：训练与推理任务管理
- 兼容重定向：
  - `/Task -> /model/Task`
  - `/trainData -> /model/trainData`
  - `/modelData -> /model/modelData`

---

## 核心架构与数据流

### 1) DTO 分层

- 请求参数：`src/Dto/SendDto/*`
- 响应结构：`src/Dto/ReceiveDto/*`
- SSE 实时快照：`src/Dto/SseSnapshot/*`

### 2) API 统一收口

- 业务接口主要集中在 `src/api/model-api.ts`
- 登录等公共接口在 `src/api/common-api.ts`
- 标准响应类型：

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

### 3) 请求统一封装（`src/utils/request.ts`）

- 业务代码禁止直接使用原生 `axios`。
- 统一通过 `get/post/http` 封装调用。
- 请求拦截器会自动携带 `satoken`（来自 `userStore().getToken`）。
- 响应拦截器约定：
  - `code === 200`：走成功流
  - `code !== 200`：统一弹错并 `reject`
  - 非 JSON（如下载流）直接放行原始响应

### 4) 鉴权与登录态

- 路由守卫：除 `/login` 外均校验 token。
- token 读取优先级：Pinia 内存态 -> localStorage。
- 本地存储 key：`Constants.USER_TOKEN`（当前为 `token_value`）。
- 无 token 时：清理本地信息并跳转 `/login`。

### 5) 实时任务（SSE）

- 训练任务：`/api/sse/train/subscribe`
  - 事件：`model:train_event:first`、`model:train_event`
- 推理任务：`/api/sse/infer/subscribe`
  - 事件：`model:inference_event:first`、`model:inference_event`

约定：首包用于初始化快照，增量事件按 `taskId` 合并更新，组件卸载时必须关闭连接避免泄漏。

---

## 核心开发规范（必须遵守）

### 1. DTO 使用规范

- 前端发送参数统一定义到 `SendDto/`，后端返回数据统一定义到 `ReceiveDto/`。
- 组件内禁止临时拼“匿名请求体类型”替代 DTO。
- SSE 数据类型统一维护在 `SseSnapshot/`。

### 2. 新增接口标准流程

新增接口必须按顺序补齐：

1. 定义/更新 `SendDto` 与 `ReceiveDto`
2. 在 `src/api/*.ts` 新增方法并标注返回类型
3. 在页面/组件中调用 API，严格按类型消费 `res.data`

### 3. API 与请求规范

- 组件层不允许直接 `axios.get/post`。
- 统一走 `src/utils/request.ts`。
- 重要！！！ 非 200 的处理由拦截器统一兜底，业务层只处理成功态数据和必要兜底。

### 4. 路由与鉴权规范

- 路由配置只在 `src/router/index.ts` 改动。
- 新增页面时必须明确是否受守卫保护。
- 涉及 token 的逻辑，统一通过 `userStore` + `utils/utils.ts` 工具方法维护一致性。

### 5. 状态管理规范

- 全局状态统一放 Pinia（`src/stores/`）。
- 登录态相关数据仅由 `user` store 维护，禁止在多个组件重复维护同类全局状态。

### 6. SSE 规范

- 统一使用 `EventSource`，并在组件卸载时 close。
- 首包、增量包事件名必须与后端约定一致，不得“猜事件名”。
- 快照字段变更时，先更新 `SseSnapshot` 类型再改组件逻辑。

### 7. 样式规范

- 页面/组件样式默认 `lang="less"`。
- 变量优先使用 `src/styles/var.less`，避免硬编码。
- 全局样式放 `src/styles/` 统一管理。

### 8. TypeScript 规范

- 不绕过类型系统，不滥用 `any`（历史代码除外，新增代码必须收敛类型）。
- 业务数据、API 返回、组件状态尽量显式类型化。

---

## 环境配置

`.env.development` 示例：

```env
NODE_ENV=development
VITE_APP_TITLE='情感分析系统'
VITE_APP_API_URL='http://127.0.0.1:8888'
```

- 后端基础地址通过 `VITE_APP_API_URL` 注入，实际使用点：`src/utils/constants.ts -> BASE_URL`。
- `@` 路径别名已在 `vite.config.ts` 和 `tsconfig.app.json` 配置为 `src`。

---

## 协作与提交流程建议

- 提交前最少执行：
  - `npm run type-check`
  - 关键页面自测（训练任务、推理任务、模型列表、登录跳转）
- 涉及接口变更时，优先检查 DTO、API 封装、页面调用三者是否同步。
- 业务语义不明确时先确认需求，不要自行猜测字段含义。

---

## 现状说明（避免误解）

- `src/api/admin-api.ts` 目前是历史注释代码，不属于当前主流程。
- `src/styles/style.less` 与 `src/styles/login.less` 存在，但 `main.ts` 当前仅显式引入了 Element Plus 样式；如需启用对应全局样式，请在入口统一引入并评估影响。
- 当前子路由中存在大小写路径 `Task`，新增重定向或导航时请保持兼容性。
