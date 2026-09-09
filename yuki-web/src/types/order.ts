// 与后端 order/payment/review 模块 DTO/VO 对应
export interface OrderItemVO {
  id: number
  productId: number
  skuId: number
  /** 下单时刻快照 */
  productName: string
  /** 快照格式："颜色:黑;存储:256G" */
  skuSpecs: string
  image?: string
  price: string
  quantity: number
  subtotal: string
}

export interface OrderPreviewVO {
  items: OrderItemVO[]
  totalAmount: string
  freightAmount: string
  /** 应付 = 总金额 + 运费 - 优惠 */
  payAmount: string
}

export interface OrderItemDTO {
  skuId: number
  quantity: number
}

export interface OrderCreateDTO {
  addressId: number
  items: OrderItemDTO[]
  remark?: string
}

/** 创建订单必须返回的三要素（接口返回值约定） */
export interface CreateOrderResult {
  orderNo: string
  status: number
  payAmount: string
}

/** 10待支付 20待发货 30已发货 40已完成 50已取消 60已退款 */
export type OrderStatus = 10 | 20 | 30 | 40 | 50 | 60

export interface OrderListVO {
  orderNo: string
  status: OrderStatus
  totalAmount: string
  freightAmount: string
  payAmount: string
  /** yyyy-MM-dd HH:mm:ss */
  createdAt: string
  items: OrderItemVO[]
}

export interface OrderDetailVO {
  orderNo: string
  status: OrderStatus
  totalAmount: string
  freightAmount: string
  discountAmount: string
  payAmount: string
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  remark?: string
  expressCompany?: string
  expressNo?: string
  payTime?: string
  deliveryTime?: string
  finishTime?: string
  createdAt: string
  items: OrderItemVO[]
}

export interface OrderQuery {
  status?: OrderStatus
  page?: number
  pageSize?: number
}

export interface PayResultVO {
  paymentNo: string
  /** 1-模拟支付 2-支付宝 3-微信 */
  payType: number
  payParams?: Record<string, string>
  expireTime?: string
}

export interface PaymentCallbackDTO {
  paymentNo: string
  amount: string
  /** 1-成功 2-失败 */
  status: number
  tradeNo?: string
}

export interface ReviewCreateDTO {
  /** 评分 1~5 */
  rating: number
  content: string
  images?: string[]
  /** 匿名 0-否 1-是 */
  isAnonymous?: number
}
