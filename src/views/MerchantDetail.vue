<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { modelApi } from '@/api/model-api.ts'
import { userStore } from '@/stores/user'
import type { MerchantDetailRec } from '@/Dto/ReceiveDto/MerchantDetailRec.ts'
import type { ProductAddSend } from '@/Dto/SendDto/ProductAddSend.ts'
import type { ProductEditSend } from '@/Dto/SendDto/ProductEditSend.ts'

const route = useRoute()
const router = useRouter()
const store = userStore()

const loading = ref(false)
const detail = ref<MerchantDetailRec | null>(null)
const addDialogVisible = ref(false)
const addSubmitting = ref(false)
const imageUploading = ref(false)
const imageFileInputRef = ref<HTMLInputElement>()
const addFormRef = ref()
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editImageUploading = ref(false)
const editImageFileInputRef = ref<HTMLInputElement>()
const editFormRef = ref()
type ProductAddForm = Omit<ProductAddSend, 'price'> & { price: number | null }
type ProductEditForm = Omit<ProductEditSend, 'price'> & { price: number | null }

const addForm = reactive<ProductAddForm>({
  productName: '',
  productDetail: '',
  imageUrl: '',
  price: null,
  merchantId: 0,
})
const editForm = reactive<ProductEditForm>({
  productId: 0,
  productName: '',
  productDetail: '',
  imageUrl: '',
  price: null,
})

const addFormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productDetail: [{ required: true, message: '请输入商品详细信息', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传商品图片', trigger: 'change' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    {
      trigger: 'blur',
      validator: (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        const priceText = String(value ?? '').trim()
        const priceNum = Number(priceText)
        if (!Number.isFinite(priceNum) || priceNum <= 0) {
          callback(new Error('价格必须大于0'))
          return
        }
        if (!/^\d+(\.\d{1,2})?$/.test(priceText)) {
          callback(new Error('价格最多保留两位小数'))
          return
        }
        callback()
      },
    },
  ],
}
const editFormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productDetail: [{ required: true, message: '请输入商品详细信息', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传商品图片', trigger: 'change' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    {
      trigger: 'blur',
      validator: (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        const priceText = String(value ?? '').trim()
        const priceNum = Number(priceText)
        if (!Number.isFinite(priceNum) || priceNum <= 0) {
          callback(new Error('价格必须大于0'))
          return
        }
        if (!/^\d+(\.\d{1,2})?$/.test(priceText)) {
          callback(new Error('价格最多保留两位小数'))
          return
        }
        callback()
      },
    },
  ],
}

const canManageMerchantProducts = computed(() => {
  if (store.userInfo.userType !== 'merchant') {
    return false
  }
  if (detail.value == null) {
    return false
  }
  return store.userInfo.merchantId === detail.value.merchantId
})

function parseMerchantId() {
  const rawId = Array.isArray(route.params.merchantId)
    ? route.params.merchantId[0]
    : route.params.merchantId
  const merchantId = Number(rawId)
  if (!Number.isFinite(merchantId) || merchantId <= 0) return null
  return merchantId
}

async function fetchMerchantDetail() {
  const merchantId = parseMerchantId()
  if (merchantId === null) {
    ElMessage.error('商铺ID无效')
    router.replace({ name: 'merchants' })
    return
  }

  loading.value = true
  try {
    const res = await modelApi.getMerchantDetailData(merchantId)
    detail.value = res.data
  } catch {
    detail.value = null
    ElMessage.error('获取商铺详情失败')
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

function goProductDetail(productId: number) {
  router.push({ name: 'productDetail', params: { productId } })
}

function resetAddForm() {
  addForm.productName = ''
  addForm.productDetail = ''
  addForm.imageUrl = ''
  addForm.price = null
  addForm.merchantId = detail.value?.merchantId ?? 0
  imageUploading.value = false
  addFormRef.value?.resetFields()
}

function openAddDialog() {
  if (!canManageMerchantProducts.value) {
    return
  }
  resetAddForm()
  addDialogVisible.value = true
}

function openImagePicker() {
  imageFileInputRef.value?.click()
}

async function handleImageChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  imageUploading.value = true
  try {
    const res = await modelApi.uploadFileToOss(formData)
    addForm.imageUrl = res.data
    addFormRef.value?.validateField('imageUrl')
  } finally {
    imageUploading.value = false
    ;(event.target as HTMLInputElement).value = ''
  }
}

async function submitAddForm() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (addForm.price == null) return
  const payload: ProductAddSend = {
    ...addForm,
    price: addForm.price,
  }
  addSubmitting.value = true
  try {
    await modelApi.postProductAddData(payload)
    addDialogVisible.value = false
    resetAddForm()
    await fetchMerchantDetail()
  } finally {
    addSubmitting.value = false
  }
}

function resetEditForm() {
  editForm.productId = 0
  editForm.productName = ''
  editForm.productDetail = ''
  editForm.imageUrl = ''
  editForm.price = null
  editImageUploading.value = false
  editFormRef.value?.resetFields()
}

function openEditDialog(item: MerchantDetailRec['products'][number]) {
  if (!canManageMerchantProducts.value) {
    return
  }
  editForm.productId = item.productId
  editForm.productName = item.name
  editForm.productDetail = item.details
  editForm.imageUrl = item.imageUrl ?? ''
  editForm.price = item.price
  editDialogVisible.value = true
}

function openEditImagePicker() {
  editImageFileInputRef.value?.click()
}

async function handleEditImageChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  editImageUploading.value = true
  try {
    const res = await modelApi.uploadFileToOss(formData)
    editForm.imageUrl = res.data
    editFormRef.value?.validateField('imageUrl')
  } finally {
    editImageUploading.value = false
    ;(event.target as HTMLInputElement).value = ''
  }
}

async function submitEditForm() {
  const valid = await editFormRef.value?.validate()
  if (!valid) return
  if (editForm.price == null) return
  const payload: ProductEditSend = {
    ...editForm,
    price: editForm.price,
  }
  editSubmitting.value = true
  try {
    await modelApi.postProductEditData(payload)
    editDialogVisible.value = false
    resetEditForm()
    await fetchMerchantDetail()
  } finally {
    editSubmitting.value = false
  }
}

async function handleDeleteProduct(productId: number) {
  if (!canManageMerchantProducts.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确认删除该商品吗？', '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await modelApi.postProductDeleteData(productId)
  await fetchMerchantDetail()
}

onMounted(() => {
  fetchMerchantDetail()
})
</script>

<template>
  <div class="merchant-detail-page" v-loading="loading">
    <template v-if="detail">
      <section class="merchant-header">
        <img
          :src="detail.avatarUrl || 'https://via.placeholder.com/240x180?text=No+Image'"
          :alt="detail.name"
          class="merchant-cover"
        />
        <div class="merchant-main">
          <h1 class="merchant-name">{{ detail.name }}</h1>
          <p v-if="detail.description" class="merchant-desc">{{ detail.description }}</p>
        </div>
        <div v-if="detail.rating !== undefined" class="rating-pill">
          <span class="star">★</span>
          <span class="score">{{ detail.rating.toFixed(1) }}</span>
          <span>综合得分</span>
        </div>
      </section>

      <section class="metrics-grid">
        <article v-if="detail.commentCount !== undefined" class="metric-card">
          <p class="metric-value">{{ formatCommentCount(detail.commentCount) }}</p>
          <p class="metric-label">总评价人数</p>
        </article>
        <article v-if="detail.positiveRate !== undefined" class="metric-card">
          <p class="metric-value">{{ formatPositiveRate(detail.positiveRate) }}</p>
          <p class="metric-label">好评率</p>
        </article>
        <article v-if="detail.rating !== undefined" class="metric-card">
          <p class="metric-value">{{ detail.rating.toFixed(1) }}</p>
          <p class="metric-label">综合得分</p>
        </article>
      </section>

      <section class="product-section">
        <div class="section-head">
          <h2 class="section-title">商品列表</h2>
          <button v-if="canManageMerchantProducts" class="add-icon-btn" type="button" @click.stop="openAddDialog">
            <el-icon><Plus /></el-icon>
          </button>
        </div>
        <el-empty v-if="!detail.products || detail.products.length === 0" description="暂无商品数据" />
        <div v-else class="product-grid">
          <article
            v-for="item in detail.products"
            :key="item.productId"
            class="product-card"
            @click="goProductDetail(item.productId)"
          >
            <img
              :src="item.imageUrl || 'https://via.placeholder.com/600x360?text=No+Image'"
              :alt="item.name"
              class="product-image"
            />
            <div class="product-body">
              <h3 class="product-name">{{ item.name }}</h3>
              <div class="product-meta">
                <span v-if="item.rating !== undefined" class="meta-score">★ {{ item.rating.toFixed(1) }}</span>
                <span v-if="item.commentCount !== undefined" class="meta-comments">({{ formatCommentCount(item.commentCount) }}条评价)</span>
              </div>
              <p v-if="item.details" class="product-desc">{{ item.details }}</p>
                <div class="product-foot">
                  <p v-if="item.price !== undefined" class="product-price">{{ formatPrice(item.price) }}</p>
                  <div v-if="canManageMerchantProducts" class="card-actions">
                    <button class="action-icon-btn action-edit-btn" type="button" @click.stop="openEditDialog(item)">
                      <el-icon><Edit /></el-icon>
                    </button>
                    <button class="action-icon-btn action-delete-btn" type="button" @click.stop="handleDeleteProduct(item.productId)">
                      <el-icon><Delete /></el-icon>
                    </button>
                  </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <el-dialog v-if="canManageMerchantProducts" v-model="addDialogVisible" title="添加商品" width="560px" destroy-on-close>
        <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
          <el-form-item label="商品图片" prop="imageUrl">
            <div class="upload-box" :class="{ filled: !!addForm.imageUrl }" @click="openImagePicker">
              <img v-if="addForm.imageUrl" :src="addForm.imageUrl" alt="商品图片" class="upload-preview" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>点击上传图片</span>
              </div>
              <div v-if="imageUploading" class="upload-mask">上传中...</div>
            </div>
            <input
              ref="imageFileInputRef"
              class="hidden-file-input"
              type="file"
              accept="image/*"
              @change="handleImageChange"
            />
          </el-form-item>
          <el-form-item label="商品名称" prop="productName">
            <el-input v-model="addForm.productName" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品详情" prop="productDetail">
            <el-input v-model="addForm.productDetail" type="textarea" :rows="3" placeholder="请输入商品详细信息" />
          </el-form-item>
          <el-form-item label="商品价格" prop="price">
            <el-input v-model.number="addForm.price" placeholder="请输入价格（最多两位小数）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="addDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="addSubmitting" @click="submitAddForm">提交</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-if="canManageMerchantProducts" v-model="editDialogVisible" title="编辑商品" width="560px" destroy-on-close @closed="resetEditForm">
        <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="100px">
          <el-form-item label="商品图片" prop="imageUrl">
            <div class="upload-box" :class="{ filled: !!editForm.imageUrl }" @click="openEditImagePicker">
              <img v-if="editForm.imageUrl" :src="editForm.imageUrl" alt="商品图片" class="upload-preview" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>点击上传图片</span>
              </div>
              <div v-if="editImageUploading" class="upload-mask">上传中...</div>
            </div>
            <input
              ref="editImageFileInputRef"
              class="hidden-file-input"
              type="file"
              accept="image/*"
              @change="handleEditImageChange"
            />
          </el-form-item>
          <el-form-item label="商品名称" prop="productName">
            <el-input v-model="editForm.productName" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品详情" prop="productDetail">
            <el-input v-model="editForm.productDetail" type="textarea" :rows="3" placeholder="请输入商品详细信息" />
          </el-form-item>
          <el-form-item label="商品价格" prop="price">
            <el-input v-model.number="editForm.price" placeholder="请输入价格（最多两位小数）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="editSubmitting" @click="submitEditForm">提交</el-button>
          </div>
        </template>
      </el-dialog>
    </template>
    <el-empty v-else-if="!loading" description="暂无商铺详情数据" />
  </div>
</template>

<style scoped lang="less">
.merchant-detail-page {
  min-height: 100%;
  padding: 20px;
  background: #f3f5f9;
  box-sizing: border-box;
}

.merchant-header {
  display: grid;
  grid-template-columns: 130px 1fr auto;
  gap: 20px;
  align-items: start;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.merchant-cover {
  width: 130px;
  height: 100px;
  border-radius: 10px;
  object-fit: cover;
}

.merchant-name {
  margin: 0 0 12px;
  font-size: 24px;
  line-height: 1.2;
  color: #1f2937;
  font-weight: 700;
}

.merchant-desc {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.rating-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 18px;
  background: #e8f0ff;
  color: #334155;
  font-size: 13px;
  white-space: nowrap;
}

.star {
  color: #f59e0b;
}

.score {
  color: #2563eb;
  font-weight: 700;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  border-radius: 12px;
  background: #fff;
  padding: 20px;
}

.metric-value {
  margin: 0 0 8px;
  font-size: 22px;
  line-height: 1.1;
  font-weight: 700;
  color: #1f2937;
}

.metric-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  color: #1f2937;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.add-icon-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: #2196f3;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.28);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.product-card {
  cursor: pointer;
  overflow: hidden;
  border-radius: 12px;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.product-image {
  width: 100%;
  height: 210px;
  object-fit: cover;
}

.product-body {
  padding: 14px 16px 16px;
}

.product-name {
  margin: 0 0 8px;
  font-size: 18px;
  color: #1f2937;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 14px;
}

.meta-score {
  color: #2563eb;
  font-weight: 600;
}

.meta-comments {
  color: #6b7280;
}

.product-desc {
  margin: 0 0 12px;
  color: #4b5563;
  line-height: 1.6;
  font-size: 14px;
}

.product-price {
  margin: 0;
  color: #2563eb;
  font-size: 18px;
  font-weight: 700;
}

.product-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
}

.card-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
}

.action-edit-btn {
  background: #f4b400;
}

.action-delete-btn {
  background: #ef4444;
}

.upload-box {
  position: relative;
  width: 180px;
  height: 120px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.upload-box.filled {
  border-style: solid;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #64748b;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.hidden-file-input {
  display: none;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1200px) {
  .merchant-name {
    font-size: 22px;
  }

  .metric-value,
  .section-title,
  .product-name,
  .product-price {
    font-size: 18px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .merchant-detail-page {
    padding: 14px;
  }

  .merchant-header {
    grid-template-columns: 1fr;
  }

  .merchant-cover {
    width: 100%;
    height: 200px;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
