<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { bannedProductGuard, refStateCopy, sourceLabel, PRICE_DISCLAIMER, favoriteToast } from '../../domain/goods';

interface ProductListRef { listId: number; title: string }
interface Product {
  id: number;
  name: string;
  brand: string;
  category: string;
  state: string;
  tags?: string | null;
  sourceType?: string | null;
  sourceRef?: string | null;
  editorTestOnly?: boolean;
  priceMin?: number | null;
  priceMax?: number | null;
  favoriteCount?: number;
  inLists?: ProductListRef[];
}

const route = useRoute();
const router = useRouter();
const id = computed(() => String(route.params.id));
const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref('');
const actionError = ref('');
const feedback = ref('');
const favoriting = ref(false);
const faved = ref(false);
const favoriteKnown = ref(false);
const banned = computed(() => product.value ? bannedProductGuard(product.value.category, product.value.name) : null);
let latestRequest = 0;

function priceLabel(p: Product): string {
  const low = p.priceMin;
  const high = p.priceMax;
  if (typeof low === 'number' && typeof high === 'number') {
    return low === high ? '参考 ¥' + low : '参考 ¥' + low + '–' + high;
  }
  if (typeof low === 'number') return '参考 ¥' + low + ' 起';
  if (typeof high === 'number') return '参考不高于 ¥' + high;
  return '暂未提供参考价格';
}
async function load() {
  const request = ++latestRequest;
  product.value = null;
  loading.value = true;
  error.value = '';
  actionError.value = '';
  feedback.value = '';
  faved.value = false;
  favoriteKnown.value = false;
  try {
    const response = await api.productGet({ path: { id: id.value } });
    if (request !== latestRequest) return;
    if (!response || typeof response !== 'object' || typeof (response as Product).id !== 'number') {
      throw new Error('商品资料格式不完整，请稍后重试。');
    }
    product.value = response as Product;
    if (typeof (response as { favorited?: unknown })?.favorited === 'boolean') {
      faved.value = (response as { favorited: boolean }).favorited;
      favoriteKnown.value = true;
    }
  } catch (cause) {
    if (request !== latestRequest) return;
    error.value = cause instanceof Error ? cause.message : '商品资料加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
async function toggleFavorite() {
  if (!product.value || product.value.state !== 'ON_SHELF' || favoriting.value) return;
  const targetId = id.value;
  if (!getAccessToken()) {
    void router.push({ path: '/login', query: { return: '/goods/' + targetId } });
    return;
  }
  favoriting.value = true;
  actionError.value = '';
  feedback.value = '';
  try {
    const expected = !faved.value;
    const response = (faved.value
      ? await api.productUnfavorite({ path: { id: targetId } })
      : await api.productFavorite({ path: { id: targetId } })) as { favorited?: boolean; favoriteCount?: number };
    if (targetId !== id.value || !product.value) return;
    if (response?.favorited !== expected) throw new Error('平台未确认收藏状态，请刷新后重试。');
    faved.value = response.favorited;
    favoriteKnown.value = true;
    if (typeof response.favoriteCount === 'number' && Number.isFinite(response.favoriteCount)) {
      product.value.favoriteCount = response.favoriteCount;
    }
    feedback.value = favoriteToast(faved.value);
  } catch (cause) {
    if (targetId === id.value) actionError.value = cause instanceof Error ? cause.message : '收藏操作失败，请稍后重试。';
  } finally {
    favoriting.value = false;
  }
}
watch(() => route.params.id, () => void load(), { immediate: true });
</script>

<template>
  <div class="detail">
    <button type="button" class="back" @click="router.push('/goods')">← 返回用品</button>
    <p v-if="loading" class="status" role="status">正在读取商品资料…</p>
    <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="load">重试</button></p>

    <template v-if="product">
      <template v-if="product.state === 'CLEARED'">
        <div class="removed" data-testid="ref-state">
          <span aria-hidden="true">○</span>
          <h1>商品信息已移除</h1>
          <p>这条资料已停止公开，原名称、价格和推荐理由不再展示。</p>
          <button type="button" @click="router.push('/goods')">返回用品列表</button>
        </div>
      </template>
      <p v-else-if="banned" class="status error" data-testid="banned">{{ banned }}</p>
      <template v-else>
        <header class="heading">
          <div class="product-mark" aria-hidden="true">✦</div>
          <div>
            <p class="eyebrow">{{ product.category }} · 用品资料</p>
            <h1>{{ product.name }}</h1>
            <p class="brand">{{ product.brand || '品牌未注明' }}</p>
          </div>
        </header>

        <p v-if="product.state !== 'ON_SHELF'" class="state" data-testid="ref-state">
          {{ refStateCopy(product.state).text || '商品状态待核实' }}。以下为历史资料，请核对最新情况。
        </p>
        <section class="facts" aria-labelledby="facts-title">
          <h2 id="facts-title">资料概览</h2>
          <dl>
            <div><dt>参考价格</dt><dd>{{ priceLabel(product) }}</dd></div>
            <div><dt>资料来源</dt><dd>{{ sourceLabel(product.sourceType || '') }}</dd></div>
            <div v-if="product.sourceRef"><dt>来源依据</dt><dd>{{ product.sourceRef }}</dd></div>
            <div v-if="product.tags"><dt>标签</dt><dd>{{ product.tags }}</dd></div>
            <div v-if="product.editorTestOnly"><dt>资料标记</dt><dd>标注为编辑部测试，具体方法与证据请以来源材料为准。</dd></div>
            <div><dt>收藏数量</dt><dd>{{ product.favoriteCount ?? '未提供' }}</dd></div>
          </dl>
          <p class="disclaimer">{{ PRICE_DISCLAIMER }}</p>
        </section>

        <div class="actions">
          <button v-if="product.state === 'ON_SHELF'" type="button" class="primary" data-testid="fav" :disabled="favoriting" @click="toggleFavorite">
            {{ favoriting ? '正在确认…' : favoriteKnown && faved ? '★ 取消收藏' : '☆ 收藏商品' }}
          </button>
          <p v-else class="action-note">该商品当前不可用，已暂停收藏入口。</p>
          <button type="button" class="ghost" @click="router.push('/me/favorites')">查看我的收藏</button>
        </div>
        <p v-if="feedback" class="feedback" role="status">{{ feedback }}</p>
        <p v-if="actionError" class="status error" role="alert">{{ actionError }}</p>
        <p v-if="product.state === 'ON_SHELF' && !favoriteKnown" class="hint">当前收藏状态尚未由平台返回；点击收藏后，以平台确认的结果为准。</p>

        <section class="lists" aria-labelledby="lists-title">
          <h2 id="lists-title">收录它的清单</h2>
          <ul v-if="Array.isArray(product.inLists) && product.inLists.length">
            <li v-for="list in product.inLists" :key="list.listId">
              <button type="button" @click="router.push('/lists/' + list.listId)"><span>{{ list.title }}</span><span aria-hidden="true">↗</span></button>
            </li>
          </ul>
          <p v-else class="muted">目前没有与此商品关联的公开清单。</p>
        </section>
      </template>
    </template>
  </div>
</template>

<style scoped>
.detail{max-width:860px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#a64613;cursor:pointer;font:inherit}.heading{display:flex;align-items:center;gap:24px;margin:30px 0}.product-mark{width:110px;height:110px;flex:none;border-radius:24px;background:linear-gradient(145deg,#fdebd5,#f6f6ea);color:#bf7b42;display:grid;place-items:center;font-size:54px}.eyebrow{color:#a64613;font-size:12px;font-weight:800;letter-spacing:.14em;margin:0 0 8px}h1{font-size:clamp(28px,4vw,42px);letter-spacing:-.03em;margin:0;overflow-wrap:anywhere}.brand{color:#76695e;margin:8px 0 0}.facts,.lists{background:#fff;border:1px solid #eaded1;border-radius:18px;padding:23px;box-shadow:0 8px 28px #3a231308}.facts h2,.lists h2{font-size:19px;margin:0 0 18px}dl{display:grid;gap:14px;margin:0}dl div{display:grid;grid-template-columns:110px 1fr;gap:12px}dt{font-size:13px;color:#817367}dd{margin:0;line-height:1.6;overflow-wrap:anywhere}.disclaimer{font-size:12px;color:#827467;line-height:1.7;border-top:1px solid #eee6df;margin:20px 0 0;padding-top:13px}.state{padding:14px 16px;border-radius:12px;background:#fff2e4;color:#8b5626;line-height:1.6}.actions{display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-top:20px}.actions button{min-height:44px;padding:0 20px;border-radius:999px;font:inherit;cursor:pointer}.primary{background:#a64613;color:#fff;border:1px solid #a64613}.primary:disabled{opacity:.6;cursor:wait}.ghost{background:#fff;color:#69462e;border:1px solid #e8d8c9}.action-note{font-size:13px;color:#835826;margin:0}.feedback{color:#365c3a;font-size:13px}.hint,.muted{font-size:13px;line-height:1.6;color:#766b61}.lists{margin-top:26px}.lists ul{list-style:none;margin:0;padding:0;display:grid;gap:8px}.lists li button{width:100%;display:flex;justify-content:space-between;text-align:left;gap:10px;padding:12px 14px;border:1px solid #e6eadd;background:#f8fbf6;color:#42654b;border-radius:11px;cursor:pointer;font:inherit}.removed{margin-top:26px;padding:38px 25px;text-align:center;border-radius:18px;background:#f4f1ed;color:#655a50}.removed span{font-size:40px;color:#9b9084}.removed h1{font-size:27px}.removed p{line-height:1.7}.removed button{border:0;background:#a64613;color:#fff;border-radius:999px;padding:11px 18px;cursor:pointer;font:inherit}.status{padding:17px;background:#f6f1eb;border-radius:12px;color:#6c6055;margin-top:20px}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:580px){.heading{align-items:start;gap:14px}.product-mark{width:70px;height:70px;border-radius:16px;font-size:35px}.facts,.lists{padding:18px}dl div{grid-template-columns:1fr;gap:2px}.actions button{width:100%}}
</style>
