<script setup lang="ts">
import AppTopBar from '@/components/AppTopBar.vue'
</script>

<template>
  <div class="app-layout">
    <AppTopBar />
    <main class="layout-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped lang="less">
.app-layout {
  min-height: 100vh;
  background: #f3f5f9;
}

.layout-main {
  height: calc(100vh - 72px);
  margin-top: 72px;
  overflow: auto;
}
</style>
