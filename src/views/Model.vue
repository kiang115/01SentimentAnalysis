<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeTab = computed({
  get() {
    const name = route.name as string
    if (name === 'trainData') return 'trainData'
    if (name === 'modelData') return 'modelData'
    if (name === 'Task') return 'Task'
    return 'trainData'
  },
  set(name: string) {
    router.push(`/model/${name}`)
  },
})
</script>

<template>
  <div class="model-manage-page">
    <div class="page-header">
      <h1 class="page-title">模型管理</h1>
      <p class="page-subtitle">管理训练数据、模型训练和推理、模型版本及效果</p>
    </div>
    <el-tabs v-model="activeTab" class="model-tabs">
      <el-tab-pane label="训练数据管理" name="trainData" />
      <el-tab-pane label="模型数据管理" name="modelData" />
      <el-tab-pane label="训练和推理管理" name="Task" />
    </el-tabs>
    <div class="page-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped lang="less">
.model-manage-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 20px;
  box-sizing: border-box;
}
.page-header {
  flex-shrink: 0;
  margin-bottom: 16px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px;
}
.page-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}
.model-tabs {
  flex-shrink: 0;
  margin-bottom: 16px;
}
.model-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}
.page-content {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
</style>
