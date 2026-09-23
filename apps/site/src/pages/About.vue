<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isBrandPage = computed(() => route.name === 'siteBrand');

const principles = [
  { number: '01', title: '照护有记录', description: '让体重、健康事件和日常处理留在同一只宠物的时间线里，回看时知道发生过什么。' },
  { number: '02', title: '下一次有安排', description: '把需要再次处理的事从记录中理清，区分未来计划与本次待办。' },
  { number: '03', title: '家人有边界', description: '由宠物所有者决定共享哪只宠物、给谁查看或管理；加入家庭不会自动公开个人档案。' },
] as const;

const palette = [
  { name: '暖橘', value: '#FF7A2F', description: '品牌图形与插画' },
  { name: '深橘', value: '#B85111', description: '清楚可读的主要按钮' },
  { name: '奶油白', value: '#FFF9F3', description: '轻柔的页面底色' },
  { name: '深墨', value: '#2B2118', description: '主要文字' },
] as const;
</script>

<template>
  <div class="about site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span>
      <span aria-current="page">{{ isBrandPage ? '品牌展示' : '关于我们' }}</span>
    </nav>

    <template v-if="isBrandPage">
      <header class="hero brand-hero">
        <div>
          <p class="site-eyebrow">BRAND IN PROGRESS</p>
          <h1 class="site-section-title">一个温暖、清楚的<br />养宠日常入口。</h1>
          <p class="site-lead">目前展示的是产品视觉方向。cutePet 为工作名称，爪印与配色仍待正式确认；本页不作为可授权使用的品牌规范。</p>
        </div>
        <div class="brand-preview" role="img" aria-label="cutePet 工作名称与爪印占位标识的视觉示意">
          <span class="paw" aria-hidden="true">
            <svg viewBox="0 0 32 32" fill="currentColor"><path d="M16 24.8c-3.4 0-4.5 2.2-7.1 1.1-2.4-1-2.8-4.1-.5-6.1 2.4-2.2 3.7-6.6 7.6-6.6s5.2 4.4 7.6 6.6c2.3 2 .8 5.1-1.5 6.1-2.6 1.1-3.8-1.1-6.8-1.1Z"/><ellipse cx="7.2" cy="10.4" rx="2.4" ry="3.5"/><ellipse cx="13" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="19.4" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="25" cy="10.4" rx="2.4" ry="3.5"/></svg>
          </span>
          <strong>cutePet<span>.</span></strong>
          <small>标识示意 · 未定稿</small>
        </div>
      </header>

      <section class="palette-section" aria-labelledby="palette-title">
        <p class="site-eyebrow">COLOR DIRECTION</p>
        <h2 id="palette-title">让信息暖一些，也清楚一些。</h2>
        <p>轻暖底色承托照护内容；主要行动使用对比更明确的深橘。颜色只辅助理解，状态仍需文字说明。</p>
        <div class="palette">
          <article v-for="color in palette" :key="color.value">
            <span class="swatch" :style="{ background: color.value }" aria-hidden="true"></span>
            <strong>{{ color.name }}</strong>
            <span>{{ color.description }}</span>
          </article>
        </div>
      </section>
      <div class="bottom-link"><router-link class="site-text-link" to="/about">了解产品方向 <span aria-hidden="true">→</span></router-link></div>
    </template>

    <template v-else>
      <header class="hero about-hero">
        <div>
          <p class="site-eyebrow">ABOUT CUTEPET</p>
          <h1 class="site-section-title">把每一份在意，<br />变成有条理的照顾。</h1>
          <p class="site-lead">我们正在设计一个围绕宠物照护的产品：记录已发生的事，安排下一次任务，也让共同照顾的家人知道自己能做什么。</p>
          <div class="hero-actions">
            <router-link class="site-button" to="/products">了解产品能力 <span aria-hidden="true">→</span></router-link>
            <router-link class="site-button outline" to="/help">查看帮助</router-link>
          </div>
        </div>
        <div class="story" aria-label="产品方向：记录、安排、协作">
          <div><span>01</span><strong>留下照护记录</strong></div>
          <div><span>02</span><strong>看见下一次安排</strong></div>
          <div><span>03</span><strong>与家人安心协作</strong></div>
        </div>
      </header>

      <section class="principles" aria-labelledby="principles-title">
        <p class="site-eyebrow">WHAT WE VALUE</p>
        <h2 id="principles-title">从真实的照护任务出发。</h2>
        <div class="principle-grid">
          <article v-for="item in principles" :key="item.number">
            <span>{{ item.number }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section class="status" aria-labelledby="status-title">
        <div>
          <p class="site-eyebrow">CURRENT STATUS</p>
          <h2 id="status-title">产品仍在开发与评审中</h2>
          <p>正式品牌名称、运营主体、团队介绍、联系渠道和备案信息尚未核实，因此这里不展示成立时间、人员名单或资质编号。可用入口与规则状态会在对应页面明确说明。</p>
        </div>
        <div class="status-links">
          <router-link class="site-text-link" to="/download">查看使用入口状态 <span aria-hidden="true">→</span></router-link>
          <router-link class="site-text-link" to="/contact">查看联系渠道状态 <span aria-hidden="true">→</span></router-link>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.about { padding-block: 28px 100px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.hero { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(280px, .9fr); align-items: center; gap: clamp(25px, 5vw, 80px); padding: clamp(54px, 7vw, 110px) 0 86px; }
.hero .site-lead { max-width: 650px; }
.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 30px; }
.story { display: grid; gap: 12px; padding: 32px; border: 1px solid #efd8c5; border-radius: 28px; background: linear-gradient(145deg, #fff0e3, #fffaf5); }
.story div { display: flex; align-items: center; gap: 18px; min-height: 70px; padding: 12px 19px; border: 1px solid #f0dfd0; border-radius: 15px; background: #fff; }
.story div:nth-child(2) { margin-left: 24px; }
.story div:nth-child(3) { margin-left: 48px; }
.story span { color: var(--site-action); font-size: 12px; font-weight: 800; }
.story strong { font-size: 16px; }
.principles { padding: 22px 0 86px; }
.principles h2, .palette-section h2 { margin: 0 0 25px; font-size: clamp(25px, 3vw, 35px); line-height: 1.3; }
.principle-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.principle-grid article { min-height: 220px; padding: 28px; border: 1px solid var(--site-line); border-radius: 20px; background: #fff; }
.principle-grid article span { color: var(--site-action); font-size: 12px; font-weight: 800; }
.principle-grid h3 { margin: 23px 0 8px; font-size: 21px; }
.principle-grid p { margin: 0; color: var(--site-muted); font-size: 14px; line-height: 1.85; }
.status { display: flex; align-items: center; justify-content: space-between; gap: 32px; padding: clamp(25px, 4vw, 40px); border: 1px solid var(--site-line); border-radius: 22px; background: #fff; }
.status h2 { margin: 0 0 10px; font-size: clamp(22px, 2.5vw, 29px); }
.status p:not(.site-eyebrow) { max-width: 62ch; margin: 0; color: var(--site-muted); font-size: 14px; line-height: 1.8; }
.status-links { display: grid; gap: 7px; flex: none; }
.brand-preview { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 290px; border: 1px solid #efd8c5; border-radius: 28px; background: radial-gradient(circle at 70% 35%, #ffddbd, transparent 55%), #fff1e8; }
.paw { display: grid; place-items: center; width: 80px; height: 80px; border-radius: 25px; background: var(--site-action); color: #fff; transform: rotate(-7deg); }
.paw svg { width: 48px; height: 48px; }
.brand-preview strong { margin-top: 20px; font-size: 34px; letter-spacing: -.06em; }
.brand-preview strong span { color: var(--site-orange); }
.brand-preview small { color: var(--site-muted); }
.palette-section { padding-top: 24px; border-top: 1px solid var(--site-line); }
.palette-section p:not(.site-eyebrow) { max-width: 65ch; margin: -12px 0 28px; color: var(--site-muted); }
.palette { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.palette article { display: grid; gap: 5px; padding: 12px; border: 1px solid var(--site-line); border-radius: 16px; background: #fff; }
.swatch { height: 90px; border: 1px solid rgba(43, 33, 24, .08); border-radius: 10px; }
.palette article strong { margin-top: 8px; }
.palette article span:last-child { color: var(--site-muted); font-size: 12px; }
.bottom-link { margin-top: 30px; }
@media (max-width: 760px) {
  .about { padding-block: 22px 68px; }
  .hero { grid-template-columns: 1fr; padding: 55px 0 68px; }
  .story div:nth-child(2), .story div:nth-child(3) { margin-left: 0; }
  .principle-grid { grid-template-columns: 1fr; }
  .principle-grid article { min-height: 0; }
  .status { flex-direction: column; align-items: flex-start; }
  .status-links { width: 100%; }
  .palette { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
