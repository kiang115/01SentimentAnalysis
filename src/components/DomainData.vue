<script setup lang="ts">
import { ref } from 'vue'
import DomainAddForm from '@/components/DomainAddForm.vue'
import DomainTable from '@/components/DomainTable.vue'

const domainTableRef = ref<InstanceType<typeof DomainTable>>()

function handleAddSuccess() {
  domainTableRef.value?.fetchData()
}
</script>

<template>
  <div class="domain-data-page">
    <DomainTable ref="domainTableRef" />
    <DomainAddForm @success="handleAddSuccess" />
  </div>
</template>

<style scoped lang="less">
.domain-data-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  min-height: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>
