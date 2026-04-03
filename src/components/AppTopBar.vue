<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { modelApi } from '@/api/model-api'
import { commonApi } from '@/api/common-api'
import { userStore } from '@/stores/user'
import type { MerchantCreateRec } from '@/Dto/SendDto/MerchantCreateSend'
import type { DomainItem } from '@/Dto/ReceiveDto/DomainsInfoRec'

const router = useRouter()
const store = userStore()
const logoutSubmitting = ref(false)
const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const domainLoading = ref(false)
const merchantAvatarUploading = ref(false)
const merchantAvatarInputRef = ref<HTMLInputElement>()
const createFormRef = ref()
const domains = ref<DomainItem[]>([])

const systemTitle = import.meta.env.VITE_APP_TITLE

const userInfo = computed(() => store.userInfo)
const isMerchantUser = computed(() => userInfo.value.userType === 'merchant')
const isAdminUser = computed(() => userInfo.value.userType === 'admin')
const merchantEntryLabel = computed(() => (
  userInfo.value.merchantId === null ? '创建商铺' : '我的商铺'
))
type MerchantCreateForm = Omit<MerchantCreateRec, 'domainId'> & {
  domainId: number | null
}

const createMerchantForm = ref<MerchantCreateForm>({
  avatarUrl: '',
  name: '',
  description: '',
  domainId: null,
})
const createMerchantRules = {
  avatarUrl: [{ required: true, message: '请上传商铺头像', trigger: 'change' }],
  name: [{ required: true, message: '请输入商铺名字', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商铺描述', trigger: 'blur' }],
  domainId: [{ required: true, message: '请选择商铺领域', trigger: 'change' }],
}

function goMerchantList() {
  router.push({ name: 'merchants' })
}

function resetCreateMerchantForm() {
  createMerchantForm.value = {
    avatarUrl: '',
    name: '',
    description: '',
    domainId: null,
  }
  merchantAvatarUploading.value = false
  createFormRef.value?.resetFields()
}

async function openCreateMerchantDialog() {
  domainLoading.value = true
  try {
    const res = await modelApi.queryDomainsInfo()
    domains.value = res.data
    resetCreateMerchantForm()
    createDialogVisible.value = true
  } finally {
    domainLoading.value = false
  }
}

async function handleMerchantEntry() {
  if (!isMerchantUser.value) {
    return
  }

  if (userInfo.value.merchantId === null) {
    await openCreateMerchantDialog()
    return
  }

  router.push({
    name: 'merchantDetail',
    params: { merchantId: userInfo.value.merchantId },
  })
}

function goModelPage() {
  router.push('/model')
}

function openMerchantAvatarPicker() {
  if (merchantAvatarUploading.value) {
    return
  }

  merchantAvatarInputRef.value?.click()
}

async function handleMerchantAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  merchantAvatarUploading.value = true
  try {
    const res = await modelApi.uploadFileToOss(formData)
    createMerchantForm.value.avatarUrl = res.data
    createFormRef.value?.validateField('avatarUrl')
  } finally {
    merchantAvatarUploading.value = false
    input.value = ''
  }
}

async function submitCreateMerchant() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (createMerchantForm.value.domainId == null) return

  createSubmitting.value = true
  try {
    const payload: MerchantCreateRec = {
      ...createMerchantForm.value,
      domainId: createMerchantForm.value.domainId,
    }
    const res = await modelApi.addMerchant(payload)
    store.updateMerchantId(res.data.merchantId)
    createDialogVisible.value = false
    resetCreateMerchantForm()
  } finally {
    createSubmitting.value = false
  }
}

async function handleUserCommand(command: string) {
  if (command !== 'logout' || logoutSubmitting.value) {
    return
  }

  logoutSubmitting.value = true
  try {
    if (userInfo.value.userId === null) {
      ElMessage.error('用户信息缺失，请重新登录')
      return
    }

    await commonApi.logout({ userId: userInfo.value.userId })
  } catch {
  } finally {
    store.loginOut()
    logoutSubmitting.value = false
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <header class="app-top-bar">
    <button class="brand-button" type="button" @click="goMerchantList">
      {{ systemTitle }}
    </button>

    <nav class="top-actions">
      <button
        v-if="isMerchantUser"
        type="button"
        class="nav-action"
        :class="{ placeholder: userInfo.merchantId === null }"
        @click="handleMerchantEntry"
      >
        {{ merchantEntryLabel }}
      </button>

      <button
        v-if="isAdminUser"
        type="button"
        class="nav-action"
        @click="goModelPage"
      >
        模型管理
      </button>

      <el-dropdown trigger="click" @command="handleUserCommand">
        <button type="button" class="user-entry">
          <el-avatar :size="36" :src="userInfo.avatarUrl" />
          <span class="user-name">{{ userInfo.userName }}</span>
          <el-icon class="user-arrow"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout" :disabled="logoutSubmitting">
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>
  </header>

  <el-dialog
    v-model="createDialogVisible"
    title="创建商铺"
    width="560px"
    destroy-on-close
    @closed="resetCreateMerchantForm"
  >
    <el-form
      ref="createFormRef"
      v-loading="domainLoading"
      :model="createMerchantForm"
      :rules="createMerchantRules"
      label-width="100px"
    >
      <el-form-item label="商铺头像" prop="avatarUrl">
        <div
          class="upload-box"
          :class="{ filled: !!createMerchantForm.avatarUrl }"
          @click="openMerchantAvatarPicker"
        >
          <img
            v-if="createMerchantForm.avatarUrl"
            :src="createMerchantForm.avatarUrl"
            alt="商铺头像"
            class="upload-preview"
          />
          <div v-else class="upload-placeholder">
            <el-icon><Plus /></el-icon>
            <span>点击上传头像</span>
          </div>
          <div v-if="merchantAvatarUploading" class="upload-mask">上传中...</div>
        </div>
        <input
          ref="merchantAvatarInputRef"
          class="hidden-file-input"
          type="file"
          accept="image/*"
          @change="handleMerchantAvatarChange"
        />
      </el-form-item>

      <el-form-item label="商铺名字" prop="name">
        <el-input v-model="createMerchantForm.name" placeholder="请输入商铺名字" />
      </el-form-item>

      <el-form-item label="商铺描述" prop="description">
        <el-input
          v-model="createMerchantForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入商铺描述"
        />
      </el-form-item>

      <el-form-item label="商铺领域" prop="domainId">
        <el-select v-model="createMerchantForm.domainId" placeholder="请选择商铺领域" style="width: 100%">
          <el-option
            v-for="domain in domains"
            :key="domain.domainId"
            :label="domain.domainName"
            :value="domain.domainId"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="submitCreateMerchant">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.app-top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 72px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(18px);
  -webkit-backdrop-filter: saturate(180%) blur(18px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.brand-button,
.nav-action,
.user-entry {
  border: none;
  background: transparent;
  cursor: pointer;
}

.brand-button {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-action {
  height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.nav-action:hover {
  color: #0f172a;
  background: rgba(15, 23, 42, 0.06);
}

.nav-action.placeholder {
  color: #94a3b8;
}

.nav-action.placeholder:hover {
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.12);
}

.user-entry {
  height: 44px;
  max-width: 220px;
  padding: 4px 8px 4px 4px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #0f172a;
  transition: background-color 0.2s ease;
}

.user-entry:hover {
  background: rgba(15, 23, 42, 0.05);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 14px;
}

.upload-box {
  position: relative;
  width: 100%;
  height: 180px;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
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
  gap: 8px;
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

@media (max-width: 768px) {
  .app-top-bar {
    padding: 0 12px;
  }

  .top-actions {
    gap: 8px;
    overflow-x: auto;
    justify-content: flex-end;
  }

  .brand-button {
    flex-shrink: 0;
    font-size: 18px;
  }

  .nav-action,
  .user-entry {
    flex-shrink: 0;
  }

  .user-entry {
    max-width: 160px;
  }
}
</style>
