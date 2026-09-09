// 与后端 cart 模块 DTO/VO 对应
export interface CartItemVO {
  id: number
  skuId: number
  productId: number
  /** 以下为加入购物车时刻的快照 */
  productName: string
  /** 快照格式："颜色:黑;存储:256G" */
  skuSpecs: string
  image?: string
  price: string
  quantity: number
  /** 结算勾选 0-否 1-是 */
  checked: number
}

export interface CartItemAddDTO {
  skuId: number
  quantity: number
  checked?: number
}

export interface CartItemUpdateDTO {
  quantity?: number
  checked?: number
}
