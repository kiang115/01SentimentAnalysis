<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { commonApi } from "@/api/common-api";
import { modelApi } from "@/api/model-api";
import { userStore } from "@/stores/user";
import type { UserLoginSend } from "@/Dto/SendDto/UserLoginSend";
import type { UserRegisterSend } from "@/Dto/SendDto/UserRegisterSend";

type AuthMode = "login" | "register";

const router = useRouter();
const store = userStore();

const authMode = ref<AuthMode>("login");
const loginSubmitting = ref(false);
const registerSubmitting = ref(false);
const avatarUploading = ref(false);
const loginFormRef = ref();
const registerFormRef = ref();
const avatarInputRef = ref<HTMLInputElement>();

const loginForm = reactive<UserLoginSend>({
  userName: "",
  password: "",
});

const registerForm = reactive<UserRegisterSend>({
  userName: "",
  password: "",
  userType: "",
  avatarUrl: "",
});

const roleOptions = [
  { label: "消费者", value: "consumer" },
  { label: "商家", value: "merchant" },
  { label: "管理员", value: "admin" },
];

const loginRules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const registerRules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  userType: [{ required: true, message: "请选择用户类型", trigger: "change" }],
  avatarUrl: [{ required: true, message: "请上传用户头像", trigger: "change" }],
};

function switchMode(mode: AuthMode) {
  authMode.value = mode;
}

function resetRegisterForm() {
  registerForm.userName = "";
  registerForm.password = "";
  registerForm.userType = "";
  registerForm.avatarUrl = "";
  registerFormRef.value?.clearValidate();
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  loginSubmitting.value = true;
  try {
    const res = await commonApi.login(loginForm);
    store.setLoginInfo(res.data);
    await router.push("/merchants");
  } finally {
    loginSubmitting.value = false;
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  registerSubmitting.value = true;
  try {
    await commonApi.register(registerForm);
    ElMessage.success("注册成功，请登录");
    resetRegisterForm();
    authMode.value = "login";
  } finally {
    registerSubmitting.value = false;
  }
}

function openAvatarPicker() {
  if (avatarUploading.value) return;
  avatarInputRef.value?.click();
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  avatarUploading.value = true;
  try {
    const res = await modelApi.uploadFileToOss(formData);
    registerForm.avatarUrl = res.data;
    registerFormRef.value?.validateField("avatarUrl");
  } finally {
    avatarUploading.value = false;
    input.value = "";
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="hero-copy">
        <p class="hero-kicker">Sentiment Analysis System</p>
        <h1>评论情感分析系统</h1>
        <p class="hero-desc">
          商家，消费者的一站式运营和决策平台
        </p>
      </div>

      <el-card class="auth-card" shadow="never">
        <div class="auth-switch">
          <button
            class="switch-btn"
            :class="{ active: authMode === 'login' }"
            type="button"
            @click="switchMode('login')"
          >
            登录
          </button>
          <button
            class="switch-btn"
            :class="{ active: authMode === 'register' }"
            type="button"
            @click="switchMode('register')"
          >
            注册
          </button>
        </div>

        <el-form
          v-if="authMode === 'login'"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="loginForm.userName"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loginSubmitting"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="registerForm.userName"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              clearable
            />
          </el-form-item>
          <el-form-item label="用户类型" prop="userType">
            <el-select
              v-model="registerForm.userType"
              placeholder="请选择用户类型"
              class="full-width"
            >
              <el-option
                v-for="item in roleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="用户头像" prop="avatarUrl">
            <div
              class="upload-box"
              :class="{ filled: !!registerForm.avatarUrl, uploading: avatarUploading }"
              @click="openAvatarPicker"
            >
              <img
                v-if="registerForm.avatarUrl"
                :src="registerForm.avatarUrl"
                alt="用户头像"
                class="upload-preview"
              />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>点击上传头像</span>
              </div>
              <div v-if="avatarUploading" class="upload-mask">上传中...</div>
            </div>
            <input
              ref="avatarInputRef"
              class="hidden-file-input"
              type="file"
              accept="image/*"
              @change="handleAvatarChange"
            />
          </el-form-item>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="registerSubmitting"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>

        <p class="auth-tip">
          <template v-if="authMode === 'login'">
            还没有账号？
            <button class="text-btn" type="button" @click="switchMode('register')">
              立即注册
            </button>
          </template>
          <template v-else>
            已有账号？
            <button class="text-btn" type="button" @click="switchMode('login')">
              去登录
            </button>
          </template>
        </p>
      </el-card>
    </section>
  </div>
</template>

<style scoped lang="less">
.login-page {
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top left, rgba(255, 236, 215, 0.9), transparent 38%),
    radial-gradient(circle at right center, rgba(185, 229, 255, 0.8), transparent 30%),
    linear-gradient(135deg, #f5efe4 0%, #edf4fb 52%, #f7fafc 100%);
}

.login-hero {
  min-height: calc(100vh - 48px);
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 420px);
  gap: 48px;
  align-items: center;
}

.hero-copy {
  padding: 32px 0;
}

.hero-kicker {
  margin: 0 0 18px;
  color: #b45309;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0 0 18px;
  color: #0f172a;
  font-size: 52px;
  line-height: 1.08;
  font-weight: 700;
}

.hero-desc {
  max-width: 520px;
  margin: 0;
  color: #475569;
  font-size: 17px;
  line-height: 1.8;
}

.auth-card {
  border: none;
  border-radius: 24px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(12px);
}

.auth-switch {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 6px;
  border-radius: 16px;
  background: #eef2f7;
  margin-bottom: 22px;
}

.switch-btn {
  height: 44px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #475569;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.switch-btn.active {
  background: #0f172a;
  color: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.18);
}

.auth-form {
  margin-top: 6px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 10px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
}

.auth-tip {
  margin: 18px 0 4px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.text-btn {
  border: none;
  background: transparent;
  color: #c2410c;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.full-width {
  width: 100%;
}

.upload-box {
  position: relative;
  width: 100%;
  height: 160px;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.upload-box:hover {
  border-color: #2563eb;
  background: #f1f5f9;
}

.upload-box.filled {
  border-style: solid;
}

.upload-box.uploading {
  cursor: wait;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 14px;
}

.upload-icon {
  font-size: 24px;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.42);
  color: #fff;
  font-size: 14px;
}

.hidden-file-input {
  display: none;
}

@media (max-width: 960px) {
  .login-page {
    padding: 16px;
  }

  .login-hero {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .hero-copy {
    padding: 12px 0 0;
  }

  .hero-copy h1 {
    font-size: 38px;
  }

  .hero-desc {
    font-size: 15px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 12px;
  }

  .auth-card {
    padding: 4px;
    border-radius: 18px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }
}
</style>
