<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { CATEGORIES, PRICE_DISCLAIMER } from '../../domain/goods';

const route = useRoute();
const router = useRouter();
const category = ref(String(route.query.c ?? ''));
const items = ref<{ id: number; name: string; brand: string; category: string; hotScore: number }[]>([]);
const loading = ref(true);
const error = ref('');

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const res = (await api.productsList({
      query: { category: category.value || undefined, sort: 'hot' },
    })) as unknown as { items: typeof items.value };
    items.value = res.items ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 catalog-service）';
  } finally {
    loading.value = false;
  }
}

function pick(c: string) {
  category.value = category.value === c ? '' : c;
  void router.replace({ path: '/goods', query: category.value ? { c: category.value } : {} });
  void load();
}

onMounted(load);
</script>

<template>
  <div class="goods">
    <h1>逛好物</h1>
    <p class="disclaimer">{{ PRICE_DISCLAIMER }}</p>

    <div class="cats">
      <button v-for="c in CATEGORIES" :key="c" type="button" :class="{ on: category === c }" @click="pick(c)">
        {{ c }}
      </button>
    </div>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="loading" class="muted">加载中…</p>
    <p v-else-if="items.length === 0" class="muted" data-testid="empty">该分类暂无商品</p>

    <ul v-else class="grid">
      <li v-for="p in items" :key="p.id">
        <button type="button" class="card" @click="router.push(`/goods/${p.id}`)">
          <strong>{{ p.name }}</strong>
          <span class="meta">{{ p.brand }} · {{ p.category }}</span>
          <span class="hot">热度 {{ Math.round(p.hotScore ?? 0) }}</span>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.goods { max-width: 960px; margin: 0 auto; padding: 24px 16px; }
.disclaimer { color: #7a6e63; font-size: 12px; }
.cats { display: flex; gap: 8px; overflow-x: auto; padding: 12px 0; }
.cats button { height: 32px; padding: 0 14px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; white-space: nowrap; }
.cats button.on { background: #4d8dff; color: #fff; font-weight: 600; }
.grid { list-style: none; padding: 0; display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.card { width: 100%; text-align: left; background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 16px; padding: 16px; display: grid; gap: 6px; cursor: pointer; }
.meta { color: #7a6e63; font-size: 13px; }
.hot { color: #4d8dff; font-size: 12px; }
.muted { color: #7a6e63; }
.err { color: #ef4444; font-size: 13px; }
</style>
