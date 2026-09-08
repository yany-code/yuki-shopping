// 与后端 product 模块 VO 对应；可空字段标 ?（Jackson non_null）
export interface CategoryTreeVO {
  id: number
  parentId: number
  name: string
  /** 层级 1-一级 2-二级 3-三级 */
  level: number
  sort: number
  icon?: string
  children?: CategoryTreeVO[]
}

export interface BrandVO {
  id: number
  name: string
  logo?: string
  description?: string
}

export interface ProductListVO {
  id: number
  name: string
  subtitle?: string
  mainImage?: string
  /** 金额字符串，如 "8999.00"，展示直接用，计算先 toCents */
  priceMin: string
  priceMax: string
  sales: number
}

export interface SkuVO {
  id: number
  skuCode?: string
  /** 规格组合 JSON 字符串，如 {"颜色":"黑","容量":"256G"} */
  specs: string
  price: string
  stock: number
  image?: string
}

/** 评价摘要：api接口库.md 第 5 节要求，后端 VO 尚未加（契约缺口，先按可选处理） */
export interface ReviewSummaryVO {
  ratingAvg: string
  reviewCount: number
}

export interface ProductDetailVO {
  id: number
  categoryId: number
  brandId?: number
  name: string
  subtitle?: string
  mainImage?: string
  /** 图文详情（富文本 HTML） */
  detail?: string
  priceMin: string
  priceMax: string
  sales: number
  skus: SkuVO[]
  images: string[]
  reviewSummary?: ReviewSummaryVO
}

export interface ReviewVO {
  id: number
  userId?: number
  /** 评价人昵称（匿名时后端脱敏） */
  nickname?: string
  productId: number
  skuId?: number
  /** 评分 1~5 星 */
  rating: number
  content?: string
  images?: string[]
  /** 匿名评价 0-否 1-是 */
  isAnonymous: number
  status: number
  /** yyyy-MM-dd HH:mm:ss */
  createdAt: string
}

export type ProductSort = 'default' | 'price_asc' | 'price_desc' | 'sales' | 'newest'

export interface ProductQuery {
  keyword?: string
  categoryId?: number
  brandId?: number
  minPrice?: string
  maxPrice?: string
  sort?: ProductSort
  page?: number
  pageSize?: number
}

export interface ReviewQuery {
  rating?: number
  page?: number
  pageSize?: number
}
