<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { bannedProductGuard, refStateCopy, sourceLabel, PRICE_DISCLAIMER, favoriteToast } from '../../domain/goods';

const route = useRoute();
const router = useRouter();
const id = String(route.params.id);

interface Product {
  id: number;
  name: string;
  brand: string;
  category: string;
  state: string;
  tags: string;
  sourceType: string;
  editorTestOnly: boolean;
  priceMin: number | null;
  priceMax: number | null;
  referenceDisplay: string;
  favoriteCount: number;
}

const p = ref<Product | null>(null);
const error = ref('');
const faved = ref(false);
const banned = ref<string | null>(null);

onMounted(async () => {
  try {
    p.value = (await api.productGet({ path: { id } })) as unknown as Product;
    banned.value = bannedProductGuard(p.value.category, p.value.name);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 catalog-service）';
  }
});

async function toggleFavorite() {
  try {
    if (faved.value) await api.productUnfavorite({ path: { id } });
    else await api.productFavorite({ path: { id } });
    faved.value = !faved.value;
    alert(favoriteToast(faved.value));
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败（未登录会进入登录闸门）';
  }
}
</script>

<template>
  <div class="detail">
    <header>
      <button type="button" class="back" @click="router.push('/goods')">‹ 返回</button>
      <h1>商品详情</h1>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="banned" class="banned" data-testid="banned">{{ banned }}</p>
    <p v-else-if="!p" class="muted">加载中…</p>

    <template v-else>
      <p v-if="p.state !== 'ON_SHELF'" class="ref" :class="refStateCopy(p.state).tone" data-testid="ref-state">
        {{ refStateCopy(p.state).text }}
      </p>

      <h2>{{ p.name }}</h2>
      <p class="meta">
        {{ p.brand }} · {{ p.category }}
        <template v-if="p.priceMin !== null"> · 参考 ¥{{ p.priceMin }}–{{ p.priceMax }}</template>
      </p>

      <ul class="facts">
        <li>标签：{{ p.tags || '—' }}</li>
        <li>来源：{{ sourceLabel(p.sourceType) }}</li>
        <li v-if="p.editorTestOnly">🏅 编辑部测试</li>
        <li>收藏：{{ p.favoriteCount }}</li>
      </ul>

      <p class="disclaimer">{{ PRICE_DISCLAIMER }}</p>

      <div class="actions">
        <button type="button" class="primary" data-testid="fav" @click="toggleFavorite">
          {{ faved ? '★ 已收藏' : '☆ 收藏' }}
        </button>
        <button type="button" class="ghost" @click="router.push('/news?ch=营养')">看相关评测</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.meta { color: #7a6e63; font-size: 14px; }
.ref { border-radius: 8px; padding: 8px 12px; font-size: 13px; }
.ref.offShelf { background: #fff1e8; color: #b45309; }
.ref.removed { background: #f0e6dc; color: #7a6e63; }
.facts { list-style: none; padding: 0; display: grid; gap: 6px; font-size: 14px; }
.disclaimer { color: #7a6e63; font-size: 12px; }
.actions { display: flex; gap: 10px; }
.primary { height: 44px; padding: 0 24px; border: none; border-radius: 999px; background: #4d8dff; color: #fff; font-weight: 600; }
.ghost { height: 44px; padding: 0 20px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.banned { background: #fdecec; color: #b91c1c; border-radius: 8px; padding: 12px; font-size: 14px; }
.muted { color: #7a6e63; }
.err { color: #ef4444; font-size: 13px; }
</style>
