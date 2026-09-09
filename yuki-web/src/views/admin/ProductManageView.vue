<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { productApi } from '@/api/product'
import { showApiError } from '@/utils/feedback'
import { priceRangeText, specsText } from '@/utils/format'
import type { PageResult } from '@/types/api'
import type { BrandVO, CategoryTreeVO, ProductDetailVO, SkuVO } from '@/types/product'
import type { AdminProductVO, ProductSaveDTO, SkuSaveDTO } from '@/types/admin'

const data = ref<PageResult<AdminProductVO>>({ list: [], page: 1, pageSize: 10, total: 0 })
const keyword = ref('')
const status = ref<number | undefined>(undefined)
const loading = ref(false)

async function load(page = 1) {
  loading.value = true
  try {
    data.value = await adminApi.products({
      keyword: keyword.value || undefined,
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

onMounted(() => {
  load()
  loadOptions()
})

// ---- 筛选下拉：分类树（级联，仅叶子可选）与品牌 ----
const categories = ref<CategoryTreeVO[]>([])
const brands = ref<BrandVO[]>([])
const cascaderProps = { value: 'id', label: 'name', emitPath: false }

async function loadOptions() {
  try {
    const [tree, brandPage] = await Promise.all([productApi.categoryTree(), productApi.brands()])
    categories.value = tree
    brands.value = brandPage.list
  } catch (e) {
    showApiError(e)
  }
}

function categoryName(id: number): string {
  const walk = (nodes: CategoryTreeVO[]): string | undefined => {
    for (const n of nodes) {
      if (n.id === id) return n.name
      const hit = n.children && walk(n.children)
      if (hit) return hit
    }
    return undefined
  }
  return walk(categories.value) ?? `#${id}`
}

// ---- 新建 / 编辑商品 ----
const productVisible = ref(false)
const productSaving = ref(false)
const editingId = ref<number | null>(null)
const productFormRef = ref<FormInstance>()
const productForm = reactive<ProductSaveDTO>({
  categoryId: 0,
  brandId: undefined,
  name: '',
  subtitle: '',
  mainImage: '',
  detail: '',
})

const productRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

function openCreate() {
  editingId.value = null
  Object.assign(productForm, { categoryId: 0, brandId: undefined, name: '', subtitle: '', mainImage: '', detail: '' })
  productVisible.value = true
}

async function openEdit(row: AdminProductVO) {
  editingId.value = row.id
  Object.assign(productForm, {
    categoryId: row.categoryId,
    brandId: row.brandId,
    name: row.name,
    subtitle: row.subtitle ?? '',
    mainImage: row.mainImage ?? '',
    detail: '',
  })
  productVisible.value = true
  // detail 不在列表项里，单独拉一次（下架商品也走管理端详情）
  try {
    const d = await adminApi.productDetail(row.id)
    productForm.detail = d.detail ?? ''
  } catch (e) {
    showApiError(e)
  }
}

async function saveProduct() {
  await productFormRef.value?.validate()
  productSaving.value = true
  try {
    if (editingId.value == null) {
      await adminApi.createProduct(productForm)
      ElMessage.success('商品已创建，请继续添加 SKU')
    } else {
      await adminApi.updateProduct(editingId.value, productForm)
      ElMessage.success('商品已更新')
    }
    productVisible.value = false
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  } finally {
    productSaving.value = false
  }
}

// ---- 上架 / 下架 ----
async function toggleStatus(row: AdminProductVO) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    next === 0 ? `下架后「${row.name}」将对用户不可见，确定？` : `确定上架「${row.name}」？`,
    next === 0 ? '下架商品' : '上架商品',
    { type: 'warning' },
  )
  try {
    await adminApi.setProductStatus(row.id, next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  }
}

// ---- SKU 管理 ----
const skuVisible = ref(false)
const skuLoading = ref(false)
const skuProduct = ref<ProductDetailVO | null>(null)
const skuEditingId = ref<number | null>(null)
const skuForm = reactive<SkuSaveDTO>({ specs: '', price: '', stock: 0, image: '' })

async function openSkus(row: AdminProductVO) {
  skuVisible.value = true
  skuEditingId.value = null
  Object.assign(skuForm, { specs: '', price: '', stock: 0, image: '' })
  await loadSkus(row.id)
}

async function loadSkus(productId: number) {
  skuLoading.value = true
  try {
    skuProduct.value = await adminApi.productDetail(productId)
  } catch (e) {
    showApiError(e)
  } finally {
    skuLoading.value = false
  }
}

function editSku(sku: SkuVO) {
  skuEditingId.value = sku.id
  Object.assign(skuForm, {
    skuCode: sku.skuCode,
    specs: sku.specs,
    price: sku.price,
    stock: sku.stock,
    image: sku.image ?? '',
  })
}

function resetSkuForm() {
  skuEditingId.value = null
  Object.assign(skuForm, { skuCode: undefined, specs: '', price: '', stock: 0, image: '' })
}

async function saveSku() {
  if (!skuProduct.value) return
  try {
    JSON.parse(skuForm.specs)
  } catch {
    ElMessage.warning('规格必须是合法 JSON，如 {"颜色":"黑色"}')
    return
  }
  if (!/^\d+(\.\d{1,2})?$/.test(skuForm.price)) {
    ElMessage.warning('价格格式不正确，如 99.90')
    return
  }
  try {
    if (skuEditingId.value == null) {
      await adminApi.addSku(skuProduct.value.id, { ...skuForm, image: skuForm.image || undefined })
      ElMessage.success('SKU 已添加')
    } else {
      await adminApi.updateSku(skuEditingId.value, { ...skuForm, image: skuForm.image || undefined })
      ElMessage.success('SKU 已更新')
    }
    resetSkuForm()
    await loadSkus(skuProduct.value.id)
    await load(data.value.page)
  } catch (e) {
    showApiError(e)
  }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="商品名称关键词" clearable class="kw" @keyup.enter="load(1)" />
      <el-select v-model="status" placeholder="全部状态" clearable class="status-select">
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="openCreate">新建商品</el-button>
    </div>

    <el-table v-loading="loading" :data="data.list" border>
      <el-table-column label="商品" min-width="280">
        <template #default="{ row }">
          <div class="cell-product">
            <el-image :src="row.mainImage" fit="cover" class="cell-img" />
            <div>
              <div>{{ row.name }}</div>
              <div class="cell-sub">{{ row.subtitle }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="110">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column label="价格区间" width="140">
        <template #default="{ row }">{{ priceRangeText(row.priceMin, row.priceMax) }}</template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" align="right" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="primary" plain @click="openSkus(row)">SKU</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
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

    <!-- 新建 / 编辑商品 -->
    <el-dialog v-model="productVisible" :title="editingId == null ? '新建商品' : '编辑商品'" width="560px">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="productForm.subtitle" maxlength="200" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="productForm.categoryId"
            :options="categories"
            :props="cascaderProps"
            placeholder="选择三级分类"
            class="full"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="productForm.brandId" clearable placeholder="选择品牌" class="full">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主图 URL">
          <el-input v-model="productForm.mainImage" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="图文详情">
          <el-input v-model="productForm.detail" type="textarea" :rows="4" placeholder="支持 HTML 片段" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSaving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- SKU 管理 -->
    <el-drawer v-model="skuVisible" :title="`SKU 管理 · ${skuProduct?.name ?? ''}`" size="640px">
      <div v-loading="skuLoading">
        <el-table :data="skuProduct?.skus ?? []" border size="small">
          <el-table-column prop="skuCode" label="编码" width="90" />
          <el-table-column label="规格" min-width="160">
            <template #default="{ row }">{{ specsText(row.specs) }}</template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="90" align="right" />
          <el-table-column prop="stock" label="库存" width="70" align="right" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button size="small" link type="primary" @click="editSku(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-divider>{{ skuEditingId == null ? '新增 SKU' : `编辑 SKU #${skuEditingId}` }}</el-divider>
        <el-form label-width="90px" size="small">
          <el-form-item label="规格 JSON">
            <el-input v-model="skuForm.specs" placeholder='{"颜色":"黑色","存储":"256GB"}' />
          </el-form-item>
          <el-form-item label="价格">
            <el-input v-model="skuForm.price" placeholder="99.90" class="sku-price" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="skuForm.stock" :min="0" :max="999999" />
          </el-form-item>
          <el-form-item label="图片 URL">
            <el-input v-model="skuForm.image" placeholder="可选" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSku">{{ skuEditingId == null ? '添加' : '保存' }}</el-button>
            <el-button v-if="skuEditingId != null" @click="resetSkuForm">取消编辑</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.kw {
  width: 220px;
}

.status-select {
  width: 140px;
}

.cell-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cell-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}

.cell-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.full {
  width: 100%;
}

.sku-price {
  width: 160px;
}
</style>
