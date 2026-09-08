import { http } from '@/utils/request'
import { ApiError } from '@/utils/request'
import { productFixtures } from '@/mocks/product'
import type { PageResult } from '@/types/api'
import type {
  BrandVO,
  CategoryTreeVO,
  ProductDetailVO,
  ProductListVO,
  ProductQuery,
  ReviewQuery,
  ReviewVO,
} from '@/types/product'

// .env.development 中 VITE_USE_MOCK=1 开启；后端阶段二完成后删掉各函数的 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const productApi = {
  categoryTree: async (): Promise<CategoryTreeVO[]> => {
    if (USE_MOCK) return productFixtures.tree()
    return http.get<unknown, CategoryTreeVO[]>('/categories/tree')
  },

  brands: async (page = 1, pageSize = 100): Promise<PageResult<BrandVO>> => {
    if (USE_MOCK) return productFixtures.brands(page, pageSize)
    return http.get<unknown, PageResult<BrandVO>>('/brands', { params: { page, pageSize } })
  },

  page: async (q: ProductQuery): Promise<PageResult<ProductListVO>> => {
    if (USE_MOCK) return productFixtures.page(q)
    return http.get<unknown, PageResult<ProductListVO>>('/products', { params: q })
  },

  detail: async (id: number): Promise<ProductDetailVO> => {
    if (USE_MOCK) {
      const detail = productFixtures.detail(id)
      if (!detail) throw new ApiError(40400, '商品不存在或已下架')
      return detail
    }
    return http.get<unknown, ProductDetailVO>(`/products/${id}`)
  },

  reviews: async (productId: number, q: ReviewQuery): Promise<PageResult<ReviewVO>> => {
    if (USE_MOCK) return productFixtures.reviews(productId, q)
    return http.get<unknown, PageResult<ReviewVO>>(`/products/${productId}/reviews`, { params: q })
  },
}
