<!--
  推理任务列表页：展示从 REST 拉取的任务卡片，并通过 SSE 实时更新处理中/已完成/异常任务的进度。
  - 顶部「开始推理」打开配置弹窗 InferPanel，确认后刷新列表并重连 SSE。
  - 任务数据来源：listInferTasks()；实时进度来源：SSE model:inference_event:first / model:inference_event。
  - 快照集合：首包（first）仅把 status===0（处理中）加入 snapshotMap；后续单条（event）不论 status 一律保留。
  - 展示：推理持续时间、处理总评论数、平均处理速度、状态文案、推理结束时间 — 有快照用快照，无快照用 DB；推理开始时间、模型信息仅用 DB。
  - 刷新列表时清空 snapshotMap，再建立 SSE，以便新列表先以 DB 展示，随后 SSE 再填充快照。
-->
<template>
  <el-descriptions class="margin-top" title="推理任务" :column="3" border>
    <template #extra>
      <el-button type="primary" @click="panelVisible = true">开始推理</el-button>
    </template>
  </el-descriptions>

  <InferPanel v-model="panelVisible" @confirm="onInferConfirm" />

  <ul v-if="taskList.length" class="task-list">
    <li v-for="task in taskList" :key="task.taskId">
      <el-card style="max-width: 400px" shadow="hover">
        <template #header>
          <div class="card-header">
            <span style="font-weight: bold;">推理任务: #{{ task.taskId }}</span>
            <div class="text-small">
              {{ task.inferenceStartTime }}
              <template v-if="displayEndTime(task)"> - {{ displayEndTime(task) }}</template>
              <template v-else> - 进行中</template>
            </div>
            <el-tag type="info" size="small">{{ displayStatusText(task) }}</el-tag>
          </div>
        </template>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" class="text-center mb-4">
            <el-statistic
              title="推理持续时间"
              :value="displayDuration(task)"
              value-style="font-weight:bold"
            >
              <template #suffix>
                <span style="font-weight: bold;">/秒</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" class="text-center mb-4">
            <el-statistic
              title="处理总评论数"
              :value="displayProcessedCount(task)"
              value-style="font-weight:bold"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" class="text-center mb-4">
            <el-statistic
              :value="displayAvgSpeed(task)"
              value-style="font-weight:bold"
              class="nowrap-statistic"
            >
              <template #title>平均处理速度</template>
              <template #suffix>
                <span style="font-weight: bold;">/秒</span>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
        <div v-if="progressPercent(task) != null" class="progress-block">
          <el-progress
            :percentage="progressPercent(task) ?? 0"
            :stroke-width="12"
          />
          <div class="progress-text">{{ getSnapshot(task)!.processedCount }} / {{ task.processedCount }}</div>
        </div>
        <template #footer>
          <div class="model-info">
            <span class="label">模型信息：</span>
            <template v-if="task.modelInfoList?.length">
              <el-tag
                v-for="m in task.modelInfoList"
                :key="m.modelId"
                size="small"
                class="model-tag"
              >
                {{ m.domainName }} (v{{ m.modelVersion }})
              </el-tag>
            </template>
            <span v-else>—</span>
          </div>
        </template>
      </el-card>
    </li>
  </ul>
  <el-empty v-else-if="!loading" description="暂无推理任务" />
  <div v-else class="loading-wrap">
    <el-icon class="is-loading"><Loading /></el-icon>
    <span>加载中...</span>
  </div>
</template>

<script lang="ts" setup>
/**
 * 推理任务列表组件 (Infer.vue)
 *
 * 功能概要：
 * - 通过 listInferTasks 拉取任务列表并展示为卡片（推理持续时间、处理总评论数、平均处理速度、模型信息等）。
 * - 建立 SSE 连接订阅 /api/sse/infer/subscribe，用服务端推送的快照实时更新卡片。
 * - 点击「开始推理」打开 InferPanel 配置并提交推理；确认后刷新列表并重新建立 SSE 连接。
 *
 * 数据与展示规则：
 * - taskList：来自 REST，为权威任务列表。
 * - snapshotMap：首包（model:inference_event:first）仅加入 status===0 的快照；后续单条（model:inference_event）任意状态都保留，不因 status 删除；刷新列表时清空。
 * - 先快照后 DB 的字段：推理持续时间、处理总评论数、平均处理速度、状态文案、推理结束时间。
 * - 仅 DB 的字段：推理开始时间、模型信息。
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'
import Constants from '@/utils/constants'
import type { InferTaskSnapshot } from '@/Dto/SseSnapshot/Infer-task-snapshot'
import InferPanel from './InferPanel.vue'
import type { InferenceTaskDto } from '@/Dto/responseDto/InferenceTaskDto'
// ==================== 状态与引用 ====================

/** 任务列表，来源为 listInferTasks() 的 data */
const taskList = ref<InferenceTaskDto[]>([])
/** 列表加载中，用于显示 loading 或空状态 */
const loading = ref(true)
/** 是否显示「推理信息配置」弹窗 InferPanel */
const panelVisible = ref(false)
/** SSE 连接实例，用于在 onUnmounted 或重连前关闭 */
const eventSourceRef = ref<EventSource | null>(null)
/** taskId -> 最新快照；首包仅加入 status===0；后续 event 任意状态都保留；刷新列表时清空 */
const snapshotMap = ref<Map<number, InferTaskSnapshot>>(new Map())

// ==================== 展示用计算（优先快照，无快照用 REST 数据） ====================

/** 根据 taskId 从 snapshotMap 取快照，无则 undefined */
function getSnapshot(task: InferenceTaskDto): InferTaskSnapshot | undefined {
  return snapshotMap.value.get(task.taskId)
}

/** 卡片「推理持续时间」展示值：有快照用快照 duration（秒），否则用 task.inferenceDuration（毫秒转秒）；统一保留两位小数，空为「—」 */
function displayDuration(task: InferenceTaskDto): number | string {
  const snap = getSnapshot(task)
  if (snap != null) return snap.duration > 0 ? snap.duration.toFixed(2) : '—'
  if (task.inferenceDuration == null) return '—'
  return (task.inferenceDuration / 1000).toFixed(2)
}

/** 卡片「处理总评论数」展示值：有快照用快照 processedCount，否则用 task.processedCount；空为「—」 */
function displayProcessedCount(task: InferenceTaskDto): number | string {
  const snap = getSnapshot(task)
  if (snap != null) return snap.processedCount
  return task.processedCount ?? '—'
}

/** 卡片「平均处理速度」展示值：有快照且快照 processSpeed 有值时用快照，否则用 DB avgProcessSpeed；空为「—」 */
function displayAvgSpeed(task: InferenceTaskDto): number | string {
  const snap = getSnapshot(task)
  if (snap != null && snap.processSpeed != null) {
    return snap.processSpeed.toFixed(2)
  }
  return task.avgProcessSpeed ?? '—'
}

/** 卡片「推理结束时间」展示值：有快照用快照 currentTime，否则用 task.inferenceEndTime；无则为 null（模板显示「进行中」） */
function displayEndTime(task: InferenceTaskDto): string | null {
  const snap = getSnapshot(task)
  if (snap != null) return snap.currentTime
  return task.inferenceEndTime ?? null
}

/** 卡片「状态文案」展示值：有快照用快照 statusMsg，否则用 task.processStatusName */
function displayStatusText(task: InferenceTaskDto): string {
  const snap = getSnapshot(task)
  return snap?.statusMsg ?? task.processStatusName
}

/** 实时进度百分比：SSE 当前评论数 / 数据库推理总评论数，0～100；无快照或总数≤0 时返回 null（不显示进度条） */
function progressPercent(task: InferenceTaskDto): number | null {
  const snap = getSnapshot(task)
  if (snap == null) return null
  const total = task.processedCount
  if (total == null || total <= 0) return null
  return Math.min(100, Math.round((snap.processedCount / total) * 100))
}

// ==================== 快照合并（后续单条一律保留，不因 status 删除） ====================

/**
 * 将单条快照合并进 snapshotMap：不论 status 一律 set，不删除。
 * 通过整体替换 snapshotMap.value 触发 Vue 响应式更新。
 */
function applySnapshot(snapshot: InferTaskSnapshot) {
  const next = new Map(snapshotMap.value)
  next.set(snapshot.taskId, snapshot)
  snapshotMap.value = next
}

// ==================== SSE 连接 ====================

/**
 * 建立 SSE 连接并监听推理进度事件。
 * 若已有连接则先 close 再新建，保证同一时刻只有一条连接。
 * 监听事件：model:inference_event:first（全量快照，仅 status===0 加入 map）、model:inference_event（单条快照，一律保留）。
 */
function connectSSE() {
  if (eventSourceRef.value) {
    eventSourceRef.value.close()
    eventSourceRef.value = null
  }
  const url = Constants.BASE_URL + '/api/sse/infer/subscribe'
  const es = new EventSource(url)
  eventSourceRef.value = es

  /** 连接建立后下发的全量快照：仅把 status === 0（处理中）的加入 map */
  es.addEventListener('model:inference_event:first', (e: MessageEvent) => {
    try {
      const list = JSON.parse(e.data as string) as InferTaskSnapshot[]
      const next = new Map(snapshotMap.value)
      for (const snap of list) {
        if (snap.status === 0) next.set(snap.taskId, snap)
      }
      snapshotMap.value = next
    } catch {
      // 解析失败时忽略
    }
  })

  /** 后续定时下发的单条快照，用于更新对应任务的实时进度 */
  es.addEventListener('model:inference_event', (e: MessageEvent) => {
    try {
      const snap = JSON.parse(e.data as string) as InferTaskSnapshot
      applySnapshot(snap)
    } catch {
      // 解析失败时忽略
    }
  })
}

// ==================== 数据加载与生命周期 ====================

/** 拉取任务列表，清空快照集后建立 SSE 连接（关旧建新） */
async function loadTaskList() {
  loading.value = true
  try {
    const res = await modelApi.listInferTasks()
    taskList.value = (res as { data: InferenceTaskDto[] }).data ?? []
    snapshotMap.value = new Map()
    connectSSE()
  } catch {
    taskList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadTaskList)
onUnmounted(() => {
  eventSourceRef.value?.close()
  eventSourceRef.value = null
})

/** InferPanel 点击推理确认后：刷新列表并重连 SSE */
function onInferConfirm() {
  loadTaskList()
}
</script>

<style lang="less" scoped>
/* 任务卡片列表 */
.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.task-list li {
  margin-bottom: 16px;
}
/* 实时进度条：仅当有快照且总数>0 时显示 */
.progress-block {
  margin-top: 12px;
}
.progress-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
/* 卡片头部：任务 ID、时间范围、状态标签 */
.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.text-small {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
/* 卡片底部：模型信息标签 */
.model-info {
  font-size: 12px;
}
.model-info .label {
  margin-right: 8px;
}
.model-tag {
  margin-right: 6px;
}
/* 列表加载中占位 */
.loading-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 24px;
}
</style>


