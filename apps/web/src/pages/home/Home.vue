<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';

interface Pet { id: number | string; name: string; species?: string; breed?: string }
interface Pending { id?: number | string; petId?: number | string; petName?: string; title?: string; type?: string; due?: string; nextDue?: string }

const router = useRouter();
const loggedIn = ref(Boolean(getAccessToken()));
const pets = ref<Pet[]>([]);
const pending = ref<Pending[]>([]);
const loading = ref(false);
const error = ref('');
const petLoaded = ref(false);
const pendingLoaded = ref(false);
const featureCards = [
  { number: '01', title: '读一篇有用的内容', body: '从日常照护到成长知识，按主题找到需要的信息。', to: '/news', className: 'news' },
  { number: '02', title: '探索宠物友好地点', body: '按城市与类型浏览场所，出行前再核对开放情况。', to: '/explore', className: 'explore' },
  { number: '03', title: '查找用品资料', body: '整理参数、评测与清单，方便做自己的选择。', to: '/goods', className: 'goods' },
];

async function loadSummary() {
  if (!loggedIn.value) return;
  loading.value = true;
  error.value = '';
  petLoaded.value = false;
  pendingLoaded.value = false;
  const results = await Promise.allSettled([api.petsList(), api.remindersPending()]);
  if (results[0].status === 'fulfilled') {
    const data = results[0].value as unknown;
    pets.value = Array.isArray(data) ? data as Pet[] : (data as { items?: Pet[] })?.items ?? [];
    petLoaded.value = true;
  } else {
    error.value = '宠物摘要暂时无法加载，请到宠物页重试。';
  }
  if (results[1].status === 'fulfilled') {
    const data = results[1].value as unknown;
    pending.value = Array.isArray(data) ? data as Pending[] : (data as { items?: Pending[] })?.items ?? [];
    pendingLoaded.value = true;
  } else if (!error.value) {
    error.value = '提醒摘要暂时无法加载，请到宠物页查看。';
  }
  loading.value = false;
}

onMounted(loadSummary);
</script>

<template>
  <div class="home">
    <section class="hero" aria-labelledby="home-title">
      <div class="hero-copy">
        <p class="eyebrow">YOUR PET, YOUR EVERYDAY</p>
        <h1 id="home-title">照护有记录，<br><em>每一天都有方向。</em></h1>
        <p class="hero-lead">把档案、健康记录和下一次安排放在一起，家人也能看见同一份照护进度。</p>
        <div class="hero-actions">
          <router-link class="button primary" :to="loggedIn ? '/pets/new' : '/login?return=%2Fpets%2Fnew'">{{ loggedIn ? '为宠物建档' : '开始记录' }} <span aria-hidden="true">↗</span></router-link>
          <router-link class="button secondary" to="/news">先看看资讯</router-link>
        </div>
        <p class="hero-note">健康提醒为站内记录与待办，实际送达以通知渠道状态为准。</p>
      </div>
      <div class="hero-art" aria-hidden="true"><div class="art-sun"></div><div class="art-card art-card-one"><span>♡</span><strong>今天也记得陪伴</strong><small>日常记录</small></div><div class="art-card art-card-two"><span>✦</span><strong>下一次安排</strong><small>心里有数</small></div><div class="art-paw">✿</div></div>
    </section>

    <div class="home-main">
      <section v-if="loggedIn" class="dashboard" aria-labelledby="care-title">
        <div class="section-head"><div><p class="eyebrow">DAILY CARE</p><h2 id="care-title">今天的照护</h2></div><router-link to="/pets">查看全部宠物 <span aria-hidden="true">→</span></router-link></div>
        <p v-if="error" class="status error" role="status">{{ error }}</p>
        <p v-if="loading" class="status" role="status">正在读取照护摘要…</p>
        <div v-else class="dashboard-grid">
          <article class="daily-card">
            <div class="card-kicker">平台提醒 <span>{{ pendingLoaded ? pending.length : '—' }}</span></div>
            <template v-if="!pendingLoaded"><p>提醒摘要暂时无法确认，请到宠物档案中查看。</p><router-link to="/pets">查看宠物 <span aria-hidden="true">→</span></router-link></template>
            <template v-else-if="pending.length"><p>有 {{ pending.length }} 项提醒计划，打开后核对详情；健康记录不会自动完成待办。</p><router-link :to="pending[0]?.petId ? `/pets/${pending[0].petId}/reminders` : '/pets'">查看提醒 <span aria-hidden="true">→</span></router-link></template>
            <template v-else><p>当前没有平台提醒。照护可以按自己的节奏继续。</p><router-link to="/pets">查看宠物 <span aria-hidden="true">→</span></router-link></template>
          </article>
          <article class="daily-card soft"><div class="card-kicker">我的宠物 <span>{{ petLoaded ? pets.length : '—' }}</span></div><p>{{ !petLoaded ? '宠物摘要暂时无法确认，请到列表中重试。' : pets.length ? `已有 ${pets.length} 份宠物档案，可继续补充今天的记录。` : '还没有宠物档案，从基本信息开始。' }}</p><router-link :to="petLoaded && !pets.length ? '/pets/new' : '/pets'">{{ petLoaded && !pets.length ? '建立档案' : '打开档案' }} <span aria-hidden="true">→</span></router-link></article>
        </div>
      </section>

      <section v-else class="journey" aria-labelledby="journey-title"><div class="section-head"><div><p class="eyebrow">A SIMPLE ROUTINE</p><h2 id="journey-title">从一条记录，开始更从容的照护</h2></div></div><div class="journey-steps"><div><span>01</span><strong>建立宠物档案</strong><p>把基本资料放在一起，随时查看与补充。</p></div><div><span>02</span><strong>留下照护记录</strong><p>记录体重和健康事件，变化有迹可循。</p></div><div><span>03</span><strong>安排下一次</strong><p>按需要创建提醒，家人能看到同一进度。</p></div></div></section>

      <section class="discover" aria-labelledby="discover-title"><div class="section-head"><div><p class="eyebrow">EXPLORE MORE</p><h2 id="discover-title">照护之外，也有值得发现的事</h2></div></div><div class="feature-grid"><router-link v-for="card in featureCards" :key="card.to" :to="card.to" :class="['feature-card', card.className]"><span class="feature-number">{{ card.number }}</span><div><h3>{{ card.title }}</h3><p>{{ card.body }}</p></div><span class="feature-arrow" aria-hidden="true">↗</span></router-link></div></section>
    </div>
  </div>
</template>

<style scoped>
.home { width: min(1320px, calc(100% - 64px)); margin: auto; }
.hero { min-height: 550px; display: grid; grid-template-columns: 1.05fr .95fr; align-items: center; gap: 36px; padding: 50px 0 58px; }
.hero-copy { position: relative; z-index: 1; }
.eyebrow { margin: 0 0 13px; color: var(--primary); font-size: 12px; font-weight: 800; letter-spacing: .16em; }
h1 { margin: 0; font-size: clamp(46px, 5.3vw, 76px); font-weight: 850; line-height: 1.16; letter-spacing: -.075em; }
h1 em { color: var(--primary); font-style: normal; }
.hero-lead { max-width: 490px; margin: 23px 0 28px; color: var(--ink-2); font-size: 17px; line-height: 1.9; }
.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; }
.button { display: inline-flex; align-items: center; justify-content: center; gap: 22px; min-height: 48px; padding: 0 25px; border-radius: 999px; font-size: 14px; font-weight: 750; text-decoration: none; }
.button.primary { background: var(--primary); color: #fff; }.button.primary:hover { background: #923b0b; }
.button.secondary { background: #fff; color: var(--primary); border: 1px solid var(--line); }.button.secondary:hover { background: var(--primary-soft); }
.hero-note { margin: 18px 0 0; color: var(--ink-2); font-size: 12px; }
.hero-art { position: relative; min-height: 470px; border-radius: 36px; overflow: hidden; background: radial-gradient(circle at 34% 38%, #fff6e8 0 24%, transparent 25%), linear-gradient(145deg, #fbe4d5, #f7d8c3 60%, #f5c6a8); }
.art-sun { position: absolute; width: 270px; height: 270px; top: 48px; right: 52px; border-radius: 50%; background: #fff8e8; box-shadow: 0 0 0 45px rgba(255,255,255,.18); }
.art-card { position: absolute; display: grid; gap: 4px; min-width: 200px; padding: 21px; border: 1px solid #fff; border-radius: 22px; background: rgba(255,255,255,.88); box-shadow: 0 20px 35px rgba(120,75,47,.1); transform: rotate(-7deg); }
.art-card span { color: var(--primary); font-size: 32px; }.art-card strong { font-size: 17px; }.art-card small { color: var(--ink-2); }
.art-card-one { top: 62px; left: 45px; }.art-card-two { right: 38px; bottom: 62px; transform: rotate(7deg); }
.art-paw { position: absolute; left: 39%; top: 40%; color: #b85111; font-size: 150px; line-height: 1; opacity: .32; }
.home-main { display: grid; gap: 76px; padding-top: 20px; }
.section-head { display: flex; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 24px; }.section-head h2 { margin: 0; font-size: clamp(27px, 3vw, 39px); }.section-head a { color: var(--primary); font-weight: 700; text-decoration: none; }
.dashboard-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; }.daily-card { min-height: 215px; display: flex; flex-direction: column; align-items: flex-start; padding: 27px; border-radius: 24px; background: #fff; border: 1px solid var(--line); }.daily-card.soft { background: #fff1e8; border-color: transparent; }.card-kicker { width: 100%; display: flex; justify-content: space-between; color: var(--ink); font-size: 17px; font-weight: 750; }.card-kicker span { color: var(--primary); font-size: 28px; line-height: 1; }.daily-card p { max-width: 360px; color: var(--ink-2); }.daily-card a { margin-top: auto; min-height: 44px; display: inline-flex; align-items: center; gap: 10px; color: var(--primary); font-weight: 750; text-decoration: none; }
.status { padding: 14px 18px; border-radius: 12px; background: #fff; }.status.error { background: #fff0ed; color: #a63322; }
.journey-steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }.journey-steps > div { min-height: 170px; padding: 24px; border-radius: 20px; background: #fff; border: 1px solid var(--line); }.journey-steps span { color: var(--primary); font-size: 13px; font-weight: 800; }.journey-steps strong { display: block; margin-top: 15px; font-size: 18px; }.journey-steps p { margin-bottom: 0; color: var(--ink-2); font-size: 14px; }
.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }.feature-card { min-height: 220px; display: flex; flex-direction: column; justify-content: space-between; padding: 25px; border-radius: 22px; color: var(--ink); text-decoration: none; transition: transform .2s var(--e-out); }.feature-card:hover { transform: translateY(-4px); }.feature-card.news { background: #f5e9de; }.feature-card.explore { background: #e5f2e7; }.feature-card.goods { background: #f6e5dd; }.feature-number { color: var(--primary); font-size: 13px; font-weight: 800; }.feature-card h3 { margin: 0; font-size: 21px; }.feature-card p { color: var(--ink-2); font-size: 14px; }.feature-arrow { align-self: flex-end; color: var(--primary); font-size: 23px; }
@media (max-width: 850px) { .hero { grid-template-columns: 1fr; }.hero-art { min-height: 320px; }.hero-copy { padding-top: 8px; } }
@media (max-width: 650px) { .home { width: calc(100% - 32px); }.hero { gap: 24px; padding: 38px 0; }.hero-art { min-height: 270px; }.art-card { min-width: 145px; padding: 14px; }.art-card strong { font-size: 13px; }.art-card-one { top: 24px; left: 20px; }.art-card-two { right: 15px; bottom: 18px; }.art-paw { font-size: 100px; }.home-main { gap: 54px; }.dashboard-grid, .journey-steps, .feature-grid { grid-template-columns: 1fr; }.section-head { align-items: start; flex-direction: column; }.feature-card { min-height: 165px; } }
</style>
