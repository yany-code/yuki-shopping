<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { showApiError } from '@/utils/feedback'
import { priceRangeText } from '@/utils/format'
import type { CategoryTreeVO, ProductListVO } from '@/types/product'
import type { PageResult } from '@/types/api'

const router = useRouter()

const categories = ref<CategoryTreeVO[]>([])
const hot = ref<PageResult<ProductListVO>>({ list: [], page: 1, pageSize: 8, total: 0 })
const loading = ref(false)

function goCategory(c: CategoryTreeVO) {
  router.push({ path: '/products', query: { categoryId: c.id } })
}

onMounted(async () => {
  loading.value = true
  try {
    const [tree, page] = await Promise.all([
      productApi.categoryTree(),
      productApi.page({ sort: 'sales', page: 1, pageSize: 8 }),
    ])
    categories.value = tree
    hot.value = page
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="home">
    <el-card shadow="never" class="hero">
      <h1>由岐商城</h1>
      <p class="slogan">正品保障 · 极速配送 · 售后无忧</p>
      <el-button type="primary" size="large" @click="router.push('/products')">逛全部商品</el-button>
    </el-card>

    <el-card shadow="never" class="section">
      <template #header><span class="section-title">热门分类</span></template>
      <div class="cats">
        <div v-for="c1 in categories" :key="c1.id" class="cat-group">
          <div class="cat-l1" @click="goCategory(c1)">{{ c1.name }}</div>
          <div class="cat-children">
            <template v-for="c2 in c1.children" :key="c2.id">
              <el-tag
                v-for="c3 in c2.children?.length ? c2.children : [c2]"
                :key="c3.id"
                class="cat-tag"
                @click="goCategory(c3)"
              >
                {{ c3.name }}
              </el-tag>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-header">
          <span class="section-title">热销商品</span>
          <el-button text type="primary" @click="router.push('/products')">查看全部</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col v-for="p in hot.list" :key="p.id" :span="6" class="col">
          <el-card shadow="hover" class="product-card" @click="router.push(`/products/${p.id}`)">
            <el-image :src="p.mainImage" fit="cover" class="thumb" lazy />
            <div class="name">{{ p.name }}</div>
            <div class="meta">
              <span class="price">{{ priceRangeText(p.priceMin, p.priceMax) }}</span>
              <span class="sales">已售 {{ p.sales }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  text-align: center;
  padding: 40px 24px;
  background: linear-gradient(135deg, #ecf5ff 0%, #fdf6ec 100%);
}

.hero h1 {
  margin: 0 0 8px;
}

.slogan {
  color: #909399;
  margin: 0 0 24px;
}

.section-title {
  font-weight: 600;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cat-group {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.cat-l1 {
  font-weight: 600;
  width: 80px;
  flex-shrink: 0;
  cursor: pointer;
}

.cat-l1:hover {
  color: var(--el-color-primary);
}

.cat-children {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cat-tag {
  cursor: pointer;
}

.col {
  margin-bottom: 16px;
}

.product-card {
  cursor: pointer;
}

.product-card :deep(.el-card__body) {
  padding: 12px;
}

.thumb {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 4px;
  display: block;
}

.name {
  margin-top: 8px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price {
  color: #f56c6c;
  font-weight: 700;
}

.sales {
  color: #909399;
  font-size: 12px;
}
</style>
