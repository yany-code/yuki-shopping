import { http } from '@/utils/request'
import { orderFixtures } from '@/mocks/order'
import type { ReviewCreateDTO } from '@/types/order'
import type { ReviewVO } from '@/types/product'

// 后端阶段五完成后删掉 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const reviewApi = {
  create: async (orderNo: string, itemId: number, dto: ReviewCreateDTO): Promise<ReviewVO> => {
    if (USE_MOCK) return orderFixtures.createReview(orderNo, itemId, dto)
    return http.post<unknown, ReviewVO>(`/orders/${orderNo}/items/${itemId}/review`, dto)
  },
}
