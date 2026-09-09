<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi } from '@/api/cart'
import { productApi } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { showApiError } from '@/utils/feedback'
import type { PageResult } from '@/types/api'
import type { ProductDetailVO, ReviewVO, SkuVO } from '@/types/product'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const detail = ref<ProductDetailVO | null>(null)
const loading = ref(true)
const activeImage = ref('')
const quantity = ref(1)
const activeTab = ref('detail')

// ---- SKU 规格选择 ----
const selected = reactive<Record<string, string>>({})

function parseSpecs(sku: SkuVO): Record<string, string> {
  try {
    return JSON.parse(sku.specs) as Record<string, string>
  } catch {
    return {}
  }
}

const specGroups = computed(() => {
  const groups = new Map<string, Set<string>>()
  for (const sku of detail.value?.skus ?? []) {
    for (const [k, v] of Object.entries(parseSpecs(sku))) {
      if (!groups.has(k)) groups.set(k, new Set())
      groups.get(k)!.add(v)
    }
  }
  return [...groups.entries()].map(([name, values]) => ({ name, values: [...values] }))
})

const matchedSku = computed<SkuVO | null>(() => {
  const skus = detail.value?.skus ?? []
  if (!skus.length) return null
  return (
    skus.find((sku) => {
      const specs = parseSpecs(sku)
      return (
        Object.keys(specs).length > 0 &&
        Object.entries(specs).every(([k, v]) => selected[k] === v)
      )
    }) ?? null
  )
})

function selectSpec(name: string, value: string) {
  selected[name] = value
  const sku = matchedSku.value
  if (sku?.image) activeImage.value = sku.image
  if (sku && quantity.value > sku.stock) quantity.value = Math.max(1, sku.stock)
}

const priceText = computed(() => {
  if (matchedSku.value) return `¥${matchedSku.value.price}`
  const d = detail.value
  if (!d) return ''
  return d.priceMin === d.priceMax ? `¥${d.priceMin}` : `¥${d.priceMin} ~ ¥${d.priceMax}`
})

const stockText = computed(() => {
  const sku = matchedSku.value
  if (!sku) return '请选择规格'
  return sku.stock > 0 ? `有货（剩余 ${sku.stock} 件）` : '暂时缺货'
})

// ---- 评价 ----
const reviewData = ref<PageResult<ReviewVO>>({ list: [], page: 1, pageSize: 5, total: 0 })
const reviewRating = ref<number | undefined>(undefined)
const reviewLoading = ref(false)

const ratingOptions = [
  { value: undefined, label: '全部' },
  { value: 5, label: '5 星' },
  { value: 4, label: '4 星' },
  { value: 3, label: '3 星' },
  { value: 2, label: '2 星' },
  { value: 1, label: '1 星' },
]

async function loadReviews(page = 1) {
  reviewLoading.value = true
  try {
    reviewData.value = await productApi.reviews(id, {
      rating: reviewRating.value,
      page,
      pageSize: reviewData.value.pageSize,
    })
  } catch (e) {
    showApiError(e)
  } finally {
    reviewLoading.value = false
  }
}

function onRatingChange() {
  loadReviews(1)
}

// ---- 初始化 ----
onMounted(async () => {
  try {
    detail.value = await productApi.detail(id)
    activeImage.value = detail.value.mainImage ?? detail.value.images[0] ?? ''
    // 默认选中第一个 SKU 的规格组合
    const first = detail.value.skus[0]
    if (first) Object.assign(selected, parseSpecs(first))
    await loadReviews()
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
})

function requireSku(): SkuVO | null {
  if (!matchedSku.value) {
    ElMessage.warning('请先选择完整规格')
    return null
  }
  return matchedSku.value
}

const user = useUserStore()
const adding = ref(false)

async function addToCart() {
  const sku = requireSku()
  if (!sku) return
  if (!user.isLogined) {
    user.redirectToLogin()
    return
  }
  adding.value = true
  try {
    await cartApi.add({ skuId: sku.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (e) {
    showApiError(e)
  } finally {
    adding.value = false
  }
}

// 直接下单流（绕过购物车）需结算页支持直购模式，先用购物车路径
function buyNow() {
  if (!requireSku()) return
  ElMessage.info('请先加入购物车，在购物车中勾选结算')
}
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="detail">
      <el-page-header class="back" @back="router.back()">
        <template #content><span class="back-title">{{ detail.name }}</span></template>
      </el-page-header>

      <el-card shadow="never">
        <div class="main">
          <div class="gallery">
            <el-image :src="activeImage" fit="cover" class="main-image" />
            <div class="thumbs">
              <el-image
                v-for="img in detail.images"
                :key="img"
                :src="img"
                fit="cover"
                class="thumb"
                :class="{ active: img === activeImage }"
                @click="activeImage = img"
              />
            </div>
          </div>

          <div class="info">
            <h2 class="name">{{ detail.name }}</h2>
            <p class="subtitle">{{ detail.subtitle }}</p>

            <div class="price-row">
              <span class="price">{{ priceText }}</span>
              <span class="sales">已售 {{ detail.sales }}</span>
            </div>

            <div v-for="group in specGroups" :key="group.name" class="spec-group">
              <span class="spec-name">{{ group.name }}</span>
              <el-button
                v-for="v in group.values"
                :key="v"
                size="small"
                :type="selected[group.name] === v ? 'primary' : 'default'"
                :plain="selected[group.name] !== v"
                @click="selectSpec(group.name, v)"
              >
                {{ v }}
              </el-button>
            </div>

            <div class="stock-row">
              <span :class="{ 'out-of-stock': matchedSku && matchedSku.stock === 0 }">{{ stockText }}</span>
            </div>

            <div class="quantity-row">
              <span class="spec-name">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="matchedSku?.stock || 999"
                :disabled="!matchedSku || matchedSku.stock === 0"
              />
            </div>

            <div class="actions">
              <el-button type="danger" size="large" :disabled="!matchedSku || matchedSku.stock === 0" @click="buyNow">
                立即购买
              </el-button>
              <el-button type="warning" size="large" :loading="adding" :disabled="!matchedSku || matchedSku.stock === 0" @click="addToCart">
                加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="tabs-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <!-- 后端富文本，内容由管理端维护 -->
            <div class="rich" v-html="detail.detail ?? '<p>暂无详情</p>'"></div>
          </el-tab-pane>

          <el-tab-pane name="reviews">
            <template #label>
              商品评价<template v-if="detail.reviewSummary">（{{ detail.reviewSummary.reviewCount }}）</template>
            </template>

            <div v-if="detail.reviewSummary" class="review-summary">
              <span class="avg">{{ detail.reviewSummary.ratingAvg }}</span>
              <el-rate :model-value="Number(detail.reviewSummary.ratingAvg)" disabled allow-half />
              <span class="count">共 {{ detail.reviewSummary.reviewCount }} 条评价</span>
            </div>

            <div class="review-filter">
              <el-radio-group v-model="reviewRating" @change="onRatingChange">
                <el-radio-button v-for="o in ratingOptions" :key="o.label" :value="o.value">
                  {{ o.label }}
                </el-radio-button>
              </el-radio-group>
            </div>

            <div v-loading="reviewLoading">
              <el-empty v-if="!reviewData.list.length" description="暂无评价" />
              <div v-for="r in reviewData.list" :key="r.id" class="review-item">
                <div class="review-head">
                  <span class="nickname">{{ r.nickname ?? '匿名用户' }}</span>
                  <el-rate :model-value="r.rating" disabled />
                  <span class="time">{{ r.createdAt }}</span>
                </div>
                <div class="review-content">{{ r.content }}</div>
                <div v-if="r.images?.length" class="review-images">
                  <el-image
                    v-for="img in r.images"
                    :key="img"
                    :src="img"
                    fit="cover"
                    class="review-img"
                    :preview-src-list="r.images"
                    preview-teleported
                  />
                </div>
              </div>
              <div class="pager">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="reviewData.total"
                  :page-size="reviewData.pageSize"
                  :current-page="reviewData.page"
                  @current-change="loadReviews"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <el-result v-else-if="!loading" icon="error" title="商品不存在或已下架">
      <template #extra>
        <el-button type="primary" @click="router.push('/products')">返回商品列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.detail-page {
  padding: 16px 0;
}

.back {
  margin-bottom: 16px;
}

.back-title {
  font-size: 14px;
}

.main {
  display: flex;
  gap: 32px;
}

.gallery {
  width: 420px;
  flex-shrink: 0;
}

.main-image {
  width: 420px;
  height: 420px;
  border-radius: 8px;
  display: block;
}

.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
}

.thumb.active {
  border-color: var(--el-color-primary);
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  margin: 0 0 4px;
}

.subtitle {
  color: #909399;
  margin: 0 0 16px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  background: #fff5f5;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.price {
  color: #f56c6c;
  font-size: 26px;
  font-weight: 700;
}

.sales {
  color: #909399;
  font-size: 13px;
}

.spec-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.spec-name {
  color: #909399;
  width: 48px;
  flex-shrink: 0;
}

.stock-row {
  margin: 8px 0 16px;
  color: #67c23a;
}

.out-of-stock {
  color: #f56c6c;
}

.quantity-row {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  gap: 12px;
}

.tabs-card {
  margin-top: 16px;
}

.rich {
  padding: 8px 0;
  line-height: 1.8;
}

.review-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avg {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.count {
  color: #909399;
}

.review-filter {
  margin-bottom: 16px;
}

.review-item {
  border-top: 1px solid #ebeef5;
  padding: 16px 0;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  font-weight: 600;
}

.time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  margin-top: 8px;
  color: #303133;
}

.review-images {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.review-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
}
</style>
