<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { showApiError } from '@/utils/feedback'
import type { AddressDTO, AddressVO } from '@/types/user'

const list = ref<AddressVO[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await userApi.addresses()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Required<AddressDTO>>({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

// 与后端 AddressDTO 校验对齐
const rules: FormRules = {
  receiverName: [
    { required: true, message: '请输入收货人', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50', trigger: 'blur' },
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { max: 20, message: '长度不能超过 20', trigger: 'blur' },
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200', trigger: 'blur' },
  ],
}

function resetForm() {
  Object.assign(form, {
    receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0,
  })
}

function openAdd() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(a: AddressVO) {
  editingId.value = a.id
  Object.assign(form, {
    receiverName: a.receiverName,
    receiverPhone: a.receiverPhone,
    province: a.province,
    city: a.city,
    district: a.district,
    detail: a.detail,
    isDefault: a.isDefault,
  })
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value === null) {
      await userApi.addAddress(form)
    } else {
      await userApi.updateAddress(editingId.value, form)
    }
    ElMessage.success('地址已保存')
    dialogVisible.value = false
    await load()
  } catch (e) {
    showApiError(e)
  } finally {
    saving.value = false
  }
}

async function remove(a: AddressVO) {
  await ElMessageBox.confirm(
    `确定删除「${a.receiverName}，${a.province}${a.city}${a.district}${a.detail}」吗？`,
    '删除地址',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  )
  try {
    await userApi.deleteAddress(a.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    showApiError(e)
  }
}

async function setDefault(a: AddressVO) {
  try {
    await userApi.setDefaultAddress(a.id)
    ElMessage.success('已设为默认地址')
    await load()
  } catch (e) {
    showApiError(e)
  }
}
</script>

<template>
  <div v-loading="loading" class="address-page">
    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span>收货地址</span>
          <el-button type="primary" @click="openAdd">新增地址</el-button>
        </div>
      </template>

      <el-empty v-if="!list.length" description="还没有收货地址，点击右上角新增" />

      <el-row v-else :gutter="16">
        <el-col v-for="a in list" :key="a.id" :span="12" class="col">
          <el-card shadow="hover" class="address-card">
            <div class="line1">
              <span class="name">{{ a.receiverName }}</span>
              <span class="phone">{{ a.receiverPhone }}</span>
              <el-tag v-if="a.isDefault === 1" type="success" size="small">默认</el-tag>
            </div>
            <div class="line2">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</div>
            <div class="actions">
              <el-button text type="primary" @click="openEdit(a)">编辑</el-button>
              <el-button v-if="a.isDefault !== 1" text type="primary" @click="setDefault(a)">
                设为默认
              </el-button>
              <el-button text type="danger" @click="remove(a)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增地址' : '编辑地址'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" maxlength="20" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="如：北京市" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="如：北京市" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="form.district" placeholder="如：海淀区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
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
.address-page {
  padding: 24px 0;
}

.card {
  max-width: 960px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.col {
  margin-bottom: 16px;
}

.line1 {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
}

.phone {
  color: #909399;
}

.line2 {
  color: #606266;
  margin-bottom: 8px;
  min-height: 40px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
