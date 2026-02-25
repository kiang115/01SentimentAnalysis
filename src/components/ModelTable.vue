<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Delete, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadUserFile, UploadRawFile } from 'element-plus/es/components/upload/src/upload'
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

// ── Delete dialog (unified: batch + single) ─────────────────
const deleteDialogVisible = ref(false)
const deleteConfirmInput = ref('')
const pendingDeleteIds = ref<number[]>([])

const canConfirmDelete = computed(() => deleteConfirmInput.value === '确定删除')

function openBatchDeleteDialog() {
  if (selectedRows.value.length === 0) return
  pendingDeleteIds.value = selectedRows.value.map(r => r.modelId)
  deleteConfirmInput.value = ''
  deleteDialogVisible.value = true
}

function handleDeleteSingle(modelId: number) {
  pendingDeleteIds.value = [modelId]
  deleteConfirmInput.value = ''
  deleteDialogVisible.value = true
}

async function handleDeleteConfirm() {
  if (!canConfirmDelete.value) return
  deleteLoading.value = true
  try {
    const res = await modelApi.deleteModels(pendingDeleteIds.value)
    ElMessage.success(res.message)
    deleteDialogVisible.value = false
    pendingDeleteIds.value = []
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
  pendingDeleteIds.value = []
}

function handleDownload(modelId: number) {
  modelApi.downLoadModel(modelId)
}

// ── Upload dialog ───────────────────────────────────────────
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadFiles = ref<UploadUserFile[]>([])
const uploadForm = reactive({
  domainId: null as number | null,
  description: '',
})

const REQUIRED_FILES = ['adapter_config.json', 'adapter_model.bin']

function openUploadDialog() {
  uploadDialogVisible.value = true
}

function handleUploadChange(file: { name: string; raw: UploadRawFile }, fileList: UploadUserFile[]) {
  uploadFiles.value = fileList
}

function handleUploadRemove(_file: any, fileList: UploadUserFile[]) {
  uploadFiles.value = fileList
}

function validateFiles(): boolean {
  const names = uploadFiles.value.map(f => f.name)
  if (names.length !== 2) {
    ElMessage.warning('必须上传且仅上传 2 个文件：adapter_config.json 和 adapter_model.bin')
    return false
  }
  for (const required of REQUIRED_FILES) {
    if (!names.includes(required)) {
      ElMessage.warning(`缺少必需文件：${required}`)
      return false
    }
  }
  return true
}

async function handleUploadConfirm() {
  if (!validateFiles()) return
  if (!uploadForm.domainId) {
    ElMessage.warning('请选择适用领域')
    return
  }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('domainId', String(uploadForm.domainId))
    fd.append('description', uploadForm.description)
    uploadFiles.value.forEach(f => {
      if ((f as any).raw) fd.append('files', (f as any).raw)
    })
    await modelApi.uploadModel(fd)
    ElMessage.success('上传成功')
    handleUploadCancel()
    fetchData()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

function handleUploadCancel() {
  uploadDialogVisible.value = false
  uploadFiles.value = []
  uploadForm.domainId = null
  uploadForm.description = ''
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="model-table">
    <div class="table-toolbar">
      <el-button type="primary" :icon="Upload" @click="openUploadDialog">
        上传模型
      </el-button>
      <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0"
        @click="openBatchDeleteDialog" style="margin-left: 12px">
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

      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link size="small" @click="handleDeleteSingle(row.modelId)">
            删除
          </el-button>
          <el-button type="primary" link size="small" @click="handleDownload(row.modelId)">
            下载
          </el-button>
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
        <strong>{{ pendingDeleteIds.length }}</strong> 条模型记录
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

    <!-- Upload Model Dialog -->
    <el-dialog v-model="uploadDialogVisible" title="上传模型文件" width="560px" destroy-on-close
      @close="handleUploadCancel">
      <div class="upload-section">
        <label class="upload-label">选择模型文件</label>
        <el-upload drag multiple :auto-upload="false" :limit="2" :file-list="uploadFiles"
          :on-change="handleUploadChange" :on-remove="handleUploadRemove"
          :on-exceed="() => ElMessage.warning('最多只能上传 2 个文件')">
          <el-icon style="font-size: 40px; color: var(--el-color-primary); margin-bottom: 8px">
            <Upload />
          </el-icon>
          <div>拖放模型文件到此处或点击上传</div>
          <template #tip>
            <div class="el-upload__tip">
              必须包含 adapter_config.json 和 adapter_model.bin 两个文件
            </div>
          </template>
        </el-upload>
      </div>

      <div class="upload-section">
        <label class="upload-label">适用领域</label>
        <el-select v-model="uploadForm.domainId" placeholder="请选择领域" style="width: 100%">
          <el-option v-for="d in domains" :key="d.domainId" :label="d.domainName"
            :value="d.domainId" />
        </el-select>
      </div>

      <div class="upload-section">
        <label class="upload-label">模型描述</label>
        <el-input v-model="uploadForm.description" type="textarea" :rows="4"
          placeholder="请输入模型描述" />
      </div>

      <template #footer>
        <el-button @click="handleUploadCancel">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUploadConfirm">
          确认上传
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

.upload-section {
  margin-bottom: 20px;
}

.upload-label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}
</style>
