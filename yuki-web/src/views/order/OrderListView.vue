<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import PayDialog from '@/components/PayDialog.vue'
import { showApiError } from '@/utils/feedback'
import { orderStatusTag, orderStatusText } from '@/utils/format'
import type { PageResult } from '@/types/api'
import type { OrderListVO, OrderStatus } from '@/types/order'

const router = useRouter()

const data = ref<PageResult<OrderListVO>>({ list: [], page: 1, pageSize: 10, total: 0 })
const status = ref<OrderStatus | undefined>(undefined)
const loading = ref(false)

const tabs = [
  { label: '全部', value: undefined },
  { label: '待支付', value: 10 as OrderStatus },
  { label: '待发货', value: 20 as OrderStatus },
  { label: '已发货', value: 30 as OrderStatus },
  { label: '已完成', value: 40 as OrderStatus },
  { label: '已取消', value: 50 as OrderStatus },
]

async function load(page = 1) {
  loading.value = true
  try {
    data.value = await orderApi.list({ status: status.value, page, pageSize: data.value.pageSize })
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => load())

function onTabChange() {
  load(1)
}

// ---- 支付 ----
const payVisible = ref(false)
const payTarget = ref({ orderNo: '', payAmount: '' })

function openPay(o: OrderListVO) {
  payTarget.value = { orderNo: o.orderNo, payAmount: o.payAmount }
  payVisible.value = true
}

async function cancel(o: OrderListVO) {
  await ElMessageBox.confirm(`确定取消订单 ${o.orderNo} 吗？库存将自动释放。`, '取消订单', {
    type: 'warning',
    confirmButtonText: '取消订单',
    cancelButtonText: '再想想',
  })
  try {
    await orderApi.cancel(o.orderNo)
    ElMessage.success('订单已取消')
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  }
}

async function confirm(o: OrderListVO) {
  await ElMessageBox.confirm('确认已收到货了吗？', '确认收货', {
    type: 'info',
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
  })
  try {
    await orderApi.confirm(o.orderNo)
    ElMessage.success('已确认收货')
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  }
}
</script>

<template>
  <div class="orders-page">
    <el-card shadow="never">
      <template #header><span class="title">我的订单</span></template>

      <el-tabs :model-value="status === undefined ? 'all' : String(status)" @tab-change="(n: string | number) => { status = n === 'all' ? undefined : Number(n) as OrderStatus; onTabChange() }">
        <el-tab-pane v-for="t in tabs" :key="t.label" :label="t.label" :name="t.value === undefined ? 'all' : String(t.value)" />
      </el-tabs>

      <div v-loading="loading">
        <el-empty v-if="!loading && !data.list.length" description="暂无订单">
          <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
        </el-empty>

        <el-card v-for="o in data.list" :key="o.orderNo" shadow="never" class="order-card">
          <div class="order-head">
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-time">{{ o.createdAt }}</span>
            <el-tag :type="orderStatusTag(o.status)">{{ orderStatusText(o.status) }}</el-tag>
          </div>

          <div class="order-body" @click="router.push(`/orders/${o.orderNo}`)">
            <div v-for="item in o.items" :key="item.id" class="order-item">
              <el-image :src="item.image" fit="cover" class="item-img" />
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-specs">{{ item.skuSpecs }}</div>
              </div>
              <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
            </div>
          </div>

          <div class="order-foot">
            <span class="pay-amount">实付：¥{{ o.payAmount }}</span>
            <div class="actions">
              <el-button size="small" @click="router.push(`/orders/${o.orderNo}`)">查看详情</el-button>
              <el-button v-if="o.status === 10" size="small" type="danger" @click="openPay(o)">
                去支付
              </el-button>
              <el-button v-if="o.status === 10" size="small" @click="cancel(o)">取消订单</el-button>
              <el-button v-if="o.status === 30" size="small" type="success" @click="confirm(o)">
                确认收货
              </el-button>
              <el-button v-if="o.status === 40" size="small" type="primary" plain @click="router.push(`/orders/${o.orderNo}`)">
                去评价
              </el-button>
            </div>
          </div>
        </el-card>

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
    </el-card>

    <PayDialog
      v-model="payVisible"
      :order-no="payTarget.orderNo"
      :pay-amount="payTarget.payAmount"
      @paid="load(data.page)"
      @later="load(data.page)"
    />
  </div>
</template>

<style scoped>
.orders-page {
  padding: 16px 0;
}

.title {
  font-weight: 600;
}

.order-card {
  margin-bottom: 16px;
}

.order-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.order-no {
  font-family: monospace;
  font-weight: 600;
}

.order-time {
  color: #909399;
  font-size: 13px;
}

.order-body {
  cursor: pointer;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.item-img {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-specs {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.item-price {
  color: #606266;
  font-size: 13px;
}

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.pay-amount {
  color: #f56c6c;
  font-weight: 700;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
</style>
