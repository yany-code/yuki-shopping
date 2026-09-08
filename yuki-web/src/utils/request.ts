import axios from 'axios'
import type { ApiResponse } from '@/types/api'
import type { TokenVO } from '@/types/auth'
import { userTokens } from '@/utils/token'
import { useUserStore } from '@/stores/user'

declare module 'axios' {
  interface InternalAxiosRequestConfig {
    _retried?: boolean
  }
}

export class ApiError extends Error {
  code: number
  traceId?: string

  constructor(code: number, message: string, traceId?: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.traceId = traceId
  }
}

export const http = axios.create({ baseURL: '/api/v1', timeout: 10000 })

http.interceptors.request.use((config) => {
  const token = userTokens.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 单飞刷新：并发多个 40100 只发一次 refresh 请求
let refreshing: Promise<boolean> | null = null

async function tryRefresh(): Promise<boolean> {
  if (!userTokens.getRefresh()) return false
  refreshing ??= doRefresh().finally(() => (refreshing = null))
  return refreshing
}

async function doRefresh(): Promise<boolean> {
  try {
    // 用裸 axios，避免走自己的拦截器造成递归
    const res = await axios.post<ApiResponse<TokenVO>>('/api/v1/auth/refresh', {
      refreshToken: userTokens.getRefresh(),
    })
    if (res.data.code !== 0) throw new Error(res.data.message)
    // 轮换语义：旧 refresh 已作废，两个 token 必须原子地一起换
    userTokens.set(res.data.data.accessToken, res.data.data.refreshToken)
    return true
  } catch {
    // useUserStore() 必须惰性调用：模块加载期 Pinia 尚未激活，
    // 且 request → store → api → request 存在循环依赖，顶格调用会拿到未初始化的实例
    useUserStore().logoutLocal()
    return false
  }
}

http.interceptors.response.use(
  async (res) => {
    const body = res.data as ApiResponse<unknown>
    if (body.code === 0) return body.data as never // 成功：解包 data，调用方拿到的就是业务数据

    if (body.code === 40100 && !res.config._retried) {
      if (await tryRefresh()) {
        res.config._retried = true
        return http.request(res.config) as never
      }
      useUserStore().redirectToLogin()
    }
    return Promise.reject(new ApiError(body.code, body.message ?? '请求失败', body.traceId))
  },
  // 后端约定 HTTP 恒为 200，走到这里只有超时/断网等传输层错误，统一包装避免原生 AxiosError 漏到页面
  () => Promise.reject(new ApiError(-1, '网络异常，请稍后重试')),
)
