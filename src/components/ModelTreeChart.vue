<template>
  <div class="model-tree-chart">
    <div class="tree-chart-header">
      <span class="section-title">模型树状图</span>
      <el-select
        v-model="selectedDomain"
        placeholder="选择领域"
        style="width: 160px"
        :disabled="!treeChartData?.domainNameList?.length"
      >
        <el-option
          v-for="name in treeChartData?.domainNameList ?? []"
          :key="name"
          :label="name"
          :value="name"
        />
      </el-select>
    </div>
    <div v-if="currentTree" ref="chartRef" class="chart-container"></div>
    <div v-else class="chart-empty">暂无数据</div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import type { EChartsOption } from 'echarts'
import * as echarts from 'echarts/core'
import { TreeChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { modelApi } from '@/api/model-api'
import type { ModelTreeChartRec, ModelTreeNodeRec } from '@/Dto/ReceiveDto/ModelTreeChartRec'

echarts.use([TreeChart, TooltipComponent, CanvasRenderer])

const treeChartData = ref<ModelTreeChartRec | null>(null)
const selectedDomain = ref('')
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

const selectedIndex = computed(() => {
  const list = treeChartData.value?.domainNameList
  const name = selectedDomain.value
  if (!list?.length || !name) return -1
  const idx = list.indexOf(name)
  return idx >= 0 ? idx : 0
})

const currentTree = computed((): ModelTreeNodeRec | null => {
  const data = treeChartData.value
  const idx = selectedIndex.value
  if (!data?.modelTreeNodeList?.length || idx < 0) return null
  return data.modelTreeNodeList[idx] ?? null
})

/** 递归为树节点挂载 itemStyle（根据 active），不修改 name/value/children */
function applyStyle(node: ModelTreeNodeRec & { itemStyle?: object }) {
  if (node.active) {
    node.itemStyle = {
      color: '#5470c6',
      borderColor: '#2f4554',
      borderWidth: 1,
    }
  } else {
    node.itemStyle = {
      color: '#ccc',
      opacity: 0.6,
    }
  }
  if (node.children && node.children.length > 0) {
    node.children.forEach((child) => applyStyle(child as ModelTreeNodeRec & { itemStyle?: object }))
  }
}

/** 深拷贝当前树并应用样式，供 ECharts 使用 */
function getTreeDataForChart(): (ModelTreeNodeRec & { itemStyle?: object }) | null {
  const tree = currentTree.value
  if (!tree) return null
  const copy = JSON.parse(JSON.stringify(tree)) as ModelTreeNodeRec & { itemStyle?: object }
  applyStyle(copy)
  return copy
}

function buildTreeOption(): EChartsOption {
  const treeData = getTreeDataForChart()
  if (!treeData) return {}

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: unknown) => {
        const p = params as { name?: string; value?: number | null }
        const name = p.name ?? '—'
        const val = p.value
        const accuracyStr = val != null ? Number(val).toFixed(2) + '%' : '根节点'
        return `版本: ${name}<br/>精度: ${accuracyStr}`
      },
    },
    series: [
      {
        type: 'tree',
        data: [treeData as unknown as Record<string, unknown>],
        top: '5%',
        left: '15%',
        bottom: '5%',
        right: '20%',
        symbolSize: (val: unknown) => {
          const v = Array.isArray(val) ? val[0] : val
          const num = v != null && typeof v === 'number' ? v : 0
          return 10 + (num / 100) * 30
        },
        label: {
          position: 'left',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 11,
        },
        leaves: {
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left',
          },
        },
        emphasis: {
          focus: 'descendant',
        },
        expandAndCollapse: true,
        animationDuration: 550,
      },
    ],
  }
}

function initChart() {
  if (!chartRef.value || !currentTree.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    resizeHandler = () => chartInstance?.resize()
    window.addEventListener('resize', resizeHandler)
  }
  chartInstance.setOption(buildTreeOption(), { notMerge: true })
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
  [selectedDomain, currentTree],
  () => {
    if (currentTree.value) {
      nextTick(() => initChart())
    } else {
      disposeChart()
    }
  },
  { immediate: true },
)

onMounted(async () => {
  try {
    const res = await modelApi.listModelTreeChart()
    const data = res?.data ?? null
    treeChartData.value = data
    const first = data?.domainNameList?.[0]
    if (first) selectedDomain.value = first
    nextTick(() => initChart())
  } catch {
    treeChartData.value = null
  }
})

onBeforeUnmount(() => {
  disposeChart()
})
</script>

<style lang="less" scoped>
.model-tree-chart {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  border: 1px solid var(--el-border-color-lighter);
}
.tree-chart-header {
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
.chart-container {
  width: 100%;
  height: 400px;
}
.chart-empty {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style>
