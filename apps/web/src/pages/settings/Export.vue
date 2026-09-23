<script setup lang="ts">
import { useRouter } from 'vue-router';
import { EXPORT_STEPS } from '../../domain/settings';

const router = useRouter();
</script>

<template>
  <div class="export">
    <header>
      <button type="button" class="back" @click="router.push('/settings')">‹ 设置</button>
      <h1>数据导出</h1>
    </header>

    <ol class="steps">
      <li v-for="s in EXPORT_STEPS" :key="s">{{ s }}</li>
    </ol>

    <p class="notice" role="status">当前导出接口尚未接入独立身份核验。为保护你的宠物与账号资料，申请入口将在核验流程接通后开放。</p>
    <button type="button" class="primary" disabled data-testid="export-btn">申请导出暂不可用</button>
  </div>
</template>

<style scoped>
.export { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #b85111; }
.steps { padding-left: 20px; display: grid; gap: 8px; color: #2b2118; font-size: 14px; }
input { height: 48px; border-radius: 12px; border: 1px solid #f0e6dc; padding: 0 16px; font-size: 15px; }
.primary { height: 48px; border: none; border-radius: 999px; background: #b85111; color: #fff; font-weight: 600; }
.primary:disabled { opacity: .58; cursor: not-allowed; }
.notice { margin: 0; padding: 16px; border-radius: 14px; background: #fff1e8; color: #674323; line-height: 1.7; }
.err { color: #ef4444; font-size: 13px; }
.ok { color: #22c55e; font-size: 13px; }
</style>
