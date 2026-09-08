<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { showApiError } from '@/utils/feedback'
import { priceRangeText } from '@/utils/format'
import type { PageResult } from '@/types/api'
import type {
  BrandVO,
  CategoryTreeVO,
  ProductListVO,
  ProductQuery,
  ProductSort,
} from '@/types/product'

const route = useRoute()
const router = useRouter()

const categories = ref<CategoryTreeVO[]>([])
const brands = ref<BrandVO[]>([])
const data = ref<PageResult<ProductListVO>>({ list: [], page: 1, pageSize: 12, total: 0 })
const loading = ref(false)

// 从 URL query 恢复筛选状态，刷新/分享链接不丢上下文
const query = reactive<ProductQuery>({
  keyword: (route.query.keyword as string) || undefined,
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  brandId: route.query.brandId ? Number(route.query.brandId) : undefined,
  minPrice: (route.query.minPrice as string) || undefined,
  maxPrice: (route.query.maxPrice as string) || undefined,
  sort: (route.query.sort as ProductSort) || 'default',
  page: 1,
  pageSize: 12,
})

const sortOptions: { value: ProductSort; label: string }[] = [
  { value: 'default', label: '综合排序' },
  { value: 'sales', label: '销量优先' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
  { value: 'newest', label: '最新上架' },
]

const treeProps = { label: 'name', children: 'children' }

async function load() {
  loading.value = true
  try {
    data.value = await productApi.page(query)
    // 筛选状态同步回 URL，便于刷新与分享
    const q: Record<string, string> = {}
    if (query.keyword) q.keyword = query.keyword
    if (query.categoryId) q.categoryId = String(query.categoryId)
    if (query.brandId) q.brandId = String(query.brandId)
    if (query.minPrice) q.minPrice = query.minPrice
    if (query.maxPrice) q.maxPrice = query.maxPrice
    if (query.sort && query.sort !== 'default') q.sort = query.sort
    router.replace({ query: q })
  } catch (e) {
    showApiError(e)
  } finally {
    loading.value = false
  }
}

function onCategoryClick(node: CategoryTreeVO) {
  query.categoryId = node.id === 0 ? undefined : node.id
  query.page = 1
  load()
}

function onFilterChange() {
  query.page = 1
  load()
}

function onReset() {
  query.keyword = undefined
  query.categoryId = undefined
  query.brandId = undefined
  query.minPrice = undefined
  query.maxPrice = undefined
  query.sort = 'default'
  query.page = 1
  load()
}

function onPageChange(p: number) {
  query.page = p
  load()
}

onMounted(async () => {
  load()
  try {
    const tree = await productApi.categoryTree()
    // 加一棵"全部"根节点，点击即清除分类筛选
    categories.value = [{ id: 0, parentId: -1, name: '全部分类', level: 0, sort: 0, children: tree }]
  } catch (e) {
    showApiError(e)
  }
  try {
    brands.value = (await productApi.brands()).list
  } catch (e) {
    showApiError(e)
  }
})
</script>

<template>
  <div class="list-page">
    <el-aside class="aside">
      <el-card shadow="never">
        <template #header><span class="aside-title">商品分类</span></template>
        <el-tree
          :data="categories"
          :props="treeProps"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="onCategoryClick"
        />
      </el-card>
    </el-aside>

    <div class="content">
      <el-card shadow="never" class="filters">
        <el-form inline @submit.prevent>
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="商品名称/副标题"
              clearable
              class="keyword"
              @clear="onFilterChange"
              @keyup.enter="onFilterChange"
            />
          </el-form-item>
          <el-form-item label="品牌">
            <el-select v-model="query.brandId" placeholder="全部品牌" clearable class="brand" @change="onFilterChange">
              <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="价格">
            <el-input v-model="query.minPrice" placeholder="最低" class="price" @keyup.enter="onFilterChange" />
            <span class="price-sep">-</span>
            <el-input v-model="query.maxPrice" placeholder="最高" class="price" @keyup.enter="onFilterChange" />
          </el-form-item>
          <el-form-item label="排序">
            <el-select v-model="query.sort" class="sort" @change="onFilterChange">
              <el-option v-for="o in sortOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onFilterChange">筛选</el-button>
            <el-button @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div v-loading="loading" class="grid-wrap">
        <el-empty v-if="!loading && !data.list.length" description="没有符合条件的商品" />
        <el-row v-else :gutter="16">
          <el-col v-for="p in data.list" :key="p.id" :span="6" class="col">
            <el-card shadow="hover" class="product-card" @click="router.push(`/products/${p.id}`)">
              <el-image :src="p.mainImage" fit="cover" class="thumb" lazy />
              <div class="name">{{ p.name }}</div>
              <div class="subtitle">{{ p.subtitle }}</div>
              <div class="meta">
                <span class="price">{{ priceRangeText(p.priceMin, p.priceMax) }}</span>
                <span class="sales">已售 {{ p.sales }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="data.total"
          :page-size="data.pageSize"
          :current-page="data.page"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.list-page {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  align-items: flex-start;
}

.aside {
  width: 220px;
  flex-shrink: 0;
}

.aside-title {
  font-weight: 600;
}

.content {
  flex: 1;
  min-width: 0;
}

.filters {
  margin-bottom: 16px;
}

.keyword {
  width: 200px;
}

.brand,
.sort {
  width: 140px;
}

.price {
  width: 80px;
}

.price-sep {
  margin: 0 6px;
  color: #909399;
}

.grid-wrap {
  min-height: 320px;
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

.subtitle {
  color: #909399;
  font-size: 12px;
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

.pager {
  display: flex;
  justify-content: center;
  padding: 8px 0 24px;
}
</style>
