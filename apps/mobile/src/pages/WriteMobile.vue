<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { CHANNELS } from '../../../web/src/domain/news';
import { SUBMISSION_KINDS, submitBlockers } from '../../../web/src/domain/submission';

const route = useRoute();
const router = useRouter();
const authorized = Boolean(getAccessToken());
const form = reactive({ kind: 'ARTICLE', channel: '猫', title: '', body: '', tags: '', evidence: '', relatedProduct: '', interest: '' });
const attempted = ref(false);
const dirty = ref(false);
const savedHint = ref('');
const error = ref('');
const submissionId = ref('');
const submittedId = ref('');
const operation = ref<'idle' | 'saving' | 'submitting'>('idle');
let revision = 0;
let timer: ReturnType<typeof setTimeout> | null = null;
let saving: Promise<boolean> | null = null;

const blockers = computed(() => {
  if (!attempted.value) return [];
  const messages = submitBlockers(form).map((code) => ({
    KIND_INVALID: '请选择投稿类型', TITLE_INVALID: '标题须为 1–120 字', BODY_TOO_SHORT: '正文至少 10 字', CHANNEL_REQUIRED: '请选择频道',
  } as Record<string, string>)[code] ?? code);
  if (form.kind === 'REVIEW' && !form.evidence.trim()) messages.push('评测请填写实际体验或资料依据');
  if (form.kind === 'REVIEW' && !form.interest) messages.push('评测请说明与商品的利益关系');
  return messages;
});

function clearTimer() { if (timer) clearTimeout(timer); timer = null; }
function touch() {
  revision += 1;
  dirty.value = true;
  savedHint.value = '';
  error.value = '';
  clearTimer();
  if (authorized && !submittedId.value) timer = setTimeout(() => { if (dirty.value) void saveDraft(false); }, 30_000);
}
function bodyForSave(): string {
  const body = form.body.trim();
  if (form.kind !== 'REVIEW') return body;
  const evidence = [
    form.relatedProduct.trim() ? `关联商品：${form.relatedProduct.trim()}` : '',
    form.evidence.trim() ? `体验或资料依据：${form.evidence.trim()}` : '',
    form.interest ? `利益关系：${form.interest}` : '',
  ].filter(Boolean).join('\n');
  return evidence ? `${body}\n\n---\n${evidence}` : body;
}

async function saveDraft(manual = true): Promise<boolean> {
  if (!authorized || submittedId.value) return false;
  if (saving) return saving;
  if (!form.title.trim() || !form.body.trim()) {
    if (manual) error.value = '先填写标题和正文，再保存云端草稿。当前输入仍留在页面中。';
    return false;
  }
  clearTimer();
  const savedRevision = revision;
  const snapshot = { title: form.title.trim(), body: bodyForSave(), tags: form.tags.trim() };
  saving = (async () => {
    operation.value = 'saving';
    error.value = '';
    try {
      if (submissionId.value) {
        const response = await api.submissionUpdate({ params: { id: submissionId.value }, body: snapshot }) as { id?: number; state?: string };
        if (String(response?.id) !== submissionId.value || response.state !== 'DRAFT') throw new Error('未收到草稿更新确认，请稍后核对');
      } else {
        const response = await api.submissionCreate({ body: { kind: form.kind, channel: form.channel, ...snapshot } }) as { submissionId?: number; state?: string };
        if (typeof response?.submissionId !== 'number' || response.state !== 'DRAFT') throw new Error('未收到云端草稿确认，请稍后核对');
        submissionId.value = String(response.submissionId);
      }
      if (savedRevision === revision) {
        dirty.value = false;
        savedHint.value = `草稿 #${submissionId.value} 已保存 · ${new Date().toLocaleTimeString('zh-CN')}`;
      } else {
        touch();
      }
      return true;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '保存失败，请重试；输入仍留在页面中';
      return false;
    } finally { operation.value = 'idle'; saving = null; }
  })();
  return saving;
}

async function submit() {
  attempted.value = true;
  if (!authorized || operation.value !== 'idle' || blockers.value.length || submittedId.value) return;
  const saved = await saveDraft();
  if (!saved || !submissionId.value) return;
  if (dirty.value) { error.value = '保存期间有新修改，请再次确认提交。'; return; }
  operation.value = 'submitting';
  error.value = '';
  try {
    const response = await api.submissionSubmit({ params: { id: submissionId.value } }) as { id?: number; state?: string };
    if (String(response?.id) !== submissionId.value || response.state !== 'PENDING') throw new Error('未收到待审核回执。请先核对投稿状态，避免重复提交。');
    submittedId.value = submissionId.value;
    dirty.value = false;
    savedHint.value = '';
    clearTimer();
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '提交失败，请稍后重试'; }
  finally { operation.value = 'idle'; }
}

onBeforeRouteLeave(() => {
  if (operation.value === 'submitting') {
    window.alert('正在等待平台提交回执，请稍候。');
    return false;
  }
  return !dirty.value || window.confirm('还有未保存的修改，确定离开吗？当前输入可能丢失。');
});
onBeforeUnmount(clearTimer);
</script>

<template>
  <div class="write-page">
    <div class="topline"><button type="button" class="back" aria-label="返回资讯" @click="router.push('/news')">‹</button><span>分享经验</span><span class="top-spacer" /></div>
    <section v-if="!authorized" class="state"><span class="eyebrow">ACCOUNT REQUIRED</span><h1>登录后继续写作</h1><p>投稿需要真实身份与平台保存回执。登录后会返回此页，由你确认是否提交。</p><router-link :to="{ path: '/login', query: { return: route.fullPath, resume: 'write' } }" class="primary-link">前往登录</router-link></section>
    <section v-else-if="submittedId" class="state success" role="status"><span class="eyebrow">SUBMITTED</span><h1>已提交审核</h1><p>稿件 #{{ submittedId }} 已收到平台待审核回执。审核通过前不会公开展示。</p><router-link to="/news" class="primary-link">返回资讯</router-link></section>
    <template v-else>
      <header class="intro"><span class="eyebrow">YOUR STORY</span><h1>把真实经验<br>写给更多人。</h1><p>写清宠物情况、观察与做法。停笔 30 秒后会尝试保存云端草稿。</p></header>
      <div class="editor">
        <div class="kind-row" role="group" aria-label="投稿类型"><button v-for="kind in SUBMISSION_KINDS" :key="kind.kind" type="button" :class="{ on: form.kind === kind.kind }" :disabled="!!submissionId || operation !== 'idle'" @click="form.kind = kind.kind; touch()">{{ kind.label }}</button></div>
        <label class="field">标题<input v-model="form.title" :disabled="operation === 'submitting'" maxlength="120" placeholder="用一句话说清这篇内容" data-testid="title" @input="touch" /></label>
        <label class="field">频道<select v-model="form.channel" :disabled="!!submissionId || operation !== 'idle'" data-testid="channel" @change="touch"><option v-for="name in CHANNELS.slice(1)" :key="name" :value="name">{{ name }}</option></select></label>
        <label class="field">正文<textarea v-model="form.body" :disabled="operation === 'submitting'" rows="10" placeholder="写下亲身经历或可核实的资料，至少 10 字" data-testid="body" @input="touch" /></label>
        <label class="field">标签（可选）<input v-model="form.tags" :disabled="operation === 'submitting'" placeholder="多个标签用逗号分隔" @input="touch" /></label>
        <section v-if="form.kind === 'REVIEW'" class="review-fields"><h2>评测说明</h2><p>以下内容会与正文一起送审，帮助读者理解依据与利益关系。</p><label class="field">关联商品（可选）<input v-model="form.relatedProduct" :disabled="operation === 'submitting'" placeholder="商品名称或可核对的链接" @input="touch" /></label><label class="field">体验或资料依据<input v-model="form.evidence" :disabled="operation === 'submitting'" placeholder="写清实际体验或资料来源" @input="touch" /></label><label class="field">利益关系<select v-model="form.interest" :disabled="operation === 'submitting'" @change="touch"><option value="">请选择</option><option value="无品牌合作">无品牌合作</option><option value="存在合作，已在文中披露">存在合作，已在文中披露</option></select></label></section>
      </div>
      <section class="feedback" aria-live="polite"><p v-for="message in blockers" :key="message" class="error">{{ message }}</p><p v-if="error" class="error" role="alert">{{ error }}</p><p v-else-if="savedHint" class="saved">{{ savedHint }}</p><p v-else-if="dirty" class="muted">当前修改尚未保存到云端</p><p v-else-if="submissionId" class="saved">草稿 #{{ submissionId }} 已由平台确认</p></section>
      <div class="actions"><button type="button" class="secondary" :disabled="operation !== 'idle'" data-testid="save-draft" @click="saveDraft()">{{ operation === 'saving' ? '保存中…' : '保存草稿' }}</button><button type="button" class="primary" :disabled="operation !== 'idle'" data-testid="submit" @click="submit">{{ operation === 'submitting' ? '提交中…' : '提交审核' }}</button></div>
      <p class="note">目前无法从云端重新载入草稿正文，离开页面前请确认。提交后进入审核，未审核通过前不会公开。</p>
    </template>
  </div>
</template>

<style scoped>
.write-page{padding:12px 16px calc(28px + env(safe-area-inset-bottom,0px));min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;gap:5px;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #eaded2;border-radius:999px;background:#fff;color:#9d4c15;font-size:27px}.top-spacer{width:44px}.intro{padding:18px 5px 22px}.eyebrow{color:#a54c15;font-size:10px;letter-spacing:.14em;font-weight:850}.intro h1,.state h1{margin:12px 0 8px;font-size:clamp(27px,8vw,36px);letter-spacing:-.04em;line-height:1.27}.intro p,.state p{margin:0;color:#6b6055;font-size:13px;line-height:1.7}.editor{display:grid;gap:18px;padding:19px 16px;border:1px solid #e9dbce;border-radius:20px;background:#fff}.kind-row{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:6px}.kind-row button{border:1px solid #e1d2c4;background:#fff;color:#75665a;border-radius:999px;min-height:44px;padding:0 3px;font-size:12px;font-weight:800}.kind-row button.on{border-color:#b85111;background:#b85111;color:#fff}.kind-row button:disabled{opacity:.65}.field{display:grid;gap:7px;color:#3b3027;font-size:13px;font-weight:750;min-width:0}.field input,.field select,.field textarea{width:100%;min-width:0;border:1px solid #d9cabe;border-radius:12px;background:#fff;color:#2b2118;padding:12px 13px;font-size:16px;font-weight:400}.field input,.field select{min-height:48px}.field textarea{resize:vertical;line-height:1.7;min-height:180px}.review-fields{display:grid;gap:14px;border-radius:15px;background:#fff5e9;padding:15px}.review-fields h2{margin:0;font-size:16px}.review-fields p{margin:0;color:#725e4d;line-height:1.6;font-size:12px}.feedback{margin:14px 2px;min-height:23px}.feedback p{margin:5px 0;font-size:12px;line-height:1.6}.error{color:#a6372b}.saved{color:#246342}.muted,.note{color:#73675c}.actions{display:grid;grid-template-columns:1fr 1fr;gap:8px}.actions button,.primary-link{min-height:48px;border-radius:999px;font-size:13px;font-weight:850;padding:0 10px}.primary,.primary-link{border:1px solid #b85111;background:#b85111;color:#fff}.primary-link{display:inline-flex;align-items:center;justify-content:center;text-decoration:none;padding:0 20px;margin-top:20px}.secondary{border:1px solid #b85111;background:#fff;color:#9c4310}.actions button:disabled{opacity:.6}.note{font-size:11px;line-height:1.7;margin:13px 2px 0}.state{padding:30px 22px;margin-top:25px;border:1px solid #eadbc9;border-radius:20px;background:#fff}.state.success{background:#f2f9f3;border-color:#cbdcca}@media(max-width:345px){.editor{padding:17px 12px}.actions button{font-size:12px}}
</style>
