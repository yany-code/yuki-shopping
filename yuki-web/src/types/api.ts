// 与 docs/api接口库.md 2.2 节一一对应
export interface ApiResponse<T> {
  code: number // 0=成功；40000/40100/40300/40400/40900/42900/50000
  message: string
  data: T
  traceId: string
}

export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}
