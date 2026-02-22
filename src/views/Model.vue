<script setup lang="ts">
import Infer from '@/components/Infer.vue'
import Train from '@/components/Train.vue'
</script>

<template>
  <div class="model-page">
    <div class="model-column model-column-train">
      <Train />
    </div>
    <div class="model-column model-column-infer">
      <Infer />
    </div>
  </div>
</template>

<style scoped lang="less">
.model-page {
  display: flex;
  flex-direction: row;
  gap: 24px;
  width: 100%;
  height: 100vh;
  min-height: 0;
  align-items: stretch;
}

.model-column {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 20px;
  overflow: hidden;
}
</style>