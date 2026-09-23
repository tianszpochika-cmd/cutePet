<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const signupId = String(route.params.id ?? '');
</script>

<template>
  <div class="signup-detail"><button type="button" class="back" @click="router.push('/me/activity-center')">‹ 活动中心</button><p class="eyebrow">SIGNUP DETAIL</p><h1>报名详情</h1><section class="state" role="status"><span class="icon" aria-hidden="true">◎</span><h2>暂不能核对这条报名</h2><p>报名 #{{ signupId }} 尚无可供当前页面读取的本人详情接口。活动名称、地点、报名状态和变更历史必须来自平台，页面不会生成示例凭证或在本地模拟取消。</p><router-link to="/me/activity-center">返回活动中心 →</router-link></section></div>
</template>

<style scoped>
.signup-detail { width: min(700px, calc(100% - 32px)); margin: 42px auto; }.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0 0 24px; font-size: clamp(31px, 5vw, 43px); }.state { padding: clamp(24px, 5vw, 40px); background: #fff; border: 1px solid #eadfd4; border-radius: 24px; }.icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 14px; background: #fff1e8; color: #b85111; font-size: 26px; }.state h2 { margin: 20px 0 8px; font-size: 21px; }.state p { color: #64574d; line-height: 1.8; }.state a { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }
</style>
