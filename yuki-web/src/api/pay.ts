import { http } from '@/utils/request'
import { orderFixtures } from '@/mocks/order'
import { takeIdempotencyKey } from '@/utils/idempotency'
import type { PaymentCallbackDTO, PayResultVO } from '@/types/order'

// 后端阶段四完成后删掉各函数的 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const payApi = {
  /** 发起支付：幂等键保证重复点击只创建一笔支付流水 */
  pay: async (orderNo: string, payType = 1): Promise<PayResultVO> => {
    if (USE_MOCK) return orderFixtures.pay(orderNo, payType)
    return http.post<unknown, PayResultVO>(
      `/orders/${orderNo}/pay`,
      { payType },
      { headers: { 'Idempotency-Key': takeIdempotencyKey() } },
    )
  },

  /**
   * 模拟支付渠道回调（Demo 专用）：真实环境中该接口由支付渠道调用，
   * 前端只在模拟支付演示时直连；验签方式需与后端阶段四约定
   */
  callback: async (dto: PaymentCallbackDTO): Promise<void> => {
    if (USE_MOCK) return orderFixtures.callback(dto)
    return http.post<unknown, void>('/payments/callback', dto)
  },
}
