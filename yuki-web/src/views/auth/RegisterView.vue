<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ApiError } from '@/utils/request'
import type { RegisterDTO } from '@/types/auth'

const router = useRouter()
const user = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
// confirmPassword 仅前端校验用，提交时剔除
const form = reactive<RegisterDTO & { confirmPassword: string }>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
})

// 与后端 RegisterDTO 校验规则对齐：username 3~50、password 8~64、nickname 必填、email 格式
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度 3~50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 64, message: '长度 8~64 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_r, v: string, cb) =>
        v === form.password ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur',
    },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50', trigger: 'blur' },
  ],
  phone: [{ max: 20, message: '长度不能超过 20', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const { confirmPassword: _, ...dto } = form
    // 空字符串不提交，后端对可空字段按缺失处理
    if (!dto.phone) delete dto.phone
    if (!dto.email) delete dto.email
    await user.register(dto)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
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
      <h2 class="title">注册由岐商城</h2>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3~50 字符）" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（8~64 字符）"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱（选填）" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" native-type="submit">
          注 册
        </el-button>
      </el-form>
      <div class="links">
        已有账号？<router-link to="/login">去登录</router-link>
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
  padding: 24px 0;
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
