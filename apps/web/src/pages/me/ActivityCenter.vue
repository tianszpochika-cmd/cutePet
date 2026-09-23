<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const tab = ref<'我的报名' | '我发布的' | '机构认证'>('我的报名');
const tabs = ['我的报名', '我发布的', '机构认证'] as const;
const detail = {
  我的报名: { title: '报名明细尚未接通', body: '当前无法读取你的实际报名记录，因此不会把空列表当成“没有报名”。可先浏览公开活动；报名结果请以平台确认信息为准。', to: '/events', action: '浏览活动' },
  我发布的: { title: '发布记录尚未接通', body: '当前缺少本人活动列表与发布提交接口。你可以预填并检查表单，页面不会展示虚构的活动或发布结果。', to: '/me/activity-center/publish', action: '检查发布条件' },
  机构认证: { title: '认证状态尚未接通', body: '机构身份必须由平台核验，不能在页面上自行切换。认证申请与进度入口接通后会在这里显示。', to: '/help', action: '查看帮助' },
};
</script>

<template>
  <div class="activity-center">
    <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
    <p class="eyebrow">MY ACTIVITIES</p>
    <h1>活动中心</h1>
    <nav class="tabs" aria-label="活动中心分类"><button v-for="item in tabs" :key="item" type="button" :class="{ active: tab === item }" :aria-current="tab === item ? 'page' : undefined" @click="tab = item">{{ item }}</button></nav>
    <section class="state" role="status"><span class="state-icon" aria-hidden="true">◎</span><h2>{{ detail[tab].title }}</h2><p>{{ detail[tab].body }}</p><router-link :to="detail[tab].to">{{ detail[tab].action }} →</router-link></section>
  </div>
</template>

<style scoped>
.activity-center { width: min(780px, calc(100% - 32px)); margin: 42px auto; }.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0 0 25px; font-size: clamp(31px, 5vw, 43px); }.tabs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 23px; }.tabs button { min-height: 44px; padding: 0 18px; border: 1px solid #eadfd4; border-radius: 999px; background: #fff; color: #64574d; }.tabs button.active { border-color: #b85111; background: #b85111; color: #fff; }.state { padding: clamp(24px, 5vw, 42px); border-radius: 24px; background: #fff; border: 1px solid #eadfd4; }.state-icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 14px; background: #fff1e8; color: #b85111; font-size: 25px; }.state h2 { margin: 20px 0 8px; font-size: 22px; }.state p { max-width: 580px; color: #64574d; line-height: 1.8; }.state a { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; text-decoration: none; font-weight: 700; }
</style>
