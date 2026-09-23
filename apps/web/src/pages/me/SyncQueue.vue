<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="sync-page">
    <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
    <p class="eyebrow">OFFLINE & SYNC</p>
    <h1>待同步记录</h1>
    <div class="empty" role="status">
      <span class="icon" aria-hidden="true">↗</span>
      <h2>目前没有可核验的同步队列</h2>
      <p>离线记录、重试和失权处理需要与真实本地草稿及服务端结果关联。此页面暂不生成演示记录，也不会将一次重试显示为已同步。</p>
      <router-link to="/pets">返回宠物档案</router-link>
    </div>
    <p class="footnote">未上传的本地输入不等于平台记录。退出账号前，请在来源页面核对是否已完成保存。</p>
  </div>
</template>

<style scoped>
.sync-page { width: min(680px, calc(100% - 32px)); margin: 42px auto; }
.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }
.eyebrow { margin: 28px 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }
h1 { margin: 0 0 24px; font-size: clamp(31px, 5vw, 43px); }
.empty { padding: clamp(24px, 5vw, 42px); border: 1px solid #eadfd4; border-radius: 24px; background: #fff; }.icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 14px; background: #fff1e8; color: #b85111; font-size: 26px; }.empty h2 { margin: 22px 0 8px; font-size: 20px; }.empty p, .footnote { color: #64574d; line-height: 1.8; }.empty a { display: inline-flex; align-items: center; min-height: 44px; margin-top: 10px; color: #b85111; font-weight: 700; text-decoration: none; }.footnote { font-size: 13px; }
</style>
