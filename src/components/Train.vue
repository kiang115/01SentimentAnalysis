<!--
  训练任务列表页：展示从 REST 拉取的任务卡片，并通过 SSE 实时更新处理中/已完成/异常任务的进度。
  - 顶部「开始训练」打开配置弹窗 TrainPanel，确认后刷新列表并按 ifAllFinished 重连 SSE。
  - 任务数据来源：listTrainTasks()（data 即 TrainTasksRec）；实时进度来源：SSE model:train_event:first / model:train_event。
  - 快照集合：首包仅在有 status 0/1 时将 0/1 入 map 否则关 SSE；后续单条按 taskId 覆盖。
  - 展示：有快照用快照（结束时间仅 status 0/1 显示 '-'），无快照用 DB；领域、模型版本仅用 DB。
-->
<template>
  <el-descriptions class="margin-top" title="训练任务" :column="3" border>
    <template #extra>
      <el-button type="primary" @click="panelVisible = true">开始训练</el-button>
    </template>
  </el-descriptions>

  <TrainPanel v-model="panelVisible" @confirm="onTrainConfirm" />
  <TrainDetailPanel v-model="detailVisible" :task="detailTask" />

  <ul v-if="taskList.length" class="task-list">
    <li v-for="task in taskList" :key="task.id">
      <el-card class="train-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="card-header-row">
              <span style="font-weight: bold;">模型训练 #{{ task.id }}</span>
              <div class="card-header-tags">
                <el-tag type="info" size="small">领域:{{ task.domainName }}</el-tag>
                <el-tag type="success" size="small">模型版本:{{ task.modelVersion }}{{ task.ifOverTrain === 1 ? ' (重训版本)' : '' }}</el-tag>
              </div>
              <el-button
                type="primary"
                link
                :disabled="!isTaskCompleted(task)"
                @click="openDetail(task)"
              >
                <el-icon><DataLine /></el-icon>
              </el-button>
            </div>
            <div class="text-small">
              {{ task.startTime }}
              <template v-if="displayEndTime(task)"> - {{ displayEndTime(task) }}</template>
              <template v-else> - 进行中</template>
            </div>
            <el-tag :type="getStatusTagType(task)" size="small">{{ displayStatusText(task) }}</el-tag>
          </div>
        </template>
        <el-row :gutter="16" class="stat-row">
          <el-col :xs="24" :sm="8" class="text-center mb-4">
            <div class="stat-block">
              <div class="stat-title">{{ getSnapshot(task) ? '当前轮次/总轮次' : '总轮次' }}</div>
              <div class="stat-value">{{ displayEpoch(task) }}</div>
            </div>
            <div v-if="getSnapshot(task)" class="text-small mt-2">
              {{ getSnapshot(task)!.currentBatch }} / {{ getSnapshot(task)!.epochTotalBatches }} - 每轮总批次
            </div>
          </el-col>
          <el-col :xs="24" :sm="8" class="text-center mb-4">
            <div class="stat-block">
              <div class="stat-title">训练总数据量</div>
              <div class="stat-value">{{ task.totalData }}</div>
            </div>
            <div class="text-small mt-2">
              训练集:验证集 {{ displayTrainValRatio(task) }}
            </div>
          </el-col>
          <el-col :xs="24" :sm="8" class="text-center mb-4">
            <div class="stat-block">
              <div class="stat-title">训练持续时间</div>
              <div class="stat-value">{{ displayDuration(task) }}</div>
            </div>
          </el-col>
        </el-row>
        <div v-if="getSnapshot(task)" class="progress-block">
          <div class="progress-row">
            <el-progress
              :percentage="displayProgressPercent(task)"
              :stroke-width="12"
              class="progress-flex"
            />
            <span v-if="getSnapshot(task)?.processSpeed != null" class="progress-speed">
              {{ displayProcessSpeed(task) }} batch/s
            </span>
          </div>
        </div>
        <template #footer>
          <div class="card-footer">
            <div class="footer-section">
              <span class="footer-section-title">训练指标</span>
              <div class="footer-metrics">
                <span class="metric-wrap"><el-tag size="small" class="metric-tag">准确率:{{ displayAccuracy(task) }}</el-tag></span>
                <span class="metric-wrap"><el-tag size="small" class="metric-tag">精确率:{{ displayPrecision(task) }}</el-tag></span>
                <span class="metric-wrap"><el-tag size="small" class="metric-tag">召回率:{{ displayRecall(task) }}</el-tag></span>
                <span class="metric-wrap"><el-tag size="small" class="metric-tag">F1:{{ displayF1(task) }}</el-tag></span>
              </div>
            </div>
          </div>
        </template>
      </el-card>
    </li>
  </ul>
  <el-empty v-else-if="!loading" description="暂无训练任务" />
  <div v-else class="loading-wrap">
    <el-icon class="is-loading"><Loading /></el-icon>
    <span>加载中...</span>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { Loading } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'
import Constants from '@/utils/constants'
import type { TrainTaskSnapshot } from '@/Dto/SseSnapshot/TrainTaskSnapshot'
import type { TrainTasksItem, TrainTasksRec } from '@/Dto/ReceiveDto/TrainTasksRec'
import TrainPanel from './TrainPanel.vue'
import TrainDetailPanel from './TrainDetailPanel.vue'

const { DataLine } = ElementPlusIconsVue

// ==================== 状态与引用 ====================

const taskList = ref<TrainTasksItem[]>([])
const loading = ref(true)
const panelVisible = ref(false)
const detailVisible = ref(false)
const detailTask = ref<TrainTasksItem | null>(null)
watch(detailVisible, (v) => {
  if (!v) detailTask.value = null
})
const eventSourceRef = ref<EventSource | null>(null)
const snapshotMap = ref<Map<number, TrainTaskSnapshot>>(new Map())

type ListTrainTasksRes = { code: number; data: TrainTasksRec; message: string }

const STATUS_TEXT: Record<number, string> = {
  0: '待处理',
  1: '处理中',
  2: '已完成',
  3: '失败',
}

// ==================== 展示用计算 ====================

function getSnapshot(task: TrainTasksItem): TrainTaskSnapshot | undefined {
  return snapshotMap.value.get(task.id)
}

/** 结束时间：有快照且 status 0/1 返回 null（显示进行中）；有快照且 2/3 用 currentTime；无快照用 task.endTime */
function displayEndTime(task: TrainTasksItem): string | null {
  const snap = getSnapshot(task)
  if (snap != null) {
    if (snap.status === 0 || snap.status === 1) return null
    return snap.currentTime
  }
  return task.endTime ?? null
}

/** 状态文案：有快照用 statusMsg，无快照按 status 映射 */
function displayStatusText(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  if (snap != null) return snap.statusMsg
  return STATUS_TEXT[task.status] ?? '—'
}

/** 状态对应的标签类型：0 待处理=info，1 处理中=warning，2 已完成=success，3 失败=danger */
function getStatusTagType(task: TrainTasksItem): 'info' | 'warning' | 'success' | 'danger' {
  const snap = getSnapshot(task)
  const status = snap != null ? snap.status : task.status
  const map: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
  }
  return map[status] ?? 'info'
}

/** 仅当 status=2（已完成）时可点击详情按钮 */
function isTaskCompleted(task: TrainTasksItem): boolean {
  const snap = getSnapshot(task)
  const status = snap != null ? snap.status : task.status
  return status === 2
}

function openDetail(task: TrainTasksItem) {
  detailTask.value = task
  detailVisible.value = true
}

/** 轮次：在 set 显示 currentEpoch/totalEpochs，否则只显示 epochs */
function displayEpoch(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  if (snap != null) return `${snap.currentEpoch} / ${snap.totalEpochs}`
  return String(task.epochs)
}

/** 训练集:验证集 百分比 */
function displayTrainValRatio(task: TrainTasksItem): string {
  const r = task.trainSplitRatio
  const train = (r * 100).toFixed(0)
  const val = ((1 - r) * 100).toFixed(0)
  return `${train}% : ${val}%`
}

/** 进度条百分比：快照可能为字符串，统一转数字 */
function displayProgressPercent(task: TrainTasksItem): number {
  const snap = getSnapshot(task)
  if (snap?.progressPercent == null) return 0
  const n = Number(snap.progressPercent)
  return Number.isFinite(n) ? n : 0
}

/** 处理速度：后端可能为字符串，统一转数字后保留两位小数 */
function displayProcessSpeed(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  if (snap?.processSpeed == null) return '—'
  const n = Number(snap.processSpeed)
  return Number.isFinite(n) ? n.toFixed(2) : '—'
}

/** 持续时间：秒数格式化为 分:秒 或 时:分 */
function displayDuration(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  const sec = snap != null ? snap.duration : task.duration
  if (sec == null || !Number.isFinite(sec)) return '—'
  const totalSec = Math.round(Number(sec))
  if (totalSec >= 3600) {
    const h = Math.floor(totalSec / 3600)
    const m = Math.floor((totalSec % 3600) / 60)
    return `${h}:${String(m).padStart(2, '0')}`
  }
  const m = Math.floor(totalSec / 60)
  const s = totalSec % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

/** 训练指标保留四位小数；有快照用快照，否则用 DB */
function displayAccuracy(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  const v = snap != null ? snap.accuracy : task.accuracy
  return v != null && Number.isFinite(v) ? Number(v).toFixed(4) : '—'
}

function displayPrecision(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  const v = snap != null ? snap.precisionRate : task.precisionRate
  return v != null && Number.isFinite(v) ? Number(v).toFixed(4) : '—'
}

function displayRecall(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  const v = snap != null ? snap.recallRate : task.recallRate
  return v != null && Number.isFinite(v) ? Number(v).toFixed(4) : '—'
}

function displayF1(task: TrainTasksItem): string {
  const snap = getSnapshot(task)
  const v = snap != null ? snap.f1Score : task.f1Score
  return v != null && Number.isFinite(v) ? Number(v).toFixed(4) : '—'
}

// ==================== 快照合并 ====================

function applySnapshot(snapshot: TrainTaskSnapshot) {
  const next = new Map(snapshotMap.value)
  next.set(snapshot.taskId, snapshot)
  snapshotMap.value = next
}

// ==================== SSE 连接 ====================

function connectSSE() {
  if (eventSourceRef.value) {
    eventSourceRef.value.close()
    eventSourceRef.value = null
  }
  const url = Constants.BASE_URL + '/api/sse/train/subscribe'
  const es = new EventSource(url)
  eventSourceRef.value = es
  console.log('[Train] SSE created:', url)

  es.addEventListener('open', () => {
    console.log('[Train] SSE connection open')
  })

  es.addEventListener('model:train_event:first', (e: MessageEvent) => {
    try {
      const list = JSON.parse(e.data as string) as TrainTaskSnapshot[]
      const hasOngoing = list.some((s) => s.status === 0 || s.status === 1)
      console.log('[Train] first event: count=', list.length, 'hasOngoing=', hasOngoing, 'snaps=', list.map((s) => ({ taskId: s.taskId, status: s.status, statusMsg: s.statusMsg })))
      if (!hasOngoing) {
        es.close()
        if (eventSourceRef.value === es) {
          eventSourceRef.value = null
        }
        console.log('[Train] first event had no status 0/1, SSE closed')
        return
      }
      const next = new Map(snapshotMap.value)
      for (const snap of list) {
        if (snap.status === 0 || snap.status === 1) {
          next.set(snap.taskId, snap)
        }
      }
      snapshotMap.value = next
      console.log('[Train] snapshotMap after first: size=', next.size, 'taskIds=', [...next.keys()])
    } catch (err) {
      console.warn('[Train] first event parse error', err)
    }
  })

  es.addEventListener('model:train_event', (e: MessageEvent) => {
    try {
      const snap = JSON.parse(e.data as string) as TrainTaskSnapshot
      applySnapshot(snap)
      console.log('[Train] event: taskId=', snap.taskId, 'status=', snap.status, 'progressPercent=', snap.progressPercent)
    } catch (err) {
      console.warn('[Train] train_event parse error', err)
    }
  })

  es.addEventListener('error', () => {
    console.warn('[Train] SSE error, closing')
    es.close()
    if (eventSourceRef.value === es) {
      eventSourceRef.value = null
    }
  })
}

function closeSSE() {
  if (eventSourceRef.value) {
    eventSourceRef.value.close()
    eventSourceRef.value = null
  }
}

// ==================== 数据加载与生命周期 ====================

async function loadTaskList() {
  loading.value = true
  try {
    const res = (await modelApi.listTrainTasks()) as unknown as ListTrainTasksRes
    const data = res?.data
    taskList.value = data?.trainTasksList ?? []
    console.log('[Train] loadTaskList: taskCount=', taskList.value.length, 'ifAllFinished=', data?.ifAllFinished, 'taskIds=', taskList.value.map((t) => t.id))
    if (data?.ifAllFinished === 1) {
      closeSSE()
      console.log('[Train] ifAllFinished=1, SSE not connected (closed if any)')
    } else {
      snapshotMap.value = new Map()
      connectSSE()
      console.log('[Train] ifAllFinished=0, SSE connecting...')
    }
  } catch (e) {
    console.warn('[Train] loadTaskList failed', e)
    taskList.value = []
    closeSSE()
  } finally {
    loading.value = false
  }
}

onMounted(loadTaskList)
onUnmounted(closeSSE)

function onTrainConfirm() {
  loadTaskList()
}
</script>

<style lang="less" scoped>
.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
}
.task-list li {
  margin-bottom: 16px;
}
.train-card {
  width: 100%;
}
.progress-block {
  margin-top: 12px;
}
.progress-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.progress-flex {
  flex: 1;
}
.progress-speed {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.stat-block {
  .stat-title {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 4px;
  }
  .stat-value {
    font-weight: bold;
    font-size: 24px;
  }
}
.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}
.text-small {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.mt-2 {
  margin-top: 4px;
}
.card-footer {
  padding: 14px 20px 16px;
  margin: 0 -20px -20px;
  border-radius: 0 0 var(--el-card-border-radius) var(--el-card-border-radius);
}
.footer-section-title {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  display: block;
}
.footer-metrics {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
  width: 100%;
  gap: 8px;
}
.footer-metrics .metric-wrap {
  flex: 1;
  display: flex;
  justify-content: center;
}
.footer-metrics .metric-tag {
  font-variant-numeric: tabular-nums;
}
.loading-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 24px;
}
.mb-4 {
  margin-bottom: 16px;
}
</style>
