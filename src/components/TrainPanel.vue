<template>
  <el-dialog
    v-model="dialogVisible"
    title="训练信息配置"
    width="760px"
    destroy-on-close
    @closed="onClosed"
  >
    <div v-if="loading" class="panel-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <template v-else-if="domainList.length">
      <div class="panel-row-single">
        <span class="row-label">选择领域：</span>
        <el-radio-group v-model="selectedDomainId">
          <el-radio
            v-for="item in domainList"
            :key="item.domainId"
            :label="item.domainId"
          >
            {{ item.domainName }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="slider-block">
        <div class="slider-item">
          <div class="slider-title">人工修正数据 (correctedNum)</div>
          <el-slider
            v-model="counts.correctedCount"
            :min="0"
            :max="currentDomain?.correctedNum ?? 0"
            :disabled="(currentDomain?.correctedNum ?? 0) === 0"
            show-input
          />
        </div>
        <div class="slider-item">
          <div class="slider-title">手动上传数据 (uploadNum)</div>
          <el-slider
            v-model="counts.uploadCount"
            :min="0"
            :max="currentDomain?.uploadNum ?? 0"
            :disabled="(currentDomain?.uploadNum ?? 0) === 0"
            show-input
          />
        </div>
        <div class="slider-item">
          <div class="slider-title">模型初始数据 (originalNum)</div>
          <el-slider
            v-model="counts.originalCount"
            :min="0"
            :max="currentDomain?.originalNum ?? 0"
            :disabled="(currentDomain?.originalNum ?? 0) === 0"
            show-input
          />
        </div>
      </div>

      <div class="panel-row-single">
        <span class="row-label">默认参数：</span>
        <el-select
          v-model="selectedParaId"
          clearable
          placeholder="请选择 paraId"
          style="width: 220px"
          @change="handleSelectPara"
        >
          <el-option
            v-for="p in currentDomain?.defaultParaList ?? []"
            :key="p.paraId"
            :label="`paraId: ${p.paraId}`"
            :value="p.paraId"
          />
        </el-select>
      </div>

      <el-collapse v-model="activeCollapse">
        <el-collapse-item title="参数表单（默认折叠）" name="train-form">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="140px"
            class="train-form"
          >
            <el-form-item label="loraR" prop="loraR">
              <el-input v-model="formData.loraR" placeholder="正整数 > 0" />
            </el-form-item>
            <el-form-item label="loraAlpha" prop="loraAlpha">
              <el-input v-model="formData.loraAlpha" placeholder="正整数 > 0" />
            </el-form-item>
            <el-form-item label="epochs" prop="epochs">
              <el-input v-model="formData.epochs" placeholder="正整数 > 0" />
            </el-form-item>
            <el-form-item label="batchSize" prop="batchSize">
              <el-input v-model="formData.batchSize" placeholder="1 或 >=2 的偶数" />
            </el-form-item>
            <el-form-item label="learningRate" prop="learningRate">
              <el-input
                v-model="formData.learningRate"
                placeholder="0~1，支持科学计数法（如 1e-5）"
              />
            </el-form-item>
            <el-form-item label="randomSeed" prop="randomSeed">
              <el-input v-model="formData.randomSeed" placeholder="正整数 > 0" />
            </el-form-item>
            <el-form-item label="loraModules" prop="loraModules">
              <el-checkbox-group v-model="formData.loraModules">
                <el-checkbox label="query">query</el-checkbox>
                <el-checkbox label="key">key</el-checkbox>
                <el-checkbox label="value">value</el-checkbox>
                <el-checkbox label="dense">dense</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="trainSplitRatio" prop="trainSplitRatio">
              <el-input v-model="formData.trainSplitRatio" placeholder="0~1 之间浮点数" />
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>

      <div class="panel-row-single mt16">
        <span class="row-label">是否覆盖训练：</span>
        <el-radio-group v-model="overwriteTrain">
          <el-radio :label="true">是</el-radio>
          <el-radio :label="false">否</el-radio>
        </el-radio-group>
      </div>
    </template>

    <el-empty v-else description="暂无训练配置数据" />

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确认训练</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'
import type { TrainDomainItem } from '@/Dto/ReceiveDto/TrainPanelRec'
import type { TrainPanelSend } from '@/Dto/SendDto/TrainPanelSend'

type TrainForm = {
  loraR: string
  loraAlpha: string
  epochs: string
  batchSize: string
  learningRate: string
  randomSeed: string
  loraModules: string[]
  trainSplitRatio: string
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'confirm'): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(false)
const domainList = ref<TrainDomainItem[]>([])
const selectedDomainId = ref<number>()
const selectedParaId = ref<number | undefined>(undefined)
const activeCollapse = ref<string[]>([])
const overwriteTrain = ref(false)

const counts = ref({
  correctedCount: 0,
  uploadCount: 0,
  originalCount: 0,
})

const formRef = ref()

const emptyForm = (): TrainForm => ({
  loraR: '',
  loraAlpha: '',
  epochs: '',
  batchSize: '',
  learningRate: '',
  randomSeed: '',
  loraModules: [],
  trainSplitRatio: '',
})

const formData = ref<TrainForm>(emptyForm())

const currentDomain = computed(() =>
  domainList.value.find((item) => item.domainId === selectedDomainId.value),
)

function isPositiveInt(value: string): boolean {
  return /^[1-9]\d*$/.test(value)
}

function isValidBatchSize(value: string): boolean {
  if (!/^\d+$/.test(value)) return false
  const n = Number(value)
  return n === 1 || (n >= 2 && n % 2 === 0)
}

function isValidFloatInRange(value: string, min: number, max: number): boolean {
  if (value == null || value === '') return false
  const n = Number(value)
  if (!Number.isFinite(n)) return false
  return n > min && n < max
}

const rules = {
  loraR: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isPositiveInt(value)) return callback(new Error('loraR 需为正整数'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  loraAlpha: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isPositiveInt(value)) return callback(new Error('loraAlpha 需为正整数'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  epochs: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isPositiveInt(value)) return callback(new Error('epochs 需为正整数'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  batchSize: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isValidBatchSize(value)) return callback(new Error('batchSize 需为 1 或 >=2 的偶数'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  learningRate: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isValidFloatInRange(value, 0, 1)) {
          return callback(new Error('learningRate 需在 0 与 1 之间'))
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  randomSeed: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isPositiveInt(value)) return callback(new Error('randomSeed 需为正整数'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  loraModules: [
    {
      validator: (
        _rule: unknown,
        value: string[],
        callback: (error?: Error) => void,
      ) => {
        if (!Array.isArray(value) || value.length === 0) {
          return callback(new Error('loraModules 至少选择一个'))
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  trainSplitRatio: [
    {
      validator: (
        _rule: unknown,
        value: string,
        callback: (error?: Error) => void,
      ) => {
        if (!isValidFloatInRange(value, 0, 1)) {
          return callback(new Error('trainSplitRatio 需在 0 与 1 之间'))
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function resetDomainState() {
  counts.value = {
    correctedCount: 0,
    uploadCount: 0,
    originalCount: 0,
  }
  selectedParaId.value = undefined
  formData.value = emptyForm()
  formRef.value?.clearValidate()
}

function handleSelectPara(paraId?: number) {
  if (!paraId) {
    formData.value = emptyForm()
    formRef.value?.clearValidate()
    return
  }
  const target = currentDomain.value?.defaultParaList.find((p) => p.paraId === paraId)
  if (!target) return
  formData.value = {
    loraR: String(target.loraR),
    loraAlpha: String(target.loraAlpha),
    epochs: String(target.epochs),
    batchSize: String(target.batchSize),
    learningRate: String(target.learningRate),
    randomSeed: String(target.randomSeed),
    loraModules: [...target.loraModules],
    trainSplitRatio: String(target.trainSplitRatio),
  }
  formRef.value?.clearValidate()
}

function close() {
  emit('update:modelValue', false)
}

function onClosed() {
  domainList.value = []
  selectedDomainId.value = undefined
  activeCollapse.value = []
  overwriteTrain.value = false
  resetDomainState()
}

async function fetchPanel() {
  loading.value = true
  try {
    const res = await modelApi.listTrainPanel()
    const list = res.data?.trainParaList ?? []
    domainList.value = list
    selectedDomainId.value = list[0]?.domainId
    resetDomainState()
  } catch {
    domainList.value = []
    selectedDomainId.value = undefined
    resetDomainState()
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  if (selectedDomainId.value == null) {
    await ElMessageBox.alert('请选择领域后再发起训练。', '提示', { type: 'warning' })
    return
  }

  const total = counts.value.correctedCount + counts.value.uploadCount + counts.value.originalCount
  if (total === 0) {
    await ElMessageBox.alert('三类评论选择数量不能全为 0，请至少选择一类数据。', '提示', {
      type: 'warning',
    })
    return
  }

  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  const para: TrainPanelSend = {
    domainId: selectedDomainId.value,
    correctedNum: counts.value.correctedCount,
    uploadNum: counts.value.uploadCount,
    originalNum: counts.value.originalCount,
    loraR: Number(formData.value.loraR),
    loraAlpha: Number(formData.value.loraAlpha),
    epochs: Number(formData.value.epochs),
    batchSize: Number(formData.value.batchSize),
    learningRate: Number(formData.value.learningRate),
    randomSeed: Number(formData.value.randomSeed),
    loraModules: formData.value.loraModules as TrainPanelSend['loraModules'],
    trainSplitRatio: Number(formData.value.trainSplitRatio),
    isOverTrain: overwriteTrain.value,
  }

  await modelApi.checkTrainData(para)
  emit('confirm')
  close()
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) fetchPanel()
  },
)

watch(
  () => selectedDomainId.value,
  () => {
    resetDomainState()
  },
)
</script>

<style lang="less" scoped>
.panel-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 24px;
}

.panel-row-single {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.row-label {
  min-width: 92px;
  font-weight: 500;
}

.slider-block {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 16px;
}

.slider-item + .slider-item {
  margin-top: 14px;
}

.slider-title {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.train-form {
  padding-top: 8px;
}

.mt16 {
  margin-top: 16px;
}
</style>
