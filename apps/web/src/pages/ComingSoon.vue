<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
const task = String(route.meta.task ?? 'M7');
</script>

<template>
  <div class="coming">
    <p class="tag">{{ task }}</p>
    <h2>页面开发中</h2>
    <p>该路由归属任务 {{ task }}，随 M7 后续轮次落地为真实页面（交互设计 §4 线框为基线）。</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.coming {
  min-height: 60vh;
  display: grid;
  place-content: center;
  text-align: center;
  gap: 8px;
  color: #7a6e63;
}
.tag {
  color: #ff7a2f;
  font-weight: 600;
  letter-spacing: 0.08em;
}
</style>
