<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { commonApi } from '@/api/common-api'
import { userStore } from '@/stores/user'

const router = useRouter()
const store = userStore()
const logoutSubmitting = ref(false)

const systemTitle = import.meta.env.VITE_APP_TITLE

const userInfo = computed(() => store.userInfo)
const isMerchantUser = computed(() => userInfo.value.userType === 'merchant')
const isAdminUser = computed(() => userInfo.value.userType === 'admin')
const merchantEntryLabel = computed(() => (
  userInfo.value.merchantId === null ? '创建商铺' : '我的商铺'
))

function goMerchantList() {
  router.push({ name: 'merchants' })
}

function handleMerchantEntry() {
  if (!isMerchantUser.value || userInfo.value.merchantId === null) {
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
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.18);
}

.brand-button,
.nav-action,
.user-entry {
  border: none;
  background: transparent;
  cursor: pointer;
}

.brand-button {
  color: #f8fafc;
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
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.nav-action:hover {
  color: #fff;
  background: rgba(148, 163, 184, 0.18);
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
  color: #f8fafc;
  transition: background-color 0.2s ease;
}

.user-entry:hover {
  background: rgba(148, 163, 184, 0.18);
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
