<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminTokens } from '@/utils/token'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

function logout() {
  adminTokens.clear()
  router.push('/admin/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <div class="brand">由岐商城 · 管理后台</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/reviews">评价审核</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title ?? '管理后台' }}</span>
        <div class="header-right">
          <router-link to="/" class="mall-link">返回商城</router-link>
          <el-button size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  border-bottom: 1px solid #e4e7ed;
}

.menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mall-link {
  font-size: 13px;
  font-weight: 400;
  color: var(--el-color-primary);
  text-decoration: none;
}
</style>
