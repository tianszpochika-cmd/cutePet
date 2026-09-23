<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { api } from '@cutepet/api-client';

interface Activity {
  id: number;
  title: string;
  city: string;
  type: string;
  beginsAt: string;
  endsAt?: string;
  address?: string;
  quota?: number;
  signupDeadline?: string | null;
  state: string;
}
interface Adoption {
  id: number;
  city: string;
  title: string;
  content: string;
  expireOn: string | null;
}

const tab = ref<'活动' | '领养'>('活动');
const activities = ref<Activity[]>([]);
const adoptions = ref<Adoption[]>([]);
const activeId = ref<number | null>(null);
const loading = ref(false);
const error = ref('');
const loadedActivities = ref(false);
const loadedAdoptions = ref(false);
let latestRequest = 0;

function dateTime(value?: string | null): string {
  if (!value) return '时间待公布';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value.replace('T', ' ') : date.toLocaleString('zh-CN', { hour12: false });
}
async function loadActivities() {
  const request = ++latestRequest;
  loading.value = true;
  loadedActivities.value = false;
  error.value = '';
  try {
    const response = await api.activitiesList();
    if (request !== latestRequest) return;
    if (!Array.isArray(response)) throw new Error('活动列表格式不完整，请稍后重试。');
    activities.value = response as Activity[];
    loadedActivities.value = true;
  } catch (cause) {
    if (request !== latestRequest) return;
    error.value = cause instanceof Error ? cause.message : '活动加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
async function loadAdoptions() {
  const request = ++latestRequest;
  loading.value = true;
  loadedAdoptions.value = false;
  error.value = '';
  try {
    const response = await api.adoptionsList();
    if (request !== latestRequest) return;
    if (!Array.isArray(response)) throw new Error('领养列表格式不完整，请稍后重试。');
    adoptions.value = response as Adoption[];
    loadedAdoptions.value = true;
  } catch (cause) {
    if (request !== latestRequest) return;
    error.value = cause instanceof Error ? cause.message : '领养信息加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
function selectTab(next: '活动' | '领养') {
  if (tab.value === next) return;
  ++latestRequest;
  tab.value = next;
  error.value = '';
  loading.value = false;
  if (next === '领养' && !loadedAdoptions.value) void loadAdoptions();
  if (next === '活动' && !loadedActivities.value) void loadActivities();
}
function retry() {
  if (tab.value === '活动') void loadActivities();
  else void loadAdoptions();
}
onMounted(() => void loadActivities());
</script>

<template>
  <div class="events">
    <div class="heading">
      <div>
        <p class="eyebrow">LOCAL LIFE · 本地生活</p>
        <h1>活动与领养信息</h1>
        <p class="lede">查看已公开的信息，安排和宠物一起出门的时间。</p>
      </div>
      <nav class="tabs" aria-label="信息类别">
        <button type="button" :aria-current="tab === '活动' ? 'page' : undefined" :class="{ on: tab === '活动' }" @click="selectTab('活动')">活动</button>
        <button type="button" :aria-current="tab === '领养' ? 'page' : undefined" :class="{ on: tab === '领养' }" data-testid="adoption-tab" @click="selectTab('领养')">领养</button>
      </nav>
    </div>

    <p v-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="retry">重试</button></p>
    <p v-else-if="loading" class="status" role="status">正在读取{{ tab }}信息…</p>

    <template v-if="tab === '活动'">
      <p id="signup-reason" class="notice" data-testid="signup-msg">报名暂未开放：身份认证、活动发布方告知文本和有效同意版本尚未接通。下方仅展示已发布活动；请勿在页面外发送个人信息报名。</p>
      <ul v-if="!loading && !error" class="list">
        <li v-for="activity in activities" :key="activity.id" class="card">
          <div class="date"><span>{{ activity.beginsAt?.slice(5, 7) || '—' }}月</span><strong>{{ activity.beginsAt?.slice(8, 10) || '—' }}</strong></div>
          <div class="card-body">
            <p class="kicker">{{ activity.city }} · {{ activity.type }}</p>
            <h2>{{ activity.title }}</h2>
            <p class="meta">{{ dateTime(activity.beginsAt) }}<span v-if="activity.address"> · {{ activity.address }}</span></p>
            <button type="button" class="detail-btn" :aria-expanded="activeId === activity.id" @click="activeId = activeId === activity.id ? null : activity.id">{{ activeId === activity.id ? '收起信息' : '查看信息' }}</button>
            <div v-if="activeId === activity.id" class="detail">
              <p v-if="activity.endsAt">结束时间：{{ dateTime(activity.endsAt) }}</p>
              <p v-if="activity.signupDeadline">报名截止：{{ dateTime(activity.signupDeadline) }}</p>
              <p v-if="activity.quota">名额上限：{{ activity.quota }} 人；剩余名额以服务端为准。</p>
              <p>发布状态：{{ activity.state === 'PUBLISHED' ? '已发布' : '状态待核验' }}</p>
            </div>
          </div>
          <button type="button" class="disabled-action" :data-testid="'signup-' + activity.id" disabled aria-describedby="signup-reason">报名待接入</button>
        </li>
        <li v-if="activities.length === 0" class="empty">目前没有已发布活动。稍后可以重试。</li>
      </ul>
    </template>

    <template v-else>
      <p class="notice" data-testid="adoption-notice">领养信息仅供了解，请自行核验发布方及检疫材料。平台尚未接通安全的联系与举报流程，请勿通过本页支付费用或发送证件信息。</p>
      <ul v-if="!loading && !error" class="list">
        <li v-for="adoption in adoptions" :key="adoption.id" class="card adoption-card">
          <div class="adoption-icon" aria-hidden="true">♡</div>
          <div class="card-body">
            <p class="kicker">{{ adoption.city }} · 领养信息</p>
            <h2>{{ adoption.title }}</h2>
            <p class="adoption-content">{{ adoption.content }}</p>
            <p v-if="adoption.expireOn" class="meta">信息有效期至 {{ adoption.expireOn }}</p>
          </div>
        </li>
        <li v-if="adoptions.length === 0" class="empty">目前没有公开的领养信息。</li>
      </ul>
      <p class="support-note">举报入口将在身份校验与受理回执接通后开放。发现疑似售卖，请暂勿与发布方交易。</p>
    </template>
  </div>
</template>

<style scoped>
.events{max-width:940px;margin:auto;padding:clamp(24px,4vw,48px) 20px 80px;color:#2b2118}.heading{display:flex;justify-content:space-between;align-items:end;gap:20px;flex-wrap:wrap}.eyebrow{color:#b45309;font-size:12px;font-weight:800;letter-spacing:.15em;margin:0 0 8px}h1{font-size:clamp(28px,4vw,42px);letter-spacing:-.03em;margin:0}.lede{color:#71675e;line-height:1.7;margin:10px 0 0}.tabs{display:flex;padding:4px;border-radius:999px;background:#f4eee7}.tabs button{border:0;background:transparent;padding:10px 22px;border-radius:999px;color:#665c53;cursor:pointer;font:inherit}.tabs button.on{background:#fff;color:#a64613;box-shadow:0 2px 12px #3a231312;font-weight:700}.notice,.status,.support-note{line-height:1.7;border-radius:14px;padding:14px 18px;margin:24px 0 18px}.notice{background:#fff5e9;color:#704323;border:1px solid #f6dec3}.status{background:#f5f1ec;color:#675d54}.error{background:#fff1ef;color:#a6372b}.error button,.detail-btn{border:0;background:none;color:#a64613;text-decoration:underline;cursor:pointer;font:inherit}.list{list-style:none;margin:0;padding:0;display:grid;gap:12px}.card{background:#fff;border:1px solid #eee3d8;border-radius:18px;box-shadow:0 8px 28px #3a23130a;padding:20px;display:flex;align-items:start;gap:20px}.date{min-width:64px;min-height:70px;border-radius:12px;background:#fff0df;color:#a64613;display:grid;place-content:center;text-align:center}.date span{font-size:12px}.date strong{font-size:25px;line-height:1.1}.card-body{flex:1;min-width:0}.kicker{margin:1px 0 7px;font-size:12px;color:#a64613;font-weight:700}.card h2{font-size:19px;margin:0 0 8px}.meta{color:#736a61;font-size:13px;line-height:1.6;margin:0}.detail-btn{margin-top:10px;padding:0}.detail{margin-top:12px;background:#faf7f3;padding:10px 14px;border-radius:10px;color:#5f554c;font-size:13px}.detail p{margin:5px 0}.disabled-action{background:#ede9e4;color:#756b62;border:0;border-radius:999px;padding:10px 14px;font:inherit;cursor:not-allowed}.adoption-icon{display:grid;place-items:center;width:64px;height:64px;border-radius:16px;background:#f2f9f0;color:#579267;font-size:29px;flex:none}.adoption-content{white-space:pre-line;line-height:1.7;color:#534b42;margin:0 0 8px;overflow-wrap:anywhere}.empty{padding:34px 20px;text-align:center;background:#fff;border:1px dashed #decfbe;border-radius:16px;color:#776a5d}.support-note{background:#f5f1ec;color:#655b52;font-size:13px}@media(max-width:640px){.heading{align-items:start}.tabs{width:100%}.tabs button{flex:1}.card{flex-wrap:wrap;gap:12px}.card-body{flex-basis:calc(100% - 84px)}.disabled-action{margin-left:76px}.adoption-card .card-body{flex-basis:calc(100% - 84px)}}
</style>
