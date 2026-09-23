<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { CATEGORIES, PRICE_DISCLAIMER } from '../../domain/goods';

interface ProductCard {
  id: number;
  name: string;
  brand: string;
  category: string;
  priceMin?: number | null;
  priceMax?: number | null;
}
interface ProductListCard {
  id: number;
  title: string;
  intro?: string;
  kind: string;
}

const route = useRoute();
const router = useRouter();
function validCategory(value: unknown): string {
  const raw = typeof value === 'string' ? value : '';
  return (CATEGORIES as readonly string[]).includes(raw) ? raw : '';
}
const category = ref(validCategory(route.query.c));
const invalidCategory = ref(Boolean(route.query.c && !category.value));
const products = ref<ProductCard[]>([]);
const lists = ref<ProductListCard[]>([]);
const loadingProducts = ref(true);
const loadingLists = ref(true);
const productsError = ref('');
const listsError = ref('');
let latestProductsRequest = 0;

async function loadProducts() {
  const request = ++latestProductsRequest;
  loadingProducts.value = true;
  productsError.value = '';
  try {
    const response = await api.productsList({ query: { category: category.value || undefined, sort: 'hot' } });
    if (request !== latestProductsRequest) return;
    const payload = response as { items?: ProductCard[] };
    if (!Array.isArray(payload?.items)) throw new Error('商品列表格式不完整，请稍后重试。');
    products.value = payload.items;
  } catch (cause) {
    if (request !== latestProductsRequest) return;
    products.value = [];
    productsError.value = cause instanceof Error ? cause.message : '商品加载失败，请稍后重试。';
  } finally {
    if (request === latestProductsRequest) loadingProducts.value = false;
  }
}
async function loadLists() {
  loadingLists.value = true;
  listsError.value = '';
  try {
    const response = await api.listsList();
    if (!Array.isArray(response)) throw new Error('清单列表格式不完整，请稍后重试。');
    lists.value = response as ProductListCard[];
  } catch (cause) {
    lists.value = [];
    listsError.value = cause instanceof Error ? cause.message : '清单加载失败，请稍后重试。';
  } finally {
    loadingLists.value = false;
  }
}
function pick(next: string) {
  const chosen = category.value === next ? '' : next;
  void router.replace({ path: '/goods', query: chosen ? { c: chosen } : {} });
}
function priceLabel(item: ProductCard): string {
  const low = item.priceMin;
  const high = item.priceMax;
  if (typeof low === 'number' && typeof high === 'number') {
    return low === high ? '参考 ¥' + low : '参考 ¥' + low + '–' + high;
  }
  if (typeof low === 'number') return '参考 ¥' + low + ' 起';
  if (typeof high === 'number') return '参考不高于 ¥' + high;
  return '未提供参考价格';
}

onMounted(() => {
  void loadProducts();
  void loadLists();
});
watch(() => route.query.c, (value) => {
  category.value = validCategory(value);
  invalidCategory.value = Boolean(value && !category.value);
  void loadProducts();
});
</script>

<template>
  <div class="goods">
    <header class="hero">
      <p class="eyebrow">GOOD THINGS · 用品参考</p>
      <h1>给每一天，找合适的用品</h1>
      <p class="lede">看看公开资料和选品清单，再结合宠物的具体情况做决定。</p>
      <p class="disclaimer">{{ PRICE_DISCLAIMER }}</p>
    </header>

    <section class="section" aria-labelledby="products-heading">
      <div class="section-heading"><h2 id="products-heading">商品资料</h2><span>按热度浏览</span></div>
      <div class="cats" role="group" aria-label="商品分类">
        <button type="button" :class="{ on: !category }" :aria-pressed="!category" @click="pick('')">全部</button>
        <button v-for="c in CATEGORIES" :key="c" type="button" :class="{ on: category === c }" :aria-pressed="category === c" @click="pick(c)">{{ c }}</button>
      </div>
      <p v-if="invalidCategory" class="notice" role="status">原分类不存在，已显示全部分类。</p>
      <p v-if="loadingProducts" class="status" role="status">正在读取商品…</p>
      <p v-else-if="productsError" class="status error" role="alert">{{ productsError }} <button type="button" @click="loadProducts">重试</button></p>
      <ul v-else class="grid">
        <li v-for="product in products" :key="product.id">
          <button type="button" class="card" @click="router.push('/goods/' + product.id)">
            <span class="card-icon" aria-hidden="true">✦</span>
            <span class="card-category">{{ product.category }}</span>
            <strong>{{ product.name }}</strong>
            <span class="meta">{{ product.brand || '品牌未注明' }}</span>
            <span class="price">{{ priceLabel(product) }}</span>
            <span class="card-link">查看资料 <span aria-hidden="true">↗</span></span>
          </button>
        </li>
        <li v-if="products.length === 0" class="empty" data-testid="empty">这个分类暂时没有公开商品。可以查看其他分类或清单。</li>
      </ul>
    </section>

    <section class="section lists-section" aria-labelledby="lists-heading">
      <div class="section-heading"><h2 id="lists-heading">选品清单</h2><span>公开整理</span></div>
      <p v-if="loadingLists" class="status" role="status">正在读取清单…</p>
      <p v-else-if="listsError" class="status error" role="alert">{{ listsError }} <button type="button" @click="loadLists">重试</button></p>
      <ul v-else class="list-grid">
        <li v-for="list in lists" :key="list.id">
          <button type="button" class="list-card" @click="router.push('/lists/' + list.id)">
            <span class="list-kind">{{ list.kind === 'OFFICIAL' ? '官方整理' : '作者分享' }}</span>
            <strong>{{ list.title }}</strong>
            <span v-if="list.intro" class="list-intro">{{ list.intro }}</span>
            <span class="card-link">查看清单 <span aria-hidden="true">↗</span></span>
          </button>
        </li>
        <li v-if="lists.length === 0" class="empty">目前没有公开清单。</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.goods{max-width:1120px;margin:auto;padding:clamp(24px,4vw,48px) 20px 80px;color:#2b2118}.hero{background:linear-gradient(125deg,#fff0dd,#f8f8f1);border:1px solid #f0dec7;border-radius:24px;padding:clamp(28px,5vw,54px);position:relative;overflow:hidden}.hero::after{content:'✦';position:absolute;right:5%;top:9%;font-size:clamp(76px,12vw,145px);color:#f3cfaa;opacity:.55;transform:rotate(-12deg);pointer-events:none}.eyebrow{position:relative;z-index:1;color:#a64613;font-size:12px;font-weight:800;letter-spacing:.15em;margin:0 0 12px}h1{position:relative;z-index:1;font-size:clamp(29px,4.5vw,50px);letter-spacing:-.04em;max-width:650px;margin:0}.lede{position:relative;z-index:1;color:#665b50;line-height:1.7;margin:13px 0 19px}.disclaimer{position:relative;z-index:1;color:#806b57;font-size:12px;line-height:1.6;max-width:650px;margin:0}.section{margin-top:42px}.section-heading{display:flex;justify-content:space-between;align-items:baseline;gap:12px;margin-bottom:16px}.section-heading h2{font-size:23px;margin:0}.section-heading span{font-size:13px;color:#827367}.cats{display:flex;gap:8px;overflow-x:auto;padding:0 0 13px;scrollbar-width:thin}.cats button{flex:none;border:1px solid #eadfd3;background:#fff;border-radius:999px;padding:9px 15px;color:#6e6053;white-space:nowrap;cursor:pointer;font:inherit}.cats button.on{background:#a64613;color:#fff;border-color:#a64613}.grid,.list-grid{list-style:none;margin:0;padding:0;display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px}.card,.list-card{height:100%;width:100%;text-align:left;border:1px solid #eee2d6;background:#fff;border-radius:18px;padding:19px;display:flex;flex-direction:column;align-items:start;gap:8px;cursor:pointer;font:inherit;color:#2b2118;box-shadow:0 8px 28px #3a231308}.card:hover,.list-card:hover{border-color:#d8a77c}.card-icon{width:42px;height:42px;display:grid;place-items:center;border-radius:12px;background:#fbeedc;color:#b36831;font-size:22px;margin-bottom:4px}.card-category,.list-kind{font-size:12px;color:#a64613;font-weight:700}.card strong,.list-card strong{font-size:18px;line-height:1.4;overflow-wrap:anywhere}.meta,.list-intro{font-size:13px;color:#74695e;line-height:1.6}.price{font-size:13px;color:#625548;margin-top:5px}.card-link{font-size:13px;color:#a64613;font-weight:700;margin-top:auto;padding-top:7px}.list-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.list-card{background:#f9fbf7;border-color:#e5eade}.list-kind{color:#4e7858}.list-intro{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}.empty{grid-column:1/-1;padding:34px 20px;text-align:center;border:1px dashed #decfbe;background:#fff;border-radius:16px;color:#776a5d}.status,.notice{padding:18px;background:#f6f1eb;border-radius:12px;color:#6c6055}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}.notice{background:#fff5e9;color:#80502c;font-size:13px}@media(max-width:800px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:580px){.hero::after{opacity:.2}.grid,.list-grid{grid-template-columns:1fr}.section{margin-top:32px}}
</style>
