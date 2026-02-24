<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { modelApi } from '@/api/model-api'
import type { ModelInfoItem, DomainItem } from '@/Dto/ReceiveDto/ModelsListRec'
import type { ModelsQuerySend } from '@/Dto/SendDto/ModelsQuerySend'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'

const loading = ref(false)
const deleteLoading = ref(false)
const tableData = ref<ModelInfoItem[]>([])
const pageInfo = ref<PageInfo<ModelInfoItem>>()
const domains = ref<DomainItem[]>([])
const selectedRows = ref<ModelInfoItem[]>([])
const modelIdInput = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: null as number | null,
  modelId: '',
  domainId: null as number | null,
  source: null as string | null,
})

const domainFilters = computed(() =>
  domains.value?.map(d => ({ text: d.domainName, value: d.domainId })) || []
)

const sourceFilters = [
  { text: '训练模型', value: 'train' },
  { text: '手动上传', value: 'upload' },
]

const serverFilter = () => true

async function fetchData() {
  loading.value = true
  try {
    const res = await modelApi.listModels(queryParams as ModelsQuerySend)
    pageInfo.value = res.data.pageInfo
    tableData.value = res.data.pageInfo.list
    domains.value = res.data.domains
  } finally {
    loading.value = false
  }
}

function handleModelIdSearch() {
  queryParams.modelId = modelIdInput.value
  queryParams.pageNum = 1
  fetchData()
}

function handleFilterChange(filters: Record<string, any[]>) {
  for (const [key, values] of Object.entries(filters)) {
    const value = values.length > 0 ? values[0] : null
    if (key === 'domainId') {
      queryParams.domainId = value
    } else if (key === 'source') {
      queryParams.source = value
    }
  }
  queryParams.pageNum = 1
  fetchData()
}

function handleCurrentChange(newPage: number) {
  queryParams.pageNum = newPage
  fetchData()
}

function handleSelectionChange(selection: ModelInfoItem[]) {
  selectedRows.value = selection
}

function formatDateTime(raw: string): string {
  if (!raw) return '—'
  return raw.replace('T', ' ')
}

// ── Delete dialog ──────────────────────────────────────────
const deleteDialogVisible = ref(false)
const deleteConfirmInput = ref('')

const canConfirmDelete = computed(() => deleteConfirmInput.value === '确定删除')

function openDeleteDialog() {
  if (selectedRows.value.length === 0) return
  deleteConfirmInput.value = ''
  deleteDialogVisible.value = true
}

async function handleDeleteConfirm() {
  if (!canConfirmDelete.value) return
  deleteLoading.value = true
  try {
    await modelApi.deleteModels(selectedRows.value.map(r => r.modelId))
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    selectedRows.value = []
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

function handleDeleteCancel() {
  deleteDialogVisible.value = false
  deleteConfirmInput.value = ''
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="model-table">
    <div class="table-toolbar">
      <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0"
        @click="openDeleteDialog">
        批量删除
      </el-button>
    </div>

    <el-table row-key="modelId" :data="tableData" v-loading="loading" stripe border
      @filter-change="handleFilterChange" @selection-change="handleSelectionChange" style="width: 100%">
      <el-table-column type="selection" width="55" />

      <el-table-column prop="modelId" label="模型编号" width="120" align="center">
        <template #header>
          <div class="model-id-header">
            <span>模型编号</span>
            <el-input v-model="modelIdInput" placeholder="输入ID筛选" size="small" clearable
              style="width: 90px" @clear="handleModelIdSearch" @keyup.enter="handleModelIdSearch" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="domainName" label="领域名称" width="120" align="center"
        column-key="domainId" :filters="domainFilters" :filter-method="serverFilter"
        :filter-multiple="false" />

      <el-table-column prop="modelVersion" label="模型版本" width="100" align="center" />

      <el-table-column prop="sourceName" label="模型来源" width="120" align="center"
        column-key="source" :filters="sourceFilters" :filter-method="serverFilter"
        :filter-multiple="false" />

      <el-table-column prop="inferredNum" label="推理评论总数" width="120" align="center" />
      <el-table-column prop="correctedNum" label="人工修正数量" width="120" align="center" />

      <el-table-column prop="accuracy" label="模型准确率" width="110" align="center">
        <template #default="{ row }">
          {{ row.accuracy != null ? row.accuracy + '%' : '—' }}
        </template>
      </el-table-column>

      <el-table-column prop="description" label="模型描述" min-width="180" align="center"
        show-overflow-tooltip />

      <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination :current-page="pageInfo?.pageNum" :page-size="pageInfo?.pageSize"
        :total="pageInfo?.total" layout="total, prev, pager, next, jumper" background
        @current-change="handleCurrentChange" />
    </div>

    <!-- Delete Confirm Dialog -->
    <el-dialog v-model="deleteDialogVisible" title="删除确认" width="440px" destroy-on-close
      @close="handleDeleteCancel">
      <p style="margin-bottom: 16px">
        请输入「<strong>确定删除</strong>」以确认删除
        <strong>{{ selectedRows.length }}</strong> 条模型记录
      </p>
      <el-input v-model="deleteConfirmInput" placeholder="请输入：确定删除" clearable
        @keyup.enter="handleDeleteConfirm" />

      <template #footer>
        <el-button @click="handleDeleteCancel">取消</el-button>
        <el-button type="danger" :disabled="!canConfirmDelete" :loading="deleteLoading"
          @click="handleDeleteConfirm">
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.model-table {
  .table-toolbar {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .model-id-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 4px 0;
  }
}
</style>
