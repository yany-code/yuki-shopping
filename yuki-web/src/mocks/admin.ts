import type { LoginDTO, TokenVO } from '@/types/auth'
import { ApiError } from '@/utils/request'

// 账号照抄 docs/02_seed.sql t_admin_user：admin/admin123(超管 role=2)、ops/admin123(普通 role=1)
const admins = [
  { username: 'admin', password: 'admin123', status: 1 },
  { username: 'ops', password: 'admin123', status: 1 },
]

export const adminFixtures = {
  login(dto: LoginDTO): TokenVO {
    const a = admins.find((x) => x.username === dto.username)
    if (!a || a.password !== dto.password) throw new ApiError(40000, '用户名或密码错误')
    if (a.status !== 1) throw new ApiError(40300, '账号已被禁用')
    const ts = Date.now()
    return {
      tokenType: 'Bearer',
      accessToken: `mock-admin-access-${a.username}-${ts}`,
      refreshToken: `mock-admin-refresh-${a.username}-${ts}`,
      expiresIn: 7200,
    }
  },
}
