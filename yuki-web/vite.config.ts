import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    port: 5173,
    proxy: {
      // 后端 context-path 就是 /api/v1，原样转发给 Spring Boot
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
