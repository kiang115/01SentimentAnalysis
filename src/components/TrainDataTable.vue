<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model-api'
import type { DataItem, DomainItem } from '@/Dto/ReceiveDto/TrainDataListRec'
import type { TrainDataQuerySend } from '@/Dto/SendDto/TrainDataQuerySend'
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
