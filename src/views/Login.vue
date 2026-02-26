<script setup lang="ts">
import {ref, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {commonApi} from "@/api/common-api"
import welcomeview from "@/views/WelcomeView.vue"
import {userStore} from "@/stores/user"

onMounted(() => {
  login();
});

async function login() {
  try {
    let res = await commonApi.login();
    ElMessage.success("你的token为" + res.data);
    userStore().setLoginInfo(res.data);
  } catch (e) {
    console.log(e);
    ElMessage.error("登录失败"); // 建议使用 error 而不是普通消息
  }
}
</script>


<template>
  <welcomeview>

  </welcomeview>
</template>

<style scoped lang="less">

</style>