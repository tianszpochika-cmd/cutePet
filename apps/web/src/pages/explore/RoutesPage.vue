<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface RouteRow {
  id: number;
  name: string;
  city: string;
  distanceM?: number;
  durationMin?: number;
  difficulty?: string;
  note?: string;
  state: string;
}

const router = useRouter();
const city = ref('');
const items = ref<RouteRow[]>([]);
const expandedId = ref<number | null>(null);
const loading = ref(false);
const error = ref('');
let latestRequest = 0;

async function load() {
  const request = ++latestRequest;
  loading.value = true;
  error.value = '';
  try {
    const query = city.value.trim() ? { city: city.value.trim() } : {};
    const response = await api.routesList({ query });
    if (request !== latestRequest) return;
    items.value = Array.isArray(response) ? response as RouteRow[] : [];
  } catch (cause) {
    if (request !== latestRequest) return;
    items.value = [];
    error.value = cause instanceof Error ? cause.message : '路线加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
function distance(value?: number) {
  return typeof value === 'number' && Number.isFinite(value) && value >= 0
    ? (Math.round(value / 100) / 10).toFixed(1) + ' 公里'
    : '距离未提供';
}
onMounted(() => void load());
</script>

<template>
  <div class="routes">
    <button type="button" class="back" @click="router.push('/explore')">← 返回场所列表</button>
    <div class="heading">
      <div>
        <p class="eyebrow">WALK TOGETHER · 遛宠路线</p>
        <h1>换条路，看看新风景</h1>
        <p class="lede">浏览已经公开的路线，按城市找合适的出行灵感。</p>
      </div>
    </div>

    <form class="search" @submit.prevent="load">
      <label for="route-city">城市</label>
      <input id="route-city" v-model="city" list="route-cities" autocomplete="address-level2" placeholder="留空查看全部城市" />
      <datalist id="route-cities"><option value="北京" /><option value="上海" /><option value="成都" /><option value="杭州" /><option value="广州" /></datalist>
      <button type="submit">查找路线</button>
    </form>

    <section class="publish-note">
      <div><strong>路线发布暂未开放</strong><p>地图选点、身份校验及审核回执接通后，才能提交可靠的路线。当前不会用手输坐标模拟选点。</p></div>
      <button type="button" data-testid="publish-route" disabled aria-describedby="route-publish-reason">发布待接入</button>
      <span id="route-publish-reason" class="sr-only">地图选点与身份校验尚未接入。</span>
    </section>

    <div class="list-heading"><h2>已公开的路线</h2><span v-if="!loading && !error">{{ items.length }} 条</span></div>
    <p v-if="loading" class="status" role="status">正在读取路线…</p>
    <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="load">重试</button></p>
    <ul v-else class="list">
      <li v-for="item in items" :key="item.id" class="card">
        <div class="route-mark" aria-hidden="true">⌁</div>
        <div class="card-body">
          <p class="kicker">{{ item.city }} · 公开路线</p>
          <h3>{{ item.name }}</h3>
          <p class="meta">{{ distance(item.distanceM) }}<span v-if="item.durationMin"> · 约 {{ item.durationMin }} 分钟</span><span v-if="item.difficulty"> · {{ item.difficulty }}</span></p>
          <button v-if="item.note" type="button" class="detail-btn" :aria-expanded="expandedId === item.id" @click="expandedId = expandedId === item.id ? null : item.id">{{ expandedId === item.id ? '收起说明' : '查看说明' }}</button>
          <p v-if="expandedId === item.id && item.note" class="note">{{ item.note }}</p>
        </div>
      </li>
      <li v-if="items.length === 0" class="empty" data-testid="empty">目前没有符合条件的公开路线。试试其他城市。</li>
    </ul>
    <p class="footer-note">路线来自用户投稿，出行时请关注实际路况、宠物状态和当地规则。</p>
  </div>
</template>

<style scoped>
.routes{max-width:860px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#a64613;cursor:pointer;font:inherit}.heading{margin:27px 0 24px}.eyebrow{color:#a64613;font-weight:800;font-size:12px;letter-spacing:.14em;margin:0 0 8px}h1{font-size:clamp(28px,4vw,42px);letter-spacing:-.03em;margin:0}.lede{color:#71675e;line-height:1.7;margin:10px 0 0}.search{display:flex;align-items:center;gap:10px;padding:10px;background:#fff;border:1px solid #eaded1;border-radius:16px}.search label{font-size:13px;font-weight:700;padding-left:8px}.search input{flex:1;min-width:0;border:0;padding:8px;font:inherit}.search button{border:0;background:#a64613;color:#fff;padding:10px 16px;border-radius:10px;cursor:pointer;font:inherit}.publish-note{margin:18px 0 30px;padding:18px 20px;background:#f4f5f1;border:1px solid #e2e6dc;border-radius:16px;display:flex;justify-content:space-between;align-items:center;gap:20px}.publish-note strong{color:#365c3a}.publish-note p{margin:5px 0 0;color:#67766a;line-height:1.6;font-size:13px}.publish-note button{background:#e4e8e0;color:#69736a;border:0;border-radius:999px;padding:10px 15px;white-space:nowrap;font:inherit;cursor:not-allowed}.list-heading{display:flex;justify-content:space-between;align-items:baseline;margin-bottom:12px}.list-heading h2{font-size:20px;margin:0}.list-heading span,.meta{font-size:13px;color:#786b60}.list{list-style:none;margin:0;padding:0;display:grid;gap:12px}.card{background:#fff;border:1px solid #eaded1;border-radius:16px;padding:19px;display:flex;gap:17px;box-shadow:0 8px 28px #3a231308}.route-mark{width:52px;height:52px;display:grid;place-items:center;border-radius:14px;background:#eaf2e7;color:#52805b;font-size:27px;flex:none}.card-body{min-width:0}.kicker{font-size:12px;font-weight:700;color:#9b5b2b;margin:1px 0 5px}.card h3{font-size:18px;margin:0 0 7px}.meta{margin:0;line-height:1.6}.detail-btn{border:0;background:none;text-decoration:underline;color:#a64613;padding:0;margin-top:8px;cursor:pointer;font:inherit}.note{background:#faf7f3;padding:12px;border-radius:10px;color:#584f46;white-space:pre-line;line-height:1.7;overflow-wrap:anywhere}.empty{padding:35px 20px;text-align:center;border:1px dashed #decfbe;background:#fff;border-radius:16px;color:#776a5d}.footer-note{font-size:12px;color:#84766a;line-height:1.6;margin-top:20px}.status{padding:20px;background:#f5f1ec;border-radius:12px;color:#6c6055}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}.sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}@media(max-width:560px){.search{flex-wrap:wrap}.search input{flex-basis:calc(100% - 64px)}.search button{width:100%}.publish-note{display:block}.publish-note button{margin-top:12px}}
</style>
