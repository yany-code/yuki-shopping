<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi } from '@/api/cart'
import { orderApi } from '@/api/order'
import { userApi } from '@/api/user'
import PayDialog from '@/components/PayDialog.vue'
import { showApiError } from '@/utils/feedback'
import type { CartItemVO } from '@/types/cart'
import type { AddressVO } from '@/types/user'
import type { OrderPreviewVO } from '@/types/order'

const router = useRouter()

const addresses = ref<AddressVO[]>([])
const addressId = ref<number>()
const cartItems = ref<CartItemVO[]>([])
const preview = ref<OrderPreviewVO | null>(null)
const remark = ref('')
const loading = ref(true)
const submitting = ref(false)

// 结算商品 = 购物车中勾选的条目
const checkedItems = computed(() => cartItems.value.filter((i) => i.checked === 1))
const orderItems = computed(() =>
  checkedItems.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity })),
)

async function loadPreview() {
  if (!orderItems.value.length) return
  preview.value = await orderApi.preview(orderItems.value)
}

onMounted(async () => {
  try {
    const [addrList, cart] = await Promise.all([userApi.addresses(), cartApi.list()])
    addresses.value = addrList
    cartItems.value = cart
    addressId.value = addrList.find((a) => a.isDefault === 1)?.id ?? addrList[0]?.id
    await loadPreview()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
})

const payVisible = ref(false)
const payTarget = ref({ orderNo: '', payAmount: '' })

async function submit() {
  if (!addressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (!orderItems.value.length) {
    ElMessage.warning('没有可结算的商品')
    return
  }
  submitting.value = true
  try {
    // 幂等键由 orderApi 内部携带：重复点击/重试不会产生重复订单
    const result = await orderApi.create({
      addressId: addressId.value,
      items: orderItems.value,
      remark: remark.value || undefined,
    })
    payTarget.value = { orderNo: result.orderNo, payAmount: result.payAmount }
    payVisible.value = true
  } catch (e) {
    showApiError(e)
  } finally {
    submitting.value = false
  }
}

function onPaid() {
  router.push(`/orders/${payTarget.value.orderNo}`)
}

function onLater() {
  router.push(`/orders/${payTarget.value.orderNo}`)
}
</script>

<template>
  <div v-loading="loading" class="checkout-page">
    <el-empty v-if="!loading && !checkedItems.length" description="没有勾选要结算的商品">
      <el-button type="primary" @click="router.push('/cart')">返回购物车</el-button>
    </el-empty>

    <template v-else>
      <el-card shadow="never" class="section">
        <template #header>
          <div class="section-header">
            <span class="section-title">收货地址</span>
            <el-button text type="primary" @click="router.push('/addresses')">管理地址</el-button>
          </div>
        </template>
        <el-empty v-if="!addresses.length" description="还没有收货地址，请先新增" />
        <el-radio-group v-else v-model="addressId" class="addr-group">
          <el-radio v-for="a in addresses" :key="a.id" :value="a.id" class="addr-radio">
            <span class="addr-name">{{ a.receiverName }} {{ a.receiverPhone }}</span>
            <el-tag v-if="a.isDefault === 1" type="success" size="small">默认</el-tag>
            <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</div>
          </el-radio>
        </el-radio-group>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">确认订单信息</span></template>
        <div v-for="item in preview?.items ?? []" :key="item.skuId" class="order-item">
          <el-image :src="item.image" fit="cover" class="item-img" />
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div class="item-specs">{{ item.skuSpecs }}</div>
          </div>
          <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
          <div class="item-subtotal">¥{{ item.subtotal }}</div>
        </div>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          maxlength="200"
          placeholder="订单备注（选填，200 字以内）"
          class="remark"
        />
      </el-card>

      <el-card shadow="never" class="section">
        <div class="amounts">
          <div class="amount-row">
            <span>商品总额</span><span>¥{{ preview?.totalAmount ?? '-' }}</span>
          </div>
          <div class="amount-row">
            <span>运费</span><span>¥{{ preview?.freightAmount ?? '-' }}</span>
          </div>
          <div class="amount-row pay">
            <span>应付金额</span>
            <span class="pay-amount">¥{{ preview?.payAmount ?? '-' }}</span>
          </div>
          <div class="submit-row">
            <el-button
              type="danger"
              size="large"
              :loading="submitting"
              :disabled="!addresses.length"
              @click="submit"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </el-card>

      <PayDialog
        v-model="payVisible"
        :order-no="payTarget.orderNo"
        :pay-amount="payTarget.payAmount"
        @paid="onPaid"
        @later="onLater"
      />
    </template>
  </div>
</template>

<style scoped>
.checkout-page {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: 600;
}

.addr-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.addr-radio {
  height: auto;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-right: 0 !important;
  align-items: flex-start;
}

.addr-name {
  font-weight: 600;
  margin-right: 8px;
}

.addr-detail {
  color: #606266;
  font-size: 13px;
  margin-top: 4px;
  white-space: normal;
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
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
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
  width: 110px;
  text-align: right;
  font-weight: 600;
}

.remark {
  margin-top: 16px;
}

.amounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  color: #606266;
}

.amount-row.pay {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  margin-top: 4px;
  color: #303133;
  font-weight: 600;
}

.pay-amount {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
