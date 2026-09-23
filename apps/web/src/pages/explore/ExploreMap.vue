<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { POI_TYPES } from '../../domain/explore';

interface PoiItem {
  id: number;
  name: string;
  type: string;
  city: string;
  avg: number | null;
  reviewCount: number;
  openHours?: string;
}

const router = useRouter();
const city = ref('北京');
const type = ref<string | undefined>();
const items = ref<PoiItem[]>([]);
const loading = ref(false);
const error = ref('');
const filterOpen = ref(false);
let latestRequest = 0;

async function load() {
  const request = ++latestRequest;
  loading.value = true;
  error.value = '';
  try {
    // 无定位授权和坐标时只按城市检索，不能把服务端的 0 km 当作真实距离。
    const query: Record<string, string> = {};
    if (city.value.trim()) query.city = city.value.trim();
    if (type.value) query.type = type.value;
    const response = await api.poisSearch({ query });
    if (request !== latestRequest) return;
    const payload = response as { items?: PoiItem[] };
    items.value = Array.isArray(payload?.items) ? payload.items : [];
  } catch (cause) {
    if (request !== latestRequest) return;
    items.value = [];
    error.value = cause instanceof Error ? cause.message : '场所加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
function selectType(next: string | undefined) {
  type.value = next;
  void load();
}
onMounted(() => void load());
</script>

<template>
  <div class="explore">
    <header class="heading">
      <div>
        <p class="eyebrow">EXPLORE · 城市探索</p>
        <h1>和它一起出门</h1>
        <p class="lede">按城市查看宠物友好场所。当前未读取定位，也不推测你和场所的距离。</p>
      </div>
      <div class="quick-links">
        <button type="button" @click="router.push('/explore/routes')">遛宠路线 <span aria-hidden="true">↗</span></button>
        <button type="button" @click="router.push('/events')">本地活动 <span aria-hidden="true">↗</span></button>
      </div>
    </header>

    <form class="search" @submit.prevent="load">
      <label for="explore-city">城市</label>
      <input id="explore-city" v-model="city" list="explore-cities" autocomplete="address-level2" data-testid="city" placeholder="输入城市或留空查看全部" />
      <datalist id="explore-cities"><option value="北京" /><option value="上海" /><option value="成都" /><option value="杭州" /><option value="广州" /></datalist>
      <button type="submit" class="search-btn">查找场所</button>
      <button type="button" class="filter-btn" :aria-expanded="filterOpen" aria-controls="explore-filters" data-testid="open-filter" @click="filterOpen = !filterOpen">筛选类型</button>
    </form>

    <div v-if="filterOpen" id="explore-filters" class="panel" data-testid="filter-panel">
      <p>场所类型</p>
      <div class="chips">
        <button type="button" :class="{ on: !type }" :aria-pressed="!type" @click="selectType(undefined)">全部</button>
        <button v-for="kind in POI_TYPES" :key="kind" type="button" :class="{ on: type === kind }" :aria-pressed="type === kind" @click="selectType(kind)">{{ kind }}</button>
      </div>
    </div>

    <section class="map-status" aria-label="地图功能状态">
      <div class="map-icon" aria-hidden="true">◎</div>
      <div>
        <strong>地图视图待接入</strong>
        <p>地图服务与定位授权接入前，先用真实场所列表浏览；不会展示模拟地图标记或虚构距离。</p>
      </div>
    </section>

    <div class="list-heading">
      <h2>场所列表</h2>
      <span v-if="!loading && !error">{{ items.length }} 个结果</span>
    </div>
    <p v-if="loading" class="status" role="status">正在查找场所…</p>
    <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="load">重试</button></p>
    <ul v-else class="list">
      <li v-for="item in items" :key="item.id">
        <button type="button" class="poi" :data-testid="'poi-' + item.id" @click="router.push('/explore/poi/' + item.id)">
          <span class="poi-main"><strong>{{ item.name }}</strong><span class="go" aria-hidden="true">↗</span></span>
          <span class="meta">{{ item.city }} · {{ item.type }} · {{ item.avg === null ? '暂无评分' : '★ ' + item.avg + '（' + item.reviewCount + ' 条）' }}</span>
          <span v-if="item.openHours" class="hours">{{ item.openHours }}</span>
        </button>
      </li>
      <li v-if="items.length === 0" class="empty" data-testid="empty">这个筛选条件下还没有公开场所。试试其他城市或类型。</li>
    </ul>
  </div>
</template>

<style scoped>
.explore{max-width:980px;margin:auto;padding:clamp(24px,4vw,48px) 20px 80px;color:#2b2118}.heading{display:flex;justify-content:space-between;align-items:end;gap:20px;flex-wrap:wrap}.eyebrow{color:#b45309;font-size:12px;font-weight:800;letter-spacing:.15em;margin:0 0 8px}h1{font-size:clamp(28px,4vw,42px);letter-spacing:-.03em;margin:0}.lede{color:#71675e;line-height:1.7;margin:10px 0 0}.quick-links{display:flex;gap:8px}.quick-links button{border:1px solid #e8d8c9;background:#fff;border-radius:999px;padding:10px 16px;color:#6c4730;cursor:pointer;font:inherit}.quick-links span{font-size:13px}.search{display:flex;align-items:center;gap:10px;margin-top:30px;padding:10px;background:#fff;border:1px solid #eaded1;border-radius:16px;box-shadow:0 8px 28px #3a23130a}.search label{font-size:13px;font-weight:700;white-space:nowrap;padding-left:8px}.search input{min-width:0;flex:1;border:0;outline-offset:3px;padding:8px;font:inherit;color:#2b2118}.search button{border:0;border-radius:10px;padding:10px 16px;font:inherit;cursor:pointer;white-space:nowrap}.search-btn{background:#a64613;color:#fff;font-weight:700}.filter-btn{background:#f8f2ec;color:#6c4730}.panel{background:#fff;border:1px solid #eaded1;border-radius:16px;padding:16px;margin-top:10px}.panel p{margin:0 0 10px;color:#6d6258;font-size:13px}.chips{display:flex;flex-wrap:wrap;gap:8px}.chips button{border:1px solid #e8d8c9;border-radius:999px;background:#fff;padding:7px 12px;color:#67584b;cursor:pointer;font:inherit}.chips button.on{background:#a64613;border-color:#a64613;color:#fff}.map-status{display:flex;align-items:center;gap:16px;margin-top:18px;padding:18px 20px;background:#f4f5f1;border:1px solid #e2e6dc;border-radius:16px}.map-icon{width:44px;height:44px;border-radius:12px;display:grid;place-items:center;background:#e2ebe0;color:#48714c;font-size:26px;flex:none}.map-status strong{color:#365c3a}.map-status p{margin:4px 0 0;color:#67766a;line-height:1.6;font-size:13px}.list-heading{display:flex;align-items:baseline;justify-content:space-between;margin:28px 0 12px}.list-heading h2{font-size:20px;margin:0}.list-heading span{font-size:13px;color:#776b5e}.list{list-style:none;margin:0;padding:0;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.poi{width:100%;height:100%;text-align:left;background:#fff;border:1px solid #eaded1;border-radius:16px;padding:18px;display:grid;align-content:start;gap:9px;cursor:pointer;font:inherit;color:#2b2118;box-shadow:0 8px 28px #3a231308}.poi:hover{border-color:#d8a77c}.poi-main{display:flex;justify-content:space-between;gap:10px}.poi strong{font-size:17px}.go{color:#a64613}.meta,.hours{font-size:13px;color:#74695f}.empty{grid-column:1/-1;padding:36px 20px;text-align:center;background:#fff;border:1px dashed #decfbe;border-radius:16px;color:#776a5d}.status{padding:20px;background:#f6f1eb;color:#6c6055;border-radius:12px}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;text-decoration:underline;color:inherit;cursor:pointer}@media(max-width:640px){.search{flex-wrap:wrap}.search input{flex-basis:calc(100% - 70px)}.search button{flex:1}.quick-links{width:100%}.quick-links button{flex:1}.list{grid-template-columns:1fr}}
</style>
