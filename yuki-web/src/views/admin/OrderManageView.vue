<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { showApiError } from '@/utils/feedback'
import { orderStatusTag, orderStatusText } from '@/utils/format'
import type { PageResult } from '@/types/api'
import type { OrderDetailVO, OrderListVO, OrderStatus } from '@/types/order'
import type { OrderShipDTO } from '@/types/admin'

const data = ref<PageResult<OrderListVO>>({ list: [], page: 1, pageSize: 10, total: 0 })
const orderNo = ref('')
const status = ref<OrderStatus | undefined>(undefined)
const loading = ref(false)

const statusOptions: { label: string; value: OrderStatus }[] = [
  { label: '待支付', value: 10 },
  { label: '待发货', value: 20 },
  { label: '已发货', value: 30 },
  { label: '已完成', value: 40 },
  { label: '已取消', value: 50 },
  { label: '已退款', value: 60 },
]

async function load(page = 1) {
  loading.value = true
  try {
    data.value = await adminApi.orders({
      orderNo: orderNo.value || undefined,
      status: status.value,
      page,
      pageSize: data.value.pageSize,
    })
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => load())

// ---- 订单详情 ----
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<OrderDetailVO | null>(null)

async function openDetail(row: OrderListVO) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await adminApi.orderDetail(row.orderNo)
  } catch (e) {
    showApiError(e)
  } finally {
    detailLoading.value = false
  }
}

// ---- 发货（仅状态 20，服务端校验兜底） ----
const shipVisible = ref(false)
const shipSaving = ref(false)
const shipOrderNo = ref('')
const shipFormRef = ref<FormInstance>()
const shipForm = reactive<OrderShipDTO>({ expressCompany: '', expressNo: '' })

const shipRules: FormRules = {
  expressCompany: [{ required: true, message: '请输入快递公司', trigger: 'blur' }],
  expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
}

function openShip(row: OrderListVO) {
  shipOrderNo.value = row.orderNo
  Object.assign(shipForm, { expressCompany: '', expressNo: '' })
  shipVisible.value = true
}

async function saveShip() {
  await shipFormRef.value?.validate()
  shipSaving.value = true
  try {
    await adminApi.ship(shipOrderNo.value, shipForm)
    ElMessage.success('已发货')
    shipVisible.value = false
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  } finally {
    shipSaving.value = false
  }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-input v-model="orderNo" placeholder="订单号" clearable class="kw" @keyup.enter="load(1)" />
      <el-select v-model="status" placeholder="全部状态" clearable class="status-select">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="data.list" border>
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          {{ row.items[0]?.productName }}
          <span v-if="row.items.length > 1" class="more">等 {{ row.items.length }} 件</span>
        </template>
      </el-table-column>
      <el-table-column label="实付金额" width="110" align="right">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="orderStatusTag(row.status)">{{ orderStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 20" size="small" type="primary" @click="openShip(row)">
            发货
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

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="orderStatusTag(detail.status)">{{ orderStatusText(detail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ detail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detail.receiverName }} {{ detail.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ detail.receiverAddress }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.expressCompany" label="物流">
              {{ detail.expressCompany }} {{ detail.expressNo }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.remark" label="备注">{{ detail.remark }}</el-descriptions-item>
            <el-descriptions-item label="商品总额">¥{{ detail.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">¥{{ detail.payAmount }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.payTime" label="支付时间">{{ detail.payTime }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.deliveryTime" label="发货时间">{{ detail.deliveryTime }}</el-descriptions-item>
          </el-descriptions>

          <el-table :data="detail.items" border size="small" class="items">
            <el-table-column prop="productName" label="商品" min-width="180" />
            <el-table-column prop="skuSpecs" label="规格快照" min-width="140" />
            <el-table-column prop="price" label="单价" width="90" align="right" />
            <el-table-column prop="quantity" label="数量" width="60" align="right" />
            <el-table-column prop="subtotal" label="小计" width="100" align="right" />
          </el-table>
        </template>
      </div>
    </el-dialog>

    <!-- 发货 -->
    <el-dialog v-model="shipVisible" title="订单发货" width="440px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="90px">
        <el-form-item label="订单号">
          <span class="mono">{{ shipOrderNo }}</span>
        </el-form-item>
        <el-form-item label="快递公司" prop="expressCompany">
          <el-input v-model="shipForm.expressCompany" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="shipForm.expressNo" placeholder="如：SF1234567890" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipSaving" @click="saveShip">确认发货</el-button>
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

.kw {
  width: 240px;
}

.status-select {
  width: 140px;
}

.more {
  color: #909399;
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.items {
  margin-top: 16px;
}

.mono {
  font-family: monospace;
}
</style>
