<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="blocked"><p class="eyebrow">ACCOUNT STATUS</p><h1>账号状态与申诉</h1><p class="lead">账号处罚、到期时间和恢复能力必须由平台查询。当前页面尚未接通状态接口，不会用本地开关给出“已恢复”结论。</p><section class="card" role="status"><span class="icon" aria-hidden="true">◎</span><h2>暂无法读取账号状态</h2><p>如果你收到处置通知，请保留通知编号与相关信息。受限账号的帮助、规则及申诉入口应保持可达；申诉提交和处理进度以真实工单为准。</p><div class="links"><router-link to="/help">前往帮助与客服 →</router-link><router-link to="/legal/community">查看社区规范状态 →</router-link></div></section><button type="button" class="back" @click="router.push('/')">返回首页</button></div>
</template>

<style scoped>
.blocked { width: min(720px, calc(100% - 32px)); margin: 48px auto; }.eyebrow { margin: 0 0 9px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }h1 { margin: 0; font-size: clamp(31px, 5vw, 43px); }.lead { color: #64574d; line-height: 1.8; }.card { margin: 25px 0 18px; padding: clamp(24px, 5vw, 40px); background: #fff; border: 1px solid #eadfd4; border-radius: 24px; }.icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 14px; background: #fff1e8; color: #b85111; font-size: 26px; }.card h2 { margin: 20px 0 8px; font-size: 21px; }.card p { color: #64574d; line-height: 1.8; }.links { display: flex; flex-wrap: wrap; gap: 8px 20px; }.links a { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }.back { min-height: 44px; border: 0; background: transparent; color: #b85111; font-weight: 700; }
</style>
