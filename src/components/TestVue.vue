<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { modelApi } from "@/api/model-api";

const loading = ref(false);
const selectedFile = ref<File | null>(null);

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw ?? null;
};

const handleSubmit = async () => {
  if (!selectedFile.value) {
    ElMessage.warning("请先选择图片");
    return;
  }

  const formData = new FormData();
  formData.append("file", selectedFile.value);

  loading.value = true;
  try {
    const res = await modelApi.uploadFileToOss(formData);
    ElMessage.success(`上传成功，URL: ${res.data}`);
  } catch {
    ElMessage.error("上传失败");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="test-upload-page">
    <el-card class="upload-card">
      <h2>图片上传测试</h2>
      <el-upload
        action="#"
        :auto-upload="false"
        :limit="1"
        accept="image/*"
        :on-change="handleFileChange"
        :show-file-list="true"
      >
        <el-button type="primary">选择图片</el-button>
      </el-upload>

      <el-button
        class="upload-btn"
        type="success"
        :loading="loading"
        @click="handleSubmit"
      >
        上传并返回URL
      </el-button>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.test-upload-page {
  padding: 24px;
}

.upload-card {
  max-width: 500px;
}

.upload-btn {
  margin-top: 16px;
}
</style>
