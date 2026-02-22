<template>
  <el-dialog
    :model-value="modelValue"
    :title="`模型准确率详情 - v${task?.modelVersion ?? '—'}`"
    width="720px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @closed="onClosed"
  >
    <template v-if="task">
      <div class="detail-summary">
        <div class="summary-card">
          <div class="summary-value">{{ task.epochs }}</div>
          <div class="summary-label">训练轮数</div>
        </div>
        <div class="summary-card">
          <div class="summary-value">{{ formatDuration(task.duration) }}</div>
          <div class="summary-label">训练持续时间</div>
        </div>
        <div class="summary-card">
          <div class="summary-value">{{ task.totalData }}</div>
          <div class="summary-label">训练总数据量</div>
          <div class="summary-sublabel">{{ trainValRatioText }}</div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">模型性能指标 (v{{ task.modelVersion }})</div>
        <div class="metrics-row">
          <div class="metric-card">
            <div class="metric-value">{{ formatMetric(task.accuracy) }}</div>
            <div class="metric-label">准确度</div>
          </div>
          <div class="metric-card">
            <div class="metric-value">{{ formatMetric(task.precisionRate) }}</div>
            <div class="metric-label">精确率</div>
          </div>
          <div class="metric-card">
            <div class="metric-value">{{ formatMetric(task.f1Score) }}</div>
            <div class="metric-label">F1 分数</div>
          </div>
          <div class="metric-card">
            <div class="metric-value">{{ formatMetric(task.recallRate) }}</div>
            <div class="metric-label">召回率</div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">训练过程指标</div>
        <div class="section-subtitle">训练过程指标变化</div>
        <div ref="chartRef" class="chart-container"></div>
      </div>

      <div class="detail-section">
        <el-descriptions title="训练配置参数详情" :column="3" border>
          <el-descriptions-item label="批次大小">{{ formatOptionalNumber(task.batchSize) }}</el-descriptions-item>
          <el-descriptions-item label="总轮数">{{ formatOptionalNumber(task.epochs) }}</el-descriptions-item>
          <el-descriptions-item label="学习率">{{ formatOptionalNumber(task.learningRate) }}</el-descriptions-item>
          <el-descriptions-item label="随机种子">{{ formatOptionalNumber(task.randomSeed) }}</el-descriptions-item>
          <el-descriptions-item label="LORA_R">{{ formatOptionalNumber(task.loraR) }}</el-descriptions-item>
          <el-descriptions-item label="LORA_ALPHA">{{ formatOptionalNumber(task.loraAlpha) }}</el-descriptions-item>
          <el-descriptions-item label="LORA模块列表" :span="2">{{ loraModulesText }}</el-descriptions-item>
          <el-descriptions-item label="训练集比例">{{ trainSplitRatioDisplay }}</el-descriptions-item>
          <el-descriptions-item label="是否重新训练">{{ ifOverTrainText }}</el-descriptions-item>
          <el-descriptions-item label="数据来源比例" :span="2">{{ dataSourceRatioText }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </template>

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { computed, watch, onBeforeUnmount, ref, nextTick } from 'vue'
import type { TrainTasksItem } from '@/Dto/ReceiveDto/TrainTasksRec'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<{
  modelValue: boolean
  task: TrainTasksItem | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
}>()

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

function formatDuration(sec: number | undefined): string {
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

function formatMetric(v: number | undefined): string {
  if (v == null || !Number.isFinite(v)) return '—'
  return Number(v).toFixed(4)
}

function formatOptionalNumber(v: number | undefined): string {
  if (v == null || !Number.isFinite(v)) return '—'
  return String(v)
}

const trainValRatioText = computed(() => {
  const t = props.task
  if (!t) return ''
  const r = t.trainSplitRatio
  const train = (r * 100).toFixed(0)
  const val = ((1 - r) * 100).toFixed(0)
  return `训练集:验证集 ${train}% : ${val}%`
})

const loraModulesText = computed(() => {
  const arr = props.task?.loraModules
  if (!arr?.length) return '—'
  return arr.join(', ')
})

const trainSplitRatioDisplay = computed(() => {
  const r = props.task?.trainSplitRatio
  if (r == null || !Number.isFinite(r)) return '—'
  return (r * 100).toFixed(0) + '%'
})

const ifOverTrainText = computed(() => {
  return props.task?.ifOverTrain === 1 ? '是' : '否'
})

const dataSourceRatioText = computed(() => {
  const t = props.task
  if (!t) return '—'
  const a = t.originalNum ?? 0
  const b = t.correctedNum ?? 0
  const c = t.uploadNum ?? 0
  return `模型初始数据集 ${a} : 人工修正数据集 ${b} : 手动上传数据 ${c}`
})

function buildChartOption(): echarts.ComposeOption<echarts.LineSeriesOption> {
  const t = props.task
  if (!t) return {}
  const trainAcc = t.trainAccList ?? []
  const trainLoss = t.trainLossList ?? []
  const valAcc = t.valAccList ?? []
  const valLoss = t.valLossList ?? []
  const len = Math.max(trainAcc.length, trainLoss.length, valAcc.length, valLoss.length)
  const xData = Array.from({ length: len }, (_, i) => i)

  return {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['训练准确率', '验证准确率', '训练损失', '验证损失'],
      bottom: 0,
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: { type: 'category', name: '轮次', data: xData, boundaryGap: false },
    yAxis: [
      {
        type: 'value',
        name: '准确率',
        min: 0,
        max: 1,
        axisLabel: { formatter: (value: number) => Math.round(value * 100) + '%' },
      },
      { type: 'value', name: '损失值' },
    ],
    series: [
      { name: '训练准确率', type: 'line', data: trainAcc, yAxisIndex: 0, smooth: true },
      { name: '验证准确率', type: 'line', data: valAcc, yAxisIndex: 0, smooth: true },
      { name: '训练损失', type: 'line', data: trainLoss, yAxisIndex: 1, smooth: true },
      { name: '验证损失', type: 'line', data: valLoss, yAxisIndex: 1, smooth: true },
    ],
  }
}

function initChart() {
  if (!chartRef.value || !props.task) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(buildChartOption())
  resizeHandler = () => chartInstance?.resize()
  window.addEventListener('resize', resizeHandler)
}

function disposeChart() {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

watch(
  () => [props.modelValue, props.task] as const,
  ([visible, task]) => {
    if (visible && task) {
      nextTick(() => initChart())
    } else {
      disposeChart()
    }
  },
  { immediate: true },
)

onBeforeUnmount(disposeChart)

function close() {
  emit('update:modelValue', false)
}

function onClosed() {
  disposeChart()
}
</script>

<style lang="less" scoped>
.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.summary-card {
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  text-align: center;
}
.summary-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}
.summary-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 6px;
}
.summary-sublabel {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.detail-section {
  margin-bottom: 20px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}
.section-subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}
.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.metric-card {
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  text-align: center;
}
.metric-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.metric-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.chart-container {
  width: 100%;
  height: 280px;
}
</style>
