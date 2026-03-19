<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { modelApi } from '@/api/model-api'
import type { MerchantsQuerySend } from '@/Dto/SendDto/MerchantsQuerySend'
import type { DomainItem, MerchantItem } from '@/Dto/ReceiveDto/MerchantDataListRec'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'

const router = useRouter()
const loading = ref(false)
const merchantList = ref<MerchantItem[]>([])
const pageInfo = ref<PageInfo<MerchantItem> | null>(null)
const domains = ref<DomainItem[]>([])
const searchText = ref('')

const queryParams = reactive<MerchantsQuerySend>({
  pageNum: 1,
  pageSize: 9,
  searchText: null,
  domainId: null,
  orderName: 'rating',
})

const sortOptions = [
  { label: '综合得分', value: 'rating' as const, icon: '★' },
  { label: '好评率', value: 'positive' as const, icon: '👍' },
  { label: '好评人数', value: 'commentNum' as const, icon: '👥' },
]

async function fetchMerchants() {
  loading.value = true
  try {
    const res = await modelApi.listMerchantData(queryParams)
    pageInfo.value = res.data.pageInfo
    merchantList.value = res.data.pageInfo.list
    domains.value = res.data.domains
  } catch {
    ElMessage.error('获取商家列表失败')
    merchantList.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.searchText = searchText.value.trim() || null
  queryParams.pageNum = 1
  fetchMerchants()
}

function selectDomain(domainId: number | null) {
  queryParams.domainId = domainId
  queryParams.pageNum = 1
  fetchMerchants()
}

function selectSort(value: 'rating' | 'positive' | 'commentNum') {
  queryParams.orderName = value
  queryParams.pageNum = 1
  fetchMerchants()
}

function handlePageChange(page: number) {
  queryParams.pageNum = page
  fetchMerchants()
}

function formatPositiveRate(rate: number) {
  return `${Math.round(rate * 100)}%`
}

function formatCommentCount(count: number) {
  if (count >= 1000) return `${(count / 1000).toFixed(1)}k+`
  return `${count}+`
}

function goMerchantDetail(merchantId: number) {
  router.push({ name: 'merchantDetail', params: { merchantId } })
}

onMounted(() => {
  fetchMerchants()
})
</script>

<template>
  <div class="merchants-page">
    <section class="query-panel">
      <el-input
        v-model="searchText"
        placeholder="搜索商家或商品..."
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>

      <div class="filter-row">
        <span class="filter-label">领域:</span>
        <div class="filter-actions">
          <button
            type="button"
            class="tag-btn"
            :class="{ active: queryParams.domainId === null }"
            @click="selectDomain(null)"
          >
            全部
          </button>
          <button
            v-for="d in domains"
            :key="d.domainId"
            type="button"
            class="tag-btn"
            :class="{ active: queryParams.domainId === d.domainId }"
            @click="selectDomain(d.domainId)"
          >
            {{ d.domainName }}
          </button>
        </div>
      </div>

      <div class="filter-row">
        <span class="filter-label">排序:</span>
        <div class="filter-actions">
          <button
            v-for="opt in sortOptions"
            :key="opt.value"
            type="button"
            class="sort-btn"
            :class="{ active: queryParams.orderName === opt.value }"
            @click="selectSort(opt.value)"
          >
            <span class="sort-icon">{{ opt.icon }}</span>
            <span>{{ opt.label }}</span>
          </button>
        </div>
      </div>
    </section>

    <div v-loading="loading" class="merchants-grid">
      <el-empty v-if="!loading && merchantList.length === 0" description="暂无商家数据" />
      <div v-else class="cards-grid">
        <article
          v-for="m in merchantList"
          :key="m.merchantId"
          class="merchant-card"
          @click="goMerchantDetail(m.merchantId)"
        >
          <div class="card-image-wrap">
            <img
              :src="m.avatarUrl || 'https://via.placeholder.com/600x360?text=No+Image'"
              :alt="m.name"
              class="card-image"
            />
            <div class="rating-badge">⭐ {{ m.rating.toFixed(1) }}</div>
          </div>

          <div class="card-body">
            <div class="card-title-row">
              <h3 class="merchant-name">{{ m.name }}</h3>
              <span class="domain-tag">{{ m.domainName }}</span>
            </div>

            <p class="merchant-desc">{{ m.description || '暂无商家介绍' }}</p>

            <div class="card-metrics">
              <span class="metric positive">👍 {{ formatPositiveRate(m.positiveRate) }}</span>
              <span class="metric count">👥 {{ formatCommentCount(m.commentCount) }}</span>
              <span class="detail-link">查看详情</span>
            </div>
          </div>
        </article>
      </div>
    </div>

    <div v-if="pageInfo && pageInfo.total > 0" class="pagination-wrapper">
      <el-pagination
        :current-page="pageInfo.pageNum"
        :page-size="pageInfo.pageSize"
        :total="pageInfo.total"
        layout="total, prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
.merchants-page {
  width: 100%;
  min-height: 100%;
  padding: 16px;
  box-sizing: border-box;
  background: #f3f5f9;
}

.query-panel {
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
}

.search-input {
  margin-bottom: 14px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.filter-label {
  width: 40px;
  color: #606266;
  font-size: 14px;
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-btn,
.sort-btn {
  border: none;
  border-radius: 8px;
  padding: 7px 12px;
  font-size: 13px;
  color: #4b5563;
  background: #f2f4f7;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: #2563eb;
    background: #eaf1ff;
  }

  &.active {
    color: #fff;
    background: #2563eb;
  }
}

.sort-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.merchants-grid {
  min-height: 220px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.merchant-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
  }
}

.card-image-wrap {
  position: relative;
  height: 172px;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rating-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #1f2937;
  background: rgba(255, 255, 255, 0.92);
}

.card-body {
  padding: 12px 14px 14px;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.merchant-name {
  margin: 0;
  font-size: 16px;
  line-height: 1.3;
  font-weight: 700;
  color: #1f2937;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.domain-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #2563eb;
  background: #ebf3ff;
}

.merchant-desc {
  min-height: 40px;
  margin: 0 0 10px;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-metrics {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.metric.positive {
  color: #16a34a;
}

.metric.count {
  color: #6b7280;
}

.detail-link {
  margin-left: auto;
  color: #2563eb;
  cursor: pointer;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .cards-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
