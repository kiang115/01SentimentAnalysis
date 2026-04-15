<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { modelApi } from '@/api/model-api'
import type { AllCommentDataItem, CommentStatusItem, DomainItem } from '@/Dto/ReceiveDto/AllCommentDataListRec'
import type { AllCommentDataListQuerySend } from '@/Dto/SendDto/AllCommentDataListQuerySend'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'
import { useRouter } from 'vue-router'

const loading = ref(false)
const tableData = ref<AllCommentDataItem[]>([])
const pageInfo = ref<PageInfo<AllCommentDataItem> | null>(null)
const domains = ref<DomainItem[]>([])
const commentStatusList = ref<CommentStatusItem[]>([])
const searchContent = ref('')
const actionLoading = ref<{
  commentId: number
  action: 'reject' | 'correct'
} | null>(null)
const router = useRouter()

const queryParams = reactive<AllCommentDataListQuerySend>({
  pageNum: 1,
  pageSize: null,
  timeOrder: '',
  statusId: 3,
  domainId: null,
  content: '',
  label: null,
})

const labelFilters = [
  { text: '好评', value: 1 },
  { text: '差评', value: 0 },
]

const domainFilters = computed(() =>
  domains.value.map(item => ({ text: item.domainName, value: item.domainId }))
)

const statusFilters = computed(() =>
  commentStatusList.value.map(item => ({ text: item.statusName, value: item.statusCode }))
)

const statusNameMap = computed<Record<number, string>>(() =>
  commentStatusList.value.reduce((acc, item) => {
    acc[item.statusCode] = item.statusName
    return acc
  }, {} as Record<number, string>)
)

const serverFilter = () => true

function formatPercent(value: number | null) {
  if (value == null) {
    return '--'
  }
  return `${(value * 100).toFixed(2)}%`
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ') : '--'
}

function formatInspected(value: boolean | null) {
  if (value == null) {
    return '--'
  }
  return value ? '已分析' : '未分析'
}

function goToProduct(productId: number | null) {
  if (productId == null) {
    return
  }
  router.push(`/product/${productId}`)
}

function goToMerchant(merchantId: number | null) {
  if (merchantId == null) {
    return
  }
  router.push(`/merchant/${merchantId}`)
}

function canOperate(row: AllCommentDataItem) {
  return row.statusId === 3
}

function showStatusDisabledButton(row: AllCommentDataItem) {
  return row.statusId === 4 || row.statusId === 5
}

function getStatusButtonText(row: AllCommentDataItem) {
  if (row.statusId == null) {
    return '--'
  }
  return statusNameMap.value[row.statusId] || '--'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await modelApi.listAllCommentDataList(queryParams)
    pageInfo.value = res.data.pageInfo
    tableData.value = res.data.pageInfo.list
    domains.value = res.data.domains
    commentStatusList.value = res.data.commentStatusList
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.content = searchContent.value
  queryParams.pageNum = 1
  fetchData()
}

function handleCurrentChange(newPage: number) {
  queryParams.pageNum = newPage
  fetchData()
}

function handleSortChange({ prop, order }: { prop: string; order: string | null }) {
  if (prop !== 'publishTime' || !order) {
    queryParams.timeOrder = ''
  } else {
    queryParams.timeOrder = order === 'ascending' ? 'asc' : 'desc'
  }
  queryParams.pageNum = 1
  fetchData()
}

function handleFilterChange(filters: Record<string, Array<string | number>>) {
  for (const [key, values] of Object.entries(filters)) {
    const value = values.length > 0 ? values[0] : null
    switch (key) {
      case 'domainId':
        queryParams.domainId = typeof value === 'number' ? value : null
        break
      case 'statusId':
        queryParams.statusId = typeof value === 'number' ? value as 0 | 1 | 2 | 3 | 4 | 5 : null
        break
      case 'label':
        queryParams.label = typeof value === 'number' ? value as 0 | 1 : null
        break
    }
  }
  queryParams.pageNum = 1
  fetchData()
}

async function handleReject(commentId: number) {
  actionLoading.value = { commentId, action: 'reject' }
  try {
    await modelApi.rejectComment(commentId)
    ElMessage.success('拒绝成功')
    await fetchData()
  } finally {
    actionLoading.value = null
  }
}

async function handleCorrect(commentId: number) {
  actionLoading.value = { commentId, action: 'correct' }
  try {
    await modelApi.correctComment(commentId)
    ElMessage.success('修正成功')
    await fetchData()
  } finally {
    actionLoading.value = null
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="all-comment-data-table">
    <div class="table-toolbar">
      <el-input
        v-model="searchContent"
        placeholder="搜索评论内容..."
        clearable
        style="width: 320px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <el-table
      :data="tableData"
      v-loading="loading"
      row-key="commentId"
      stripe
      border
      style="width: 100%"
      @filter-change="handleFilterChange"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="commentId" label="评论ID" width="100" align="center" />
      <el-table-column
        prop="content"
        label="评论内容"
        min-width="320"
        show-overflow-tooltip
        :formatter="(row) => row.content || '--'"
      />
      <el-table-column
        prop="statusName"
        label="评论状态"
        width="120"
        align="center"
        column-key="statusId"
        :filters="statusFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      >
        <template #default="{ row }">
          {{ row.statusName || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="inferResultName" label="推理结果" width="110" align="center">
        <template #default="{ row }">
          {{ row.inferResultName || '--' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="finalSentimentName"
        label="最终情感"
        width="120"
        align="center"
        column-key="label"
        :filters="labelFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      >
        <template #default="{ row }">
          {{ row.finalSentimentName || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="positiveProb" label="正向概率" width="120" align="center">
        <template #default="{ row }">
          {{ formatPercent(row.positiveProb) }}
        </template>
      </el-table-column>
      <el-table-column prop="negativeProb" label="负向概率" width="120" align="center">
        <template #default="{ row }">
          {{ formatPercent(row.negativeProb) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="domainName"
        label="领域名称"
        width="120"
        align="center"
        column-key="domainId"
        :filters="domainFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      >
        <template #default="{ row }">
          {{ row.domainName || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="modelVersion" label="模型版本" width="120" align="center">
        <template #default="{ row }">
          {{ row.modelVersion ?? '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="190">
        <template #default="{ row }">
          <div class="name-cell">
            <el-avatar :size="30" :src="row.productImageUrl" shape="square">
              {{ row.productName?.slice(0, 1) || '商' }}
            </el-avatar>
            <el-link
              v-if="row.productId != null && row.productName"
              type="primary"
              :underline="false"
              class="name-link"
              @click="goToProduct(row.productId)"
            >
              {{ row.productName }}
            </el-link>
            <span v-else class="name-text">{{ row.productName || '--' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="merchantName" label="商家名称" min-width="180">
        <template #default="{ row }">
          <div class="name-cell">
            <el-avatar :size="30" :src="row.merchantAvatarUrl">
              {{ row.merchantName?.slice(0, 1) || '商' }}
            </el-avatar>
            <el-link
              v-if="row.merchantId != null && row.merchantName"
              type="primary"
              :underline="false"
              class="name-link"
              @click="goToMerchant(row.merchantId)"
            >
              {{ row.merchantName }}
            </el-link>
            <span v-else class="name-text">{{ row.merchantName || '--' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="customerName" label="顾客名称" min-width="160">
        <template #default="{ row }">
          <div class="name-cell">
            <el-avatar :size="30" :src="row.customerAvatarUrl">
              {{ row.customerName?.slice(0, 1) || '客' }}
            </el-avatar>
            <span class="name-text">{{ row.customerName || '--' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="isInspected" label="标签分析" width="110" align="center">
        <template #default="{ row }">
          {{ formatInspected(row.isInspected) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="publishTime"
        label="发布时间"
        width="180"
        align="center"
        sortable="custom"
      >
        <template #default="{ row }">
          {{ formatTime(row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180" align="center">
        <template #default="{ row }">
          <div v-if="canOperate(row)" class="action-buttons">
            <el-button
              type="danger"
              plain
              size="small"
              :loading="actionLoading?.commentId === row.commentId && actionLoading?.action === 'reject'"
              @click="handleReject(row.commentId)"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              plain
              size="small"
              :loading="actionLoading?.commentId === row.commentId && actionLoading?.action === 'correct'"
              @click="handleCorrect(row.commentId)"
            >
              修正
            </el-button>
          </div>
          <el-button
            v-else-if="showStatusDisabledButton(row)"
            type="info"
            plain
            size="small"
            disabled
          >
            {{ getStatusButtonText(row) }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="pageInfo?.pageNum"
        :page-size="pageInfo?.pageSize"
        :total="pageInfo?.total"
        layout="total, prev, pager, next, jumper"
        background
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
.all-comment-data-table {
  .table-toolbar {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 8px;
  }

  .name-cell {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
  }

  .name-link,
  .name-text {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
