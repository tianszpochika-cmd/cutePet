<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { CHANNELS } from '../../domain/news';

interface Item { name: string; reason: string }
const route = useRoute();
const router = useRouter();
const authorized = Boolean(getAccessToken());
const form = reactive({ title: '', intro: '', channel: '猫', source: '', interest: '' });
const items = ref<Item[]>([{ name: '', reason: '' }]);
const attempted = ref(false);
const submitting = ref(false);
const submittedId = ref('');
const draftId = ref('');
const dirty = ref(false);
const error = ref('');
const blockers = computed(() => {
  if (!attempted.value) return [];
  const issues: string[] = [];
  if (!form.title.trim() || form.title.trim().length > 120) issues.push('请填写 1–120 字的清单标题');
  if (!form.intro.trim()) issues.push('请填写清单导语');
  if (!items.value.length || items.value.some((item) => !item.name.trim() || !item.reason.trim())) issues.push('每件用品都要填写名称和选择理由');
  if (!form.source.trim()) issues.push('请说明清单信息的来源');
  if (!form.interest) issues.push('请说明与用品品牌的利益关系');
  return issues;
});
function touch() { dirty.value = true; error.value = ''; }
function addItem() { items.value.push({ name: '', reason: '' }); touch(); }
function removeItem(index: number) { items.value.splice(index, 1); touch(); }
function moveItem(index: number, direction: -1 | 1) {
  const next = index + direction;
  if (next < 0 || next >= items.value.length) return;
  [items.value[index], items.value[next]] = [items.value[next]!, items.value[index]!];
  touch();
}
function buildBody() {
  return [
    form.intro.trim(),
    '',
    ...items.value.map((item, index) => `${index + 1}. ${item.name.trim()}：${item.reason.trim()}`),
    '',
    `信息来源：${form.source.trim()}`,
    `利益关系：${form.interest}`,
  ].join('\n');
}
async function submit() {
  attempted.value = true;
  if (!authorized || submitting.value || submittedId.value || blockers.value.length) return;
  submitting.value = true;
  error.value = '';
  try {
    if (draftId.value) {
      const updated = (await api.submissionUpdate({ params: { id: draftId.value }, body: { title: form.title.trim(), body: buildBody() } })) as { id?: number; state?: string };
      if (String(updated?.id) !== draftId.value || updated.state !== 'DRAFT') {
        throw new Error('未收到清单草稿更新确认，请核对后再提交');
      }
    } else {
      const created = (await api.submissionCreate({ body: { kind: 'LIST', title: form.title.trim(), body: buildBody(), channel: form.channel } })) as { submissionId?: number; state?: string };
      if (typeof created?.submissionId !== 'number' || created.state !== 'DRAFT') throw new Error('未收到清单草稿确认');
      draftId.value = String(created.submissionId);
    }
    const submitted = (await api.submissionSubmit({ params: { id: draftId.value } })) as { id?: number; state?: string };
    if (submitted?.state !== 'PENDING' || String(submitted.id) !== draftId.value) throw new Error('未收到待审核确认，请先核对稿件状态');
    submittedId.value = draftId.value;
    dirty.value = false;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '提交失败，输入仍保留在本页';
  } finally { submitting.value = false; }
}
onMounted(() => {
  if (!authorized) void router.replace({ path: '/login', query: { return: route.fullPath, resume: 'write-list' } });
});
onBeforeRouteLeave(() => !dirty.value || window.confirm('清单尚未提交，确定离开吗？当前输入可能丢失。'));
</script>

<template>
  <div class="list-editor">
    <nav class="breadcrumb"><router-link to="/submissions">我的投稿</router-link><span>／</span><span>写清单</span></nav>
    <section v-if="!authorized" class="state">正在前往登录页…</section>
    <section v-else-if="submittedId" class="state success" role="status" data-testid="submitted"><p class="eyebrow">SUBMITTED</p><h1>清单已提交审核</h1><p>稿件 #{{ submittedId }} 已进入待审核状态，通过前不会公开。</p><router-link to="/submissions">查看我的投稿 →</router-link></section>
    <template v-else>
      <header><p class="eyebrow">CURATE WITH CARE</p><h1>把选择理由写给后来的人。</h1><p>按顺序列出用品和使用经验，让每个推荐都有依据。</p></header>
      <div class="layout"><main class="paper">
        <section class="section"><div class="section-title"><span>01</span><h2>清单主题</h2></div><label class="field">标题<input v-model="form.title" data-testid="list-title" maxlength="120" placeholder="例如：第一次养猫的日常用品" @input="touch" /></label><label class="field">导语<textarea v-model="form.intro" rows="3" placeholder="这份清单适合谁？你为什么整理它？" @input="touch" /></label><label class="field">频道<select v-model="form.channel" @change="touch"><option v-for="name in CHANNELS.slice(1)" :key="name" :value="name">{{ name }}</option></select></label></section>
        <section class="section"><div class="section-title"><span>02</span><h2>逐项说明</h2></div><p class="section-note">请自行核对商品信息和在售情况；此处按文字提交，不会自动关联商品页。</p><div v-for="(item, index) in items" :key="index" class="item"><div class="item-head"><strong>第 {{ index + 1 }} 件</strong><div><button type="button" :disabled="index === 0" @click="moveItem(index, -1)">上移</button><button type="button" :disabled="index === items.length - 1" @click="moveItem(index, 1)">下移</button><button type="button" @click="removeItem(index)">移除</button></div></div><label class="field">用品名称<input v-model="item.name" placeholder="填写可识别的名称" @input="touch" /></label><label class="field">选择理由<textarea v-model="item.reason" rows="2" placeholder="写下实际使用体验与适用条件" @input="touch" /></label></div><button type="button" class="add" @click="addItem">＋ 添加一件用品</button></section>
        <section class="section"><div class="section-title"><span>03</span><h2>来源与声明</h2></div><label class="field">信息来源<input v-model="form.source" placeholder="例如：亲身使用、公开产品资料" @input="touch" /></label><label class="field">利益关系<select v-model="form.interest" data-testid="interest" @change="touch"><option value="">请选择</option><option value="无品牌合作">无品牌合作</option><option value="存在合作，已在文中披露">存在合作，已在文中披露</option></select></label></section>
        <p v-for="message in blockers" :key="message" class="error" data-testid="blocker">{{ message }}</p><p v-if="error" class="error" role="alert">{{ error }}</p>
        <button type="button" class="primary submit" :disabled="submitting" data-testid="submit" @click="submit">{{ submitting ? '提交中…' : '提交审核' }}</button>
      </main><aside class="guide"><strong>提交前核对</strong><p>名称、理由、来源和利益关系都会写进稿件正文，供审核与读者阅读。</p><p>这页目前无法重新读取未提交的清单；离开前请确认已提交。</p></aside></div>
    </template>
  </div>
</template>

<style scoped>
.list-editor{width:min(1100px,calc(100% - 64px));margin:0 auto;padding:27px 0 90px}.breadcrumb{display:flex;gap:8px;color:var(--ink-2);font-size:13px}.breadcrumb a{text-decoration:none}.eyebrow{margin:0 0 12px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.13em}header{margin:50px 0 27px}header h1,.state h1{max-width:18ch;margin:0;font-size:clamp(31px,4vw,49px)}header p:not(.eyebrow){color:var(--ink-2)}.layout{display:grid;grid-template-columns:minmax(0,1fr) 250px;gap:17px}.paper,.guide,.state{padding:clamp(22px,4vw,36px);border:1px solid var(--line);border-radius:22px;background:#fff}.paper{display:grid;gap:29px}.section{display:grid;gap:17px;padding-bottom:29px;border-bottom:1px solid var(--line)}.section-title{display:flex;align-items:center;gap:12px}.section-title span{color:var(--primary);font-size:13px;font-weight:800}.section-title h2{margin:0;font-size:22px}.section-note{margin:0;color:var(--ink-2);font-size:13px}.field{display:grid;gap:7px;font-size:13px;font-weight:750}.field input,.field textarea,.field select{width:100%;min-height:45px;padding:11px 13px;border:1px solid #d9c9bd;border-radius:11px;background:#fff;font-size:15px;font-weight:400}.field textarea{resize:vertical}.item{display:grid;gap:13px;padding:20px;border:1px solid var(--line);border-radius:16px;background:#fffaf6}.item-head{display:flex;justify-content:space-between;align-items:center;gap:10px}.item-head div{display:flex;gap:5px}.item-head button{min-height:32px;padding:0 8px;border:0;border-radius:7px;background:#f4e8dd;color:var(--primary);font-size:12px}.item-head button:disabled{opacity:.35}.add{min-height:45px;border:1px dashed var(--primary);border-radius:12px;background:#fff;color:var(--primary);font-weight:750}.primary{min-height:48px;border:0;border-radius:999px;background:var(--primary);color:#fff;font-weight:750}.primary:disabled{opacity:.5}.guide{align-self:start;position:sticky;top:96px;background:#f7e9df}.guide strong{font-size:17px}.guide p{color:var(--ink-2);font-size:13px;line-height:1.8}.error{margin:0;color:#ab3223;font-size:13px}.state{margin-top:34px;min-height:220px}.state p{color:var(--ink-2)}.state a{font-weight:750;text-decoration:none}.success{background:#f3fbf6}@media(max-width:800px){.layout{grid-template-columns:1fr}.guide{position:static}}@media(max-width:650px){.list-editor{width:calc(100% - 32px)}header{margin-top:35px}.item-head{align-items:start;flex-direction:column}}
</style>
