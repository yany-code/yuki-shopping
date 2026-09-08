import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'
import router from '@/router'
import type { LoginDTO, RegisterDTO } from '@/types/auth'
import type { UserVO } from '@/types/user'
import { userTokens } from '@/utils/token'

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserVO | null>(null)
  const isLogined = computed(() => !!userTokens.getAccess())

  async function login(dto: LoginDTO) {
    const token = await authApi.login(dto)
    userTokens.set(token.accessToken, token.refreshToken)
    profile.value = await userApi.me()
  }

  async function register(dto: RegisterDTO) {
    const token = await authApi.register(dto) // 注册即登录，后端直接发 Token
    userTokens.set(token.accessToken, token.refreshToken)
    profile.value = await userApi.me()
  }

  async function loadProfile() {
    profile.value = isLogined.value ? await userApi.me() : null
  }

  function logoutLocal() {
    userTokens.clear()
    profile.value = null
  }

  function redirectToLogin() {
    const current = router.currentRoute.value
    if (current.path === '/login') return
    router.push({ path: '/login', query: { redirect: current.fullPath } }).catch(() => {})
  }

  async function logout() {
    logoutLocal() // 后续后端加登出接口时在此补调用
  }

  return { profile, isLogined, login, register, loadProfile, logoutLocal, redirectToLogin, logout }
})
