<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { CATEGORIES } from '../../../web/src/domain/goods';

interface ProductCard { id: number; name: string; brand?: string; category: string; state?: string; priceMin?: number | null; priceMax?: number | null }
const route = useRoute();
const router = useRouter();
const category = computed(() => typeof route.query.c === 'string' ? route.query.c : '');
const items = ref<ProductCard[]>([]);
const total = ref(0);
const loading = ref(true);
const error = ref('');
let latest = 0;
let active = true;

async function load() {
  const request = ++latest;
  items.value = [];
  total.value = 0;
  loading.value = true;
  error.value = '';
  try {
    const response = await api.productsList({ query: { category: category.value || undefined, sort: 'hot' } }) as { items?: ProductCard[]; total?: number };
    if (!active || request !== latest) return;
    if (!Array.isArray(response?.items)) throw new Error('用品数据格式不完整');
    items.value = response.items.filter((item) => typeof item?.id === 'number' && typeof item.name === 'string');
    total.value = typeof response.total === 'number' ? response.total : items.value.length;
  } catch (cause) {
    if (active && request === latest) error.value = cause instanceof Error ? cause.message : '用品资料暂时无法读取';
  } finally { if (active && request === latest) loading.value = false; }
}
function pick(value: string) { void router.replace({ path: '/goods', query: value ? { c: value } : {} }); }
watch(category, () => void load(), { immediate: true });
onBeforeUnmount(() => { active = false; ++latest; });
</script>

<template>
  <div class="goods-page">
    <div class="topline"><button type="button" class="back" aria-label="返回首页" @click="router.push('/')">‹</button><span>逛好物</span><span class="spacer" /></div>
    <header class="hero"><span class="eyebrow">CHOOSE WITH CARE · 用品参考</span><h1>给小伙伴，<br>挑得更明白。</h1><p>查看已公开的用品资料，再按宠物实际需要比较。一期只提供信息参考，不提供购买服务。</p><span class="graphic" aria-hidden="true">✦</span></header>
    <section aria-labelledby="goods-title"><div class="section-head"><h2 id="goods-title">用品资料</h2><span v-if="!loading && !error">{{ total }} 件</span></div><nav class="chips" aria-label="用品分类"><button type="button" :class="{ on: !category }" @click="pick('')">全部</button><button v-for="value in CATEGORIES" :key="value" type="button" :class="{ on: category === value }" @click="pick(value)">{{ value }}</button></nav>
      <div v-if="loading" class="state" role="status"><span class="symbol">◌</span><strong>正在读取用品资料…</strong></div>
      <div v-else-if="error" class="state error" role="alert"><span class="symbol">!</span><strong>用品资料暂时无法读取</strong><p>{{ error }}</p><button type="button" @click="load">重新加载</button></div>
      <div v-else-if="!items.length" class="state"><span class="symbol">✳</span><strong>当前分类暂无公开用品</strong><p>可以切换其他分类。</p><button v-if="category" type="button" @click="pick('')">查看全部</button></div>
      <ul v-else class="grid"><li v-for="item in items" :key="item.id"><router-link :to="{ path: `/goods/${item.id}`, query: category ? { c: category } : {} }" class="card"><span class="art" aria-hidden="true">✦</span><span class="type">{{ item.category }}</span><strong>{{ item.name }}</strong><small>{{ item.brand || '品牌未注明' }}</small><span class="read-more">查看资料 <span aria-hidden="true">↗</span></span></router-link></li></ul>
    </section>
    <p class="disclaimer">展示的是平台已公开资料。参考价格与适用信息需要结合来源核对；本平台不提供购物车、支付或购买链接。</p>
  </div>
</template>

<style scoped>
.goods-page{padding:12px 16px 30px;min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #e8d9ca;border-radius:999px;background:#fff;color:#9f4d15;font-size:27px}.spacer{width:44px}.hero{position:relative;overflow:hidden;margin-top:15px;padding:25px 21px;border-radius:23px;background:linear-gradient(130deg,#e3ecdc,#f5eddc 65%,#f8dcbf)}.eyebrow{color:#476b50;font-size:10px;letter-spacing:.14em;font-weight:850}.hero h1{position:relative;z-index:1;font-size:clamp(27px,8vw,35px);letter-spacing:-.04em;line-height:1.26;margin:18px 0 8px}.hero p{position:relative;z-index:1;max-width:29ch;color:#586154;font-size:12px;line-height:1.75;margin:0}.graphic{position:absolute;right:10px;top:17px;color:#ffffff80;font-size:93px}.section-head{display:flex;justify-content:space-between;align-items:baseline;margin:26px 1px 0}.section-head h2{margin:0;font-size:20px}.section-head span{color:#76695d;font-size:12px}.chips{display:flex;gap:8px;overflow-x:auto;margin:14px -16px 14px;padding:0 16px 6px;scrollbar-width:none}.chips::-webkit-scrollbar{display:none}.chips button{flex:none;min-height:44px;padding:0 15px;border:1px solid #e3d6c8;border-radius:999px;background:#fff;color:#685b50;font-size:12px;font-weight:800}.chips button.on{background:#b85111;border-color:#b85111;color:#fff}.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;list-style:none;margin:0;padding:0}.card{display:flex;flex-direction:column;align-items:stretch;gap:7px;min-width:0;min-height:207px;padding:10px;border:1px solid #e9dfd4;border-radius:17px;background:#fff;color:#2b2118;text-decoration:none}.art{height:81px;display:grid;place-items:center;border-radius:11px;background:linear-gradient(135deg,#f7ecdd,#e7eee1);color:#bb8c52;font-size:37px}.type{color:#a3541b;font-size:10px;font-weight:800}.card strong{font-size:14px;line-height:1.35;overflow-wrap:anywhere}.card small{font-size:11px;color:#786c61}.read-more{margin-top:auto;display:flex;justify-content:space-between;align-items:center;color:#a24712;font-size:11px;font-weight:850}.state{display:grid;justify-items:center;text-align:center;gap:9px;padding:30px 15px;border:1px dashed #dacbbc;border-radius:19px;background:#fff}.state strong{font-size:14px}.state p{margin:0;color:#776b60;font-size:12px}.state button{min-height:44px;margin-top:7px;border:0;border-radius:999px;background:#b85111;color:#fff;padding:0 17px;font-weight:800}.symbol{display:grid;place-items:center;width:48px;height:48px;border-radius:15px;background:#fff0df;color:#ae5117;font-size:25px}.state.error{background:#fff8f6;border-color:#e8beb6}.disclaimer{margin:18px 2px;color:#776b60;font-size:11px;line-height:1.7}@media(max-width:345px){.card{padding:8px}.art{height:72px}}
</style>
