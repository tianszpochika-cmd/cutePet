<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { EXPORT_STEPS } from '../../domain/settings';

const router = useRouter();
const code = ref('');
const sent = ref(false);
const result = ref('');
const error = ref('');

async function requestExport() {
  error.value = '';
  if (code.value !== '123456') {
    error.value = '身份核验码不正确（dev 环境为 123456）';
    return;
  }
  try {
    const res = (await api.meExport()) as unknown as { state: string; format: string };
    result.value = `导出请求已受理（${res.format ?? 'JSON'}），生成后经消息中心下发；每次导出留痕。`;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '申请失败（未登录会进入登录闸门）';
  }
}
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

    <input v-model="code" placeholder="输入核验码（dev: 123456）" data-testid="export-code" inputmode="numeric" />
    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="result" class="ok" data-testid="export-result">{{ result }}</p>

    <button type="button" class="primary" data-testid="export-btn" @click="requestExport">申请导出 JSON</button>
  </div>
</template>

<style scoped>
.export { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.steps { padding-left: 20px; display: grid; gap: 8px; color: #2b2118; font-size: 14px; }
input { height: 48px; border-radius: 12px; border: 1px solid #f0e6dc; padding: 0 16px; font-size: 15px; }
.primary { height: 48px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.err { color: #ef4444; font-size: 13px; }
.ok { color: #22c55e; font-size: 13px; }
</style>
