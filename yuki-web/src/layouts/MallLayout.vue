<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const activeMenu = computed(() => '/' + (route.path.split('/')[1] ?? ''))

async function onLogout() {
  await user.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <el-container class="mall-layout">
    <el-header class="header">
      <router-link to="/" class="logo">由岐商城</router-link>

      <el-menu :default-active="activeMenu" mode="horizontal" :ellipsis="false" router class="nav">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/products">商品</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/orders">我的订单</el-menu-item>
      </el-menu>

      <div class="account">
        <template v-if="user.isLogined">
          <el-dropdown>
            <span class="nickname">{{ user.profile?.nickname ?? '我的' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人资料</el-dropdown-item>
                <el-dropdown-item @click="router.push('/addresses')">收货地址</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">由岐商城 · 学习项目</el-footer>
  </el-container>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-color-primary);
  white-space: nowrap;
}

.nav {
  flex: 1;
  border-bottom: none;
}

.account {
  display: flex;
  align-items: center;
}

.nickname {
  cursor: pointer;
  color: var(--el-color-primary);
}

.main {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
