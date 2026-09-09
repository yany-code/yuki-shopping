<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { showApiError } from '@/utils/feedback'
import type { CategoryTreeVO } from '@/types/product'
import type { CategorySaveDTO } from '@/types/admin'

const tree = ref<CategoryTreeVO[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    tree.value = await adminApi.categories()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => load())

// ---- 新增 / 编辑 ----
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
/** parentId=0 新增一级；否则为 parent 新增子级；editing 有 id 为编辑 */
const form = reactive<CategorySaveDTO & { parentName?: string }>({
  id: undefined,
  parentId: 0,
  name: '',
  sort: 1,
  parentName: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

function openCreateTop() {
  Object.assign(form, { id: undefined, parentId: 0, name: '', sort: 1, parentName: '' })
  dialogVisible.value = true
}

function openCreateChild(parent: CategoryTreeVO) {
  Object.assign(form, { id: undefined, parentId: parent.id, name: '', sort: 1, parentName: parent.name })
  dialogVisible.value = true
}

function openEdit(node: CategoryTreeVO) {
  Object.assign(form, { id: node.id, parentId: node.parentId, name: node.name, sort: node.sort, parentName: '' })
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await adminApi.saveCategory({ id: form.id, parentId: form.parentId, name: form.name, sort: form.sort })
    ElMessage.success(form.id == null ? '分类已新增' : '分类已更新')
    dialogVisible.value = false
    await load()
  } catch (e) {
    showApiError(e)
  } finally {
    saving.value = false
  }
}

const levelText = (level: number) => ['', '一级', '二级', '三级'][level] ?? `L${level}`
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button type="success" @click="openCreateTop">新增一级分类</el-button>
    </div>

    <el-table v-loading="loading" :data="tree" row-key="id" border default-expand-all>
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column prop="id" label="ID" width="80" align="right" />
      <el-table-column label="层级" width="80" align="center">
        <template #default="{ row }">{{ levelText(row.level) }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="right" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.level < 3" size="small" type="primary" plain @click="openCreateChild(row)">
            新增子分类
          </el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id != null ? '编辑分类' : form.parentId === 0 ? '新增一级分类' : `新增子分类 · ${form.parentName}`"
      width="440px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="30" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
