import { adminHttp } from '@/utils/request'
import { adminFixtures } from '@/mocks/admin'
import { productFixtures } from '@/mocks/product'
import { orderFixtures } from '@/mocks/order'
import type { PageResult } from '@/types/api'
import type { LoginDTO, TokenVO } from '@/types/auth'
import type { CategoryTreeVO, ProductDetailVO, SkuVO } from '@/types/product'
import type { OrderDetailVO, OrderListVO } from '@/types/order'
import type {
  AdminOrderQuery,
  AdminProductQuery,
  AdminProductVO,
  AdminReviewQuery,
  AdminReviewVO,
  CategorySaveDTO,
  OrderShipDTO,
  ProductSaveDTO,
  SkuSaveDTO,
} from '@/types/admin'

// 后端阶段六完成后删掉各函数的 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const adminApi = {
  login: async (dto: LoginDTO): Promise<TokenVO> => {
    if (USE_MOCK) return adminFixtures.login(dto)
    return adminHttp.post<unknown, TokenVO>('/admin/auth/login', dto)
  },

  // ---- 商品管理 ----

  products: async (q: AdminProductQuery): Promise<PageResult<AdminProductVO>> => {
    if (USE_MOCK) return productFixtures.adminPage(q)
    return adminHttp.get<unknown, PageResult<AdminProductVO>>('/admin/products', { params: q })
  },

  /** 契约缺口：接口库未列管理端商品详情，按 REST 惯例预测 GET /admin/products/{id} */
  productDetail: async (id: number): Promise<ProductDetailVO> => {
    if (USE_MOCK) return productFixtures.adminDetail(id)
    return adminHttp.get<unknown, ProductDetailVO>(`/admin/products/${id}`)
  },

  createProduct: async (dto: ProductSaveDTO): Promise<AdminProductVO> => {
    if (USE_MOCK) return productFixtures.createProduct(dto)
    return adminHttp.post<unknown, AdminProductVO>('/admin/products', dto)
  },

  updateProduct: async (id: number, dto: ProductSaveDTO): Promise<AdminProductVO> => {
    if (USE_MOCK) return productFixtures.updateProduct(id, dto)
    return adminHttp.put<unknown, AdminProductVO>(`/admin/products/${id}`, dto)
  },

  setProductStatus: async (id: number, status: number): Promise<void> => {
    if (USE_MOCK) return productFixtures.setProductStatus(id, status)
    return adminHttp.put<unknown, void>(`/admin/products/${id}/status`, { status })
  },

  addSku: async (productId: number, dto: SkuSaveDTO): Promise<SkuVO> => {
    if (USE_MOCK) return productFixtures.addSku(productId, dto)
    return adminHttp.post<unknown, SkuVO>(`/admin/products/${productId}/skus`, dto)
  },

  updateSku: async (skuId: number, dto: Partial<SkuSaveDTO>): Promise<SkuVO> => {
    if (USE_MOCK) return productFixtures.updateSku(skuId, dto)
    return adminHttp.put<unknown, SkuVO>(`/admin/skus/${skuId}`, dto)
  },

  // ---- 订单管理 ----

  orders: async (q: AdminOrderQuery): Promise<PageResult<OrderListVO>> => {
    if (USE_MOCK) return orderFixtures.adminList(q)
    return adminHttp.get<unknown, PageResult<OrderListVO>>('/admin/orders', { params: q })
  },

  orderDetail: async (orderNo: string): Promise<OrderDetailVO> => {
    if (USE_MOCK) return orderFixtures.detail(orderNo)
    return adminHttp.get<unknown, OrderDetailVO>(`/admin/orders/${orderNo}`)
  },

  ship: async (orderNo: string, dto: OrderShipDTO): Promise<void> => {
    if (USE_MOCK) return orderFixtures.ship(orderNo, dto)
    return adminHttp.post<unknown, void>(`/admin/orders/${orderNo}/ship`, dto)
  },

  // ---- 评价审核 ----

  reviews: async (q: AdminReviewQuery): Promise<PageResult<AdminReviewVO>> => {
    if (USE_MOCK) return productFixtures.adminReviews(q)
    return adminHttp.get<unknown, PageResult<AdminReviewVO>>('/admin/reviews', { params: q })
  },

  setReviewStatus: async (id: number, status: number): Promise<void> => {
    if (USE_MOCK) return productFixtures.setReviewStatus(id, status)
    return adminHttp.put<unknown, void>(`/admin/reviews/${id}/status`, { status })
  },

  // ---- 分类管理 ----

  categories: async (): Promise<CategoryTreeVO[]> => {
    if (USE_MOCK) return productFixtures.adminCategoryTree()
    return adminHttp.get<unknown, CategoryTreeVO[]>('/admin/categories')
  },

  saveCategory: async (dto: CategorySaveDTO): Promise<CategoryTreeVO> => {
    if (USE_MOCK) return productFixtures.saveCategory(dto)
    if (dto.id != null) return adminHttp.put<unknown, CategoryTreeVO>(`/admin/categories/${dto.id}`, dto)
    return adminHttp.post<unknown, CategoryTreeVO>('/admin/categories', dto)
  },
}
