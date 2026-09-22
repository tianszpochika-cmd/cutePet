<script setup lang="ts">
import { FOUR_SECTIONS } from '../domain/site';
</script>

<template>
  <div class="campaign">
    <section class="hero">
      <p class="eyebrow">2026 秋季 · 养宠从一条提醒开始</p>
      <h1>把疫苗、驱虫、复诊<br />交给一条时间线</h1>
      <p class="sub">L2 生命链：建档 → 记录 → 曲线 → 摘要，全家共享同一份照护进度。</p>
      <div class="actions">
        <router-link class="primary" to="/download">免费开始</router-link>
        <router-link class="ghost" to="/family">看看家庭共享</router-link>
      </div>
    </section>

    <section class="strip">
      <article v-for="s in FOUR_SECTIONS" :key="s.id">
        <h3>{{ s.label }}</h3>
        <p>{{ s.desc }}</p>
      </article>
    </section>

    <section class="share-demo">
      <h2>分享出去的样子</h2>
      <div class="mini-card">
        <span class="chip">猫</span>
        <strong>幼猫换粮的七个误区</strong>
        <span class="meta">编辑部 · 来源：编辑部整理 · cutePet</span>
        <router-link to="/share/article/puppy-food">预览分享页 →</router-link>
      </div>
      <p class="meta">公开分享与来源标注同源（U78/U92）；内容下架后分享页显示不可用态而非空白。</p>
    </section>

    <router-link class="cta" to="/download">立即体验 →</router-link>
  </div>
</template>

<style scoped>
.campaign { max-width: 960px; margin: 0 auto; padding: 32px 24px; display: grid; gap: 44px; }
.hero { text-align: center; background: linear-gradient(180deg, #fff1e8, transparent); border-radius: 32px; padding: 72px 16px 56px; display: grid; gap: 18px; justify-items: center; }
.eyebrow { color: #ff7a2f; font-weight: 700; letter-spacing: .1em; font-size: 13px; margin: 0; }
h1 { font-size: clamp(32px, 6vw, 52px); line-height: 1.25; margin: 0; }
.sub { color: #7a6e63; font-size: 17px; max-width: 520px; margin: 0; line-height: 1.8; }
.actions { display: flex; gap: 14px; flex-wrap: wrap; justify-content: center; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 14px 32px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 14px 32px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
.strip { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
.strip article { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 20px; }
.strip h3 { margin: 0 0 8px; font-size: 15px; }
.strip p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.share-demo { display: grid; gap: 14px; justify-items: center; text-align: center; }
.share-demo h2 { margin: 0; font-size: 20px; }
.mini-card { width: min(360px, 100%); background: #fff; border-radius: 22px; box-shadow: 0 14px 36px rgba(43,33,24,.1); padding: 22px; display: grid; gap: 8px; justify-items: start; text-align: left; }
.chip { background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 3px 12px; font-size: 12px; font-weight: 600; }
.meta { color: #7a6e63; font-size: 13px; margin: 0; }
.mini-card a { color: #ff7a2f; text-decoration: none; font-size: 13px; font-weight: 600; }
.cta { justify-self: center; background: #ff7a2f; color: #fff; border-radius: 999px; padding: 15px 40px; text-decoration: none; font-weight: 700; font-size: 16px; }
</style>
