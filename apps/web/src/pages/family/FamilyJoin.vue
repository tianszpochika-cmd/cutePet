<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { JOIN_SCOPE_COPY, DEFAULT_NEW_MEMBER_SHARE } from '../../domain/family';

const route = useRoute();
const router = useRouter();
const code = ref(String(route.query.code ?? ''));
const error = ref('');
const done = ref(false);

async function join() {
  if (!code.value.trim()) {
    error.value = '请输入邀请码';
    return;
  }
  error.value = '';
  try {
    await api.familyJoin({ body: { code: code.value.trim() } });
    done.value = true;
    setTimeout(() => void router.replace('/family'), 800);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加入失败（一人限一个家庭；邀请码 7 天有效）';
  }
}
</script>

<template>
  <div class="join">
    <h1>加入家庭</h1>

    <section class="scope" data-testid="join-scope">
      <h2>共享范围说明</h2>
      <p>{{ JOIN_SCOPE_COPY }}</p>
      <p class="meta">新成员默认档位：{{ DEFAULT_NEW_MEMBER_SHARE === 'READONLY' ? '只读' : '可管理' }}</p>
    </section>

    <input v-model="code" placeholder="邀请码（如 fam7day）" data-testid="join-code" />
    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="done" class="ok" data-testid="join-ok">已加入，正在跳转家庭页…</p>

    <button type="button" class="primary" :disabled="done" data-testid="join-btn" @click="join">
      确认加入
    </button>

    <p class="tip">规则提醒：你自己的宠物仍保持私有；收藏与私有互动不会对家人开放。</p>
  </div>
</template>

<style scoped>
.join { max-width: 480px; margin: 32px auto; padding: 16px; display: grid; gap: 14px; }
.scope { background: #f5f3ff; border-radius: 16px; padding: 16px; display: grid; gap: 8px; }
.scope h2 { font-size: 15px; color: #6d5bd0; margin: 0; }
.scope p { font-size: 14px; line-height: 1.8; color: #2b2118; margin: 0; }
.meta { color: #7a6e63; font-size: 13px; }
input { height: 48px; border-radius: 12px; border: 1px solid #f0e6dc; padding: 0 16px; font-size: 16px; letter-spacing: 0.1em; }
.primary { height: 48px; border: none; border-radius: 999px; background: #9b8cff; color: #fff; font-weight: 600; }
.primary:disabled { opacity: 0.5; }
.tip { color: #7a6e63; font-size: 12px; }
.err { color: #ef4444; font-size: 13px; }
.ok { color: #22c55e; font-size: 13px; }
</style>
