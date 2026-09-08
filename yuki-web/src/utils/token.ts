// 学习项目存 localStorage；升级方案是 refresh 放 httpOnly cookie，需后端配合
function createTokenStorage(prefix: string) {
  return {
    getAccess: () => localStorage.getItem(`${prefix}.access`),
    getRefresh: () => localStorage.getItem(`${prefix}.refresh`),
    set: (access: string, refresh: string) => {
      localStorage.setItem(`${prefix}.access`, access)
      localStorage.setItem(`${prefix}.refresh`, refresh)
    },
    clear: () => {
      localStorage.removeItem(`${prefix}.access`)
      localStorage.removeItem(`${prefix}.refresh`)
    },
  }
}

export const userTokens = createTokenStorage('yuki.user')
export const adminTokens = createTokenStorage('yuki.admin') // 管理后台单独一套，互不干扰
