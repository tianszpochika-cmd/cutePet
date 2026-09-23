<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { SPECIES } from '../../../web/src/domain/pet.ts';

interface Pet { id: number; name: string; species: string; breed: string; state: string; birthDate?: string | null; sex?: string | null }
interface TimelineItem { recordId: number; type: string; date: string; note?: string | null; weightKg?: number }
interface Reminder { id: number; type: string; title: string; state: string; nextDue: string | null }
type DetailTab = 'overview' | 'records' | 'reminders';

const route = useRoute();
const router = useRouter();
const isNew = computed(() => route.path === '/pets/new');
const petId = computed(() => String(route.params.id ?? ''));
const authorized = ref(Boolean(getAccessToken()));
const step = ref(0);
const form = reactive({ species: '', name: '', breed: '' });
const formError = ref('');
const saving = ref(false);
const needsReview = ref(false);
const pet = ref<Pet | null>(null);
const petLoading = ref(false);
const petError = ref('');
const timeline = ref<TimelineItem[]>([]);
const timelineLoading = ref(false);
const timelineError = ref('');
const reminders = ref<Reminder[]>([]);
const remindersLoading = ref(false);
const remindersError = ref('');
const latestKg = ref<number | null>(null);
const weightLoading = ref(false);
const weightError = ref('');
const detailTab = ref<DetailTab>('overview');
let requestVersion = 0;

function validPet(value: unknown, expectedId?: string): value is Pet {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && Number.isSafeInteger(row.id) && row.id > 0 &&
    (!expectedId || String(row.id) === expectedId) &&
    typeof row.name === 'string' && typeof row.species === 'string' &&
    typeof row.breed === 'string' && typeof row.state === 'string';
}
function validTimeline(value: unknown): value is TimelineItem {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.recordId === 'number' && typeof row.type === 'string' && typeof row.date === 'string';
}
function validReminder(value: unknown): value is Reminder {
  if (!value || typeof value !== 'object') return false;
  const row = value as Record<string, unknown>;
  return typeof row.id === 'number' && typeof row.type === 'string' &&
    typeof row.title === 'string' && typeof row.state === 'string' &&
    (row.nextDue === null || typeof row.nextDue === 'string');
}
function resetDetail() {
  pet.value = null;
  timeline.value = [];
  reminders.value = [];
  latestKg.value = null;
  petError.value = '';
  timelineError.value = '';
  remindersError.value = '';
  weightError.value = '';
}
async function loadTimeline(id: string, version: number) {
  timelineLoading.value = true;
  timelineError.value = '';
  try {
    const result: unknown = await api.petTimeline({ path: { id } });
    if (!Array.isArray(result) || !result.every(validTimeline)) throw new Error('时间线格式暂无法确认');
    if (version === requestVersion) timeline.value = result;
  } catch (cause) {
    if (version === requestVersion) timelineError.value = cause instanceof Error ? cause.message : '记录读取失败';
  } finally { if (version === requestVersion) timelineLoading.value = false; }
}
async function loadReminders(id: string, version: number) {
  remindersLoading.value = true;
  remindersError.value = '';
  try {
    const result: unknown = await api.reminderList({ path: { id } });
    if (!Array.isArray(result) || !result.every(validReminder)) throw new Error('提醒格式暂无法确认');
    if (version === requestVersion) reminders.value = result;
  } catch (cause) {
    if (version === requestVersion) remindersError.value = cause instanceof Error ? cause.message : '提醒读取失败';
  } finally { if (version === requestVersion) remindersLoading.value = false; }
}
async function loadWeights(id: string, version: number) {
  weightLoading.value = true;
  weightError.value = '';
  try {
    const result: unknown = await api.petWeights({ path: { id } });
    if (!result || typeof result !== 'object' || !Array.isArray((result as { points?: unknown }).points)) {
      throw new Error('体重摘要格式暂无法确认');
    }
    if (version === requestVersion) {
      const current = (result as { latestKg?: unknown }).latestKg;
      latestKg.value = typeof current === 'number' && Number.isFinite(current) ? current : null;
    }
  } catch (cause) {
    if (version === requestVersion) weightError.value = cause instanceof Error ? cause.message : '体重摘要读取失败';
  } finally { if (version === requestVersion) weightLoading.value = false; }
}
async function loadPet() {
  const version = ++requestVersion;
  authorized.value = Boolean(getAccessToken());
  resetDetail();
  if (isNew.value || !authorized.value) return;
  const id = petId.value;
  if (!/^[1-9]\d*$/.test(id)) { petError.value = '宠物编号无效'; return; }
  petLoading.value = true;
  try {
    const result: unknown = await api.petGet({ path: { id } });
    if (!validPet(result, id)) throw new Error('档案信息暂无法确认');
    if (version !== requestVersion) return;
    pet.value = result;
    void loadTimeline(id, version);
    void loadReminders(id, version);
    void loadWeights(id, version);
  } catch (cause) {
    if (version === requestVersion) petError.value = cause instanceof Error ? cause.message : '宠物档案读取失败';
  } finally { if (version === requestVersion) petLoading.value = false; }
}
function chooseTab(next: DetailTab) {
  detailTab.value = next;
  void router.replace({ path: route.path, query: next === 'overview' ? {} : { tab: next } });
}
function nextStep() {
  formError.value = '';
  if (step.value === 0) {
    if (!(SPECIES as readonly string[]).includes(form.species)) { formError.value = '请选择宠物类型。'; return; }
    step.value = 1;
    return;
  }
  if (step.value === 1) {
    if (!form.name.trim() || form.name.trim().length > 20) { formError.value = '昵称须为 1–20 字。'; return; }
    if (!form.breed.trim()) { formError.value = '请填写品种。'; return; }
    step.value = 2;
  }
}
async function createPet() {
  if (!authorized.value || saving.value || needsReview.value) return;
  if (!(SPECIES as readonly string[]).includes(form.species) || !form.name.trim() || !form.breed.trim()) {
    formError.value = '资料尚未填写完整，请返回核对。';
    return;
  }
  saving.value = true;
  formError.value = '';
  try {
    const result: unknown = await api.petCreate({ body: {
      species: form.species, name: form.name.trim(), breed: form.breed.trim(),
    } });
    if (!validPet(result) || result.state !== 'ACTIVE') {
      needsReview.value = true;
      formError.value = '平台响应未包含可核对的活动档案编号。请先查看宠物列表，避免重复建档。';
      return;
    }
    await router.replace('/pets/' + result.id);
  } catch (cause) {
    needsReview.value = true;
    formError.value = '建档结果尚未确认：' + (cause instanceof Error ? cause.message : '请求失败') + '。请先查看宠物列表再决定是否重试。';
  } finally { saving.value = false; }
}
watch([isNew, petId], () => {
  if (isNew.value) {
    authorized.value = Boolean(getAccessToken());
    requestVersion++;
    resetDetail();
    step.value = 0;
  } else void loadPet();
}, { immediate: true });
watch(() => route.query.tab, (value) => {
  detailTab.value = value === 'records' || value === 'reminders' ? value : 'overview';
}, { immediate: true });
</script>

<template>
  <div class="pet-flow">
    <header class="top"><button type="button" class="back" @click="router.push('/pets')">← 宠物列表</button><span class="eyebrow">{{ isNew ? 'NEW PROFILE' : 'CARE PROFILE' }}</span><h1>{{ isNew ? '建立宠物档案' : pet?.name ?? '宠物档案' }}</h1></header>
    <section v-if="!authorized" class="state" role="status"><h2>登录后继续照护</h2><p>档案、记录和提醒只能由有权账号读取。完成登录后仍由你确认下一步。</p><button type="button" class="primary" @click="router.push({ path: '/login', query: { return: route.fullPath } })">前往登录</button></section>

    <template v-else-if="isNew">
      <div class="progress" aria-label="建档进度"><span v-for="index in 3" :key="index" :class="{ active: index - 1 <= step }" /></div>
      <p class="step-name">第 {{ step + 1 }} 步 / 3 · {{ ['宠物类型', '基础资料', '核对保存'][step] }}</p>
      <section v-if="step === 0" class="card"><h2>它是哪一类伙伴？</h2><div class="species"><button v-for="item in SPECIES" :key="item" type="button" :class="{ picked: form.species === item }" :aria-pressed="form.species === item" @click="form.species = item">{{ item }}</button></div></section>
      <section v-else-if="step === 1" class="card fields"><h2>从最重要的资料开始</h2><label>昵称<input v-model="form.name" maxlength="20" autocomplete="off" placeholder="1–20 字" /></label><label>品种<input v-model="form.breed" autocomplete="off" placeholder="请填写品种" /></label></section>
      <section v-else class="card review"><h2>核对将保存的资料</h2><p><strong>{{ form.name.trim() }}</strong> · {{ form.species }} · {{ form.breed.trim() }}</p><div class="honest">本次只保存昵称、类型和品种。照片上传与体重基线没有纳入这一步；不会显示为已保存。</div></section>
      <p v-if="formError" class="error" role="alert">{{ formError }}</p>
      <div class="actions"><button type="button" class="secondary" :disabled="saving" @click="step > 0 ? step-- : router.push('/pets')">{{ step ? '上一步' : '返回列表' }}</button><button v-if="step < 2" type="button" class="primary" @click="nextStep">继续</button><button v-else type="button" class="primary" :disabled="saving || needsReview" @click="createPet">{{ saving ? '正在保存…' : needsReview ? '请先核对列表' : '确认并保存档案' }}</button></div>
      <button v-if="needsReview" type="button" class="text-button" @click="router.push('/pets')">先查看宠物列表 →</button>
    </template>

    <template v-else>
      <p v-if="petLoading" class="state compact" role="status">正在读取宠物档案…</p>
      <section v-else-if="petError" class="state error-state" role="alert"><h2>无法打开这份档案</h2><p>{{ petError }}</p><button type="button" class="secondary" @click="loadPet">重新读取</button></section>
      <template v-else-if="pet">
        <section class="identity card"><span class="avatar" aria-hidden="true">✿</span><div><strong>{{ pet.name }}</strong><p>{{ pet.species }}{{ pet.breed ? ' · ' + pet.breed : '' }} · {{ pet.state === 'ACTIVE' ? '在照护' : '档案状态：' + pet.state }}</p></div></section>
        <nav class="tabs" aria-label="宠物详情"><button type="button" :class="{ active: detailTab === 'overview' }" @click="chooseTab('overview')">概览</button><button type="button" :class="{ active: detailTab === 'records' }" @click="chooseTab('records')">记录</button><button type="button" :class="{ active: detailTab === 'reminders' }" @click="chooseTab('reminders')">提醒</button></nav>
        <section v-if="detailTab === 'overview'" class="card overview">
          <h2>照护概览</h2><p>出生日期：{{ pet.birthDate || '未填写' }}</p><p>体重：<span v-if="weightLoading">读取中…</span><span v-else-if="weightError" class="error-inline">{{ weightError }}</span><span v-else>{{ latestKg === null ? '暂无平台记录' : latestKg + ' kg' }}</span></p>
          <button v-if="pet.state === 'ACTIVE'" type="button" class="primary" @click="router.push({ path: '/quick-record', query: { petId: pet.id } })">＋ 给 {{ pet.name }} 记一件事</button>
          <p class="hint">本页只读取平台返回的资料；记录的下次日期不等于提醒计划已经创建。</p>
          <div class="small-links"><button type="button" @click="chooseTab('records')">查看时间线 →</button><button type="button" @click="chooseTab('reminders')">查看提醒 →</button></div>
        </section>
        <section v-else-if="detailTab === 'records'" class="card list-section">
          <div class="section-head"><h2>健康时间线</h2><button v-if="pet.state === 'ACTIVE'" type="button" class="text-button" @click="router.push({ path: '/quick-record', query: { petId: pet.id } })">＋ 记录</button></div>
          <p v-if="timelineLoading" class="state compact" role="status">正在读取时间线…</p>
          <div v-else-if="timelineError" class="state error-state" role="alert"><p>{{ timelineError }}</p><button type="button" class="text-button" @click="loadTimeline(petId, requestVersion)">重试</button></div>
          <p v-else-if="!timeline.length" class="state compact">还没有平台健康记录。从一次真实照护开始记录。</p>
          <ul v-else><li v-for="(item, index) in timeline" :key="item.type + '-' + item.recordId + '-' + index"><span class="time-dot" aria-hidden="true" /><div><strong>{{ item.type }} · {{ item.date }}</strong><p>{{ item.type === '体重' && typeof item.weightKg === 'number' ? item.weightKg + ' kg' : item.note || '无备注' }}</p></div></li></ul>
        </section>
        <section v-else class="card list-section">
          <h2>提醒计划</h2><p class="hint">计划和本次完成是两件事。健康类完成需要关联有效记录；当前页面不提供直接完成按钮。</p>
          <p v-if="remindersLoading" class="state compact" role="status">正在读取提醒…</p>
          <div v-else-if="remindersError" class="state error-state" role="alert"><p>{{ remindersError }}</p><button type="button" class="text-button" @click="loadReminders(petId, requestVersion)">重试</button></div>
          <p v-else-if="!reminders.length" class="state compact">当前没有平台提醒计划。健康记录里的下次日期不会自动成为提醒。</p>
          <ul v-else><li v-for="item in reminders" :key="item.id"><span class="time-dot" aria-hidden="true" /><div><strong>{{ item.title || item.type }}</strong><p>{{ item.state }} · 下次 {{ item.nextDue ?? '日期待核对' }}</p></div></li></ul>
        </section>
        <div class="footer-links"><button type="button" @click="router.push('/pets/' + pet.id + '/share')">共享范围 →</button><button type="button" @click="router.push('/pets/' + pet.id + '/transfer')">所有权 →</button></div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.pet-flow{display:grid;gap:15px;padding:18px 16px calc(30px + env(safe-area-inset-bottom));color:#30241d}.top{display:grid;justify-items:start;gap:6px}.back{min-height:44px;padding:0;border:0;background:transparent;color:#9b410d;font-size:13px;font-weight:750}.eyebrow{color:#a64a10;font-size:11px;font-weight:800;letter-spacing:.12em}.top h1{margin:0;font-size:29px;line-height:1.2}.card,.state{padding:18px;border:1px solid #e9ddd1;border-radius:18px;background:#fff}.state{display:grid;justify-items:start;gap:11px}.state.compact{display:block;color:#6e6053;font-size:13px}.state.error-state{background:#fff4f1;color:#a2312a}.state h2,.card h2{margin:0;font-size:19px}.state p,.hint{margin:0;color:#6e6053;font-size:13px;line-height:1.7}.progress{display:grid;grid-template-columns:repeat(3,1fr);gap:6px}.progress span{height:5px;border-radius:999px;background:#e5d9cd}.progress span.active{background:#b85111}.step-name{margin:0;color:#8c4312;font-size:12px;font-weight:800}.species{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:16px}.species button{min-height:65px;border:1px solid #e4d5c7;border-radius:13px;background:#fffaf5;color:#5b4637;font-size:15px;font-weight:750}.species button.picked{border-color:#b85111;background:#b85111;color:#fff}.fields{display:grid;gap:14px}.fields label{display:grid;gap:7px;font-size:13px;font-weight:750}.fields input{width:100%;min-height:48px;padding:0 13px;border:1px solid #d9cbbf;border-radius:10px;font-size:16px}.review p{margin:16px 0;font-size:15px}.honest{padding:13px;border-radius:11px;background:#fff4e8;color:#70451f;font-size:12px;line-height:1.7}.actions{display:grid;grid-template-columns:1fr 1.3fr;gap:9px}.primary,.secondary{min-height:48px;padding:10px 14px;border-radius:12px;font-size:14px;font-weight:750}.primary{border:0;background:#b85111;color:#fff}.secondary{border:1px solid #d3aa89;background:#fff;color:#8d3f0c}button:disabled{opacity:.55}.error{margin:0;color:#a12e28;font-size:13px;line-height:1.6}.text-button{min-height:44px;padding:0;border:0;background:transparent;color:#97410c;font-size:13px;font-weight:750}.identity{display:flex;align-items:center;gap:13px}.avatar{width:51px;height:51px;flex:none;display:grid;place-items:center;border-radius:15px;background:#fff0e0;color:#b85111;font-size:27px}.identity strong{font-size:18px}.identity p{margin:5px 0 0;color:#716357;font-size:12px}.tabs{display:grid;grid-template-columns:repeat(3,1fr);gap:7px}.tabs button{min-height:44px;border:1px solid #e6d9cd;border-radius:11px;background:#fff;color:#655246;font-size:13px;font-weight:750}.tabs button.active{border-color:#b85111;background:#b85111;color:#fff}.overview{display:grid;gap:13px}.overview p:not(.hint){margin:0;font-size:13px}.overview .primary{width:100%}.error-inline{color:#a12e28}.small-links,.footer-links{display:flex;justify-content:space-between;gap:8px}.small-links button,.footer-links button{min-height:44px;padding:0;border:0;background:transparent;color:#99420f;font-size:12px;font-weight:750;text-align:left}.footer-links{padding:0 3px}.list-section{display:grid;gap:14px}.section-head{display:flex;justify-content:space-between;align-items:center}.list-section ul{display:grid;gap:0;list-style:none;margin:0;padding:0}.list-section li{display:flex;gap:11px;padding:13px 0;border-top:1px solid #eee4da}.time-dot{width:9px;height:9px;flex:none;margin-top:5px;border-radius:50%;background:#b85111}.list-section li strong{font-size:13px}.list-section li p{margin:5px 0 0;color:#716357;font-size:12px;line-height:1.55}.list-section .state{margin:0}.hint{font-size:12px}button:focus-visible,input:focus-visible{outline:3px solid #783307;outline-offset:2px}@media(max-width:350px){.card,.state{padding:15px}.actions{grid-template-columns:1fr 1fr}.primary,.secondary{font-size:13px}}
</style>
