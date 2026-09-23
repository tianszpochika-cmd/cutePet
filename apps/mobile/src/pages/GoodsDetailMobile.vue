<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { bannedProductGuard, PRICE_DISCLAIMER, sourceLabel } from '../../../web/src/domain/goods';

interface Product { id: number; name: string; brand?: string | null; category: string; state: string; tags?: string | null; sourceType?: string | null; sourceRef?: string | null; priceMin?: number | null; priceMax?: number | null }
const route = useRoute();
const router = useRouter();
const id = computed(() => String(route.params.id ?? ''));
const listRoute = computed(() => ({ path: '/goods', query: typeof route.query.c === 'string' ? { c: route.query.c } : {} }));
function back() { void router.push(listRoute.value); }
const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref('');
let latest = 0;
let active = true;

const blocked = computed(() => product.value ? bannedProductGuard(product.value.category, product.value.name) : null);
const price = computed(() => {
  const low = product.value?.priceMin;
  const high = product.value?.priceMax;
  if (typeof low === 'number' && typeof high === 'number') return low === high ? `参考 ¥${low}` : `参考 ¥${low}–${high}`;
  if (typeof low === 'number') return `参考 ¥${low} 起`;
  if (typeof high === 'number') return `参考不高于 ¥${high}`;
  return '未提供参考价格';
});

async function load() {
  const request = ++latest;
  loading.value = true;
  error.value = '';
  product.value = null;
  try {
    const response = await api.productGet({ params: { id: id.value } }) as Product;
    if (!active || request !== latest) return;
    if (typeof response?.id !== 'number' || typeof response.state !== 'string') throw new Error('用品资料格式不完整');
    product.value = response;
  } catch (cause) {
    if (active && request === latest) error.value = cause instanceof Error ? cause.message : '用品资料不存在或已不可见';
  } finally { if (active && request === latest) loading.value = false; }
}
watch(id, () => void load(), { immediate: true });
onBeforeUnmount(() => { active = false; ++latest; });
</script>

<template>
  <div class="detail-page">
    <div class="topline"><button type="button" class="back" aria-label="返回用品列表" @click="back">‹</button><span>用品资料</span><span class="spacer" /></div>
    <section v-if="loading" class="state" role="status"><span class="symbol">◌</span><h1>正在读取用品资料…</h1></section>
    <section v-else-if="error" class="state error" role="alert"><span class="symbol">!</span><h1>用品资料暂时无法查看</h1><p>{{ error }}</p><button type="button" @click="load">重新加载</button><router-link :to="listRoute">返回用品</router-link></section>
    <section v-else-if="product?.state === 'CLEARED'" class="state"><span class="symbol">○</span><h1>用品信息已移除</h1><p>这条资料已停止公开，名称和价格不再展示。</p><router-link :to="listRoute">查看其他用品</router-link></section>
    <section v-else-if="blocked" class="state error"><span class="symbol">!</span><h1>此用品暂不可查看</h1><p>{{ blocked }}</p><router-link :to="listRoute">返回用品</router-link></section>
    <section v-else-if="product && !['ON_SHELF', 'OFF_SHELF'].includes(product.state)" class="state"><span class="symbol">○</span><h1>用品状态待核实</h1><p>当前资料不适合继续展示，请稍后再看。</p><router-link :to="listRoute">返回用品</router-link></section>
    <template v-else-if="product">
      <header class="hero"><span class="eyebrow">{{ product.category }} · 用品参考</span><span class="art" aria-hidden="true">✦</span><h1>{{ product.name }}</h1><p>{{ product.brand || '品牌未注明' }}</p></header>
      <p v-if="product.state === 'OFF_SHELF'" class="off-shelf">此用品已下架。以下是历史资料，到访或选用前请核对最新情况。</p>
      <section class="facts"><span class="eyebrow">AT A GLANCE</span><h2>资料概览</h2><dl><div><dt>类别</dt><dd>{{ product.category }}</dd></div><div><dt>参考价格</dt><dd>{{ price }}</dd></div><div><dt>资料来源</dt><dd>{{ sourceLabel(product.sourceType || '') }}</dd></div><div v-if="product.sourceRef"><dt>来源依据</dt><dd>{{ product.sourceRef }}</dd></div><div v-if="product.tags"><dt>资料标签</dt><dd>{{ product.tags }}</dd></div></dl><p>{{ PRICE_DISCLAIMER }}</p></section>
      <section class="guide"><span class="eyebrow">BEFORE YOU CHOOSE</span><h2>选用前，再核对几件事</h2><ol><li>确认适用的宠物年龄、体型和使用场景。</li><li>核对材质、规格、清洁方式与资料来源。</li><li>如涉及健康问题，请先咨询专业兽医。</li></ol></section>
      <router-link :to="listRoute" class="browse">查看其他用品 <span aria-hidden="true">↗</span></router-link>
    </template>
  </div>
</template>

<style scoped>
.detail-page{padding:12px 16px 30px;min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #e8d9ca;border-radius:999px;background:#fff;color:#9f4d15;font-size:27px}.spacer{width:44px}.hero{position:relative;overflow:hidden;margin-top:15px;padding:23px 20px 24px;border-radius:23px;background:linear-gradient(140deg,#f7e9d5,#fdf2e3 65%,#e3eee1)}.eyebrow{color:#a14d18;font-size:10px;letter-spacing:.13em;font-weight:850}.art{display:grid;place-items:center;width:100%;height:125px;margin:18px 0 15px;border-radius:14px;background:linear-gradient(135deg,#e5eee2,#fff6e9);color:#ba8651;font-size:58px}.hero h1{margin:0 0 7px;font-size:clamp(26px,8vw,34px);letter-spacing:-.04em;line-height:1.3;overflow-wrap:anywhere}.hero p{margin:0;color:#75675b;font-size:13px}.off-shelf{margin:14px 0 0;padding:13px 15px;border-radius:13px;background:#fff0e2;color:#83501f;font-size:12px;line-height:1.7}.facts,.guide{margin-top:14px;padding:20px 17px;border:1px solid #e9ded2;border-radius:18px;background:#fff}.facts h2,.guide h2{margin:6px 0 18px;font-size:20px}.facts dl{display:grid;gap:16px;margin:0}.facts dl div{display:grid;gap:4px}.facts dt{color:#867769;font-size:11px}.facts dd{margin:0;color:#332a23;font-size:14px;line-height:1.6;overflow-wrap:anywhere;white-space:pre-wrap}.facts p{margin:20px 0 0;padding-top:12px;border-top:1px solid #eee5dc;color:#7b6d61;font-size:11px;line-height:1.7}.guide{background:#f3f7ef;border-color:#e1eadd}.guide .eyebrow{color:#4b7052}.guide ol{margin:0;padding-left:19px;display:grid;gap:10px;color:#536050;font-size:12px;line-height:1.7}.browse{display:flex;justify-content:space-between;align-items:center;min-height:50px;margin-top:17px;padding:0 17px;border-radius:999px;background:#b85111;color:#fff;text-decoration:none;font-size:13px;font-weight:800}.state{display:grid;justify-items:center;text-align:center;gap:10px;margin-top:35px;padding:30px 18px;border:1px dashed #d9cabc;border-radius:19px;background:#fff}.state h1{font-size:19px;margin:0}.state p{margin:0;color:#6e6257;font-size:12px;line-height:1.7}.state button,.state a{display:inline-flex;align-items:center;justify-content:center;min-height:44px;padding:0 18px;border:1px solid #b85111;border-radius:999px;background:#b85111;color:#fff;text-decoration:none;font-size:12px;font-weight:800}.state a{background:#fff;color:#a54810}.state.error{background:#fff8f5;border-color:#e7c3ba}.symbol{display:grid;place-items:center;width:49px;height:49px;border-radius:15px;background:#fff0df;color:#a94f16;font-size:25px}
</style>
