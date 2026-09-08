// 与后端 auth 模块 DTO/VO 对应
export interface TokenVO {
  tokenType: string
  accessToken: string
  refreshToken: string
  /** accessToken 有效期（秒） */
  expiresIn: number
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  nickname: string
  phone?: string
  email?: string
}
