<script setup lang="ts">
import { useRoute } from 'vue-router';
import { FOUR_SECTIONS, PRODUCTS, ctaFor } from '../domain/site';

const route = useRoute();
const cta = ctaFor(route.path);
</script>

<template>
  <div class="home">
    <section class="hero">
      <p class="eyebrow">宠物管理 · 资讯 · 探索 · 用品</p>
      <h1>让每一天，<br />都更懂你的宠物</h1>
      <p class="sub">
        从建档与健康提醒，到 7 频道资讯、附近场所与纯内容导购——四端一致的宠物生活服务。
      </p>
      <div class="actions">
        <router-link class="primary" :to="cta.to">{{ cta.label }}</router-link>
        <router-link class="ghost" to="/services">了解场景</router-link>
      </div>
    </section>

    <!-- T9.1 四板块 -->
    <section class="grid">
      <article v-for="s in FOUR_SECTIONS" :key="s.id" class="card">
        <h3>{{ s.label }}</h3>
        <p>{{ s.desc }}</p>
        <router-link :to="s.to">了解详情 →</router-link>
      </article>
    </section>

    <!-- T9.1 产品矩阵 -->
    <section class="matrix">
      <h2>产品矩阵</h2>
      <div class="grid4">
        <article v-for="p in PRODUCTS" :key="p.id" class="prod">
          <h3>{{ p.label }}</h3>
          <p>{{ p.desc }}</p>
          <span class="port">端口 {{ p.port }}</span>
          <router-link :to="p.href.startsWith('/') ? p.href : '/download'">{{ p.cta }} →</router-link>
        </article>
      </div>
    </section>

    <section class="banner">
      <h2>严格合规与先审后发</h2>
      <p>
        全站 UGC 先审后发、兽药/处方类禁入、举报 48 小时反馈、未成年人按方案甲年龄分支与监护人流程处理。
        商业交易暂未上线（第一版纯内容导购）。
      </p>
      <router-link class="primary" to="/center">逛逛内容中心</router-link>
    </section>
  </div>
</template>

<style scoped>
.home { max-width: 1120px; margin: 0 auto; padding: 32px 24px; display: grid; gap: 48px; }
.hero { text-align: center; padding: 72px 16px 56px; background: linear-gradient(180deg, #fff1e8, transparent); border-radius: 32px; }
.eyebrow { color: #ff7a2f; font-weight: 600; letter-spacing: .12em; font-size: 13px; margin: 0; }
h1 { font-size: clamp(34px, 6vw, 56px); line-height: 1.2; margin: 14px 0; color: #2b2118; }
.sub { color: #7a6e63; font-size: 17px; max-width: 560px; margin: 0 auto 28px; line-height: 1.8; }
.actions { display: flex; gap: 14px; justify-content: center; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 30px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 13px 30px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px 20px; display: grid; gap: 10px; }
.card h3 { margin: 0; font-size: 17px; }
.card p { margin: 0; color: #7a6e63; font-size: 14px; line-height: 1.7; }
.card a { color: #ff7a2f; text-decoration: none; font-size: 13px; font-weight: 600; }
.matrix h2 { text-align: center; font-size: 24px; }
.grid4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
.prod { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px 20px; display: grid; gap: 8px; }
.prod h3 { margin: 0; font-size: 16px; }
.prod p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.port { color: #4d8dff; font-size: 12px; font-weight: 600; }
.prod a { color: #ff7a2f; text-decoration: none; font-size: 13px; font-weight: 600; }
.banner { background: #fff1e8; border-radius: 24px; padding: 40px 32px; text-align: center; display: grid; gap: 14px; justify-items: center; }
.banner h2 { margin: 0; font-size: 22px; }
.banner p { margin: 0; color: #7a6e63; max-width: 640px; line-height: 1.8; font-size: 14px; }
</style>
