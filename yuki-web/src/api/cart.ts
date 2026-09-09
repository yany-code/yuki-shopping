import { http } from '@/utils/request'
import { cartFixtures } from '@/mocks/cart'
import type { CartItemAddDTO, CartItemUpdateDTO, CartItemVO } from '@/types/cart'

// 后端阶段三完成后删掉各函数的 mock 分支
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1'

export const cartApi = {
  list: async (): Promise<CartItemVO[]> => {
    if (USE_MOCK) return cartFixtures.list()
    return http.get<unknown, CartItemVO[]>('/cart')
  },

  add: async (dto: CartItemAddDTO): Promise<CartItemVO> => {
    if (USE_MOCK) return cartFixtures.add(dto)
    return http.post<unknown, CartItemVO>('/cart/items', dto)
  },

  update: async (id: number, dto: CartItemUpdateDTO): Promise<CartItemVO> => {
    if (USE_MOCK) return cartFixtures.update(id, dto)
    return http.put<unknown, CartItemVO>(`/cart/items/${id}`, dto)
  },

  remove: async (id: number): Promise<void> => {
    if (USE_MOCK) return cartFixtures.remove(id)
    return http.delete<unknown, void>(`/cart/items/${id}`)
  },

  checkAll: async (checked: number): Promise<void> => {
    if (USE_MOCK) return cartFixtures.checkAll(checked)
    return http.put<unknown, void>('/cart/items/check-all', { checked })
  },
}
