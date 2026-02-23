<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElForm } from 'element-plus'
import { modelApi } from '@/api/model-api'
import jschardet from 'jschardet'
import Papa from 'papaparse'
import type { DataItem, DomainItem } from '@/Dto/ReceiveDto/TrainDataListRec'
import type { TrainDataQuerySend } from '@/Dto/SendDto/TrainDataQuerySend'
import type { TrainDataAddSend } from '@/Dto/SendDto/TrainDataAddSend'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'

const tableRef = ref()
const loading = ref(false)
const tableData = ref<DataItem[]>([])
const pageInfo = ref<PageInfo<DataItem>>()
const domains = ref<DomainItem[]>([])
const searchContent = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: null as number | null,
  content: '',
  label: null as number | null,
  source: '',
  domainId: null as number | null,
  orderName: '',
  order: '',
})

const sourceMap: Record<string, string> = {
  original: '原始数据',
  upload: '上传数据',
  corrected: '修正数据',
}

const labelFilters = [
  { text: '积极', value: 1 },
  { text: '消极', value: 0 },
]

const sourceFilters = [
  { text: '原始数据', value: 'original' },
  { text: '上传数据', value: 'upload' },
  { text: '修正数据', value: 'corrected' },
]

const domainFilters = computed(() =>
  domains.value.map(d => ({ text: d.domainName, value: d.domainId }))
)

const serverFilter = () => true

async function fetchData() {
  loading.value = true
  try {
    const res = await modelApi.listTrainData(queryParams as TrainDataQuerySend)
    pageInfo.value = res.data.pageInfo
    tableData.value = res.data.pageInfo.list
    domains.value = res.data.domains
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.content = searchContent.value
  queryParams.pageNum = 1
  fetchData()
}

function handleSortChange({ prop, order }: { prop: string; order: string | null }) {
  if (!order) {
    queryParams.orderName = ''
    queryParams.order = ''
  } else {
    queryParams.orderName = prop === 'createdAt' ? 'time' : 'count'
    queryParams.order = order === 'ascending' ? 'asc' : 'desc'
  }
  queryParams.pageNum = 1
  fetchData()
}

function handleFilterChange(filters: Record<string, any[]>) {
  for (const [key, values] of Object.entries(filters)) {
    const value = values.length > 0 ? values[0] : null
    switch (key) {
      case 'label':
        queryParams.label = value
        break
      case 'source':
        queryParams.source = value ?? ''
        break
      case 'domainId':
        queryParams.domainId = value
        break
    }
  }
  queryParams.pageNum = 1
  fetchData()
}

function handleCurrentChange(newPage: number) {
  queryParams.pageNum = newPage
  fetchData()
}

const addDialogVisible = ref(false)
const addFormRef = ref<InstanceType<typeof ElForm>>()
const addForm = reactive<TrainDataAddSend>({
  content: '',
  label: null as unknown as number,
  domainId: null as unknown as number,
})
const addFormRules = reactive({
  content: [{ required: true, message: '请输入数据内容', trigger: 'blur' }],
  label: [{ required: true, message: '请选择数据标签', trigger: 'change' }],
  domainId: [{ required: true, message: '请选择领域', trigger: 'change' }],
})

async function handleAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await modelApi.addTrainData(addForm)
  ElMessage.success('添加成功')
  addDialogVisible.value = false
  addFormRef.value?.resetFields()
  fetchData()
}

const uploadDialogVisible = ref(false)
const currentFile = ref<File | null>(null)
const previewList = ref<any[]>([])
const errorMessage = ref('')
const uploadDomainId = ref<number | null>(null)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const onFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  errorMessage.value = ''
  previewList.value = []

  const reader = new FileReader()
  reader.onload = (event) => {
    const result = event.target?.result as string
    const { encoding } = jschardet.detect(result)

    if (encoding !== 'UTF-8' && encoding !== 'ASCII') {
      errorMessage.value = `编码错误: 检测到 ${encoding}，请使用 UTF-8 编码。`
      return
    }

    currentFile.value = file
    parseCsv(file)
  }
  reader.readAsBinaryString(file.slice(0, 10240))
}

const parseCsv = (file: File) => {
  Papa.parse(file, {
    header: true,
    preview: 10,
    skipEmptyLines: true,
    complete: (results) => {
      const data = results.data as any[]

      const headers = results.meta.fields || []
      if (!headers.includes('content') || !headers.includes('label')) {
        errorMessage.value = '格式错误: 必须包含 content 和 label 列'
        return
      }

      previewList.value = data.map(row => ({
        ...row,
        isValid: row.content?.trim() && (row.label === '0' || row.label === '1')
      }))
    }
  })
}

const confirmUpload = async () => {
  if (!currentFile.value || !uploadDomainId.value) return

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', currentFile.value)
    fd.append('domainId', String(uploadDomainId.value))
    await modelApi.uploadTrainData(fd)
    ElMessage.success('上传成功')
    cancelUpload()
    fetchData()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const cancelUpload = () => {
  uploadDialogVisible.value = false
  previewList.value = []
  errorMessage.value = ''
  currentFile.value = null
  uploadDomainId.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="train-data-table">
    <div class="table-toolbar">
      <el-input
        v-model="searchContent"
        placeholder="搜索数据内容..."
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
        style="width: 320px"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
      <el-button type="primary" :icon="Plus" @click="addDialogVisible = true" style="margin-left: 12px">
        添加单个数据
      </el-button>
      <el-button type="success" :icon="Upload" @click="uploadDialogVisible = true" style="margin-left: 12px">
        上传数据文件
      </el-button>
    </div>

    <el-table
      ref="tableRef"
      row-key="id"
      :data="tableData"
      v-loading="loading"
      stripe
      border
      @sort-change="handleSortChange"
      @filter-change="handleFilterChange"
      style="width: 100%"
    >
      <el-table-column prop="id" label="数据编号" width="100" align="center" />
      <el-table-column prop="content" label="数据内容" min-width="300" show-overflow-tooltip />
      <el-table-column
        prop="domainName"
        label="领域名称"
        width="120"
        align="center"
        column-key="domainId"
        :filters="domainFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      />
      <el-table-column
        prop="label"
        label="数据标签"
        width="100"
        align="center"
        column-key="label"
        :filters="labelFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      >
        <template #default="{ row }">
          <el-tag :type="row.label === 1 ? 'success' : 'danger'" disable-transitions>
            {{ row.label === 1 ? '积极' : '消极' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="source"
        label="数据集来源"
        width="130"
        align="center"
        column-key="source"
        :filters="sourceFilters"
        :filter-method="serverFilter"
        :filter-multiple="false"
      >
        <template #default="{ row }">
          {{ sourceMap[row.source] || row.source }}
        </template>
      </el-table-column>
      <el-table-column
        prop="trainCount"
        label="训练次数"
        width="120"
        align="center"
        sortable="custom"
      />
      <el-table-column
        prop="createdAt"
        label="创建日期"
        width="180"
        align="center"
        sortable="custom"
      >
        <template #default="{ row }">
          {{ row.createdAt?.replace('T', ' ') }}
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

    <el-dialog v-model="addDialogVisible" title="添加训练数据" width="500" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="数据内容" prop="content">
          <el-input v-model="addForm.content" type="textarea" :rows="3" placeholder="请输入数据内容" />
        </el-form-item>
        <el-form-item label="数据标签" prop="label">
          <el-radio-group v-model="addForm.label">
            <el-radio :value="1">好评</el-radio>
            <el-radio :value="0">差评</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属领域" prop="domainId">
          <el-select v-model="addForm.domainId" placeholder="请选择领域">
            <el-option
              v-for="d in domains"
              :key="d.domainId"
              :label="d.domainName"
              :value="d.domainId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传训练数据文件" width="700" destroy-on-close @close="cancelUpload">
      <div style="margin-bottom: 16px">
        <label style="font-weight: 500; margin-right: 8px">选择 CSV 文件：</label>
        <input ref="fileInputRef" type="file" accept=".csv" @change="onFileChange" />
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />

      <div v-if="previewList.length > 0" style="margin-bottom: 16px">
        <div style="margin-bottom: 8px; font-weight: 500">数据预览（前 10 行）</div>
        <el-table :data="previewList" border stripe size="small" max-height="300">
          <el-table-column prop="content" label="content" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span :style="{ color: !row.content?.trim() ? '#f56c6c' : '' }">
                {{ row.content || '(空)' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="label" label="label" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.label !== '0' && row.label !== '1' ? '#f56c6c' : '' }">
                {{ row.label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="校验状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isValid ? 'success' : 'danger'" size="small">
                {{ row.isValid ? '合法' : '不合法' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="previewList.length > 0" style="margin-bottom: 16px">
        <label style="font-weight: 500; margin-right: 8px">所属领域：</label>
        <el-select v-model="uploadDomainId" placeholder="请选择领域" style="width: 220px">
          <el-option
            v-for="d in domains"
            :key="d.domainId"
            :label="d.domainName"
            :value="d.domainId"
          />
        </el-select>
      </div>

      <template #footer>
        <el-button @click="cancelUpload">取消</el-button>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!currentFile || !uploadDomainId"
          @click="confirmUpload"
        >
          确认上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.train-data-table {
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
}
</style>
