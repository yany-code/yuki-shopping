import type { PageResult } from '@/types/api'
import type {
  CreateOrderResult,
  OrderCreateDTO,
  OrderDetailVO,
  OrderItemDTO,
  OrderItemVO,
  OrderListVO,
  OrderPreviewVO,
  OrderQuery,
  PaymentCallbackDTO,
  PayResultVO,
  ReviewCreateDTO,
} from '@/types/order'
import type { ReviewVO } from '@/types/product'
import type { AdminOrderQuery, OrderShipDTO } from '@/types/admin'
import { formatCents, toCents } from '@/utils/money'
import { ApiError } from '@/utils/request'
import { cartFixtures } from '@/mocks/cart'
import { productFixtures } from '@/mocks/product'

// 数据照抄 docs/02_seed.sql 中 yuki 的订单（订单 3 属于 tom，按数据隔离不出现）
// 另补充一个 status=30 的已发货订单，让「确认收货」流程可演示

function now(): string {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 镜像种子库中 yuki 的地址（下单只读快照，不回查） */
const seedAddresses: Record<number, { receiverName: string; receiverPhone: string; receiverAddress: string }> = {
  1: { receiverName: '由岐', receiverPhone: '13800001234', receiverAddress: '北京市 北京市 海淀区 中关村大街1号 3号楼 502 室' },
  2: { receiverName: '由岐', receiverPhone: '13800001234', receiverAddress: '上海市 上海市 浦东新区 世纪大道100号 环球金融中心 32F' },
}

function snapshotItem(id: number, skuId: number, quantity: number): OrderItemVO {
  const info = productFixtures.skuInfo(skuId)
  if (!info) throw new Error(`fixture 缺少 skuId=${skuId}`)
  return {
    id,
    productId: info.productId,
    skuId,
    productName: info.productName,
    skuSpecs: info.skuSpecs,
    image: info.image,
    price: info.price,
    quantity,
    subtotal: formatCents(toCents(info.price) * quantity),
  }
}

const orders: OrderDetailVO[] = [
  {
    orderNo: 'YK20260901101530001', status: 40,
    totalAmount: '8999.00', freightAmount: '0.00', discountAmount: '0.00', payAmount: '8999.00',
    receiverName: '由岐', receiverPhone: '13800001234',
    receiverAddress: '北京市 北京市 海淀区 中关村大街1号 3号楼 502 室',
    expressCompany: '顺丰速运', expressNo: 'SF1234567890',
    payTime: '2026-09-01 10:17:05', deliveryTime: '2026-09-02 09:30:00', finishTime: '2026-09-03 18:20:00',
    createdAt: '2026-09-01 10:15:30',
    items: [snapshotItem(1, 1, 1)],
  },
  {
    orderNo: 'YK20260905103000002', status: 10,
    totalAmount: '3197.00', freightAmount: '0.00', discountAmount: '0.00', payAmount: '3197.00',
    receiverName: '由岐', receiverPhone: '13800001234',
    receiverAddress: '北京市 北京市 海淀区 中关村大街1号 3号楼 502 室',
    remark: '尽量发顺丰',
    createdAt: '2026-09-05 10:30:00',
    items: [snapshotItem(2, 10, 1), snapshotItem(3, 8, 2)],
  },
  {
    orderNo: 'YK20260906120000004', status: 30,
    totalAmount: '399.00', freightAmount: '0.00', discountAmount: '0.00', payAmount: '399.00',
    receiverName: '由岐', receiverPhone: '13800001234',
    receiverAddress: '北京市 北京市 海淀区 中关村大街1号 3号楼 502 室',
    expressCompany: '中通快递', expressNo: 'ZT9876543210',
    payTime: '2026-09-06 12:02:10', deliveryTime: '2026-09-07 09:15:00',
    createdAt: '2026-09-06 12:00:00',
    items: [snapshotItem(4, 8, 1)],
  },
]

// 已评价明细（种子：订单 1 的明细 1 已评价，uk_order_item_id 唯一约束）
const reviewedItemIds = new Set<number>([1])

interface PaymentState {
  paymentNo: string
  orderNo: string
  amount: string
  /** 1-成功 2-失败 */
  status: number
  tradeNo?: string
}

// 种子：订单 2 有一笔失败支付流水
const payments = new Map<string, PaymentState>([
  ['PAY20260905103100002', { paymentNo: 'PAY20260905103100002', orderNo: 'YK20260905103000002', amount: '3197.00', status: 2 }],
])

// 创建订单的幂等结果缓存：同一 Idempotency-Key 重复提交返回第一次的结果
const idempotencyCache = new Map<string, CreateOrderResult>()

let orderSeq = 5
let itemSeq = 5
let paySeq = 3

function genOrderNo(): string {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `YK${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}${String(orderSeq++).padStart(4, '0')}`
}

function genPaymentNo(): string {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `PAY${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}${String(paySeq++).padStart(4, '0')}`
}

function buildItems(itemDTOs: OrderItemDTO[]): OrderItemVO[] {
  return itemDTOs.map((i) => {
    const info = productFixtures.skuInfo(i.skuId)
    if (!info) throw new ApiError(40400, '商品不存在')
    return {
      id: itemSeq++,
      productId: info.productId,
      skuId: i.skuId,
      productName: info.productName,
      skuSpecs: info.skuSpecs,
      image: info.image,
      price: info.price,
      quantity: i.quantity,
      subtotal: formatCents(toCents(info.price) * i.quantity),
    }
  })
}

function sumAmount(items: OrderItemVO[]): string {
  return formatCents(items.reduce((sum, i) => sum + toCents(i.subtotal), 0))
}

function toListVO(o: OrderDetailVO): OrderListVO {
  return {
    orderNo: o.orderNo,
    status: o.status,
    totalAmount: o.totalAmount,
    freightAmount: o.freightAmount,
    payAmount: o.payAmount,
    createdAt: o.createdAt,
    items: o.items,
  }
}

function findOrder(orderNo: string): OrderDetailVO {
  const order = orders.find((o) => o.orderNo === orderNo)
  if (!order) throw new ApiError(40400, '订单不存在')
  return order
}

export const orderFixtures = {
  /** 试算：按勾选购物车/SKU 实时价格计算，不扣库存 */
  preview(dto: { items: OrderItemDTO[] }): OrderPreviewVO {
    const items = buildItems(dto.items)
    const totalAmount = sumAmount(items)
    return { items, totalAmount, freightAmount: '0.00', payAmount: totalAmount }
  },

  create(dto: OrderCreateDTO, idempotencyKey?: string): CreateOrderResult {
    if (idempotencyKey) {
      const cached = idempotencyCache.get(idempotencyKey)
      if (cached) return cached
    }

    const addr = seedAddresses[dto.addressId]
    if (!addr) throw new ApiError(40400, '收货地址不存在')
    if (!dto.items.length) throw new ApiError(40000, '订单明细不能为空')

    // 校验上架状态与库存（模拟后端事务前置校验）
    for (const i of dto.items) {
      const info = productFixtures.skuInfo(i.skuId)
      if (!info || !info.onSale) throw new ApiError(40900, '商品已下架')
      if (info.stock < i.quantity) throw new ApiError(40900, `「${info.productName}」库存不足`)
    }
    // 条件扣减，任一失败整单回滚
    const deducted: OrderItemDTO[] = []
    for (const i of dto.items) {
      if (!productFixtures.deductStock(i.skuId, i.quantity)) {
        deducted.forEach((d) => productFixtures.restoreStock(d.skuId, d.quantity))
        throw new ApiError(40900, '库存不足，订单创建失败')
      }
      deducted.push(i)
    }

    const items = buildItems(dto.items)
    const totalAmount = sumAmount(items)
    const order: OrderDetailVO = {
      orderNo: genOrderNo(),
      status: 10,
      totalAmount,
      freightAmount: '0.00',
      discountAmount: '0.00',
      payAmount: totalAmount,
      receiverName: addr.receiverName,
      receiverPhone: addr.receiverPhone,
      receiverAddress: addr.receiverAddress,
      remark: dto.remark,
      createdAt: now(),
      items,
    }
    orders.unshift(order)
    // 删除已结算的购物车条目
    cartFixtures.removeBySkuIds(dto.items.map((i) => i.skuId))

    const result: CreateOrderResult = { orderNo: order.orderNo, status: order.status, payAmount: order.payAmount }
    if (idempotencyKey) idempotencyCache.set(idempotencyKey, result)
    return result
  },

  list(q: OrderQuery): PageResult<OrderListVO> {
    let list = [...orders]
    if (q.status) list = list.filter((o) => o.status === q.status)
    list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
    const page = q.page ?? 1
    const pageSize = q.pageSize ?? 10
    const start = (page - 1) * pageSize
    return {
      list: list.slice(start, start + pageSize).map(toListVO),
      page,
      pageSize,
      total: list.length,
    }
  },

  detail(orderNo: string): OrderDetailVO {
    return findOrder(orderNo)
  },

  /** 取消：10→50 并释放库存，其他状态 40900 */
  cancel(orderNo: string) {
    const order = findOrder(orderNo)
    if (order.status !== 10) throw new ApiError(40900, '当前状态不允许取消')
    order.status = 50
    order.items.forEach((i) => productFixtures.restoreStock(i.skuId, i.quantity))
  },

  /** 确认收货：30→40 */
  confirm(orderNo: string) {
    const order = findOrder(orderNo)
    if (order.status !== 30) throw new ApiError(40900, '当前状态不允许确认收货')
    order.status = 40
    order.finishTime = now()
  },

  /** 发起支付：仅状态 10；同一订单重复发起返回原支付单（幂等） */
  pay(orderNo: string, payType: number): PayResultVO {
    const order = findOrder(orderNo)
    if (order.status !== 10) throw new ApiError(40900, '当前状态不允许支付')
    for (const p of payments.values()) {
      if (p.orderNo === orderNo && p.status !== 1) {
        return { paymentNo: p.paymentNo, payType, expireTime: now() }
      }
    }
    const paymentNo = genPaymentNo()
    payments.set(paymentNo, { paymentNo, orderNo, amount: order.payAmount, status: 2 })
    return { paymentNo, payType, expireTime: now() }
  },

  /** 支付回调：验签（mock 校验金额一致）、重复成功回调幂等 */
  callback(dto: PaymentCallbackDTO) {
    const payment = payments.get(dto.paymentNo)
    if (!payment) throw new ApiError(40400, '支付流水不存在')
    if (toCents(dto.amount) !== toCents(payment.amount)) throw new ApiError(40900, '回调金额与支付单不一致')
    if (payment.status === 1) return // 重复成功回调直接返回，不重复推进订单
    if (dto.status === 1) {
      payment.status = 1
      payment.tradeNo = dto.tradeNo
      const order = findOrder(payment.orderNo)
      if (order.status === 10) {
        order.status = 20
        order.payTime = now()
      }
    }
  },

  /** 评价：订单归属+状态 40+明细归属+uk_order_item_id 唯一 */
  createReview(orderNo: string, itemId: number, dto: ReviewCreateDTO): ReviewVO {
    const order = findOrder(orderNo)
    if (order.status !== 40) throw new ApiError(40900, '订单未完成，暂不能评价')
    const item = order.items.find((i) => i.id === itemId)
    if (!item) throw new ApiError(40400, '订单明细不存在')
    if (reviewedItemIds.has(itemId)) throw new ApiError(40900, '该明细已评价过')
    reviewedItemIds.add(itemId)
    return productFixtures.addReview({
      userId: 1,
      nickname: dto.isAnonymous === 1 ? '匿**' : '由岐',
      productId: item.productId,
      skuId: item.skuId,
      rating: dto.rating,
      content: dto.content,
      images: dto.images,
      isAnonymous: dto.isAnonymous ?? 0,
    })
  },

  // ---- 管理端操作（无用户隔离，模拟后端管理端全量查询） ----

  adminList(q: AdminOrderQuery): PageResult<OrderListVO> {
    let list = [...orders]
    if (q.status) list = list.filter((o) => o.status === q.status)
    if (q.orderNo) list = list.filter((o) => o.orderNo.includes(q.orderNo!))
    list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
    const page = q.page ?? 1
    const pageSize = q.pageSize ?? 10
    const start = (page - 1) * pageSize
    return {
      list: list.slice(start, start + pageSize).map(toListVO),
      page,
      pageSize,
      total: list.length,
    }
  },

  /** 发货：仅 20 已支付待发货 → 30，写入物流信息与发货时间 */
  ship(orderNo: string, dto: OrderShipDTO) {
    const order = findOrder(orderNo)
    if (order.status !== 20) throw new ApiError(40900, '仅待发货订单可发货')
    order.status = 30
    order.expressCompany = dto.expressCompany
    order.expressNo = dto.expressNo
    order.deliveryTime = now()
  },
}
