<script setup lang="ts">
import TrainDataTable from '@/components/TrainDataTable.vue'
import TrainParaTable from '@/components/TrainParaTable.vue';
</script>

<template>
  <div class="train-data-page">
    <TrainDataTable />
  </div>
  <div class="train-data-page">
    <TrainParaTable />
  </div>
</template>

<style scoped lang="less">
.train-data-page {
  width: 100%;
  min-height: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>
