<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElForm, ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import jschardet from 'jschardet'
import Papa from 'papaparse'
import { modelApi } from '@/api/model-api'
import type { DomainAddSend } from '@/Dto/SendDto/DomainAddSend'

type PreviewItem = {
  content: string
  label: string
  isValid: boolean
}

const emit = defineEmits<{
  success: []
}>()

const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm>>()
const imageInputRef = ref<HTMLInputElement>()
const csvInputRef = ref<HTMLInputElement>()

const imageUploading = ref(false)
const submitting = ref(false)
const csvErrorMessage = ref('')
const previewList = ref<PreviewItem[]>([])
const currentCsvFile = ref<File | null>(null)

const form = reactive<DomainAddSend>({
  domainName: '',
  domainUrl: '',
  domainImageUrl: '',
  domainDescription: '',
  file: null,
})

const formRules = reactive({
  domainName: [{ required: true, message: '请输入领域名称', trigger: 'blur' }],
  domainUrl: [
    { required: true, message: '请输入领域地址', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (!value) {
          callback()
          return
        }
        if (!/^[A-Za-z]+$/.test(value)) {
          callback(new Error('领域地址只能包含英文字母，且不能有空格'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  domainImageUrl: [{ required: true, message: '请上传领域图片', trigger: 'change' }],
})

const hasInvalidPreviewRows = computed(() => previewList.value.some(item => !item.isValid))

function resetCsvState() {
  csvErrorMessage.value = ''
  previewList.value = []
  currentCsvFile.value = null
  form.file = null
  if (csvInputRef.value) {
    csvInputRef.value.value = ''
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.domainImageUrl = ''
  form.domainDescription = ''
  resetCsvState()
  if (imageInputRef.value) {
    imageInputRef.value.value = ''
  }
}

function openImagePicker() {
  if (imageUploading.value) return
  imageInputRef.value?.click()
}

async function handleImageChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  imageUploading.value = true
  try {
    const res = await modelApi.uploadFileToOss(formData)
    form.domainImageUrl = res.data
    formRef.value?.validateField('domainImageUrl')
  } finally {
    imageUploading.value = false
    input.value = ''
  }
}

function handleCsvFileChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  csvErrorMessage.value = ''
  previewList.value = []

  if (!file.name.toLowerCase().endsWith('.csv')) {
    csvErrorMessage.value = '文件格式错误: 只能上传 csv 文件'
    resetCsvState()
    return
  }

  const reader = new FileReader()
  reader.onload = e => {
    const result = e.target?.result as string
    const detected = jschardet.detect(result)
    const encoding = detected.encoding?.toUpperCase() || ''
    if (encoding && encoding !== 'UTF-8' && encoding !== 'ASCII') {
      csvErrorMessage.value = `编码错误: 检测到 ${detected.encoding}，请使用 UTF-8 编码。`
      resetCsvState()
      return
    }

    currentCsvFile.value = file
    form.file = file
    parseCsvPreview(file)
  }
  reader.readAsBinaryString(file.slice(0, 10240))
}

function parseCsvPreview(file: File) {
  Papa.parse(file, {
    header: true,
    preview: 10,
    skipEmptyLines: true,
    complete: results => {
      const headers = results.meta.fields || []
      if (!headers.includes('content') || !headers.includes('label')) {
        csvErrorMessage.value = '格式错误: 必须包含 content 和 label 列'
        previewList.value = []
        currentCsvFile.value = null
        form.file = null
        return
      }

      const rows = (results.data as Record<string, string>[]).map(row => ({
        content: row.content || '',
        label: row.label || '',
        isValid: !!row.content?.trim() && (row.label === '0' || row.label === '1'),
      }))

      previewList.value = rows
      if (rows.length === 0) {
        csvErrorMessage.value = 'CSV 文件为空或没有可解析的数据'
        currentCsvFile.value = null
        form.file = null
      } else if (rows.some(row => !row.isValid)) {
        csvErrorMessage.value = '预览中存在不合法数据，请修正后再上传'
      }
    },
    error: () => {
      csvErrorMessage.value = 'CSV 文件解析失败'
      previewList.value = []
      currentCsvFile.value = null
      form.file = null
    },
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!currentCsvFile.value || !form.file) {
    ElMessage.warning('请上传初始 csv 文件')
    return
  }
  if (csvErrorMessage.value || previewList.value.length === 0 || hasInvalidPreviewRows.value) {
    ElMessage.warning('请先修正 csv 文件后再提交')
    return
  }

  const formData = new FormData()
  formData.append('domainName', form.domainName)
  formData.append('domainUrl', form.domainUrl)
  formData.append('domainImageUrl', form.domainImageUrl)
  formData.append('domainDescription', form.domainDescription || '')
  formData.append('file', form.file)

  submitting.value = true
  try {
    const res = await modelApi.addDomain(formData)
    emit('success')
    resetForm()

    try {
      await ElMessageBox.confirm(res.data, '新增成功', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success',
        distinguishCancelAndClose: true,
      })
      await router.push('/model/Task')
    } catch {
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="domain-add-card">
    <div class="section-head">
      <h3>新增领域</h3>
      <span>创建新的领域并上传原始训练数据</span>
    </div>

    <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" class="domain-form">
      <el-form-item label="领域名称" prop="domainName">
        <el-input v-model="form.domainName" placeholder="请输入领域名称" clearable />
      </el-form-item>

      <el-form-item label="领域地址" prop="domainUrl">
        <el-input v-model="form.domainUrl" placeholder="仅允许英文字母，如 waimai" clearable />
      </el-form-item>

      <el-form-item label="领域图片" prop="domainImageUrl">
        <div class="upload-wrapper">
          <div
            class="upload-box"
            :class="{ filled: !!form.domainImageUrl, uploading: imageUploading }"
            @click="openImagePicker"
          >
            <img v-if="form.domainImageUrl" :src="form.domainImageUrl" alt="领域图片" class="upload-preview" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span>点击上传领域图片</span>
            </div>
            <div v-if="imageUploading" class="upload-mask">上传中...</div>
          </div>
          <input
            ref="imageInputRef"
            class="hidden-file-input"
            type="file"
            accept="image/*"
            @change="handleImageChange"
          />
        </div>
      </el-form-item>

      <el-form-item label="领域描述">
        <el-input
          v-model="form.domainDescription"
          type="textarea"
          :rows="4"
          placeholder="请输入领域描述，可为空"
        />
      </el-form-item>

      <el-form-item label="初始CSV文件">
        <div class="csv-section">
          <input ref="csvInputRef" type="file" accept=".csv" @change="handleCsvFileChange" />
          <div class="csv-tip">必须包含 content 和 label 两列，label 只能为 0 或 1</div>

          <el-alert
            v-if="csvErrorMessage"
            :title="csvErrorMessage"
            type="error"
            show-icon
            :closable="false"
            class="csv-alert"
          />

          <div v-if="previewList.length > 0" class="preview-section">
            <div class="preview-title">CSV 预览（前 10 行）</div>
            <el-table :data="previewList" border stripe size="small" max-height="280">
              <el-table-column prop="content" label="content" min-width="240" show-overflow-tooltip>
                <template #default="{ row }">
                  <span :style="{ color: !row.content?.trim() ? '#f56c6c' : '' }">
                    {{ row.content || '(空)' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="label" label="label" width="100" align="center">
                <template #default="{ row }">
                  <span :style="{ color: row.label !== '0' && row.label !== '1' ? '#f56c6c' : '' }">
                    {{ row.label || '(空)' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="校验状态" width="110" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isValid ? 'success' : 'danger'" size="small">
                    {{ row.isValid ? '合法' : '不合法' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确认新增
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped lang="less">
.domain-add-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;

  h3 {
    margin: 0;
    font-size: 18px;
    color: #0f172a;
  }

  span {
    color: #64748b;
    font-size: 13px;
  }
}

.domain-form {
  max-width: 900px;
}

.upload-wrapper {
  width: 100%;
}

.upload-box {
  position: relative;
  width: 240px;
  height: 160px;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.upload-box:hover {
  border-color: #2563eb;
  background: #f1f5f9;
}

.upload-box.filled {
  border-style: solid;
}

.upload-box.uploading {
  cursor: wait;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 14px;
}

.upload-icon {
  font-size: 24px;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.42);
  color: #fff;
  font-size: 14px;
}

.hidden-file-input {
  display: none;
}

.csv-section {
  width: 100%;
}

.csv-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.csv-alert {
  margin-top: 12px;
}

.preview-section {
  margin-top: 16px;
}

.preview-title {
  margin-bottom: 8px;
  font-weight: 500;
  color: #0f172a;
}

@media (max-width: 768px) {
  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .upload-box {
    width: 100%;
    max-width: 280px;
  }
}
</style>
