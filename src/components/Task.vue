<script setup lang="ts">
import Infer from '@/components/Infer.vue'
import Train from '@/components/Train.vue'
import TaskInfoTables from '@/components/TaskInfoTables.vue'
</script>

<template>
  <div class="model-page">
    <div class="model-top">
      <TaskInfoTables />
    </div>
    <div class="model-bottom">
      <div class="model-column model-column-train">
        <Train />
      </div>
      <div class="model-column model-column-infer">
        <Infer />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.model-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: auto;
  padding: 20px;
  box-sizing: border-box;
}

.model-top {
  flex-shrink: 0;
  width: 100%;
  min-width: 0;
  overflow: hidden;
  box-sizing: border-box;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.model-bottom {
  display: flex;
  flex-direction: row;
  gap: 24px;
  flex: 1;
  width: 100%;
  min-height: 820px;
  align-items: stretch;
  box-sizing: border-box;
}

.model-column {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 20px;
  overflow: hidden;
}
</style>
