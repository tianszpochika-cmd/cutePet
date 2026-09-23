<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { reviewStateCopy } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const submissionId = String(route.params.id ?? '');
const authorized = Boolean(getAccessToken());
const loading = ref(true);
const error = ref('');
const state = ref('');
async function load() {
  if (!authorized) return;
  loading.value = true;
  error.value = '';
  state.value = '';
  try {
    const result = await api.submissionList();
    if (!Array.isArray(result)) throw new Error('投稿记录暂时无法读取');
    const row = (result as { submissionId?: number; state?: string }[]).find((item) => String(item.submissionId ?? '') === submissionId);
    state.value = row?.state ?? '';
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '投稿记录暂时无法读取';
  } finally { loading.value = false; }
}
onMounted(() => {
  if (!authorized) void router.replace({ path: '/login', query: { return: route.fullPath, resume: 'version' } });
  else void load();
});
</script>

<template>
  <div class="version-page">
    <nav class="breadcrumb"><router-link to="/submissions">我的投稿</router-link><span>／</span><span>版本对比</span></nav>
    <header><p class="eyebrow">REVISION HISTORY</p><h1>确认每一次修改。</h1><p>只有读取到真实的线上版与修改稿，才能准确比较内容。</p></header>
    <section v-if="!authorized" class="state">正在前往登录页…</section>
    <section v-else-if="loading" class="state" role="status">正在核对投稿状态…</section>
    <section v-else-if="error" class="state error" role="alert"><h2>暂时无法核对投稿</h2><p>{{ error }}</p><button type="button" @click="load">重试</button></section>
    <section v-else class="state"><span v-if="state" class="badge">{{ reviewStateCopy(state).label }}</span><h2>版本对比暂不可用</h2><p>投稿 #{{ submissionId }} 的线上正文与修改稿目前无法完整读取。为了避免比较到错误版本，这里暂不显示差异，也不提供审核操作。</p><p v-if="!state">当前列表也尚未返回这篇投稿的可核对状态。</p><router-link to="/submissions">返回我的投稿 →</router-link></section>
  </div>
</template>

<style scoped>
.version-page{width:min(100% - 32px,800px);margin:0 auto;padding:27px 0 90px}.breadcrumb{display:flex;gap:8px;color:var(--ink-2);font-size:13px}.breadcrumb a{text-decoration:none}.eyebrow{margin:0 0 12px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.13em}header{margin:55px 0 27px}header h1{margin:0;font-size:clamp(32px,4vw,48px)}header p:not(.eyebrow){color:var(--ink-2)}.state{min-height:235px;padding:clamp(25px,4vw,40px);border:1px solid var(--line);border-radius:22px;background:#fff;color:var(--ink-2)}.state h2{margin:13px 0;font-size:24px;color:var(--ink)}.state p{max-width:590px;line-height:1.8}.state a{display:inline-flex;margin-top:9px;color:var(--primary);font-weight:750;text-decoration:none}.state button{border:0;background:none;color:var(--primary);font-weight:750}.state.error{color:#a63322}.badge{display:inline-block;padding:4px 11px;border-radius:999px;background:var(--primary-soft);color:var(--primary);font-size:12px;font-weight:800}@media(max-width:650px){header{margin-top:37px}}
</style>
