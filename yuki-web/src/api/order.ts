import { http } from '@/utils/request'
import { orderFixtures } from '@/mocks/order'
import { takeIdempotencyKey } from '@/utils/idempotency'
import type { PageResult } from '@/types/api'
import type {
  CreateOrderResult,
  OrderCreateDTO,
  OrderDetailVO,
  OrderItemDTO,
  OrderListVO,
  OrderPreviewVO,
  OrderQuery,
} from '@/types/order'

// 后端阶段四完成后删掉各函数的 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const orderApi = {
  preview: async (items: OrderItemDTO[]): Promise<OrderPreviewVO> => {
    if (USE_MOCK) return orderFixtures.preview({ items })
    return http.post<unknown, OrderPreviewVO>('/orders/preview', { items })
  },

  /** 创建订单：幂等键保证重试/双击不产生重复订单 */
  create: async (dto: OrderCreateDTO): Promise<CreateOrderResult> => {
    if (USE_MOCK) return orderFixtures.create(dto, takeIdempotencyKey())
    return http.post<unknown, CreateOrderResult>('/orders', dto, {
      headers: { 'Idempotency-Key': takeIdempotencyKey() },
    })
  },

  list: async (q: OrderQuery): Promise<PageResult<OrderListVO>> => {
    if (USE_MOCK) return orderFixtures.list(q)
    return http.get<unknown, PageResult<OrderListVO>>('/orders', { params: q })
  },

  detail: async (orderNo: string): Promise<OrderDetailVO> => {
    if (USE_MOCK) return orderFixtures.detail(orderNo)
    return http.get<unknown, OrderDetailVO>(`/orders/${orderNo}`)
  },

  cancel: async (orderNo: string): Promise<void> => {
    if (USE_MOCK) return orderFixtures.cancel(orderNo)
    return http.post<unknown, void>(`/orders/${orderNo}/cancel`)
  },

  confirm: async (orderNo: string): Promise<void> => {
    if (USE_MOCK) return orderFixtures.confirm(orderNo)
    return http.post<unknown, void>(`/orders/${orderNo}/confirm`)
  },
}
