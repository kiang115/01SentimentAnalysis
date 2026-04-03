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
    if (name === 'domainData') return 'domainData'
    if (name === 'auditCenter') return 'auditCenter'
    if (name === 'Task') return 'Task'
    return 'Task'
  },
  set(name: string) {
    router.push(`/model/${name}`)
  },
})
</script>

<template>
  <div class="model-manage-page">
    <el-tabs v-model="activeTab" class="model-tabs">
      <el-tab-pane label="训练和推理" name="Task" />
      <el-tab-pane label="审核中心" name="auditCenter" />
      <el-tab-pane label="模型数据" name="modelData" />
      <el-tab-pane label="训练数据" name="trainData" />
      <el-tab-pane label="领域扩展" name="domainData" />
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
