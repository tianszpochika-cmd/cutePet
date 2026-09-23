<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { POI_TYPES } from '../../../web/src/domain/explore';

interface PoiCard { id: number; name: string; type: string; city: string; avg?: number | null; reviewCount?: number; openHours?: string | null }

const route = useRoute();
const router = useRouter();
const cityDraft = ref('');
const city = computed(() => typeof route.query.city === 'string' ? route.query.city.trim() : '');
const type = computed(() => typeof route.query.type === 'string' ? route.query.type : '');
const items = ref<PoiCard[]>([]);
const total = ref(0);
const loading = ref(false);
const error = ref('');
let latestRequest = 0;
let active = true;

async function load() {
  const request = ++latestRequest;
  items.value = [];
  total.value = 0;
  error.value = '';
  if (!city.value) { loading.value = false; return; }
  loading.value = true;
  try {
    const response = await api.poisSearch({ query: { city: city.value, type: type.value || undefined } }) as { items?: PoiCard[]; total?: number };
    if (!active || request !== latestRequest) return;
    if (!Array.isArray(response?.items)) throw new Error('地点数据格式不完整');
    items.value = response.items.filter((item) => typeof item?.id === 'number' && typeof item.name === 'string');
    total.value = typeof response.total === 'number' ? response.total : items.value.length;
  } catch (cause) {
    if (active && request === latestRequest) error.value = cause instanceof Error ? cause.message : '地点暂时无法读取';
  } finally { if (active && request === latestRequest) loading.value = false; }
}

function chooseCity() {
  const value = cityDraft.value.trim();
  if (!value) { error.value = '请先输入城市名称'; return; }
  void router.replace({ path: '/explore', query: { city: value, type: type.value || undefined } });
}
function chooseType(next: string) {
  void router.replace({ path: '/explore', query: { city: city.value || undefined, type: next || undefined } });
}

watch([city, type], () => { cityDraft.value = city.value; void load(); }, { immediate: true });
onBeforeUnmount(() => { active = false; ++latestRequest; });
</script>

<template>
  <div class="explore-page">
    <header class="hero"><span class="eyebrow">GO OUT TOGETHER · 城市探索</span><h1>一起出门，<br>从选城市开始。</h1><p>手动选择城市，查看已公开的宠物友好地点。页面不会读取定位或推算距离。</p></header>

    <form class="city-card" @submit.prevent="chooseCity">
      <label for="explore-city">想探索哪座城市？</label>
      <div class="city-row"><input id="explore-city" v-model="cityDraft" type="search" name="city" placeholder="输入城市名称" autocomplete="address-level2" maxlength="40" /><button type="submit">查看地点</button></div>
      <p>{{ city ? `正在查看：${city}` : '无需定位授权，选好城市就能查看列表。' }}</p>
    </form>

    <nav class="shortcuts" aria-label="继续探索"><router-link :to="{ path: '/events', query: city ? { city } : {} }">活动信息 <span aria-hidden="true">↗</span></router-link><router-link to="/goods">逛好物 <span aria-hidden="true">↗</span></router-link></nav>

    <section aria-labelledby="poi-title">
      <div class="section-head"><div><span class="eyebrow">PLACES TO KNOW</span><h2 id="poi-title">地点列表</h2></div><span v-if="city && !loading && !error" class="count">{{ total }} 个地点</span></div>
      <nav class="chips" aria-label="地点类型"><button type="button" :class="{ on: !type }" @click="chooseType('')">全部</button><button v-for="value in POI_TYPES" :key="value" type="button" :class="{ on: type === value }" @click="chooseType(value)">{{ value }}</button></nav>
      <div v-if="!city" class="state"><span class="symbol" aria-hidden="true">⌖</span><strong>先选择一座城市</strong><p>地点会按城市与类型从平台读取。</p></div>
      <div v-else-if="loading" class="state" role="status"><span class="symbol" aria-hidden="true">◌</span><strong>正在读取{{ city }}的地点…</strong></div>
      <div v-else-if="error" class="state error" role="alert"><span class="symbol" aria-hidden="true">!</span><strong>地点暂时无法读取</strong><p>{{ error }}</p><button type="button" @click="load">重新加载</button></div>
      <div v-else-if="!items.length" class="state"><span class="symbol" aria-hidden="true">✳</span><strong>当前条件下没有公开地点</strong><p>可以换一个类别或城市。</p><button v-if="type" type="button" @click="chooseType('')">查看全部类型</button></div>
      <ul v-else class="poi-list"><li v-for="item in items" :key="item.id"><router-link :to="{ path: `/explore/poi/${item.id}`, query: { city, type: type || undefined } }" class="poi-card" :data-testid="`poi-${item.id}`"><span class="poi-art" aria-hidden="true">⌖</span><span class="poi-body"><strong>{{ item.name }}</strong><small>{{ item.city || city }} · {{ item.type }}</small><small v-if="item.avg !== null && typeof item.avg === 'number'">★ {{ item.avg }} <span v-if="typeof item.reviewCount === 'number'">· {{ item.reviewCount }} 条公开评价</span></small><small v-else>暂无公开评分</small></span><span class="arrow" aria-hidden="true">›</span></router-link></li></ul>
      <p class="footnote">到访前请核对地址、营业状态和现场规则。未授权定位时不展示距离。</p>
    </section>
  </div>
</template>

<style scoped>
.explore-page{padding:16px 16px 30px;min-width:0}.hero{padding:22px 20px 25px;border-radius:23px;background:linear-gradient(140deg,#e5efe5,#f2f4e7 58%,#fff2df)}.eyebrow{color:#55705a;font-size:10px;letter-spacing:.13em;font-weight:850}.hero h1{font-size:clamp(27px,8vw,36px);letter-spacing:-.04em;line-height:1.25;margin:17px 0 8px}.hero p{color:#5c6658;font-size:13px;line-height:1.7;margin:0;max-width:32ch}.city-card{position:relative;display:grid;gap:9px;margin:-12px 9px 0;padding:17px;border:1px solid #e3d8cc;border-radius:19px;background:#fff;box-shadow:0 10px 24px #49372712}.city-card label{font-weight:800;font-size:14px}.city-row{display:flex;gap:8px;min-width:0}.city-row input{width:0;flex:1;min-width:0;min-height:48px;border:1px solid #d9cdc0;border-radius:12px;padding:0 11px;background:#fff}.city-row button{flex:none;min-height:48px;border:0;border-radius:12px;background:#b85111;color:#fff;padding:0 12px;font-size:13px;font-weight:800}.city-card p{margin:0;color:#70675d;font-size:11px;line-height:1.5}.shortcuts{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;margin:18px 0 26px}.shortcuts a{display:flex;justify-content:space-between;align-items:center;gap:5px;min-width:0;padding:0 13px;border:1px solid #e6d7c6;border-radius:14px;background:#fff8ef;color:#85410f;text-decoration:none;font-weight:800;font-size:13px}.section-head{display:flex;justify-content:space-between;align-items:end;gap:7px}.section-head h2{margin:5px 0 0;font-size:20px}.count{font-size:11px;color:#776b60;white-space:nowrap}.chips{display:flex;gap:8px;overflow-x:auto;margin:14px -16px 13px;padding:0 16px 6px;scrollbar-width:none}.chips::-webkit-scrollbar{display:none}.chips button{flex:none;min-height:44px;border:1px solid #e2d7cc;border-radius:999px;background:#fff;color:#685c50;padding:0 16px;font-size:13px;font-weight:750}.chips button.on{background:#b85111;border-color:#b85111;color:#fff}.state{display:grid;justify-items:center;text-align:center;gap:7px;padding:29px 14px;border:1px dashed #d9cdbf;border-radius:19px;background:#fff;color:#635c52}.state strong{color:#2b2118}.state p{margin:0;font-size:12px;line-height:1.6}.symbol{display:grid;place-items:center;width:49px;height:49px;border-radius:15px;background:#e8efe7;color:#43684c;font-size:26px}.state button{min-height:44px;margin-top:7px;border:0;border-radius:999px;background:#b85111;color:#fff;padding:0 17px;font-weight:800}.state.error{background:#fff7f5;border-color:#e6bcb2}.poi-list{list-style:none;margin:0;padding:0;display:grid;gap:9px}.poi-card{display:flex;align-items:center;gap:12px;min-width:0;min-height:100px;padding:13px;border:1px solid #e8ddd2;border-radius:17px;background:#fff;text-decoration:none;color:#2b2118}.poi-art{flex:none;display:grid;place-items:center;width:56px;height:62px;border-radius:12px;background:linear-gradient(135deg,#dfeadb,#f1f3e6);color:#587967;font-size:28px}.poi-body{display:grid;gap:5px;flex:1;min-width:0}.poi-body strong{font-size:15px;line-height:1.35;overflow-wrap:anywhere}.poi-body small{font-size:11px;color:#746b60;line-height:1.4}.arrow{color:#a54c15;font-size:24px}.footnote{color:#756a60;font-size:11px;line-height:1.6;margin:15px 2px 0}@media(max-width:345px){.city-row button{padding:0 9px;font-size:12px}.city-card{margin-left:0;margin-right:0}}
.city-row input{font-size:16px}
</style>
