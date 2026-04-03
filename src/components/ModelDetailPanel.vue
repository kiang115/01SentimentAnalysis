<template>
  <div class="model-detail-panel">
    <div class="panel-body">
      <div class="panel-left">
        <div class="line-chart-header">
          <span class="section-title">模型准确率折线图</span>
          <el-select
            v-model="selectedDomain"
            placeholder="选择领域"
            style="width: 160px"
            :disabled="!chartData?.domainNameList?.length"
          >
            <el-option
              v-for="name in chartData?.domainNameList ?? []"
              :key="name"
              :label="name"
              :value="name"
            />
          </el-select>
        </div>
        <div v-if="currentDomainData" ref="chartRef" class="chart-container"></div>
        <div v-else class="chart-empty">暂无数据</div>
      </div>
      <div class="panel-right">
        <div class="pie-section">
          <div class="pie-title">评论状态分布饼图</div>
          <div v-if="pieData" ref="pieChartRef" class="pie-container"></div>
          <div v-else class="pie-empty">暂无数据</div>
        </div>
        <div class="progress-title-wrap">
          <span class="progress-section-title">各领域实际准确率</span>
        </div>
        <div class="progress-section">
          <div
            v-for="(name, i) in pieData?.domainNameList ?? []"
            :key="name"
            class="progress-row"
          >
            <div class="progress-bar-line">
              <span class="progress-label">{{ name }}领域</span>
              <el-progress
                :percentage="pieData!.domainAccuracyList[i] ?? 0"
                :stroke-width="10"
                :format="(p: number) => (p ?? 0).toFixed(2) + '%'"
                class="progress-bar"
              />
            </div>
            <span class="progress-ratio">
              {{ (pieData!.rightNumList[i] ?? 0) }} / {{ (pieData!.inferredNumList[i] ?? 0) }}
            </span>
          </div>
          <div v-if="!pieData?.domainNameList?.length" class="progress-empty">暂无领域数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, PieChart, type LineSeriesOption } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { modelApi } from '@/api/model-api'
import type { ModelLineChart, DomainLinesData } from '@/Dto/ReceiveDto/ModelLineChart'
import type { ModelPieChart } from '@/Dto/ReceiveDto/ModelPieChart'

echarts.use([LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const chartData = ref<ModelLineChart | null>(null)
const pieData = ref<ModelPieChart | null>(null)
const selectedDomain = ref('')
const chartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let pieChartInstance: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

const currentDomainData = computed((): DomainLinesData | null => {
  const data = chartData.value
  const name = selectedDomain.value
  if (!data?.domainLinesDataList?.length || !name) return null
  return data.domainLinesDataList.find((d) => d.domainName === name) ?? null
})

function buildChartOption(): echarts.ComposeOption<LineSeriesOption> {
  const domain = currentDomainData.value
  if (!domain?.allSmallVersions?.length) return {}

  const series = domain.majorVersionDataList.map((item) => ({
    name: item.majorVersion,
    type: 'line' as const,
    data: item.versionAccuracyList,
    connectNulls: true,
    smooth: true,
  }))

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: domain.allMajorVersions, bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      name: '小版本',
      nameLocation: 'middle',
      nameGap: 28,
      data: domain.allSmallVersions,
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      name: '准确率',
      min: 0,
      max: 100,
      axisLabel: { formatter: (value: number) => Math.round(value) + '%' },
    },
    series,
  }
}

function initChart() {
  if (!chartRef.value || !currentDomainData.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    if (!resizeHandler) {
      resizeHandler = () => {
        chartInstance?.resize()
        pieChartInstance?.resize()
      }
      window.addEventListener('resize', resizeHandler)
    }
  }
  chartInstance.setOption(buildChartOption(), { notMerge: true })
}

function buildPieOption() {
  const data = pieData.value
  if (!data?.statusNameList?.length) return {}
  const pieSeriesData = data.statusNameList.map((name, i) => ({
    name,
    value: data.statusCountList[i] ?? 0,
  }))
  return {
    tooltip: { trigger: 'item' },
    legend: { show: false },
    series: [{ type: 'pie', radius: '60%', data: pieSeriesData }],
  }
}

function initPieChart() {
  if (!pieChartRef.value || !pieData.value) return
  if (!pieChartInstance) {
    pieChartInstance = echarts.init(pieChartRef.value)
    if (!resizeHandler) {
      resizeHandler = () => {
        chartInstance?.resize()
        pieChartInstance?.resize()
      }
      window.addEventListener('resize', resizeHandler)
    }
  }
  pieChartInstance.setOption(buildPieOption())
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

function disposePieChart() {
  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }
}

watch(
  [selectedDomain, currentDomainData],
  () => {
    if (currentDomainData.value) {
      nextTick(() => initChart())
    } else {
      disposeChart()
    }
  },
  { immediate: true },
)

watch(
  pieData,
  (data) => {
    if (data) {
      nextTick(() => initPieChart())
    } else {
      disposePieChart()
    }
  },
  { immediate: true },
)

onMounted(async () => {
  try {
    const [lineRes, pieRes] = await Promise.all([
      modelApi.listModelLineChart(),
      modelApi.listModelPieChart(),
    ])
    const data = lineRes?.data
    chartData.value = data ?? null
    const first = data?.domainNameList?.[0]
    if (first) selectedDomain.value = first
    pieData.value = pieRes?.data ?? null
    nextTick(() => {
      initChart()
      initPieChart()
    })
  } catch {
    chartData.value = null
    pieData.value = null
  }
})

onBeforeUnmount(() => {
  disposeChart()
  disposePieChart()
})
</script>

<style lang="less" scoped>
.model-detail-panel {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  border: 1px solid var(--el-border-color-lighter);
}
.line-chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.panel-body {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 24px;
  min-height: 0;
  align-items: stretch;
}
.panel-left {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 16px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  overflow: hidden;
}
.panel-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  padding: 16px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  overflow: hidden;
}
.chart-container {
  width: 100%;
  height: 320px;
}
.chart-empty {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.pie-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.pie-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}
.pie-container {
  flex: 1;
  min-height: 120px;
  width: 100%;
}
.pie-empty {
  flex: 1;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.progress-title-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 4px 0 0;
}
.progress-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.progress-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.progress-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.progress-bar-line {
  display: flex;
  align-items: center;
  gap: 12px;
}
.progress-label {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--el-text-color-primary);
  min-width: 72px;
}
.progress-bar {
  flex: 1;
  min-width: 0;
}
.progress-bar :deep(.el-progress-bar__outer) {
  background-color: var(--el-fill-color-light);
}
.progress-bar :deep(.el-progress-bar__inner) {
  background-color: var(--el-color-primary);
}
.progress-bar :deep(.el-progress__text) {
  font-size: 12px !important;
}
.progress-ratio {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  padding-left: 84px;
}
.progress-empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 1100px) {
  .panel-body {
    grid-template-columns: 1fr;
  }
}
</style>
