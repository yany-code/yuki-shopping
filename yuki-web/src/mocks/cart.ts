import type { CartItemAddDTO, CartItemUpdateDTO, CartItemVO } from '@/types/cart'
import { ApiError } from '@/utils/request'
import { productFixtures } from '@/mocks/product'

// 数据照抄 docs/02_seed.sql 的 t_cart_item（yuki 的 3 条），状态在会话内可变
function seedItem(id: number, skuId: number, quantity: number, checked: number): CartItemVO {
  const info = productFixtures.skuInfo(skuId)
  if (!info) throw new Error(`fixture 缺少 skuId=${skuId}`)
  return {
    id,
    skuId,
    productId: info.productId,
    productName: info.productName,
    skuSpecs: info.skuSpecs,
    image: info.image,
    price: info.price,
    quantity,
    checked,
  }
}

let items: CartItemVO[] = [seedItem(1, 6, 1, 1), seedItem(2, 10, 1, 1), seedItem(3, 18, 1, 0)]
let idSeq = 4

export const cartFixtures = {
  list(): CartItemVO[] {
    return [...items]
  },

  add(dto: CartItemAddDTO): CartItemVO {
    const info = productFixtures.skuInfo(dto.skuId)
    if (!info) throw new ApiError(40400, '商品不存在')
    // uk_user_sku：同一 SKU 累加数量
    const exist = items.find((i) => i.skuId === dto.skuId)
    if (exist) {
      exist.quantity += dto.quantity
      if (dto.checked !== undefined) exist.checked = dto.checked
      return { ...exist }
    }
    const item: CartItemVO = {
      id: idSeq++,
      skuId: dto.skuId,
      productId: info.productId,
      productName: info.productName,
      skuSpecs: info.skuSpecs,
      image: info.image,
      price: info.price,
      quantity: dto.quantity,
      checked: dto.checked ?? 1,
    }
    items.push(item)
    return { ...item }
  },

  update(id: number, dto: CartItemUpdateDTO): CartItemVO {
    const item = items.find((i) => i.id === id)
    if (!item) throw new ApiError(40400, '购物车条目不存在')
    if (dto.quantity !== undefined) {
      if (dto.quantity < 1) throw new ApiError(40000, '数量必须大于 0')
      item.quantity = dto.quantity
    }
    if (dto.checked !== undefined) item.checked = dto.checked
    return { ...item }
  },

  remove(id: number) {
    items = items.filter((i) => i.id !== id)
  },

  checkAll(checked: number) {
    items.forEach((i) => (i.checked = checked))
  },

  /** 下单成功后按 skuId 移除已结算条目（模拟后端"删除已结算购物车条目"） */
  removeBySkuIds(skuIds: number[]) {
    const set = new Set(skuIds)
    items = items.filter((i) => !set.has(i.skuId))
  },
}
