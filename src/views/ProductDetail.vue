<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { modelApi } from '@/api/model-api.ts'
import { userStore } from '@/stores/user'
import type { ProductDetailRec } from '@/Dto/ReceiveDto/ProductDetailRec.ts'
import ProductTagAnalysisSection from '@/components/ProductTagAnalysisSection.vue'
import ProductCommentsSection from '@/components/ProductCommentsSection.vue'

const route = useRoute()
const router = useRouter()
const store = userStore()

const loading = ref(false)
const detail = ref<ProductDetailRec | null>(null)

const scoreStars = computed(() => {
  if (!detail.value || detail.value.rating === undefined) return ''
  const count = Math.max(1, Math.min(5, Math.round(detail.value.rating)))
  return '★'.repeat(count) + '☆'.repeat(5 - count)
})

const isAdmin = computed(() => store.userInfo.userType === 'admin')
const isConsumer = computed(() => store.userInfo.userType === 'consumer')
const isMerchantOwner = computed(() => {
  if (store.userInfo.userType !== 'merchant') {
    return false
  }
  if (detail.value == null) {
    return false
  }
  return store.userInfo.merchantId === detail.value.merchantId
})
const canAnalyzeTags = computed(() => isAdmin.value || isMerchantOwner.value)
const canViewCommentInference = computed(() => isAdmin.value || isMerchantOwner.value)
const canSendComment = computed(() => isAdmin.value || isConsumer.value)

function parseProductId() {
  const rawId = Array.isArray(route.params.productId)
    ? route.params.productId[0]
    : route.params.productId
  const productId = Number(rawId)
  if (!Number.isFinite(productId) || productId <= 0) return null
  return productId
}

async function fetchProductDetail() {
  const productId = parseProductId()
  if (productId === null) {
    ElMessage.error('商品ID无效')
    router.replace({ name: 'merchants' })
    return
  }

  loading.value = true
  try {
    const res = await modelApi.getProductDetailData(productId)
    detail.value = res.data
  } catch {
    detail.value = null
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

function formatPositiveRate(rate: number) {
  const value = rate <= 1 ? rate * 100 : rate
  return `${value.toFixed(1)}%`
}

function formatCommentCount(count: number) {
  return count.toLocaleString()
}

function formatPrice(price: number) {
  return `¥${price.toFixed(2)}`
}

function goMerchantDetail() {
  if (!detail.value) return
  router.push({ name: 'merchantDetail', params: { merchantId: detail.value.merchantId } })
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<template>
  <div class="product-detail-page" v-loading="loading">
    <template v-if="detail">
      <section class="overview-card">
        <img
          :src="detail.imageUrl || 'https://via.placeholder.com/640x420?text=No+Image'"
          :alt="detail.name"
          class="product-cover"
        />

        <div class="overview-main">
          <div class="title-row">
            <h1 class="product-title">{{ detail.name }}</h1>
            <button type="button" class="merchant-link" @click="goMerchantDetail">店铺：{{ detail.merchantName }}</button>
          </div>

          <div class="rating-row">
            <span v-if="detail.rating !== undefined" class="star-text">{{ scoreStars }}</span>
            <span v-if="detail.rating !== undefined" class="rating-text">{{ detail.rating.toFixed(1) }}</span>
            <span v-if="detail.positiveRate !== undefined" class="meta-text">好评率 {{ formatPositiveRate(detail.positiveRate) }}</span>
<!--            <span v-if="detail.commentCount !== undefined" class="meta-text">{{ formatCommentCount(detail.commentCount) }}人评价</span>-->
          </div>

          <div class="metrics-grid">
            <article v-if="detail.rating !== undefined" class="metric-card">
              <p class="metric-label">综合得分</p>
              <p class="metric-value blue">{{ detail.rating.toFixed(1) }}</p>
            </article>

            <article v-if="detail.positiveRate !== undefined" class="metric-card">
              <p class="metric-label">好评率</p>
              <p class="metric-value green">{{ formatPositiveRate(detail.positiveRate) }}</p>
            </article>

            <article v-if="detail.commentCount !== undefined" class="metric-card">
              <p class="metric-label">评论人数</p>
              <p class="metric-value">{{ formatCommentCount(detail.commentCount) }}</p>
            </article>

            <article v-if="detail.price !== undefined" class="metric-card">
              <p class="metric-label">价格</p>
              <p class="metric-value orange">{{ formatPrice(detail.price) }}</p>
            </article>
          </div>

          <section class="description-card">
            <h2 class="section-title">商品详情</h2>
            <p class="description-text">{{ detail.details || '暂无商品详情' }}</p>
          </section>
        </div>
      </section>

      <ProductTagAnalysisSection :product-id="detail.productId" :can-analyze-tags="canAnalyzeTags" />

      <ProductCommentsSection
        :product-id="detail.productId"
        :domain-id="detail.domainId"
        :can-send-comment="canSendComment"
        :can-view-inference-tags="canViewCommentInference"
      />
    </template>

    <el-empty v-else-if="!loading" description="暂无商品详情数据" />
  </div>
</template>

<style scoped lang="less">
.product-detail-page {
  min-height: 100%;
  padding: 20px;
  background: #f3f5f9;
  box-sizing: border-box;
}

.overview-card {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 28px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
}

.product-cover {
  width: 100%;
  height: 290px;
  border-radius: 10px;
  object-fit: cover;
}

.overview-main {
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.product-title {
  margin: 0 0 10px;
  font-size: 42px;
  line-height: 1.2;
  color: #1f2937;
}

.rating-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 16px;
}

.star-text {
  color: #f59e0b;
  letter-spacing: 2px;
}

.rating-text {
  color: #334155;
  font-weight: 600;
}

.meta-text {
  color: #64748b;
}

.merchant-link {
  flex-shrink: 0;
  border: none;
  background: #eff6ff;
  padding: 6px 12px;
  border-radius: 8px;
  color: #2563eb;
  font-size: 14px;
  line-height: 1.2;
  cursor: pointer;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.metric-label {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 13px;
}

.metric-value {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
  color: #1f2937;
  font-weight: 700;
}

.metric-value.blue {
  color: #2563eb;
}

.metric-value.green {
  color: #16a34a;
}

.metric-value.orange {
  color: #ea580c;
}

.description-card {
  margin-top: 14px;
  padding: 16px;
  border-radius: 10px;
  background: #f3f4f6;
}

.section-title {
  margin: 0 0 12px;
  color: #1f2937;
  font-size: 28px;
}

.description-text {
  margin: 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 1200px) {
  .overview-card {
    grid-template-columns: 1fr;
  }

  .product-cover {
    height: 260px;
  }

  .product-title {
    font-size: 34px;
  }

  .metric-value {
    font-size: 26px;
  }

  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .product-detail-page {
    padding: 14px;
  }

  .overview-card,
  .description-card {
    padding: 14px;
  }

  .product-title {
    font-size: 28px;
  }

  .title-row {
    flex-direction: column;
    gap: 8px;
  }

  .rating-row {
    font-size: 14px;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .section-title {
    font-size: 22px;
  }

  .description-text {
    font-size: 14px;
  }
}
</style>
