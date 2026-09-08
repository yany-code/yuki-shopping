// 与后端 user 模块 DTO/VO 对应；可空字段标 ?（Jackson non_null：null 字段整个缺失）
export interface UserVO {
  id: number
  username: string
  nickname: string
  phone?: string
  email?: string
  avatar?: string
}

export interface UserProfileDTO {
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
}

export interface AddressVO {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detail: string
  /** 默认地址 0-否 1-是 */
  isDefault: number
}

export interface AddressDTO {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detail: string
  /** 默认地址 0-否 1-是，缺省按 0 处理 */
  isDefault?: number
}
