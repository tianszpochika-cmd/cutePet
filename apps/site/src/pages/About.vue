<script setup lang="ts">
import { BRAND_TOKENS } from '../domain/site';

const FACTS = [
  { k: '成立', v: '2026（筹备中，占位信息待负责人确认）' },
  { k: '定位', v: '一站式宠物生活服务：管理 · 资讯 · 探索 · 用品（纯内容导购）' },
  { k: '团队', v: '产品 / 设计 / 工程 / 运营（名单待补充）' },
  { k: '合规', v: '先审后发 · 举报48h · 方案甲未成年人流程 · 非商店分发记录在案' },
];
</script>

<template>
  <div class="about">
    <h1>关于我们</h1>

    <section class="facts">
      <div v-for="f in FACTS" :key="f.k" class="row">
        <span>{{ f.k }}</span>
        <strong>{{ f.v }}</strong>
      </div>
    </section>

    <section class="brand">
      <h2>品牌展示</h2>
      <div class="logo-row">
        <span class="logo-mark">🐾</span>
        <div>
          <p class="name">{{ BRAND_TOKENS.name }}</p>
          <p class="note">{{ BRAND_TOKENS.logoNote }}</p>
        </div>
      </div>

      <h3>色彩</h3>
      <div class="colors">
        <div v-for="c in BRAND_TOKENS.colors" :key="c.token" class="swatch">
          <span class="chip" :style="{ background: c.value }" />
          <code>{{ c.value }}</code>
          <em>{{ c.usage }}</em>
        </div>
      </div>

      <h3>字体</h3>
      <ul><li v-for="f in BRAND_TOKENS.fonts" :key="f">{{ f }}</li></ul>

      <h3>动效原则</h3>
      <ul><li v-for="m in BRAND_TOKENS.motion" :key="m">{{ m }}</li></ul>
    </section>

    <router-link class="cta" to="/contact">联系我们 →</router-link>
  </div>
</template>

<style scoped>
.about { max-width: 860px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 26px; }
h1 { font-size: 32px; margin: 0; }
.facts { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 8px 24px; }
.row { display: flex; gap: 20px; padding: 16px 0; border-bottom: 1px solid #f0e6dc; font-size: 14px; }
.row:last-child { border-bottom: none; }
.row span { color: #7a6e63; width: 48px; flex: none; }
.brand { background: #fff9f3; border-radius: 24px; padding: 28px; display: grid; gap: 14px; }
.brand h2 { margin: 0; font-size: 20px; }
.brand h3 { margin: 8px 0 0; font-size: 15px; color: #7a6e63; }
.logo-row { display: flex; gap: 16px; align-items: center; }
.logo-mark { font-size: 46px; background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px 18px; }
.name { margin: 0; font-weight: 800; font-size: 20px; }
.note { margin: 6px 0 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.colors { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; }
.swatch { background: #fff; border-radius: 12px; padding: 12px; display: grid; gap: 6px; }
.chip { height: 40px; border-radius: 8px; box-shadow: inset 0 0 0 1px rgba(43,33,24,.08); }
.swatch code { font-size: 12px; color: #2b2118; }
.swatch em { font-style: normal; font-size: 12px; color: #7a6e63; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 6px; font-size: 14px; color: #2b2118; }
.cta { justify-self: start; background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 30px; text-decoration: none; font-weight: 600; }
</style>
