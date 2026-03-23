<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElForm } from 'element-plus'
import { modelApi } from '@/api/model-api'
import type { TrainParaItem, DomainItem } from '@/Dto/ReceiveDto/TrainParaListRec'
import type { TrainParaQuerySend } from '@/Dto/SendDto/TrainParaQuerySend'
import type { AddTrainParaSend } from '@/Dto/SendDto/AddTrainParaSend'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'

const loading = ref(false)
const deleteLoading = ref(false)
const tableData = ref<TrainParaItem[]>([])
const pageInfo = ref<PageInfo<TrainParaItem>>()
const domains = ref<DomainItem[]>([])
const selectedRows = ref<TrainParaItem[]>([])
const paraIdInput = ref('')

const queryParams = reactive<TrainParaQuerySend>({
  pageNum: 1,
  pageSize: null,
  paraId: '',
  domainId: null,
})

const domainFilters = computed(() =>
  // 这样写：如果 domains.value 是 undefined/null，会直接返回 undefined
  domains.value?.map(d => ({ text: d.domainName, value: d.domainId })) || []
)

const serverFilter = () => true

async function fetchData() {
  loading.value = true
  try {
    const res = await modelApi.listTrainParam(queryParams)
    pageInfo.value = res.data.pageInfo
    tableData.value = res.data.pageInfo.list
    domains.value = res.data.domains
  } finally {
    loading.value = false
  }
}

function handleParaIdSearch() {
  queryParams.paraId = paraIdInput.value
  queryParams.pageNum = 1
  fetchData()
}

function handleFilterChange(filters: Record<string, any[]>) {
  for (const [key, values] of Object.entries(filters)) {
    const value = values.length > 0 ? values[0] : null
    if (key === 'domainId') {
      queryParams.domainId = value
    }
  }
  queryParams.pageNum = 1
  fetchData()
}

function handleCurrentChange(newPage: number) {
  queryParams.pageNum = newPage
  fetchData()
}

function handleSelectionChange(selection: TrainParaItem[]) {
  selectedRows.value = selection
}

async function handleDelete() {
  if (selectedRows.value.length === 0) return
  deleteLoading.value = true
  try {
    await modelApi.deleteTrainParam(selectedRows.value.map(r => r.paraId))
    ElMessage.success('删除成功')
    selectedRows.value = []
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// ── Add dialog ──────────────────────────────────────────────
const addDialogVisible = ref(false)
const addFormRef = ref<InstanceType<typeof ElForm>>()

const loraModuleOptions = ['query', 'key', 'value', 'dense']

type AddTrainParaForm = Omit<AddTrainParaSend, 'domainId'> & {
  domainId: number | null
}

const addForm = reactive<AddTrainParaForm>({
  domainId: null,
  loraR: 8,
  loraAlpha: 16,
  epochs: 5,
  batchSize: 64,
  learningRate: 0.00001,
  randomSeed: '42',
  loraModules: [],
  trainSplitRatio: 0.8,
})

const addFormRules = reactive({
  domainId: [{ required: true, message: '请选择领域', trigger: 'change' }],
  loraR: [{ required: true, message: '请输入 LORA_R', trigger: 'blur' }],
  loraAlpha: [{ required: true, message: '请输入 LORA_ALPHA', trigger: 'blur' }],
  epochs: [{ required: true, message: '请输入训练轮数', trigger: 'blur' }],
  batchSize: [{ required: true, message: '请输入批次大小', trigger: 'blur' }],
  learningRate: [{ required: true, message: '请输入学习率', trigger: 'blur' }],
  randomSeed: [{ required: true, message: '请输入随机种子', trigger: 'blur' }],
  loraModules: [{ required: true, type: 'array', min: 1, message: '请至少选择一个 LORA 模块', trigger: 'change' }],
  trainSplitRatio: [{ required: true, message: '请设置训练集比例', trigger: 'change' }],
})

const trainValRatioLabel = computed(() => {
  const r = addForm.trainSplitRatio
  return `训练集 ${Math.round(r * 100)}% / 验证集 ${Math.round((1 - r) * 100)}%`
})

function openAddDialog() {
  addDialogVisible.value = true
}

async function handleAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (addForm.domainId == null) return
  const payload: AddTrainParaSend = {
    ...addForm,
    domainId: addForm.domainId,
  }
  try {
    await modelApi.addTrainParam(payload)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    addFormRef.value?.resetFields()
    fetchData()
  } catch {
    ElMessage.error('添加失败')
  }
}

function handleAddCancel() {
  addDialogVisible.value = false
  addFormRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="train-para-table">
    <div class="table-toolbar">
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        添加参数配置
      </el-button>
      <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0" :loading="deleteLoading"
        @click="handleDelete" style="margin-left: 12px">
        批量删除
      </el-button>
    </div>

    <el-table row-key="paraId" :data="tableData" v-loading="loading" stripe border @filter-change="handleFilterChange"
      @selection-change="handleSelectionChange" style="width: 100%">
      <el-table-column type="selection" width="55" />

      <el-table-column prop="paraId" label="参数编号" width="100" align="center">
        <template #header>
          <div class="para-id-header">
            <span>参数编号</span>
            <el-input v-model="paraIdInput" placeholder="输入ID筛选" size="small" clearable style="width: 90px"
              @clear="handleParaIdSearch" @keyup.enter="handleParaIdSearch" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="domainName" label="领域名称" width="120" align="center" column-key="domainId"
        :filters="domainFilters" :filter-method="serverFilter" :filter-multiple="false" />

      <el-table-column prop="loraR" label="LORA_R" width="90" align="center" />
      <el-table-column prop="loraAlpha" label="LORA_ALPHA" width="110" align="center" />
      <el-table-column prop="epochs" label="训练轮数" width="90" align="center" />
      <el-table-column prop="batchSize" label="批次大小" width="90" align="center" />
      <el-table-column prop="learningRate" label="学习率" width="110" align="center" />
      <el-table-column prop="randomSeed" label="随机种子" width="100" align="center" />

      <el-table-column label="LORA 模块" min-width="180" align="center">
        <template #default="{ row }">
          <el-tag v-for="m in row.loraModules" :key="m" size="small" type="info" style="margin: 2px"
            disable-transitions>
            {{ m }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="训练集比例" width="110" align="center">
        <template #default="{ row }">
          {{ row.trainSplitRatio != null ? Math.round(row.trainSplitRatio * 100) + '%' : '—' }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination :current-page="pageInfo?.pageNum" :page-size="pageInfo?.pageSize" :total="pageInfo?.total"
        layout="total, prev, pager, next, jumper" background @current-change="handleCurrentChange" />
    </div>

    <!-- Add Parameter Dialog -->
    <el-dialog v-model="addDialogVisible" title="添加训练参数配置" width="580px" destroy-on-close @close="handleAddCancel">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="120px">
        <el-form-item label="所属领域" prop="domainId">
          <el-select v-model="addForm.domainId" placeholder="请选择领域" style="width: 100%">
            <el-option v-for="d in domains" :key="d.domainId" :label="d.domainName" :value="d.domainId" />
          </el-select>
        </el-form-item>

        <div class="detail-section">
          <div class="section-title">LoRA 参数</div>
          <el-form-item label="LORA_R" prop="loraR">
            <el-input-number v-model="addForm.loraR" :min="1" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="LORA_ALPHA" prop="loraAlpha">
            <el-input-number v-model="addForm.loraAlpha" :min="1" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="LORA 模块" prop="loraModules">
            <el-checkbox-group v-model="addForm.loraModules">
              <el-checkbox v-for="m in loraModuleOptions" :key="m" :value="m">{{ m }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </div>

        <div class="detail-section">
          <div class="section-title">训练参数</div>
          <el-form-item label="训练轮数" prop="epochs">
            <el-input-number v-model="addForm.epochs" :min="1" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="批次大小" prop="batchSize">
            <el-input-number v-model="addForm.batchSize" :min="1" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="学习率" prop="learningRate">
            <el-input-number v-model="addForm.learningRate" :min="0.000001" :step="0.00001" :precision="6"
              style="width: 100%" />
          </el-form-item>
          <el-form-item label="随机种子" prop="randomSeed">
            <el-input v-model="addForm.randomSeed" placeholder="例如：42" style="width: 100%" />
          </el-form-item>
        </div>

        <div class="detail-section">
          <div class="section-title">数据集划分</div>
          <el-form-item label="训练集比例" prop="trainSplitRatio">
            <div style="width: 100%">
              <el-slider v-model="addForm.trainSplitRatio" :min="0.5" :max="0.9" :step="0.05" show-stops
                style="padding: 0 8px" />
              <div class="ratio-label">{{ trainValRatioLabel }}</div>
            </div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="handleAddCancel">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.train-para-table {
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

  .para-id-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 4px 0;
  }
}

.detail-section {
  padding: 12px 12px 4px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  margin-bottom: 16px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.ratio-label {
  text-align: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
