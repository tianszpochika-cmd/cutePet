<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { PRICE_DISCLAIMER } from '../../domain/goods';

interface ListItem {
  ord: number;
  productId: number;
  name: string;
  reason?: string;
  refState: 'VISIBLE' | 'OFF_SHELF_BADGE' | 'REMOVED_TEXT_ONLY';
}
interface ProductList {
  id: number;
  title: string;
  kind: string;
  publishable: boolean;
  items: ListItem[];
}

const route = useRoute();
const router = useRouter();
const id = computed(() => String(route.params.id));
const list = ref<ProductList | null>(null);
const loading = ref(true);
const error = ref('');
let latestRequest = 0;

async function load() {
  const request = ++latestRequest;
  list.value = null;
  loading.value = true;
  error.value = '';
  try {
    const response = await api.listGet({ path: { id: id.value } }) as ProductList;
    if (request !== latestRequest) return;
    if (!response || typeof response !== 'object' || typeof response.id !== 'number' || !Array.isArray(response.items)) {
      throw new Error('清单资料格式不完整，请稍后重试。');
    }
    list.value = response;
  } catch (cause) {
    if (request !== latestRequest) return;
    error.value = cause instanceof Error ? cause.message : '清单加载失败，请稍后重试。';
  } finally {
    if (request === latestRequest) loading.value = false;
  }
}
function stateText(state: ListItem['refState']): string {
  if (state === 'OFF_SHELF_BADGE') return '已下架 · 历史资料仅供参考';
  if (state === 'REMOVED_TEXT_ONLY') return '信息已移除';
  return '';
}
watch(() => route.params.id, () => void load(), { immediate: true });
</script>

<template>
  <div class="list-detail">
    <button type="button" class="back" @click="router.push('/goods')">← 返回用品</button>
    <p v-if="loading" class="status" role="status">正在读取清单…</p>
    <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="load">重试</button></p>

    <template v-if="list">
      <header class="heading">
        <p class="eyebrow">{{ list.kind === 'OFFICIAL' ? '官方整理' : list.kind === 'AUTHOR' ? '作者分享' : '公开清单' }}</p>
        <h1>{{ list.title }}</h1>
        <p class="lede">{{ list.items.length }} 项用品资料。清单仅供比较与参考，实际情况请以最新来源为准。</p>
      </header>

      <p v-if="!list.publishable && list.items.length" class="warn" data-testid="unpublishable">清单中的商品目前都不可用，以下仅保留可公开的历史状态。</p>
      <p v-if="!list.items.length" class="empty">这份清单目前没有公开条目。你可以返回用品页浏览其他清单。</p>
      <ol v-else class="items">
        <li v-for="(item, index) in list.items" :key="item.productId" class="item">
          <span class="ord">{{ index + 1 }}</span>
          <div class="item-body">
            <p v-if="item.refState !== 'VISIBLE'" class="ref" :class="{ removed: item.refState === 'REMOVED_TEXT_ONLY' }" data-testid="item-ref">{{ stateText(item.refState) }}</p>
            <strong v-if="item.refState !== 'REMOVED_TEXT_ONLY'">{{ item.name }}</strong>
            <strong v-else>商品信息已移除</strong>
            <p v-if="item.refState !== 'REMOVED_TEXT_ONLY' && item.reason" class="reason">{{ item.reason }}</p>
          </div>
          <button v-if="item.refState !== 'REMOVED_TEXT_ONLY'" type="button" class="detail-link" @click="router.push('/goods/' + item.productId)">查看资料 <span aria-hidden="true">↗</span></button>
        </li>
      </ol>
      <p class="disclaimer">{{ PRICE_DISCLAIMER }}</p>
    </template>
  </div>
</template>

<style scoped>
.list-detail{max-width:820px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#a64613;cursor:pointer;font:inherit}.heading{margin:30px 0}.eyebrow{font-size:12px;font-weight:800;letter-spacing:.14em;color:#55765a;margin:0 0 10px}h1{font-size:clamp(28px,4vw,42px);letter-spacing:-.03em;margin:0;overflow-wrap:anywhere}.lede{color:#71675e;line-height:1.7;margin:10px 0 0}.warn{background:#fff3e5;color:#8b5626;border-radius:12px;padding:14px 16px;line-height:1.6}.items{list-style:none;padding:0;margin:0;display:grid;gap:12px}.item{display:flex;align-items:start;gap:15px;background:#fff;border:1px solid #eaded1;border-radius:16px;padding:19px;box-shadow:0 8px 28px #3a231308}.ord{width:31px;height:31px;border-radius:10px;background:#eaf2e7;color:#527a5a;font-weight:800;display:grid;place-items:center;flex:none}.item-body{min-width:0;flex:1}.item-body strong{font-size:17px;line-height:1.4;overflow-wrap:anywhere}.reason{font-size:13px;color:#70665c;line-height:1.7;white-space:pre-line;overflow-wrap:anywhere;margin:8px 0 0}.ref{display:inline-block;font-size:12px;font-weight:700;color:#8b5626;background:#fff2e4;border-radius:999px;padding:4px 10px;margin:0 0 8px}.ref.removed{color:#756b62;background:#f0ece7}.detail-link{border:1px solid #dcd1c4;border-radius:999px;background:#fff;color:#a64613;padding:9px 13px;white-space:nowrap;cursor:pointer;font:inherit;font-size:13px}.empty,.status{padding:28px 20px;background:#f6f1eb;color:#6c6055;border-radius:14px;line-height:1.6}.empty{text-align:center;background:#fff;border:1px dashed #decfbe}.error{background:#fff1ef;color:#a6372b}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}.disclaimer{font-size:12px;color:#817367;line-height:1.7;margin-top:22px}@media(max-width:580px){.item{flex-wrap:wrap;gap:10px}.item-body{flex-basis:calc(100% - 45px)}.detail-link{margin-left:45px}}
</style>
