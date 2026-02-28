<template>
  <div class="task-info-charts">
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="chart-card">
          <el-select v-model="trainDomain" placeholder="选择领域" size="small" class="chart-select">
            <el-option
              v-for="name in trainLineData?.domainNameList ?? []"
              :key="name"
              :label="name"
              :value="name"
            />
          </el-select>
          <div ref="trainLineRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <el-select v-model="pieIndex" placeholder="选择饼图" size="small" class="chart-select">
            <el-option
              v-for="(name, i) in inferPieData?.pieNameList ?? []"
              :key="name"
              :label="name"
              :value="i"
            />
          </el-select>
          <div ref="inferPieRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">{{ tasksHotTitle }}</div>
          <div ref="tasksHotRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { modelApi } from '@/api/model-api'
import type { TrainLineChart, DomainLinesData } from '@/Dto/ReceiveDto/TrainLineChart'
import type { InferPieChart } from '@/Dto/ReceiveDto/InferPieChart'
import type { TasksHotChart, DailyData } from '@/Dto/ReceiveDto/TasksHotChart'
import * as echarts from 'echarts/core'
import type { EChartsOption } from 'echarts'
import { LineChart, PieChart, HeatmapChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  CalendarComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart,
  PieChart,
  HeatmapChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  VisualMapComponent,
  CalendarComponent,
  CanvasRenderer
])

const trainLineRef = ref<HTMLElement | null>(null)
const inferPieRef = ref<HTMLElement | null>(null)
const tasksHotRef = ref<HTMLElement | null>(null)

const trainLineData = ref<TrainLineChart | null>(null)
const inferPieData = ref<InferPieChart | null>(null)
const tasksHotData = ref<TasksHotChart | null>(null)

const trainDomain = ref('')
const pieIndex = ref(0)

let trainLineChart: echarts.ECharts | null = null
let inferPieChart: echarts.ECharts | null = null
let tasksHotChart: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

const tasksHotTitle = computed(() => {
  const m = tasksHotData.value?.month
  return m != null ? `${m}月 任务执行热力图` : '任务执行热力图'
})

function getCurrentDomainLines(): DomainLinesData | undefined {
  if (!trainLineData.value || !trainDomain.value) return undefined
  return trainLineData.value.domainLinesDataList.find((d) => d.domainName === trainDomain.value)
}

function buildTrainLineOption(): EChartsOption {
  const domainLines = getCurrentDomainLines()
  if (!domainLines) return {}

  const xData = domainLines.allSmallVersions
  const series = domainLines.majorVersionDataList.map((major) => ({
    name: major.majorVersion,
    type: 'line' as const,
    data: major.versionAccuracyList,
    connectNulls: true
  }))

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: domainLines.allMajorVersions, bottom: 0 },
    grid: { left: '10%', right: '6%', top: '8%', bottom: '22%', containLabel: true },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45, fontSize: 10 } },
    yAxis: { type: 'value', name: '准确率', axisLabel: { fontSize: 10 } },
    series
  }
}

function buildInferPieOption(): EChartsOption {
  const data = inferPieData.value
  if (!data) return {}
  const name = data.pieNameList[pieIndex.value] ?? ''
  const pieData = pieIndex.value === 0 ? data.inferredPie : data.unInferredPie
  return {
    title: { text: name, left: 'center', top: 8 },
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '55%'],
        data: pieData.map((p) => ({ name: p.domainName, value: p.commentNum }))
      }
    ]
  }
}

function buildTasksHotOption(): EChartsOption {
  const data = tasksHotData.value
  if (!data) return {}

  const yearMonth = `${data.year}-${data.month.padStart(2, '0')}`
  const calendarData: [string, number][] = data.dailyDataList.map((d) => [d.date, d.totalCount])
  const dayMap = new Map<string, DailyData>()
  data.dailyDataList.forEach((d) => dayMap.set(d.date, d))

  return {
    tooltip: {
      formatter: (params: unknown) => {
        const p = params as { data: [string, number] }
        const dateStr = Array.isArray(p.data) ? p.data[0] : ''
        const day = dayMap.get(dateStr)
        if (!day) return ''
        return `${day.date}<br/>总任务次数: ${day.totalCount}<br/>推理任务次数: ${day.inferCount}<br/>训练任务次数: ${day.trainCount}`
      }
    },
    visualMap: {
      show: false,
      min: 0,
      max: data.maxTotalCount,
      inRange: { color: ['#f0f9ff', '#0ea5e9', '#0369a1'] }
    },
    calendar: {
      top: 8,
      left: 20,
      right: 20,
      bottom: 8,
      cellSize: ['auto', 14],
      range: yearMonth,
      itemStyle: { borderWidth: 0.5 },
      yearLabel: { show: false },
      monthLabel: { fontSize: 12, fontWeight: 'bold' },
      dayLabel: {
        firstDay: 1,
        nameMap: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      }
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: calendarData
      }
    ]
  }
}

function initTrainLine() {
  if (!trainLineRef.value) return
  trainLineChart = echarts.init(trainLineRef.value)
  trainLineChart.setOption(buildTrainLineOption())
  nextTick(() => trainLineChart?.resize())
}

function initInferPie() {
  if (!inferPieRef.value) return
  inferPieChart = echarts.init(inferPieRef.value)
  inferPieChart.setOption(buildInferPieOption())
  nextTick(() => inferPieChart?.resize())
}

function initTasksHot() {
  if (!tasksHotRef.value) return
  tasksHotChart = echarts.init(tasksHotRef.value)
  tasksHotChart.setOption(buildTasksHotOption())
  nextTick(() => tasksHotChart?.resize())
}

function disposeAll() {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  if (trainLineChart) {
    trainLineChart.dispose()
    trainLineChart = null
  }
  if (inferPieChart) {
    inferPieChart.dispose()
    inferPieChart = null
  }
  if (tasksHotChart) {
    tasksHotChart.dispose()
    tasksHotChart = null
  }
}

watch(
  () => [trainLineData.value, trainDomain.value] as const,
  () => {
    if (trainLineChart && getCurrentDomainLines()) {
      trainLineChart.setOption(buildTrainLineOption())
    }
  }
)

watch(
  () => [inferPieData.value, pieIndex.value] as const,
  () => {
    if (inferPieChart && inferPieData.value) {
      inferPieChart.setOption(buildInferPieOption())
    }
  }
)

watch(
  () => tasksHotData.value,
  () => {
    if (tasksHotChart && tasksHotData.value) {
      tasksHotChart.setOption(buildTasksHotOption())
    }
  }
)

onMounted(() => {
  modelApi.listTrainLineChart().then((res) => {
    trainLineData.value = res.data
    if (!trainDomain.value && res.data.domainNameList.length) {
      trainDomain.value = res.data.domainNameList[0] ?? ''
    }
    nextTick(() => {
      if (getCurrentDomainLines()) initTrainLine()
    })
  })
  modelApi.listInferPieChart().then((res) => {
    inferPieData.value = res.data
    nextTick(() => initInferPie())
  })
  modelApi.listTasksHotChart().then((res) => {
    tasksHotData.value = res.data
    nextTick(() => initTasksHot())
  })
  resizeHandler = () => {
    trainLineChart?.resize()
    inferPieChart?.resize()
    tasksHotChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(disposeAll)
</script>

<style lang="less" scoped>
.task-info-charts {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}
.task-info-charts :deep(.el-col) {
  min-width: 0;
}
.chart-card {
  padding: 10px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  min-width: 0;
  overflow: hidden;
}
.chart-select {
  width: 100%;
  margin-bottom: 6px;
}
.chart-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
}
.chart-container {
  width: 100%;
  height: 200px;
  min-width: 0;
  overflow: hidden;
}
</style>
