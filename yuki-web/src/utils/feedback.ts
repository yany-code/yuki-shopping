import { ElMessage } from 'element-plus'
import { ApiError } from '@/utils/request'

/** 页面 catch 里统一调用：业务错误带 traceId 展示，非 ApiError 静默忽略（如守卫已处理跳转） */
export function showApiError(e: unknown) {
  if (e instanceof ApiError) {
    ElMessage.error(e.traceId ? `${e.message}（${e.traceId}）` : e.message)
  }
}
