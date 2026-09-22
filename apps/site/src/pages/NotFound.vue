<script setup lang="ts">
import { SITE_NAV } from '../domain/site';
</script>

<template>
  <div class="nf">
    <p class="code">404</p>
    <h1>页面走丢了 🐾</h1>
    <p class="sub">你要找的页面不存在或已被移动。</p>
    <div class="links">
      <router-link class="primary" to="/">回首页</router-link>
      <router-link v-for="n in SITE_NAV.slice(1, 6)" :key="n.id" class="ghost" :to="n.to">{{ n.label }}</router-link>
    </div>
  </div>
</template>

<style scoped>
.nf { min-height: 64vh; display: grid; place-content: center; text-align: center; gap: 12px; padding: 32px; }
.code { font-size: 88px; font-weight: 800; color: #ffb98a; margin: 0; line-height: 1; }
h1 { font-size: 26px; margin: 0; }
.sub { color: #7a6e63; margin: 0 0 8px; }
.links { display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 12px 26px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 12px 22px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 14px; }
</style>
