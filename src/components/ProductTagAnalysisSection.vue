<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'
import type { ProductTagAnalyzeRec } from '@/Dto/ReceiveDto/ProductTagAnalyzeRec'
import type { ProductTagStats } from '@/Dto/ReceiveDto/ProductTagStatsListRec'

const props = defineProps<{
  productId: number
  canAnalyzeTags: boolean
}>()

const statsLoading = ref(false)
const analyzeLoading = ref(false)
const showAllTags = ref(false)
const tagStatsList = ref<ProductTagStats[]>([])

const sortedTagStatsList = computed(() => {
  return [...tagStatsList.value].sort((left, right) => right.totalCount - left.totalCount)
})

const visibleTagStatsList = computed(() => {
  if (showAllTags.value) return sortedTagStatsList.value
  return sortedTagStatsList.value.slice(0, 5)
})

const hasMoreTags = computed(() => sortedTagStatsList.value.length > 5)

function formatAnalyzeSuccessMessage(result: ProductTagAnalyzeRec) {
  return `商品 ${result.productId} 评论标签分析完成，已处理 ${result.processedCommentCount} 条评论，触发现有标签 ${result.touchedTagCount} 个，新增标签 ${result.newTagCount} 个`
}

async function fetchTagStats() {
  statsLoading.value = true
  try {
    const res = await modelApi.listProductTagStats(props.productId)
    tagStatsList.value = res.data.productTagStatsList
    showAllTags.value = false
  } catch {
    tagStatsList.value = []
    showAllTags.value = false
  } finally {
    statsLoading.value = false
  }
}

async function analyzeProductTags() {
  if (!props.canAnalyzeTags) return
  if (analyzeLoading.value) return

  analyzeLoading.value = true
  const loadingInstance = ElLoading.service({
    lock: true,
    fullscreen: true,
    text: '正在分析评论标签属性，请稍候...',
    background: 'rgba(15, 23, 42, 0.45)',
  })

  try {
    const res = await modelApi.analyzeProductTags(props.productId)
    ElMessage.success(formatAnalyzeSuccessMessage(res.data))
    setTimeout(() => window.location.reload(), 1000)
  } finally {
    analyzeLoading.value = false
    loadingInstance.close()
  }
}

function toggleTagDisplay() {
  showAllTags.value = !showAllTags.value
}

watch(
  () => props.productId,
  () => {
    void fetchTagStats()
  },
  { immediate: true },
)
</script>

<template>
  <section class="tag-analysis-section">
    <div class="section-header">
      <h2 class="section-title">评论属性分析</h2>
      <el-button
        v-if="props.canAnalyzeTags"
        type="primary"
        :loading="analyzeLoading"
        :disabled="analyzeLoading"
        @click="analyzeProductTags"
      >
        分析评论属性
      </el-button>
    </div>

    <section class="tag-analysis-card" v-loading="statsLoading">
      <el-empty v-if="!statsLoading && tagStatsList.length === 0" description="暂无标签分析数据" />

      <div v-else class="tag-pill-list">
        <div v-for="item in visibleTagStatsList" :key="item.tagId" class="tag-pill">
          <span class="tag-name">{{ item.tagName }}</span>
          <span class="tag-count positive">{{ item.positiveCount }}</span>
          <span class="tag-divider">/</span>
          <span class="tag-count negative">{{ item.negativeCount }}</span>
        </div>

        <el-button
          v-if="hasMoreTags"
          class="toggle-button"
          circle
          plain
          @click="toggleTagDisplay"
        >
          <el-icon><ArrowUp v-if="showAllTags" /><ArrowDown v-else /></el-icon>
        </el-button>
      </div>
    </section>
  </section>
</template>

<style scoped lang="less">
.tag-analysis-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
}

.tag-analysis-card {
  border-radius: 12px;
  background: #fff;
  padding: 16px;

  :deep(.el-empty) {
    padding: 8px 0;

    .el-empty__image {
      display: none;
    }
  }
}

.tag-pill-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.tag-name {
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.tag-count {
  font-size: 18px;
  font-weight: 700;
}

.tag-count.positive {
  color: #16a34a;
}

.tag-count.negative {
  color: #dc2626;
}

.tag-divider {
  color: #94a3b8;
  font-weight: 600;
}

.toggle-button {
  width: 48px;
  height: 48px;
  border-color: #e5e7eb;
  color: #64748b;
}

@media (max-width: 768px) {
  .section-header {
    align-items: stretch;
    flex-direction: column;
  }

  .tag-pill-list {
    gap: 10px;
  }

  .tag-pill {
    min-height: 44px;
    padding: 0 14px;
  }

  .tag-name {
    font-size: 15px;
  }

  .tag-count {
    font-size: 17px;
  }
}
</style>
