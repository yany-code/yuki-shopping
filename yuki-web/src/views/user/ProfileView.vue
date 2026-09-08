<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { showApiError } from '@/utils/feedback'
import type { UserProfileDTO } from '@/types/user'

const user = useUserStore()
const loading = ref(false)

onMounted(async () => {
  if (user.profile) return
  loading.value = true
  try {
    await user.loadProfile()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
})

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<UserProfileDTO>({ nickname: '', phone: '', email: '', avatar: '' })

// 与后端 UserProfileDTO 校验对齐
const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50', trigger: 'blur' },
  ],
  phone: [{ max: 20, message: '长度不能超过 20', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

function openEdit() {
  form.nickname = user.profile?.nickname ?? ''
  form.phone = user.profile?.phone ?? ''
  form.email = user.profile?.email ?? ''
  form.avatar = user.profile?.avatar ?? ''
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    // 空字符串不提交，后端按字段缺失做局部更新
    const dto: UserProfileDTO = { nickname: form.nickname }
    if (form.phone) dto.phone = form.phone
    if (form.email) dto.email = form.email
    if (form.avatar) dto.avatar = form.avatar
    user.profile = await userApi.updateMe(dto)
    ElMessage.success('资料已更新')
    dialogVisible.value = false
  } catch (e) {
    showApiError(e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div v-loading="loading" class="profile-page">
    <el-card v-if="user.profile" class="card">
      <template #header>
        <div class="card-header">
          <span>个人资料</span>
          <el-button type="primary" plain @click="openEdit">编辑资料</el-button>
        </div>
      </template>

      <div class="info">
        <el-avatar :size="72" :src="user.profile.avatar">{{ user.profile.nickname }}</el-avatar>
        <el-descriptions :column="1" border class="desc">
          <el-descriptions-item label="用户名">{{ user.profile.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ user.profile.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.profile.phone ?? '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.profile.email ?? '未填写' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑资料" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="头像 URL" prop="avatar">
          <el-input v-model="form.avatar" placeholder="留空则不修改" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 24px 0;
}

.card {
  max-width: 720px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.desc {
  flex: 1;
}
</style>
