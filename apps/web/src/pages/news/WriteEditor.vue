<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { SUBMISSION_KINDS, submitBlockers } from '../../domain/submission';
import { CHANNELS } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const authorized = Boolean(getAccessToken());
const editingId = ref(route.params.id ? String(route.params.id) : '');
const canEdit = ref(!editingId.value);
const form = reactive({ kind: 'ARTICLE', title: '', body: '', channel: '猫', cover: '', tags: '', relatedProduct: '', sourceEvidence: '', interest: '' });
const attempted = ref(false);
const dirty = ref(false);
const savedHint = ref('');
const error = ref('');
const operation = ref<'idle' | 'saving' | 'submitting'>('idle');
const submittedId = ref('');
let revision = 0;
let saveTimer: ReturnType<typeof setTimeout> | null = null;
let saving: Promise<boolean> | null = null;

const blockers = computed(() => {
  if (!attempted.value) return [];
  const base = submitBlockers({ kind: form.kind, title: form.title, body: form.body, channel: form.channel });
  const messages = base.map((code) => ({
    KIND_INVALID: '请选择投稿类型', TITLE_INVALID: '标题须为 1–120 字', BODY_TOO_SHORT: '正文至少 10 字', CHANNEL_REQUIRED: '请选择频道',
  } as Record<string, string>)[code] ?? code);
  if (form.kind === 'REVIEW' && !form.sourceEvidence.trim()) messages.push('评测请填写来源依据');
  if (form.kind === 'REVIEW' && !form.interest) messages.push('评测请说明与商品的利益关系');
  return messages;
});

function clearTimer() { if (saveTimer) clearTimeout(saveTimer); saveTimer = null; }
function touch() {
  revision += 1;
  dirty.value = true;
  savedHint.value = '';
  error.value = '';
  clearTimer();
  saveTimer = setTimeout(() => { if (dirty.value) void saveDraft(false); }, 30_000);
}
function bodyForSave(): string {
  if (form.kind !== 'REVIEW') return form.body.trim();
  const disclosure = [
    form.relatedProduct.trim() ? `关联商品：${form.relatedProduct.trim()}` : '',
    form.sourceEvidence.trim() ? `来源依据：${form.sourceEvidence.trim()}` : '',
    form.interest ? `利益关系：${form.interest}` : '',
  ].filter(Boolean).join('\n');
  return disclosure ? `${form.body.trim()}\n\n---\n${disclosure}` : form.body.trim();
}

async function saveDraft(manual = true): Promise<boolean> {
  if (!authorized || !canEdit.value || submittedId.value) return false;
  if (saving) return saving;
  if (!form.title.trim() || !form.body.trim()) {
    if (manual) error.value = '填写标题和正文后，才能保存云端草稿。当前输入仍留在页面中。';
    return false;
  }
  clearTimer();
  const currentRevision = revision;
  const snapshot = {
    title: form.title.trim(), body: bodyForSave(), cover: form.cover.trim() || undefined, tags: form.tags.trim() || undefined,
  };
  saving = (async () => {
    operation.value = 'saving';
    error.value = '';
    try {
      if (editingId.value) {
        const result = (await api.submissionUpdate({ params: { id: editingId.value }, body: snapshot })) as { id?: number; state?: string };
        if (String(result?.id) !== editingId.value || result.state !== 'DRAFT') {
          throw new Error('未收到草稿更新确认，请核对后再提交');
        }
      } else {
        const result = (await api.submissionCreate({ body: { kind: form.kind, channel: form.channel, ...snapshot } })) as { submissionId?: number; state?: string };
        if (typeof result?.submissionId !== 'number' || result.state !== 'DRAFT') throw new Error('未收到草稿保存确认');
        editingId.value = String(result.submissionId);
      }
      if (revision === currentRevision) {
        dirty.value = false;
        savedHint.value = `草稿 #${editingId.value} 已保存 · ${new Date().toLocaleTimeString()}`;
      } else {
        touch();
      }
      return true;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '保存失败，请重试；输入仍保留在本页';
      return false;
    } finally { operation.value = 'idle'; saving = null; }
  })();
  return saving;
}

async function submit() {
  attempted.value = true;
  if (!authorized || !canEdit.value || operation.value !== 'idle' || blockers.value.length) return;
  const saved = await saveDraft();
  if (!saved || !editingId.value) return;
  operation.value = 'submitting';
  error.value = '';
  try {
    const result = (await api.submissionSubmit({ params: { id: editingId.value } })) as { id?: number; state?: string };
    if (result?.state !== 'PENDING' || String(result.id) !== editingId.value) throw new Error('未收到待审核确认，请勿重复提交，先核对投稿状态');
    submittedId.value = editingId.value;
    dirty.value = false;
    savedHint.value = '';
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '提交失败，请重试';
  } finally { operation.value = 'idle'; }
}

onMounted(() => {
  if (!authorized) void router.replace({ path: '/login', query: { return: route.fullPath, resume: 'write' } });
});
onUnmounted(clearTimer);
onBeforeRouteLeave(() => !dirty.value || window.confirm('还有未保存的修改，确定离开吗？当前输入可能丢失。'));
</script>

<template>
  <div class="editor">
    <nav class="breadcrumb"><router-link to="/news">资讯</router-link><span>／</span><router-link to="/submissions">我的投稿</router-link><span>／</span><span>写作</span></nav>
    <section v-if="!authorized" class="state">正在前往登录页…</section>
    <section v-else-if="!canEdit" class="state"><h1>暂时无法恢复这篇草稿</h1><p>这篇稿件的完整内容暂时无法载入。为了避免覆盖原文，这里先不开放编辑。</p><router-link to="/submissions">返回我的投稿 →</router-link></section>
    <section v-else-if="submittedId" class="state success" role="status" data-testid="submitted"><span class="eyebrow">SUBMITTED</span><h1>已提交审核</h1><p>稿件 #{{ submittedId }} 已进入待审核状态。审核通过前不会公开展示。</p><p>当前投稿列表可能尚未显示待审核稿件，请保留这个编号。</p><router-link to="/submissions">查看我的投稿 →</router-link></section>
    <template v-else>
      <header><div><p class="eyebrow">SHARE YOUR EXPERIENCE</p><h1>{{ editingId ? '继续写作' : '写一篇真实的经验' }}</h1><p>先把经历写清楚，提交后再由编辑审核。</p></div><span v-if="savedHint" class="saved" data-testid="saved-hint">{{ savedHint }}</span></header>
      <div class="layout"><main class="paper">
        <div class="kinds" role="group" aria-label="投稿类型"><button v-for="kind in SUBMISSION_KINDS" :key="kind.kind" type="button" :class="{ on: form.kind === kind.kind }" :disabled="!!editingId" @click="form.kind = kind.kind; touch()">{{ kind.label }}</button></div>
        <label class="field title-field">标题<input v-model="form.title" class="title" maxlength="120" data-testid="title" placeholder="给文章起一个清楚的标题" @input="touch" /></label>
        <div class="row"><label class="field">频道<select v-model="form.channel" data-testid="channel" :disabled="!!editingId" @change="touch"><option v-for="name in CHANNELS.slice(1)" :key="name" :value="name">{{ name }}</option></select></label><label class="field">标签<input v-model="form.tags" placeholder="多个标签用逗号分隔" @input="touch" /></label></div>
        <label class="field">正文<textarea v-model="form.body" rows="16" data-testid="body" placeholder="从你的观察或照护经历写起，至少 10 字" @input="touch" /></label>
        <section v-if="form.kind === 'REVIEW'" class="review-fields" data-testid="review-extra"><h2>评测来源与利益关系</h2><p>下列信息会随稿件正文一起送审，帮助读者判断内容来源。</p><label class="field">关联商品<input v-model="form.relatedProduct" placeholder="商品名称或可核对的链接" @input="touch" /></label><label class="field">来源依据<input v-model="form.sourceEvidence" data-testid="source-evidence" placeholder="资料、数据或实际体验依据" @input="touch" /></label><label class="field">利益关系<select v-model="form.interest" data-testid="review-interest" @change="touch"><option value="">请选择</option><option value="无品牌合作">无品牌合作</option><option value="存在合作，已在文中披露">存在合作，已在文中披露</option></select></label></section>
      </main><aside class="guide"><strong>写作提示</strong><p>清楚描述适用的宠物、做法和观察结果。健康相关内容仅供科普，具体诊疗请咨询兽医。</p><p>停笔 30 秒后会尝试保存云端草稿；保存失败时可手动重试。</p><p>目前离开后暂时无法重新载入草稿正文，请尽量在本页完成并提交。</p><router-link to="/submissions">查看我的投稿 →</router-link></aside></div>
      <div class="bottom"><div><p v-for="message in blockers" :key="message" class="error" data-testid="blocker">{{ message }}</p><p v-if="error" class="error" role="alert">{{ error }}</p><p v-if="dirty && !error" class="muted">当前修改尚未保存</p></div><div class="actions"><button type="button" class="secondary" :disabled="operation !== 'idle'" data-testid="save-draft" @click="saveDraft()">{{ operation === 'saving' ? '保存中…' : '保存草稿' }}</button><button type="button" class="primary" :disabled="operation !== 'idle'" data-testid="submit-review" @click="submit">{{ operation === 'submitting' ? '提交中…' : '提交审核' }}</button></div></div>
    </template>
  </div>
</template>

<style scoped>
.editor{width:min(1200px,calc(100% - 64px));margin:0 auto;padding:27px 0 90px}.breadcrumb{display:flex;gap:8px;color:var(--ink-2);font-size:13px}.breadcrumb a{text-decoration:none}header{display:flex;justify-content:space-between;align-items:end;gap:20px;margin:45px 0 28px}.eyebrow{margin:0 0 10px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.13em}header h1,.state h1{margin:0;font-size:clamp(31px,4vw,49px)}header p:not(.eyebrow){margin:13px 0 0;color:var(--ink-2)}.saved{flex:none;color:#176444;font-size:13px}.layout{display:grid;grid-template-columns:minmax(0,1fr) 260px;gap:19px}.paper,.guide,.state{padding:clamp(22px,4vw,38px);border:1px solid var(--line);border-radius:22px;background:#fff}.paper{display:grid;gap:24px}.kinds{display:flex;flex-wrap:wrap;gap:8px}.kinds button{min-height:39px;padding:0 17px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--ink-2);font-weight:700}.kinds button.on{border-color:var(--primary);background:var(--primary);color:#fff}.kinds button:disabled{opacity:.7}.field{display:grid;gap:8px;color:var(--ink);font-size:13px;font-weight:750}.field input,.field select,.field textarea{width:100%;min-width:0;padding:12px 14px;border:1px solid #d9c9bd;border-radius:11px;background:#fff;color:var(--ink);font-size:15px;font-weight:400}.field input,.field select{min-height:47px}.title-field input{font-size:23px;font-weight:700}.field textarea{resize:vertical;line-height:1.8}.row{display:grid;grid-template-columns:1fr 2fr;gap:15px}.review-fields{display:grid;gap:16px;padding:22px;border-radius:17px;background:#fff8f1}.review-fields h2{margin:0;font-size:19px}.review-fields p{margin:0;color:var(--ink-2);font-size:13px}.guide{align-self:start;position:sticky;top:96px;background:#f7e9df}.guide strong{font-size:18px}.guide p{color:var(--ink-2);font-size:13px;line-height:1.8}.guide a{font-size:13px;font-weight:700;text-decoration:none}.bottom{display:flex;justify-content:space-between;align-items:end;gap:20px;padding:23px 0}.actions{display:flex;gap:9px;flex:none}.actions button{min-height:46px;padding:0 22px;border-radius:999px;font-weight:750}.primary{border:0;background:var(--primary);color:#fff}.secondary{border:1px solid var(--primary);background:#fff;color:var(--primary)}button:disabled{opacity:.55;cursor:not-allowed}.error{margin:5px 0;color:#ab3223;font-size:13px}.muted{margin:5px 0;color:var(--ink-2);font-size:13px}.state{margin-top:35px;min-height:220px}.state p{max-width:650px;color:var(--ink-2);line-height:1.8}.state a{font-weight:750;text-decoration:none}.success{background:#f3fbf6}@media(max-width:800px){.layout{grid-template-columns:1fr}.guide{position:static}.bottom{align-items:stretch;flex-direction:column}}@media(max-width:650px){.editor{width:calc(100% - 32px)}header{align-items:start;flex-direction:column;margin-top:31px}.row{grid-template-columns:1fr}.actions button{flex:1}}
</style>
