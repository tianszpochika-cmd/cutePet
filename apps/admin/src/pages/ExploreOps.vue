<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { api } from '@cutepet/api-client';

interface PoiRow { id: number; name: string; city: string; type: string; avg?: number | null; reviewCount?: number }
interface RouteRow { id: number; name: string; city: string; state: string }
interface ActivityRow { id: number; title: string; city: string; state: string; beginsAt?: string }
const pois = ref<PoiRow[]>([]);
const routes = ref<RouteRow[]>([]);
const activities = ref<ActivityRow[]>([]);
const poiLoading = ref(true);
const routeLoading = ref(true);
const activityLoading = ref(true);
const poiError = ref('');
const routeError = ref('');
const activityError = ref('');
const selectedPoiId = ref<number | null>(null);
const selectedPoi = computed(() => pois.value.find((row) => row.id === selectedPoiId.value) ?? null);

async function loadPois() {
  poiLoading.value = true; poiError.value = '';
  try {
    const result = (await api.poisSearch()) as { items?: PoiRow[] };
    if (!result || !Array.isArray(result.items)) throw new Error('公开场所数据格式暂时不可用');
    pois.value = result.items.filter((row) => typeof row.id === 'number');
    if (!pois.value.some((row) => row.id === selectedPoiId.value)) selectedPoiId.value = null;
  } catch (cause) { pois.value = []; selectedPoiId.value = null; poiError.value = cause instanceof Error ? cause.message : '公开场所暂时无法读取'; }
  finally { poiLoading.value = false; }
}
async function loadRoutes() {
  routeLoading.value = true; routeError.value = '';
  try {
    const result = await api.routesList();
    if (!Array.isArray(result)) throw new Error('公开路线数据格式暂时不可用');
    routes.value = (result as RouteRow[]).filter((row) => row.state === 'PUBLISHED');
  } catch (cause) { routes.value = []; routeError.value = cause instanceof Error ? cause.message : '公开路线暂时无法读取'; }
  finally { routeLoading.value = false; }
}
async function loadActivities() {
  activityLoading.value = true; activityError.value = '';
  try {
    const result = await api.activitiesList();
    if (!Array.isArray(result)) throw new Error('公开活动数据格式暂时不可用');
    activities.value = (result as ActivityRow[]).filter((row) => row.state === 'PUBLISHED');
  } catch (cause) { activities.value = []; activityError.value = cause instanceof Error ? cause.message : '公开活动暂时无法读取'; }
  finally { activityLoading.value = false; }
}
onMounted(() => { void loadPois(); void loadRoutes(); void loadActivities(); });
</script>

<template>
  <div class="ops-page">
    <div class="desktop-notice">探索运营请在电脑端查看。</div>
    <div class="workspace">
      <header class="page-head"><div><p class="eyebrow">EXPLORE OPERATIONS</p><h1>场所与探索供给</h1><p>先辨认公开状态，再进入纠错、停业、评价与活动的专门处理流程。</p></div><span class="preview-badge">只读界面预览</span></header>
      <div class="boundary"><strong>公开视图不等于运营队列</strong><p>场所、路线和活动的数据来自公开接口，只展示当前可见对象。待处理纠错、隐藏评价、待审路线及重大活动变更需真实管理员身份与对象级授权，本页不会模拟完成这些处理。</p></div>
      <div class="overview">
        <article class="metric"><span>公开场所</span><strong>{{ poiLoading || poiError ? '—' : pois.length }}</strong><small>{{ poiError ? '读取失败' : '正常展示中的场所' }}</small></article>
        <article class="metric"><span>公开路线</span><strong>{{ routeLoading || routeError ? '—' : routes.length }}</strong><small>{{ routeError ? '读取失败' : '已发布路线' }}</small></article>
        <article class="metric"><span>公开活动</span><strong>{{ activityLoading || activityError ? '—' : activities.length }}</strong><small>{{ activityError ? '读取失败' : '已发布活动' }}</small></article>
      </div>
      <div class="workgrid"><section class="panel"><div class="panel-head"><div><h2>场所 · 公开列表</h2><p>只供核对场所名称与公开状态，不含待处理纠错。</p></div><button type="button" class="subtle" :disabled="poiLoading" @click="loadPois">刷新</button></div>
          <div v-if="poiLoading" class="state" role="status">正在读取公开场所…</div><div v-else-if="poiError" class="state error" role="alert"><strong>读取失败</strong><p>{{ poiError }}</p><button type="button" @click="loadPois">重试</button></div><div v-else-if="!pois.length" class="state"><strong>当前没有公开场所</strong><p>这不代表后台场所库或纠错队列为空。</p></div>
          <div v-else class="table-scroll"><table><thead><tr><th>场所</th><th>城市 / 类型</th><th>评分</th><th>查看</th></tr></thead><tbody><tr v-for="row in pois" :key="row.id" :class="{ selected: selectedPoiId === row.id }"><td><strong>{{ row.name }}</strong><small>#{{ row.id }}</small></td><td>{{ row.city }} · {{ row.type }}</td><td>{{ row.avg == null ? '暂无评分' : `${row.avg} 分` }}<small>{{ row.reviewCount ?? 0 }} 条评价</small></td><td><button type="button" class="select" :aria-pressed="selectedPoiId === row.id" @click="selectedPoiId = row.id">核对影响</button></td></tr></tbody></table></div>
        </section><aside class="detail"><p class="eyebrow">OBJECT SCOPE</p><template v-if="selectedPoi"><h2>{{ selectedPoi.name }}</h2><p>场所 #{{ selectedPoi.id }} · {{ selectedPoi.city }}</p><div class="divider"></div><h3>停业与纠错前需要核对</h3><ul><li>现值、建议值及各自证据来源</li><li>同一字段的相反建议和合并关系</li><li>真实处理人权限、当前对象版本及处理锁</li><li>公开列表影响、贡献者通知及审计</li></ul><p class="no-action">当前仅确认公开对象存在，不提供停业或纠错提交。</p></template><template v-else><h2>选择一个场所</h2><p>从公开列表中选择对象，查看处理前需要核对的范围。</p></template></aside></div>
      <div class="lower-grid"><section class="panel"><div class="panel-head"><div><h2>已发布路线</h2><p>公开信息 · 不是待审路线队列</p></div><button type="button" class="subtle" :disabled="routeLoading" @click="loadRoutes">刷新</button></div><div v-if="routeLoading" class="state" role="status">正在读取路线…</div><div v-else-if="routeError" class="state error" role="alert"><p>{{ routeError }}</p><button type="button" @click="loadRoutes">重试</button></div><p v-else-if="!routes.length" class="state">当前没有公开路线；待审数量暂不可读取。</p><ul v-else class="compact-list"><li v-for="row in routes.slice(0, 6)" :key="row.id"><strong>{{ row.name }}</strong><span>{{ row.city }} · #{{ row.id }}</span></li></ul></section>
        <section class="panel"><div class="panel-head"><div><h2>已发布活动</h2><p>公开信息 · 不含重大变更待办</p></div><button type="button" class="subtle" :disabled="activityLoading" @click="loadActivities">刷新</button></div><div v-if="activityLoading" class="state" role="status">正在读取活动…</div><div v-else-if="activityError" class="state error" role="alert"><p>{{ activityError }}</p><button type="button" @click="loadActivities">重试</button></div><p v-else-if="!activities.length" class="state">当前没有公开活动；变更审核数量暂不可读取。</p><ul v-else class="compact-list"><li v-for="row in activities.slice(0, 6)" :key="row.id"><strong>{{ row.title }}</strong><span>{{ row.city }} · #{{ row.id }}</span></li></ul></section></div>
      <section class="panel workflow"><div class="panel-head"><div><h2>待接入的处理队列</h2><p>以下动作均需当前对象、权限、服务端回执与通知结果。</p></div></div><div class="workflow-grid"><article><span class="number">01</span><h3>纠错工单</h3><p>采纳或驳回要核对同字段建议与现值，附处理依据并分别通知贡献者。</p><small>correction.handle · 队列暂不可读取</small></article><article><span class="number">02</span><h3>评价抽审</h3><p>隐藏或恢复评价后，公开展示与平均分须按有效样本重新计算。</p><small>ugv.review.hide · 证据暂不可读取</small></article><article><span class="number">03</span><h3>路线与活动</h3><p>路线先审后发；活动重大变更需比对版本并跟踪报名通知，不把入队当作送达。</p><small>route.approve / activity.manage · 待审版本暂不可读取</small></article></div></section>
    </div>
  </div>
</template>

<style scoped>
.ops-page{color:#2b2118}.workspace{max-width:1360px;margin:0 auto;display:grid;gap:20px}.desktop-notice{display:none}.page-head{display:flex;justify-content:space-between;align-items:start;gap:20px}.page-head h1{margin:0;font-size:clamp(29px,3vw,39px);letter-spacing:-.04em}.page-head p:last-child{margin:10px 0 0;color:#695d53}.eyebrow{margin:0 0 10px;color:#b85111;font-size:11px;font-weight:850;letter-spacing:.13em}.preview-badge{padding:7px 12px;border-radius:999px;background:#fff1e8;color:#8f3d0b;font-size:12px;font-weight:800;white-space:nowrap}.boundary{padding:17px 21px;border-left:4px solid #b85111;border-radius:12px;background:#fff1e8}.boundary strong{color:#8f3d0b}.boundary p{margin:5px 0 0;color:#695d53;font-size:13px;line-height:1.7}.overview{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}.metric{display:flex;flex-direction:column;gap:5px;padding:19px 21px;border:1px solid #eadfd4;border-radius:15px;background:#fff}.metric span{color:#695d53;font-size:12px}.metric strong{font-size:29px;line-height:1.2}.metric small{color:#897669;font-size:11px}.workgrid{display:grid;grid-template-columns:minmax(0,1fr) 320px;gap:17px}.panel,.detail{min-width:0;border:1px solid #eadfd4;border-radius:17px;background:#fff;box-shadow:0 12px 30px rgba(83,55,28,.04)}.panel-head{display:flex;justify-content:space-between;align-items:start;gap:14px;padding:20px 22px;border-bottom:1px solid #eadfd4}.panel-head h2{margin:0;font-size:18px}.panel-head p{margin:5px 0 0;color:#695d53;font-size:12px}.subtle,.select{min-height:34px;padding:0 11px;border:1px solid #eadfd4;border-radius:9px;background:#fff;color:#b85111;font-size:12px;font-weight:750}.subtle:disabled{opacity:.5}.table-scroll{max-height:460px;overflow:auto}table{width:100%;min-width:600px;border-collapse:collapse}th,td{padding:15px 13px;border-bottom:1px solid #f0e7df;text-align:left;font-size:12px}th{color:#695d53;background:#fffdf9}td strong{display:block;max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px}td small{display:block;margin-top:4px;color:#897669}.selected{background:#fff8f0}.detail{padding:24px}.detail h2{margin:0;font-size:23px}.detail h3{font-size:14px}.detail p{color:#695d53;font-size:13px;line-height:1.7}.detail ul{display:grid;gap:9px;padding-left:20px;color:#5d524a;font-size:12px;line-height:1.6}.divider{height:1px;margin:20px 0;background:#eadfd4}.no-action{padding:11px;border-radius:9px;background:#f6f1ed;font-weight:700}.state{padding:26px;color:#695d53;font-size:13px}.state strong{color:#2b2118;font-size:15px}.state p{margin:7px 0 10px}.state button{border:0;background:none;color:#b85111;font-weight:750}.state.error{color:#ab3131}.lower-grid{display:grid;grid-template-columns:1fr 1fr;gap:17px}.compact-list{display:grid;gap:0;list-style:none;margin:0;padding:0 20px}.compact-list li{display:flex;justify-content:space-between;gap:12px;padding:13px 0;border-bottom:1px solid #f0e7df;font-size:12px}.compact-list span{color:#897669}.workflow-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;padding:20px}.workflow-grid article{padding:19px;border-radius:12px;background:#faf7f3}.workflow-grid h3{margin:9px 0;font-size:15px}.workflow-grid p{min-height:70px;color:#695d53;font-size:12px;line-height:1.7}.workflow-grid small{color:#6f4788;font-size:10px;font-weight:750}.number{color:#b85111;font-size:12px;font-weight:850}@media(max-width:1100px){.workgrid{grid-template-columns:1fr}.workflow-grid{grid-template-columns:1fr}.workflow-grid p{min-height:0}}@media(max-width:800px){.workspace{display:none}.desktop-notice{display:block;padding:24px;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#695d53}}
</style>
