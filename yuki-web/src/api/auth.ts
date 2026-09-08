import { http } from '@/utils/request'
import type { LoginDTO, RegisterDTO, TokenVO } from '@/types/auth'

export const authApi = {
  // 拦截器已把响应解包为 data，所以泛型第二参就是业务类型
  login: (dto: LoginDTO) => http.post<unknown, TokenVO>('/auth/login', dto),
  register: (dto: RegisterDTO) => http.post<unknown, TokenVO>('/auth/register', dto),
  // 不提供 refresh：刷新由 request.ts 单飞机制内部完成，业务代码直调会绕过它
}
