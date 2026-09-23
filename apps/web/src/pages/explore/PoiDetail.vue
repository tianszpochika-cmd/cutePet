<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface Poi {
  id: number;
  name: string;
  type: string;
  city: string;
  address: string;
  phone?: string | null;
  openHours?: string | null;
  attrs?: string | null;
  state: string;
  avg: number | null;
  reviewCount: number;
  navClicks?: number;
  lat?: number | null;
  lng?: number | null;
}
interface Review {
  id: number;
  avg: number;
  content: string;
  createdAt?: string;
  state?: string;
}

const route = useRoute();
const router = useRouter();
const poiId = computed(() => String(route.params.id ?? ''));
const poi = ref<Poi | null>(null);
const reviews = ref<Review[]>([]);
const loading = ref(true);
const reviewsLoading = ref(true);
const error = ref('');
const reviewsError = ref('');
const actionMessage = ref('');
let poiRequest = 0;
let reviewsRequest = 0;

const maskedPhone = computed(() => {
  const raw = poi.value?.phone?.trim();
  if (!raw) return '未提供';
  const digits = raw.replace(/\D/g, '');
  if (digits.length < 7) return '完整号码暂不公开';
  return digits.slice(0, 3) + '****' + digits.slice(-4);
});
const mapUrl = computed(() => {
  const lat = poi.value?.lat;
  const lng = poi.value?.lng;
  if (typeof lat !== 'number' || typeof lng !== 'number' || !Number.isFinite(lat) || !Number.isFinite(lng) ||
      Math.abs(lat) > 90 || Math.abs(lng) > 180) return null;
  const url = new URL('https://www.openstreetmap.org/');
  url.searchParams.set('mlat', String(lat));
  url.searchParams.set('mlon', String(lng));
  url.hash = 'map=17/' + lat + '/' + lng;
  return url.toString();
});

async function loadPoi() {
  const version = ++poiRequest;
  const id = poiId.value;
  poi.value = null;
  loading.value = true;
  error.value = '';
  try {
    const response = await api.poiGet({ path: { id } }) as Poi;
    if (version !== poiRequest) return;
    if (!response || String(response.id) !== id || typeof response.name !== 'string') {
      throw new Error('场所资料格式不完整，请稍后重试。');
    }
    poi.value = response;
  } catch (cause) {
    if (version === poiRequest) error.value = cause instanceof Error ? cause.message : '场所信息加载失败，请稍后重试。';
  } finally {
    if (version === poiRequest) loading.value = false;
  }
}
async function loadReviews() {
  const version = ++reviewsRequest;
  const id = poiId.value;
  reviews.value = [];
  reviewsLoading.value = true;
  reviewsError.value = '';
  try {
    const response = await api.poiReviewsList({ path: { id } });
    if (version !== reviewsRequest) return;
    reviews.value = Array.isArray(response) ? response as Review[] : [];
  } catch (cause) {
    if (version === reviewsRequest) reviewsError.value = cause instanceof Error ? cause.message : '评价加载失败，请稍后重试。';
  } finally {
    if (version === reviewsRequest) reviewsLoading.value = false;
  }
}
function recordMapOpen() {
  // 点击已直接打开外部地图；计数接口失败不改变用户的导航结果。
  void api.poiNavClick({ path: { id: poiId.value } }).catch(() => undefined);
}
async function copyAddress() {
  if (!poi.value?.address) return;
  try {
    await navigator.clipboard.writeText([poi.value.city, poi.value.address].filter(Boolean).join(' '));
    actionMessage.value = '地址已复制，可粘贴到常用地图应用。';
  } catch {
    actionMessage.value = '复制未成功，请手动选取下方地址。';
  }
}
function formatDate(value?: string) {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value.replace('T', ' ') : date.toLocaleDateString('zh-CN');
}
watch(() => route.params.id, () => {
  void loadPoi();
  void loadReviews();
}, { immediate: true });
</script>

<template>
  <div class="poi-detail">
    <button type="button" class="back" @click="router.push('/explore')">← 返回场所列表</button>
    <p v-if="loading" class="status" role="status">正在读取场所信息…</p>
    <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="loadPoi">重试</button></p>

    <template v-if="poi">
      <header class="heading">
        <p class="eyebrow">{{ poi.city }} · {{ poi.type }}</p>
        <h1>{{ poi.name }}</h1>
        <p class="rating"><span v-if="poi.avg !== null">★ {{ poi.avg }} <small>（{{ poi.reviewCount }} 条评价）</small></span><span v-else>暂无评分</span></p>
      </header>
      <p v-if="poi.state === 'CLOSED'" class="closed" data-testid="closed-banner">此场所已标记停业。出发前请再次核实。</p>

      <section class="info-card" aria-labelledby="poi-info-title">
        <h2 id="poi-info-title">到访信息</h2>
        <dl>
          <div><dt>地址</dt><dd>{{ poi.address || '地址待补充' }}</dd></div>
          <div><dt>营业时间</dt><dd>{{ poi.openHours || '以场所公布为准' }}</dd></div>
          <div><dt>电话</dt><dd>{{ maskedPhone }} <small v-if="poi.phone">完整电话须待服务端完成身份校验和脱敏后开放。</small></dd></div>
          <div v-if="poi.attrs"><dt>宠物友好信息</dt><dd>{{ poi.attrs }}</dd></div>
        </dl>
        <div class="actions">
          <a v-if="mapUrl" :href="mapUrl" target="_blank" rel="noopener noreferrer" class="primary" data-testid="nav" @click="recordMapOpen">在 OpenStreetMap 查看位置 ↗</a>
          <button v-if="poi.address" type="button" class="ghost" @click="copyAddress">复制地址</button>
          <button type="button" class="ghost" data-testid="write-review" @click="router.push('/explore/poi/' + poi.id + '/review')">查看评价草稿</button>
        </div>
        <p v-if="!mapUrl" class="helper">此场所没有可用坐标，暂无法打开地图位置；可复制地址自行查找。</p>
        <p v-if="actionMessage" class="feedback" role="status">{{ actionMessage }}</p>
      </section>

      <section class="reviews" aria-labelledby="reviews-title">
        <div class="section-heading"><h2 id="reviews-title">用户评价</h2><span>{{ poi.reviewCount }} 条</span></div>
        <p v-if="reviewsLoading" class="muted" role="status">正在读取评价…</p>
        <p v-else-if="reviewsError" class="status error" role="alert">{{ reviewsError }} <button type="button" @click="loadReviews">重试</button></p>
        <ul v-else-if="reviews.length" class="review-list">
          <li v-for="review in reviews" :key="review.id">
            <div class="review-top"><strong>★ {{ review.avg }}</strong><time v-if="review.createdAt">{{ formatDate(review.createdAt) }}</time></div>
            <p>{{ review.content }}</p>
          </li>
        </ul>
        <p v-else class="muted">目前没有公开评价。</p>
      </section>
      <p class="support-note">纠错与举报需要可核验的登录身份及受理回执，当前暂未开放。页面不会模拟提交结果。</p>
    </template>
  </div>
</template>

<style scoped>
.poi-detail{max-width:820px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#a64613;font:inherit;cursor:pointer}.heading{margin:26px 0}.eyebrow{color:#a64613;font-size:12px;letter-spacing:.12em;font-weight:800;margin:0 0 9px}h1{font-size:clamp(28px,4vw,42px);margin:0;letter-spacing:-.03em}.rating{color:#b15b1d;font-weight:700}.rating small{color:#786b60;font-weight:400}.closed{padding:13px 16px;background:#fff0eb;color:#a6372b;border-radius:12px}.info-card,.reviews{background:#fff;border:1px solid #eaded1;border-radius:18px;padding:22px;margin-top:14px;box-shadow:0 8px 28px #3a231308}h2{font-size:19px;margin:0 0 15px}dl{margin:0;display:grid;gap:15px}dl div{display:grid;grid-template-columns:100px 1fr;gap:12px}dt{color:#786b60;font-size:13px}dd{margin:0;line-height:1.6;overflow-wrap:anywhere}dd small{display:block;color:#8a7968;font-size:12px}.actions{display:flex;flex-wrap:wrap;gap:9px;margin-top:23px}.actions a,.actions button{min-height:42px;display:inline-flex;align-items:center;justify-content:center;text-decoration:none;border-radius:999px;padding:0 16px;font:inherit;cursor:pointer}.primary{background:#a64613;color:#fff;border:1px solid #a64613}.ghost{background:#fff;color:#69462e;border:1px solid #e8d8c9}.helper,.feedback,.muted{font-size:13px;color:#756a60;line-height:1.6}.feedback{color:#365c3a}.section-heading,.review-top{display:flex;justify-content:space-between;align-items:baseline;gap:10px}.section-heading span,.review-top time{font-size:12px;color:#84766a}.review-list{list-style:none;margin:0;padding:0;display:grid;gap:10px}.review-list li{padding:15px;border-radius:12px;background:#faf7f3}.review-top strong{color:#aa581c}.review-list p{line-height:1.7;white-space:pre-wrap;margin:8px 0 0;overflow-wrap:anywhere}.support-note{margin-top:18px;font-size:13px;color:#6f655b;background:#f5f1ec;padding:13px 16px;border-radius:12px;line-height:1.7}.status{padding:15px;background:#f5f1ec;border-radius:12px}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:560px){dl div{grid-template-columns:1fr;gap:2px}.info-card,.reviews{padding:18px}.actions a,.actions button{width:100%}}
</style>
