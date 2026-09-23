<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface Activity { id: number; title: string; city: string; type: string; beginsAt: string; endsAt?: string; address?: string; signupDeadline?: string | null; quota?: number; state: string; changeNote?: string | null }
interface Adoption { id: number; city: string; title: string; content: string; expireOn?: string | null }
const route = useRoute();
const router = useRouter();
const cityDraft = ref('');
const city = computed(() => typeof route.query.city === 'string' ? route.query.city.trim() : '');
const tab = ref<'activity' | 'adoption'>('activity');
const activities = ref<Activity[]>([]);
const adoptions = ref<Adoption[]>([]);
const loading = ref(false);
const error = ref('');
const detail = ref<Activity | null>(null);
const activeDetailId = ref<number | null>(null);
const detailLoading = ref(false);
const detailError = ref('');
let latest = 0;
let detailRequest = 0;
let active = true;

function formatDate(value?: string | null): string {
  if (!value) return '时间待公布';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value.replace('T', ' ') : date.toLocaleString('zh-CN', { hour12: false });
}
async function load() {
  const request = ++latest;
  ++detailRequest;
  loading.value = true;
  error.value = '';
  detail.value = null;
  activeDetailId.value = null;
  detailLoading.value = false;
  detailError.value = '';
  try {
    const query = { city: city.value || undefined };
    const response = tab.value === 'activity' ? await api.activitiesList({ query }) : await api.adoptionsList({ query });
    if (!active || request !== latest) return;
    if (!Array.isArray(response)) throw new Error('公开信息格式不完整');
    if (tab.value === 'activity') activities.value = (response as Activity[]).filter((item) => typeof item?.id === 'number' && typeof item.title === 'string' && item.state === 'PUBLISHED');
    else adoptions.value = (response as Adoption[]).filter((item) => typeof item?.id === 'number' && typeof item.title === 'string');
  } catch (cause) {
    if (active && request === latest) error.value = cause instanceof Error ? cause.message : '公开信息暂时无法读取';
  } finally { if (active && request === latest) loading.value = false; }
}
function chooseCity() {
  void router.replace({ path: '/events', query: cityDraft.value.trim() ? { city: cityDraft.value.trim() } : {} });
}
function back() { void router.push({ path: '/explore', query: city.value ? { city: city.value } : {} }); }
function chooseTab(value: 'activity' | 'adoption') {
  if (tab.value === value) return;
  tab.value = value;
  void load();
}
async function showDetail(activityId: number) {
  if (activeDetailId.value === activityId) {
    ++detailRequest;
    activeDetailId.value = null;
    detail.value = null;
    detailLoading.value = false;
    detailError.value = '';
    return;
  }
  const request = ++detailRequest;
  activeDetailId.value = activityId;
  detail.value = null;
  detailLoading.value = true;
  detailError.value = '';
  try {
    const response = await api.activityGet({ params: { id: activityId } }) as Activity;
    if (!active || request !== detailRequest) return;
    if (response?.id !== activityId || response.state !== 'PUBLISHED') throw new Error('活动状态已变化，请刷新列表');
    detail.value = response;
  } catch (cause) {
    if (active && request === detailRequest) detailError.value = cause instanceof Error ? cause.message : '活动详情暂时无法读取';
  } finally { if (active && request === detailRequest) detailLoading.value = false; }
}
function retryDetail(activityId: number) {
  activeDetailId.value = null;
  void showDetail(activityId);
}
watch(city, () => { cityDraft.value = city.value; void load(); }, { immediate: true });
onBeforeUnmount(() => { active = false; ++latest; ++detailRequest; });
</script>

<template>
  <div class="events-page">
    <div class="topline"><button type="button" class="back" aria-label="返回探索" @click="back">‹</button><span>活动与领养</span><span class="spacer" /></div>
    <header class="hero"><span class="eyebrow">LOCAL LIFE · 本地生活</span><h1>看看城市里，<br>正在发生什么。</h1><p>只展示已公开的信息。参加或联系前，请核对发布状态与现场要求。</p></header>
    <form class="city-form" @submit.prevent="chooseCity"><label for="events-city">按城市筛选</label><div><input id="events-city" v-model="cityDraft" type="search" placeholder="全部城市" autocomplete="address-level2" maxlength="40" /><button type="submit">筛选</button></div></form>
    <nav class="tabs" aria-label="信息类型"><button type="button" :class="{ on: tab === 'activity' }" :aria-current="tab === 'activity' ? 'page' : undefined" @click="chooseTab('activity')">活动</button><button type="button" :class="{ on: tab === 'adoption' }" :aria-current="tab === 'adoption' ? 'page' : undefined" @click="chooseTab('adoption')">领养信息</button></nav>
    <p v-if="tab === 'activity'" class="notice">报名功能需身份认证、发布方告知与有效同意版本，当前暂未开放。页面不会模拟报名成功。</p>
    <p v-else class="notice">领养信息仅供了解。请自行核验发布方与相关材料，不通过本页支付费用或发送证件信息。</p>

    <div v-if="loading" class="state" role="status"><span class="symbol">◌</span><strong>正在读取公开信息…</strong></div>
    <div v-else-if="error" class="state error" role="alert"><span class="symbol">!</span><strong>暂时无法读取</strong><p>{{ error }}</p><button type="button" @click="load">重新加载</button></div>
      <template v-else-if="tab === 'activity'"><div v-if="!activities.length" class="state"><span class="symbol">✳</span><strong>目前没有公开活动</strong><p>可以换一座城市，或稍后再看。</p></div><ul v-else class="list"><li v-for="item in activities" :key="item.id" class="card"><div class="date"><span>{{ item.beginsAt?.slice(5, 7) || '—' }}月</span><strong>{{ item.beginsAt?.slice(8, 10) || '—' }}</strong></div><div class="card-body"><span class="kicker">{{ item.city }} · {{ item.type }}</span><h2>{{ item.title }}</h2><p>{{ formatDate(item.beginsAt) }}</p><button type="button" class="detail-button" :aria-expanded="activeDetailId === item.id" @click="showDetail(item.id)">{{ activeDetailId === item.id ? '收起详情' : '查看最新详情' }} <span aria-hidden="true">↗</span></button><div v-if="detailLoading && activeDetailId === item.id" class="detail-state" role="status">正在核对活动信息…</div><div v-if="detailError && activeDetailId === item.id" class="detail-state error" role="alert">{{ detailError }} <button type="button" @click="retryDetail(item.id)">重试</button></div><div v-if="detail?.id === item.id" class="detail"><p v-if="detail.address"><strong>地点：</strong>{{ detail.address }}</p><p><strong>开始：</strong>{{ formatDate(detail.beginsAt) }}</p><p v-if="detail.endsAt"><strong>结束：</strong>{{ formatDate(detail.endsAt) }}</p><p v-if="detail.signupDeadline"><strong>报名截止：</strong>{{ formatDate(detail.signupDeadline) }}</p><p v-if="typeof detail.quota === 'number' && detail.quota > 0"><strong>名额上限：</strong>{{ detail.quota }} 人，剩余名额需以平台实时结果为准。</p><p v-if="detail.changeNote"><strong>变更说明：</strong>{{ detail.changeNote }}</p></div></div></li></ul></template>
    <template v-else><div v-if="!adoptions.length" class="state"><span class="symbol">♡</span><strong>目前没有公开领养信息</strong><p>可以换一座城市，或稍后再看。</p></div><ul v-else class="list"><li v-for="item in adoptions" :key="item.id" class="card adoption"><div class="heart" aria-hidden="true">♡</div><div class="card-body"><span class="kicker">{{ item.city }} · 领养信息</span><h2>{{ item.title }}</h2><p class="adoption-copy">{{ item.content }}</p><p v-if="item.expireOn" class="expiry">信息有效期至 {{ item.expireOn }}</p></div></li></ul></template>
  </div>
</template>

<style scoped>
.events-page{padding:12px 16px 30px;min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #e8d9ca;border-radius:999px;background:#fff;color:#9f4d15;font-size:27px}.spacer{width:44px}.hero{margin-top:15px;padding:23px 20px;border-radius:22px;background:linear-gradient(140deg,#e8eee0,#f6eddc 65%,#f8e1d3)}.eyebrow{color:#587155;font-size:10px;letter-spacing:.13em;font-weight:850}.hero h1{font-size:clamp(27px,8vw,35px);line-height:1.27;letter-spacing:-.04em;margin:17px 0 8px}.hero p{margin:0;color:#635d50;font-size:12px;line-height:1.7}.city-form{margin:17px 0 0;padding:16px;border:1px solid #e9ddd1;border-radius:17px;background:#fff}.city-form label{display:block;font-size:12px;font-weight:800;margin-bottom:9px}.city-form div{display:flex;gap:8px}.city-form input{min-width:0;width:0;flex:1;min-height:47px;border:1px solid #d7c9bb;border-radius:12px;padding:0 11px;font-size:16px}.city-form button{min-height:47px;border:0;border-radius:12px;background:#b85111;color:#fff;padding:0 16px;font-size:12px;font-weight:850}.tabs{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:17px;padding:4px;border-radius:999px;background:#f1e9df}.tabs button{min-height:44px;border:0;border-radius:999px;background:transparent;color:#706458;font-size:13px;font-weight:800}.tabs button.on{background:#fff;color:#9e4712;box-shadow:0 2px 8px #4b2f1916}.notice{padding:12px 14px;border-radius:13px;background:#fff1df;color:#754a24;font-size:11px;line-height:1.7}.list{list-style:none;display:grid;gap:10px;margin:15px 0 0;padding:0}.card{display:flex;align-items:flex-start;gap:12px;padding:15px;border:1px solid #e9ddd2;border-radius:17px;background:#fff;min-width:0}.date,.heart{display:grid;place-content:center;text-align:center;flex:none;width:53px;min-height:58px;border-radius:13px;background:#fff0df;color:#a64e17}.date span{font-size:10px}.date strong{font-size:22px}.heart{background:#e7efe6;color:#53785e;font-size:27px}.card-body{min-width:0;flex:1}.kicker{color:#a34b15;font-size:10px;font-weight:800}.card h2{font-size:16px;line-height:1.4;overflow-wrap:anywhere;margin:6px 0}.card p{color:#74685e;font-size:11px;line-height:1.65;margin:0}.detail-button{display:flex;justify-content:space-between;align-items:center;min-height:44px;width:100%;padding:0;margin-top:5px;border:0;background:none;color:#a34b14;text-align:left;font-size:12px;font-weight:850}.detail{padding:12px;border-radius:12px;background:#f9f5ef}.detail p+p{margin-top:7px}.detail strong{color:#40362c}.detail-state{padding:10px;border-radius:10px;background:#f6f1ea;color:#6f6357;font-size:11px;line-height:1.6}.detail-state.error{color:#9f3428;background:#fff1ef}.detail-state button{border:0;background:none;color:inherit;text-decoration:underline;font-weight:850}.adoption-copy{white-space:pre-wrap;overflow-wrap:anywhere}.expiry{margin-top:8px!important;color:#a44e16!important}.state{display:grid;justify-items:center;text-align:center;gap:9px;padding:30px 15px;border:1px dashed #dacbbc;border-radius:19px;background:#fff}.state strong{font-size:14px}.state p{margin:0;color:#776b60;font-size:12px}.state button{min-height:44px;margin-top:7px;border:0;border-radius:999px;background:#b85111;color:#fff;padding:0 17px;font-weight:800}.symbol{display:grid;place-items:center;width:48px;height:48px;border-radius:15px;background:#fff0df;color:#ae5117;font-size:25px}.state.error{background:#fff8f6;border-color:#e8beb6}@media(max-width:345px){.card{padding:12px}.date,.heart{width:46px}}
</style>
