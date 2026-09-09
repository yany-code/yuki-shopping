<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import { reviewApi } from '@/api/review'
import PayDialog from '@/components/PayDialog.vue'
import { showApiError } from '@/utils/feedback'
import { orderStatusTag, orderStatusText } from '@/utils/format'
import type { OrderDetailVO, OrderItemVO, ReviewCreateDTO } from '@/types/order'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo as string

const order = ref<OrderDetailVO | null>(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    order.value = await orderApi.detail(orderNo)
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)

// ---- 支付 / 取消 / 确认收货 ----
const payVisible = ref(false)

async function cancel() {
  await ElMessageBox.confirm(`确定取消订单 ${orderNo} 吗？库存将自动释放。`, '取消订单', {
    type: 'warning',
    confirmButtonText: '取消订单',
    cancelButtonText: '再想想',
  })
  try {
    await orderApi.cancel(orderNo)
    ElMessage.success('订单已取消')
    await load()
  } catch (e) {
    showApiError(e)
  }
}

async function confirm() {
  await ElMessageBox.confirm('确认已收到货了吗？', '确认收货', {
    type: 'info',
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
  })
  try {
    await orderApi.confirm(orderNo)
    ElMessage.success('已确认收货')
    await load()
  } catch (e) {
    showApiError(e)
  }
}

// ---- 评价（仅状态 40，一条明细一次，后端 uk_order_item_id 兜底） ----
const reviewedIds = reactive(new Set<number>())
const reviewVisible = ref(false)
const reviewTarget = ref<OrderItemVO | null>(null)
const reviewSubmitting = ref(false)
const reviewForm = reactive<Required<Omit<ReviewCreateDTO, 'images'>>>({
  rating: 5,
  content: '',
  isAnonymous: 0,
})

function openReview(item: OrderItemVO) {
  reviewTarget.value = item
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.isAnonymous = 0
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewTarget.value) return
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await reviewApi.create(orderNo, reviewTarget.value.id, {
      rating: reviewForm.rating,
      content: reviewForm.content.trim(),
      isAnonymous: reviewForm.isAnonymous,
    })
    reviewedIds.add(reviewTarget.value.id)
    ElMessage.success('评价成功，感谢分享')
    reviewVisible.value = false
  } catch (e) {
    showApiError(e)
  } finally {
    reviewSubmitting.value = false
  }
}
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="order">
      <el-card shadow="never" class="section">
        <div class="status-row">
          <div>
            <el-tag :type="orderStatusTag(order.status)" size="large">
              {{ orderStatusText(order.status) }}
            </el-tag>
            <span class="order-no">订单号：{{ order.orderNo }}</span>
          </div>
          <div class="actions">
            <el-button v-if="order.status === 10" type="danger" @click="payVisible = true">
              去支付
            </el-button>
            <el-button v-if="order.status === 10" @click="cancel">取消订单</el-button>
            <el-button v-if="order.status === 30" type="success" @click="confirm">确认收货</el-button>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">收货信息</span></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item v-if="order.remark" label="订单备注">{{ order.remark }}</el-descriptions-item>
          <el-descriptions-item v-if="order.expressCompany" label="物流">
            {{ order.expressCompany }} {{ order.expressNo }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">商品明细</span></template>
        <div v-for="item in order.items" :key="item.id" class="order-item">
          <el-image
            :src="item.image"
            fit="cover"
            class="item-img"
            @click="router.push(`/products/${item.productId}`)"
          />
          <div class="item-info">
            <div class="item-name" @click="router.push(`/products/${item.productId}`)">
              {{ item.productName }}
            </div>
            <div class="item-specs">{{ item.skuSpecs }}</div>
          </div>
          <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
          <div class="item-subtotal">¥{{ item.subtotal }}</div>
          <el-button
            v-if="order.status === 40 && !reviewedIds.has(item.id)"
            size="small"
            type="primary"
            plain
            @click="openReview(item)"
          >
            评价
          </el-button>
        </div>

        <div class="amounts">
          <div class="amount-row"><span>商品总额</span><span>¥{{ order.totalAmount }}</span></div>
          <div class="amount-row"><span>运费</span><span>¥{{ order.freightAmount }}</span></div>
          <div class="amount-row"><span>优惠</span><span>-¥{{ order.discountAmount }}</span></div>
          <div class="amount-row pay"><span>实付金额</span><span class="pay-amount">¥{{ order.payAmount }}</span></div>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">订单轨迹</span></template>
        <el-timeline>
          <el-timeline-item v-if="order.finishTime" :timestamp="order.finishTime" type="success">交易完成</el-timeline-item>
          <el-timeline-item v-if="order.deliveryTime" :timestamp="order.deliveryTime" type="primary">
            商品已发出（{{ order.expressCompany }} {{ order.expressNo }}）
          </el-timeline-item>
          <el-timeline-item v-if="order.payTime" :timestamp="order.payTime" type="primary">支付成功</el-timeline-item>
          <el-timeline-item :timestamp="order.createdAt">订单创建</el-timeline-item>
        </el-timeline>
      </el-card>

      <PayDialog v-model="payVisible" :order-no="order.orderNo" :pay-amount="order.payAmount" @paid="load" @later="load" />

      <el-dialog v-model="reviewVisible" title="评价商品" width="480px">
        <div v-if="reviewTarget" class="review-form">
          <div class="review-product">
            <el-image :src="reviewTarget.image" fit="cover" class="review-img" />
            <div>
              <div class="item-name">{{ reviewTarget.productName }}</div>
              <div class="item-specs">{{ reviewTarget.skuSpecs }}</div>
            </div>
          </div>
          <div class="form-row">
            <span class="form-label">评分</span>
            <el-rate v-model="reviewForm.rating" />
          </div>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="说说商品的使用感受吧"
          />
          <el-checkbox v-model="reviewForm.isAnonymous" :true-value="1" :false-value="0" class="anonymous">
            匿名评价（昵称将脱敏展示）
          </el-checkbox>
        </div>
        <template #footer>
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
        </template>
      </el-dialog>
    </template>

    <el-result v-else-if="!loading" icon="error" title="订单不存在">
      <template #extra>
        <el-button type="primary" @click="router.push('/orders')">返回订单列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.detail-page {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-weight: 600;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  margin-left: 16px;
  font-family: monospace;
  color: #606266;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.item-img {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  cursor: pointer;
}

.item-name:hover {
  color: var(--el-color-primary);
}

.item-specs {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.item-price {
  width: 140px;
  color: #606266;
}

.item-subtotal {
  width: 100px;
  text-align: right;
  font-weight: 600;
}

.amounts {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  width: 240px;
  color: #606266;
}

.amount-row.pay {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
  color: #303133;
  font-weight: 600;
}

.pay-amount {
  color: #f56c6c;
  font-size: 18px;
}

.review-product {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.review-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.form-label {
  color: #606266;
}

.anonymous {
  margin-top: 12px;
}
</style>
