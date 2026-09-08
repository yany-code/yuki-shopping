<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ApiError } from '@/utils/request'
import type { LoginDTO } from '@/types/auth'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive<LoginDTO>({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await user.login(form)
    ElMessage.success(`欢迎回来，${user.profile?.nickname ?? form.username}`)
    router.push((route.query.redirect as string) || '/')
  } catch (e) {
    if (e instanceof ApiError) {
      ElMessage.error(e.traceId ? `${e.message}（${e.traceId}）` : e.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">登录由岐商城</h2>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" native-type="submit">
          登 录
        </el-button>
      </el-form>
      <div class="links">
        还没有账号？<router-link to="/register">立即注册</router-link>
        <el-divider direction="vertical" />
        <router-link to="/">返回首页</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
}

.auth-card {
  width: 400px;
  padding: 8px 16px 16px;
}

.title {
  text-align: center;
  margin: 12px 0 24px;
}

.submit {
  width: 100%;
}

.links {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #606266;
}

.links a {
  color: var(--el-color-primary);
}
</style>
