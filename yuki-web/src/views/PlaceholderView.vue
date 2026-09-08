<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

// 业务页统一占位：路由骨架已就位，页面到对应阶段再实现
const route = useRoute()
const title = computed(() => (route.meta.title as string) ?? '页面')
</script>

<template>
  <el-result icon="info" :title="title" sub-title="该页面属于后续实施阶段，骨架路由已就位，待实现">
    <template #extra>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </template>
  </el-result>
</template>
