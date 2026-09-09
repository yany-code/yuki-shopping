<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi } from '@/api/cart'
import { showApiError } from '@/utils/feedback'
import { formatCents, toCents } from '@/utils/money'
import type { CartItemVO } from '@/types/cart'

const router = useRouter()

const list = ref<CartItemVO[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await cartApi.list()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)

// 金额一律走"分"计算，严禁 Number 直接加价格字符串
const checkedItems = computed(() => list.value.filter((i) => i.checked === 1))
const checkedCount = computed(() => checkedItems.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() =>
  formatCents(checkedItems.value.reduce((s, i) => s + toCents(i.price) * i.quantity, 0)),
)
const allChecked = computed(() => list.value.length > 0 && list.value.every((i) => i.checked === 1))

async function onCheckChange(item: CartItemVO, val: boolean | string | number) {
  item.checked = val ? 1 : 0
  try {
    await cartApi.update(item.id, { checked: item.checked })
  } catch (e) {
    showApiError(e)
    await load()
  }
}

async function onQuantityChange(item: CartItemVO, val: number | undefined) {
  if (!val || val < 1) return
  item.quantity = val
  try {
    await cartApi.update(item.id, { quantity: val })
  } catch (e) {
    showApiError(e)
    await load()
  }
}

async function onCheckAll(val: boolean | string | number) {
  const checked = val ? 1 : 0
  try {
    await cartApi.checkAll(checked)
    list.value.forEach((i) => (i.checked = checked))
  } catch (e) {
    showApiError(e)
  }
}

async function remove(item: CartItemVO) {
  await ElMessageBox.confirm(`确定删除「${item.productName}」吗？`, '删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await cartApi.remove(item.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    showApiError(e)
  }
}

function checkout() {
  if (!checkedItems.value.length) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  router.push('/checkout')
}
</script>

<template>
  <div v-loading="loading" class="cart-page">
    <el-card shadow="never">
      <template #header><span class="title">购物车</span></template>

      <el-empty v-if="!loading && !list.length" description="购物车是空的">
        <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
      </el-empty>

      <template v-else>
        <div class="table-head">
          <el-checkbox :model-value="allChecked" @change="onCheckAll">全选</el-checkbox>
        </div>

        <div v-for="item in list" :key="item.id" class="cart-item">
          <el-checkbox
            :model-value="item.checked === 1"
            @change="(v: boolean | string | number) => onCheckChange(item, v)"
          />
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
          <div class="item-price">¥{{ item.price }}</div>
          <el-input-number
            :model-value="item.quantity"
            :min="1"
            :max="999"
            size="small"
            @change="(v: number | undefined) => onQuantityChange(item, v)"
          />
          <div class="item-subtotal">¥{{ formatCents(toCents(item.price) * item.quantity) }}</div>
          <el-button text type="danger" @click="remove(item)">删除</el-button>
        </div>

        <div class="footer-bar">
          <el-checkbox :model-value="allChecked" @change="onCheckAll">全选</el-checkbox>
          <div class="summary">
            <span>
              已选 <b>{{ checkedCount }}</b> 件，合计：
              <span class="total">¥{{ totalPrice }}</span>
            </span>
            <el-button
              type="danger"
              size="large"
              :disabled="!checkedItems.length"
              @click="checkout"
            >
              去结算
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 16px 0;
}

.title {
  font-weight: 600;
}

.table-head {
  padding: 0 12px 12px;
  border-bottom: 1px solid #ebeef5;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 12px;
  border-bottom: 1px solid #ebeef5;
}

.item-img {
  width: 80px;
  height: 80px;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  width: 100px;
  color: #606266;
}

.item-subtotal {
  width: 110px;
  color: #f56c6c;
  font-weight: 600;
  text-align: right;
}

.footer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 12px 4px;
}

.summary {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}
</style>
