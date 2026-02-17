<template>
  <el-descriptions class="margin-top" title="训练任务" :column="3" border>
    <template #extra>
      <el-button type="primary" @click="panelVisible = true">开始训练</el-button>
    </template>
  </el-descriptions>

  <TrainPanel v-model="panelVisible" @confirm="onTrainConfirm" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import TrainPanel from './TrainPanel.vue'

const panelVisible = ref(false)

function onTrainConfirm() {
  ElMessage.success('训练参数已确认')
}
</script>
