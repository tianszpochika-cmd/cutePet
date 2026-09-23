<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface Poi { id: number; name: string; type: string; city: string; address?: string | null; openHours?: string | null; attrs?: string | null; state?: string; avg?: number | null; reviewCount?: number }
interface Review { id: number; avg?: number; content: string; createdAt?: string }

const route = useRoute();
const router = useRouter();
const id = computed(() => String(route.params.id ?? ''));
const backCity = computed(() => typeof route.query.city === 'string' ? route.query.city : '');
const backType = computed(() => typeof route.query.type === 'string' ? route.query.type : '');
const poi = ref<Poi | null>(null);
const reviews = ref<Review[]>([]);
const loading = ref(true);
const error = ref('');
const reviewsLoading = ref(false);
const reviewsError = ref('');
const actionMessage = ref('');
let latest = 0;
let active = true;

function back() { void router.push({ path: '/explore', query: { city: backCity.value || undefined, type: backType.value || undefined } }); }
function dateLabel(value?: string): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString('zh-CN');
}
async function loadReviews(poiId: number, request: number) {
  reviewsLoading.value = true;
  reviewsError.value = '';
  try {
    const response = await api.poiReviewsList({ params: { id: poiId } });
    if (!active || request !== latest) return;
    if (!Array.isArray(response)) throw new Error('评价数据格式不完整');
    reviews.value = (response as Review[]).filter((item) => typeof item?.id === 'number' && typeof item.content === 'string');
  } catch (cause) {
    if (active && request === latest) reviewsError.value = cause instanceof Error ? cause.message : '评价暂时无法读取';
  } finally { if (active && request === latest) reviewsLoading.value = false; }
}
async function load() {
  const request = ++latest;
  poi.value = null;
  reviews.value = [];
  loading.value = true;
  error.value = '';
  actionMessage.value = '';
  try {
    const response = await api.poiGet({ params: { id: id.value } }) as Poi;
    if (!active || request !== latest) return;
    if (typeof response?.id !== 'number' || typeof response.name !== 'string') throw new Error('地点资料格式不完整');
    poi.value = response;
    void loadReviews(response.id, request);
  } catch (cause) {
    if (active && request === latest) error.value = cause instanceof Error ? cause.message : '地点不存在或已不可见';
  } finally { if (active && request === latest) loading.value = false; }
}
async function copyAddress() {
  if (!poi.value?.address) return;
  try {
    await navigator.clipboard.writeText(`${poi.value.city || ''} ${poi.value.address}`.trim());
    actionMessage.value = '地址已复制';
  } catch { actionMessage.value = '复制失败，请手动选取地址。'; }
}
watch(id, () => void load(), { immediate: true });
onBeforeUnmount(() => { active = false; ++latest; });
</script>

<template>
  <div class="poi-page">
    <div class="topline"><button type="button" class="back" aria-label="返回地点列表" @click="back">‹</button><span>场所详情</span><span class="spacer" /></div>
    <section v-if="loading" class="state" role="status"><span class="symbol">◌</span><h1>正在读取地点…</h1></section>
    <section v-else-if="error" class="state error" role="alert"><span class="symbol">!</span><h1>地点暂时无法查看</h1><p>{{ error }}</p><button type="button" @click="load">重新加载</button><button type="button" class="outline" @click="back">返回列表</button></section>
    <template v-else-if="poi">
      <header class="hero"><span class="eyebrow">{{ poi.city }} · {{ poi.type }}</span><h1>{{ poi.name }}</h1><p class="rating"><template v-if="typeof poi.avg === 'number'">★ {{ poi.avg }} <small v-if="typeof poi.reviewCount === 'number'">· {{ poi.reviewCount }} 条公开评价</small></template><template v-else>暂无公开评分</template></p></header>
      <p v-if="poi.state === 'CLOSED'" class="closed">此场所已标记停业。出发前请再次核实。</p>
      <section class="info-card" aria-labelledby="visit-title"><div class="section-head"><span class="eyebrow">PLAN YOUR VISIT</span><h2 id="visit-title">到访前看看</h2></div><dl><div><dt>地址</dt><dd>{{ poi.address || '暂未提供地址' }}</dd></div><div><dt>营业时间</dt><dd>{{ poi.openHours || '暂未提供，请向场所核实' }}</dd></div><div v-if="poi.attrs"><dt>宠物友好信息</dt><dd>{{ poi.attrs }}</dd></div></dl><button v-if="poi.address" type="button" class="copy" @click="copyAddress">复制地址</button><p v-if="actionMessage" class="feedback" role="status">{{ actionMessage }}</p></section>
      <section class="reviews" aria-labelledby="review-title"><div class="section-head"><span class="eyebrow">FROM VISITORS</span><h2 id="review-title">公开评价</h2></div><p v-if="reviewsLoading" class="muted" role="status">正在读取评价…</p><p v-else-if="reviewsError" class="inline-error" role="alert">{{ reviewsError }} <button type="button" @click="loadReviews(poi.id, latest)">重试</button></p><p v-else-if="!reviews.length" class="muted">目前没有公开评价。</p><ul v-else><li v-for="review in reviews" :key="review.id"><div><strong v-if="typeof review.avg === 'number'">★ {{ review.avg }}</strong><time v-if="review.createdAt">{{ dateLabel(review.createdAt) }}</time></div><p>{{ review.content }}</p></li></ul></section>
      <p class="footnote">地点信息可能变化；页面未读取定位，也不展示估算距离。评价与纠错写入需要真实身份和平台回执，当前入口暂未开放。</p>
    </template>
  </div>
</template>

<style scoped>
.poi-page{padding:12px 16px 30px;min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #e8d9ca;border-radius:999px;background:#fff;color:#9f4d15;font-size:27px}.spacer{width:44px}.hero{margin-top:15px;padding:26px 21px;border-radius:22px;background:linear-gradient(140deg,#e5efe5,#f2f5e8 62%,#fff0de)}.eyebrow{color:#527258;font-size:10px;letter-spacing:.13em;font-weight:850}.hero h1{font-size:clamp(27px,8vw,36px);letter-spacing:-.04em;line-height:1.3;overflow-wrap:anywhere;margin:16px 0 10px}.rating{margin:0;color:#935118;font-size:13px;font-weight:850}.rating small{font-size:11px;color:#686259;font-weight:500}.closed{padding:12px 14px;border-radius:13px;background:#fff0eb;color:#a33b2a;font-size:12px;line-height:1.6}.info-card,.reviews{margin-top:14px;padding:20px 17px;border:1px solid #eadfd4;border-radius:19px;background:#fff}.section-head h2{margin:4px 0 16px;font-size:20px}.info-card dl{display:grid;gap:17px;margin:0}.info-card dl div{display:grid;gap:4px}.info-card dt{font-size:11px;color:#897a6c}.info-card dd{margin:0;color:#352c25;font-size:14px;line-height:1.65;overflow-wrap:anywhere;white-space:pre-wrap}.copy{min-height:44px;margin-top:20px;padding:0 18px;border:1px solid #b85111;border-radius:999px;background:#b85111;color:#fff;font-size:12px;font-weight:800}.feedback{font-size:12px;color:#246440;line-height:1.6}.reviews ul{list-style:none;margin:0;padding:0;display:grid;gap:10px}.reviews li{padding:12px;border-radius:12px;background:#f9f7f4}.reviews li div{display:flex;justify-content:space-between;gap:8px;font-size:12px;color:#a45116}.reviews time{color:#8a7c70;font-size:11px}.reviews li p{margin:8px 0 0;line-height:1.7;font-size:13px;white-space:pre-wrap;overflow-wrap:anywhere}.muted,.footnote{color:#74695d;font-size:12px;line-height:1.7}.footnote{margin:17px 3px}.inline-error{padding:12px;border-radius:12px;background:#fff1ee;color:#9a3123;font-size:12px}.inline-error button{border:0;background:transparent;color:inherit;text-decoration:underline;font-weight:800}.state{display:grid;justify-items:center;text-align:center;gap:10px;margin-top:35px;padding:30px 18px;border:1px dashed #d9cabc;border-radius:19px;background:#fff}.state h1{font-size:19px;margin:0}.state p{margin:0;color:#6e6257;font-size:12px;line-height:1.7}.state button{min-height:44px;padding:0 18px;border:1px solid #b85111;border-radius:999px;background:#b85111;color:#fff;font-size:12px;font-weight:800}.state button.outline{background:#fff;color:#a54810}.symbol{display:grid;place-items:center;width:49px;height:49px;border-radius:15px;background:#e8efe7;color:#4c6e54;font-size:25px}.state.error{background:#fff8f5;border-color:#e7c3ba}
</style>
