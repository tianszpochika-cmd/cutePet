import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  base: '/admin/',
  plugins: [vue()],
  server: {
    host: '127.0.0.1', port: 18581, strictPort: true,
    proxy: { '/api': { target: 'http://127.0.0.1:18470', changeOrigin: true, rewrite: (path) => path.replace(/^\/api/, '') } },
  },
});
