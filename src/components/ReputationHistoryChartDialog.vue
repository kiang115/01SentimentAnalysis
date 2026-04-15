<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="820px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @closed="onClosed"
  >
    <div class="history-dialog" v-loading="loading">
      <div class="metric-switch">
        <el-radio-group v-model="selectedMetric" size="small">
          <el-radio-button
            v-for="option in metricOptions"
            :key="option.key"
            :label="option.key"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="hasHistoryData" ref="chartRef" class="chart-container"></div>
      <div v-else class="chart-empty">暂无历史数据</div>
    </div>

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import type { EChartsOption } from 'echarts'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { modelApi } from '@/api/model-api'
import type { ReputationHistoryLineChartRec } from '@/Dto/ReceiveDto/ReputationHistoryLineChartRec'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

type HistoryMetricKey = 'rating' | 'positiveRate' | 'commentCount' | 'ranking'

const props = defineProps<{
  modelValue: boolean
  targetId: number | null
  type: 0 | 1
  defaultMetric: HistoryMetricKey
  dialogTitle: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
}>()

const emptyChartData: ReputationHistoryLineChartRec = {
  timeList: [],
  ratingList: [],
  positiveRateList: [],
  commentCountList: [],
  rankingList: [],
}

const metricOptions: Array<{ key: HistoryMetricKey; label: string }> = [
  { key: 'rating', label: '综合得分' },
  { key: 'positiveRate', label: '好评率' },
  { key: 'commentCount', label: '评论人数' },
  { key: 'ranking', label: '当前排名' },
]

const loading = ref(false)
const chartRef = ref<HTMLElement | null>(null)
const chartData = ref<ReputationHistoryLineChartRec>({ ...emptyChartData })
const selectedMetric = ref<HistoryMetricKey>(props.defaultMetric)
let chartInstance: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

const hasHistoryData = computed(() => chartData.value.timeList.length > 0)

const currentMetricLabel = computed(() => {
  return metricOptions.find(option => option.key === selectedMetric.value)?.label ?? ''
})

const currentSeriesData = computed<number[]>(() => {
  switch (selectedMetric.value) {
    case 'rating':
      return chartData.value.ratingList
    case 'positiveRate':
      return chartData.value.positiveRateList
    case 'commentCount':
      return chartData.value.commentCountList
    case 'ranking':
      return chartData.value.rankingList
  }
})

function buildChartOption(): EChartsOption {
  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const first = Array.isArray(params) ? params[0] : params
        const value = Number(first?.value ?? 0)
        let displayValue = String(value)
        if (selectedMetric.value === 'rating') {
          displayValue = value.toFixed(1)
        } else if (selectedMetric.value === 'positiveRate') {
          displayValue = (value * 100).toFixed(2) + '%'
        } else if (selectedMetric.value === 'ranking') {
          displayValue = `第${value.toLocaleString()}名`
        } else {
          displayValue = value.toLocaleString()
        }
        return `${first?.axisValue ?? ''}<br/>${currentMetricLabel.value}：${displayValue}`
      },
    },
    legend: {
      data: [currentMetricLabel.value],
      bottom: 0,
    },
    grid: { left: '4%', right: '4%', bottom: '16%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.value.timeList,
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      name: currentMetricLabel.value,
      axisLabel: {
        formatter: (value: number) => {
          if (selectedMetric.value === 'positiveRate') {
            return (value * 100).toFixed(0) + '%'
          }
          if (selectedMetric.value === 'ranking') {
            return `第${value}`
          }
          return Number.isInteger(value) ? value.toString() : value.toFixed(1)
        },
      },
    },
    series: [
      {
        name: currentMetricLabel.value,
        type: 'line',
        data: currentSeriesData.value,
        smooth: true,
        connectNulls: true,
      },
    ],
  }
}

function initChart() {
  if (!chartRef.value || !hasHistoryData.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    resizeHandler = () => chartInstance?.resize()
    window.addEventListener('resize', resizeHandler)
  }
  chartInstance.setOption(buildChartOption(), { notMerge: true })
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

async function fetchHistory() {
  if (!props.targetId) {
    chartData.value = { ...emptyChartData }
    return
  }

  loading.value = true
  try {
    const res = await modelApi.getReputationLineChart({
      targetId: props.targetId,
      type: props.type,
    })
    chartData.value = res.data
  } catch {
    chartData.value = { ...emptyChartData }
  } finally {
    loading.value = false
  }
}

watch(
  () => props.defaultMetric,
  (metric) => {
    selectedMetric.value = metric
  },
)

watch(
  () => [props.modelValue, props.targetId, props.type] as const,
  async ([visible, targetId]) => {
    if (visible && targetId) {
      await fetchHistory()
      await nextTick()
      initChart()
    } else {
      chartData.value = { ...emptyChartData }
      disposeChart()
    }
  },
  { immediate: true },
)

watch(
  () => [selectedMetric.value, hasHistoryData.value] as const,
  async ([, hasData]) => {
    if (!props.modelValue) return
    if (!hasData) {
      disposeChart()
      return
    }
    await nextTick()
    initChart()
  },
)

onBeforeUnmount(disposeChart)

function close() {
  emit('update:modelValue', false)
}

function onClosed() {
  disposeChart()
}
</script>

<style scoped lang="less">
.history-dialog {
  min-height: 360px;
}

.metric-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 420px;
}

.chart-empty {
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style>
