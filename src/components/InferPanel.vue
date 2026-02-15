<template>
  <el-dialog
    v-model="dialogVisible"
    title="推理信息配置"
    width="640px"
    destroy-on-close
    @closed="onClosed"
  >
    <div v-if="loading" class="panel-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <template v-else-if="configList.length">
      <div class="panel-sort">
        <span class="sort-label">评论排序：</span>
        <el-select v-model="sort" placeholder="排序方式" style="width: 160px">
          <el-option label="按最新评论" value="newest" />
          <el-option label="按最旧评论" value="lastest" />
        </el-select>
      </div>
      <div
        v-for="row in configWithState"
        :key="row.item.domainId"
        class="panel-row"
        :class="{ 'row-disabled': row.item.uninferencedCommentNums === 0 }"
      >
        <div class="row-label">{{ row.item.domainName }}</div>
        <div class="row-slider">
          <el-slider
            v-model="row.state.commentCount"
            :min="0"
            :max="row.item.uninferencedCommentNums"
            :disabled="row.item.uninferencedCommentNums === 0"
            show-input
          />
          <span v-if="row.item.uninferencedCommentNums === 0" class="hint">无可推理评论</span>
        </div>
        <div class="row-select">
          <el-select
            v-model="row.state.selectedModelId"
            placeholder="选择模型"
            :disabled="row.item.uninferencedCommentNums === 0"
            style="width: 100%"
          >
            <el-option
              v-for="m in row.item.domainModelsDataList"
              :key="m.modelId"
              :label="`v${m.modelVersion}`"
              :value="m.modelId"
            />
          </el-select>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无配置数据" />

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="handleConfirm">点击推理</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'

interface DomainModelItem {
  modelId: number
  modelVersion: number
}

interface InferenceConfigItem {
  domainId: number
  domainName: string
  uninferencedCommentNums: number
  domainModelsDataList: DomainModelItem[]
}

interface InferencePanelData {
  inferenceConfigDataList: InferenceConfigItem[]
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

const configList = ref<InferenceConfigItem[]>([])
const rowState = ref<Array<{ commentCount: number; selectedModelId: number }>>([])
const loading = ref(false)
const sort = ref<'newest' | 'lastest'>('newest')

const configWithState = computed(() =>
  configList.value.map((item, i) => ({
    item,
    state: rowState.value[i]!,
  }))
)

function close() {
  emit('update:modelValue', false)
}

function onClosed() {
  configList.value = []
  rowState.value = []
}

async function fetchPanel() {
  loading.value = true
  try {
    const res = await modelApi.listInferPanel()
    const data = (res as { data: InferencePanelData }).data
    const list = data?.inferenceConfigDataList ?? []
    configList.value = list
    rowState.value = list.map((item) => {
      const first = item.domainModelsDataList?.[0]
      return {
        commentCount: 0,
        selectedModelId: first != null ? first.modelId : 0,
      }
    })
  } catch {
    configList.value = []
    rowState.value = []
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  const inferenceDomainPara = configWithState.value
    .map((row) => ({
      domainId: row.item.domainId,
      modelId: row.state.selectedModelId,
      inferenceReviewNums: row.state.commentCount,
    }))
    .filter((item) => item.inferenceReviewNums > 0)
  if (inferenceDomainPara.length === 0) {
    ElMessage.warning('请至少为一个领域设置推理数量')
    return
  }
  try {
    const res = await modelApi.checkInferData({ inferenceDomainPara, sort: sort.value })
    ElMessage.success((res as { message?: string }).message ?? '操作成功')
    close()
    emit('confirm')
  } catch {
    close()
    emit('confirm')
  }
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) fetchPanel()
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
.panel-sort {
  margin-bottom: 16px;
}
.panel-sort .sort-label {
  margin-right: 8px;
}
.panel-row {
  display: grid;
  grid-template-columns: 100px 1fr 160px;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}
.panel-row.row-disabled {
  opacity: 0.6;
}
.row-label {
  font-weight: 500;
}
.row-slider {
  min-width: 0;
}
.row-slider .hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.row-select {
  min-width: 0;
}
</style>
