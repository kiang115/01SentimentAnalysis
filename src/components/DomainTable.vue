<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { modelApi } from '@/api/model-api'
import type { DomainDataItem, DomainDataListRec } from '@/Dto/ReceiveDto/DomainDataListRec'

const loading = ref(false)
const tableData = ref<DomainDataItem[]>([])
const sourceNameList = ref<string[]>([])

const trainDataColumnLabel = computed(() => {
  if (sourceNameList.value.length === 0) {
    return '训练数据'
  }
  return `训练数据(${sourceNameList.value.join(':')})`
})

function formatDateTime(raw?: string): string {
  if (!raw) return '—'
  return raw.replace('T', ' ')
}

function formatTrainDataCounts(counts?: number[]): string {
  if (!counts || counts.length === 0) {
    return '—'
  }
  return counts.join(':')
}

async function fetchData() {
  loading.value = true
  try {
    const res = await modelApi.listDomainData()
    const data: DomainDataListRec = res.data
    sourceNameList.value = data.sourceNameList || []
    tableData.value = data.domainDataList || []
  } finally {
    loading.value = false
  }
}

defineExpose({
  fetchData,
})

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="domain-table-card">
    <div class="section-head">
      <h3>领域详情列表</h3>
      <span>展示当前全部领域基础信息与统计数据</span>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="domainId" label="领域编号" width="110" align="center" />
      <el-table-column prop="domainName" label="领域名称" width="140" align="center" />
      <el-table-column prop="domainUrl" label="领域地址" width="140" align="center" />

      <el-table-column prop="domainImageUrl" label="领域图片" width="120" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.domainImageUrl"
            :src="row.domainImageUrl"
            :preview-src-list="[row.domainImageUrl]"
            preview-teleported
            fit="cover"
            class="domain-image"
          />
          <span v-else>—</span>
        </template>
      </el-table-column>

      <el-table-column prop="domainDescription" label="领域描述" min-width="220" show-overflow-tooltip />

      <el-table-column :label="trainDataColumnLabel" min-width="260" align="center">
        <template #default="{ row }">
          {{ formatTrainDataCounts(row.trainDataCountList) }}
        </template>
      </el-table-column>

      <el-table-column prop="commentTotal" label="评论总数" width="120" align="center" />
      <el-table-column prop="modelTotal" label="模型版本总数" width="140" align="center" />
      <el-table-column prop="merchantTotal" label="店铺总数" width="110" align="center" />

      <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped lang="less">
.domain-table-card {
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
  margin-bottom: 16px;

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

.domain-image {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #f1f5f9;
}
</style>
