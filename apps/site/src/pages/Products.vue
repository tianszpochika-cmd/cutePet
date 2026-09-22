<script setup lang="ts">
import { PRODUCTS, FOUR_SECTIONS } from '../domain/site';
</script>

<template>
  <div class="products">
    <h1>产品与服务</h1>
    <p class="lead">四端共享同一份契约（153 路由 / 32 权限点），体验一致、部署独立。</p>

    <section class="grid">
      <article v-for="p in PRODUCTS" :key="p.id" class="card">
        <h2>{{ p.label }}</h2>
        <p>{{ p.desc }}</p>
        <ul>
          <li>固定端口：{{ p.port }}</li>
          <li>状态：设计预览版（运行验证待本地阶段）</li>
        </ul>
        <router-link class="link" to="/download">{{ p.cta }} →</router-link>
      </article>
    </section>

    <section class="features">
      <h2>四大能力</h2>
      <div class="grid">
        <article v-for="s in FOUR_SECTIONS" :key="s.id" class="feature">
          <h3>{{ s.label }}</h3>
          <p>{{ s.desc }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.products { max-width: 1120px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 28px; }
h1 { font-size: 34px; margin: 0; }
.lead { color: #7a6e63; font-size: 16px; margin: 0; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 18px; }
.card p { margin: 0; color: #7a6e63; font-size: 14px; line-height: 1.7; }
.card ul { margin: 0; padding-left: 18px; color: #2b2118; font-size: 13px; display: grid; gap: 6px; }
.link { color: #ff7a2f; text-decoration: none; font-weight: 600; font-size: 14px; }
.features h2 { font-size: 22px; }
.feature { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 20px; }
.feature h3 { margin: 0 0 8px; font-size: 16px; }
.feature p { margin: 0; color: #7a6e63; font-size: 14px; }
</style>
