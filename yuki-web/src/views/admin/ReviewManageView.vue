<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { showApiError } from '@/utils/feedback'
import type { PageResult } from '@/types/api'
import type { AdminReviewVO } from '@/types/admin'

const data = ref<PageResult<AdminReviewVO>>({ list: [], page: 1, pageSize: 10, total: 0 })
const status = ref<number | undefined>(undefined)
const loading = ref(false)

async function load(page = 1) {
  loading.value = true
  try {
    data.value = await adminApi.reviews({ status: status.value, page, pageSize: data.value.pageSize })
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => load())

/** 隐藏后用户端商品详情/评价列表即时不可见（同一份数据） */
async function toggleStatus(row: AdminReviewVO) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    next === 0 ? '隐藏后该评价将不再对用户展示，确定？' : '确定恢复展示该评价？',
    next === 0 ? '隐藏评价' : '恢复评价',
    { type: 'warning' },
  )
  try {
    await adminApi.setReviewStatus(row.id, next)
    ElMessage.success(next === 1 ? '已恢复展示' : '已隐藏')
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable class="status-select">
        <el-option label="展示中" :value="1" />
        <el-option label="已隐藏" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="data.list" border>
      <el-table-column prop="productName" label="商品" min-width="200" show-overflow-tooltip />
      <el-table-column label="用户" width="100">
        <template #default="{ row }">{{ row.nickname ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="评分" width="140">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="匿名" width="60" align="center">
        <template #default="{ row }">{{ row.isAnonymous === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '展示中' : '已隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="评价时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '隐藏' : '恢复' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="data.total"
        :page-size="data.pageSize"
        :current-page="data.page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.status-select {
  width: 140px;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
