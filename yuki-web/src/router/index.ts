import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminTokens } from '@/utils/token'

const Placeholder = () => import('@/views/PlaceholderView.vue')

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue'), meta: { title: '注册' } },
  {
    path: '/',
    component: () => import('@/layouts/MallLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { title: '首页' } },
      { path: 'products', name: 'products', component: () => import('@/views/product/ProductListView.vue'), meta: { title: '商品列表' } },
      { path: 'products/:id', name: 'product-detail', component: () => import('@/views/product/ProductDetailView.vue'), meta: { title: '商品详情' } },
      { path: 'cart', name: 'cart', component: Placeholder, meta: { requiresAuth: true, title: '购物车 · 阶段三' } },
      { path: 'checkout', name: 'checkout', component: Placeholder, meta: { requiresAuth: true, title: '结算 · 阶段四' } },
      { path: 'orders', name: 'orders', component: Placeholder, meta: { requiresAuth: true, title: '我的订单 · 阶段四' } },
      { path: 'orders/:orderNo', name: 'order-detail', component: Placeholder, meta: { requiresAuth: true, title: '订单详情 · 阶段四' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/user/ProfileView.vue'), meta: { requiresAuth: true, title: '个人资料' } },
      { path: 'addresses', name: 'addresses', component: () => import('@/views/user/AddressListView.vue'), meta: { requiresAuth: true, title: '收货地址' } },
    ],
  },
  { path: '/admin/login', name: 'admin-login', component: Placeholder, meta: { title: '管理端登录 · 阶段六' } },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: 'products', name: 'admin-products', component: Placeholder, meta: { title: '商品管理 · 阶段六' } },
      { path: 'orders', name: 'admin-orders', component: Placeholder, meta: { title: '订单管理 · 阶段六' } },
      { path: 'reviews', name: 'admin-reviews', component: Placeholder, meta: { title: '评价审核 · 阶段六' } },
      { path: 'categories', name: 'admin-categories', component: Placeholder, meta: { title: '分类管理 · 阶段六' } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { title: '页面不存在' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - 由岐商城` : '由岐商城'
  const user = useUserStore()

  if (to.meta.requiresAuth && !user.isLogined) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  // 刷新页面后首次进登录区：拉一次 /users/me 恢复资料（token 失效会在拦截器里走刷新/登出）
  if (to.meta.requiresAuth && !user.profile) {
    try {
      await user.loadProfile()
    } catch {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
  // requiresAdmin 仅做 UI 级判断，真正的权限以后端 hasRole("ADMIN")/40300 为准
  if (to.meta.requiresAdmin && !adminTokens.getAccess()) {
    return { path: '/admin/login' }
  }
  return true
})

export default router
