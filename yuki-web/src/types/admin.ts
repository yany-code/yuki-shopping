// 与后端 admin 模块 DTO/VO 对应（api接口库.md 第 4 节）
import type { OrderStatus } from '@/types/order'

/** 管理端商品列表项：比客户端多 status/categoryId/brandId */
export interface AdminProductVO {
  id: number
  categoryId: number
  brandId?: number
  name: string
  subtitle?: string
  mainImage?: string
  priceMin: string
  priceMax: string
  sales: number
  /** 0-下架 1-上架 */
  status: number
}

export interface AdminProductQuery {
  keyword?: string
  /** 0-下架 1-上架，不传查全部 */
  status?: number
  page?: number
  pageSize?: number
}

/** 创建/修改 SPU；SKU 走独立接口 */
export interface ProductSaveDTO {
  categoryId: number
  brandId?: number
  name: string
  subtitle?: string
  mainImage?: string
  detail?: string
}

export interface SkuSaveDTO {
  skuCode?: string
  /** 规格组合 JSON 字符串，如 {"颜色":"黑"} */
  specs: string
  price: string
  stock: number
  image?: string
}

export interface AdminOrderQuery {
  status?: OrderStatus
  orderNo?: string
  page?: number
  pageSize?: number
}

/** 发货请求：仅 20 已支付待发货可操作 */
export interface OrderShipDTO {
  expressCompany: string
  expressNo: string
}

/** 管理端评价项：关联出商品名，便于审核 */
export interface AdminReviewVO {
  id: number
  productId: number
  productName: string
  nickname?: string
  rating: number
  content?: string
  isAnonymous: number
  /** 0-隐藏 1-显示 */
  status: number
  createdAt: string
}

export interface AdminReviewQuery {
  /** 0-隐藏 1-显示，不传查全部 */
  status?: number
  page?: number
  pageSize?: number
}

export interface CategorySaveDTO {
  /** 修改时携带；不传为新增 */
  id?: number
  /** 0 表示新增一级分类 */
  parentId: number
  name: string
  sort: number
}
