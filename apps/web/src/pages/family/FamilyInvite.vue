<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

const router = useRouter();
const code = ref('');
const expiresAt = ref('');
const error = ref('');

async function generate() {
  error.value = '';
  try {
    const res = (await api.familyInviteCreate()) as unknown as { code: string; expiresAt: string };
    code.value = res.code;
    expiresAt.value = res.expiresAt;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '生成失败（未登录或未创建家庭）';
  }
}

async function copy() {
  const link = `${location.origin}/family/join?code=${code.value}`;
  await navigator.clipboard?.writeText(link);
  alert('邀请链接已复制（有效期 7 天，可随时重置）');
}
</script>

<template>
  <div class="invite">
    <header>
      <button type="button" class="back" @click="router.push('/family')">‹ 家庭</button>
      <h1>邀请家人</h1>
    </header>

    <div v-if="code" class="code-card" data-testid="invite-card">
      <p class="code">{{ code }}</p>
      <p class="meta">有效期至 {{ expiresAt.slice(0, 10) }}（7 天，决议）</p>
      <div class="actions">
        <button type="button" class="primary" @click="copy">复制邀请链接</button>
        <button type="button" class="ghost" @click="generate">重置邀请码</button>
      </div>
    </div>
    <button v-else type="button" class="primary wide" data-testid="generate" @click="generate">
      生成邀请码
    </button>

    <p v-if="error" class="err">{{ error }}</p>
    <p class="tip">加入前请告知家人：共享范围与默认只读规则（见加入页声明）。</p>
  </div>
</template>

<style scoped>
.invite { max-width: 480px; margin: 32px auto; padding: 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.code-card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px; text-align: center; display: grid; gap: 10px; }
.code { font-size: 28px; font-weight: 700; letter-spacing: 0.2em; color: #9b8cff; }
.meta { color: #7a6e63; font-size: 13px; }
.actions { display: flex; gap: 10px; justify-content: center; }
.primary { height: 44px; padding: 0 20px; border: none; border-radius: 999px; background: #9b8cff; color: #fff; font-weight: 600; }
.wide { width: 100%; }
.ghost { height: 44px; padding: 0 20px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.tip { color: #7a6e63; font-size: 12px; }
.err { color: #ef4444; font-size: 13px; }
</style>
