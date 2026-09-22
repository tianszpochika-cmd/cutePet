<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { refStateCopy } from '../../domain/goods';

const route = useRoute();
const router = useRouter();
const id = String(route.params.id);

const list = ref<{
  id: number;
  title: string;
  kind: string;
  publishable: boolean;
  items: { ord: number; productId: number; name: string; reason: string; refState: string }[];
} | null>(null);
const error = ref('');

onMounted(async () => {
  try {
    list.value = (await api.listGet({ path: { id } })) as unknown as typeof list.value;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 catalog-service）';
  }
});
</script>

<template>
  <div class="list-detail">
    <header>
      <button type="button" class="back" @click="router.push('/goods')">‹ 返回</button>
      <h1 v-if="list">{{ list.title }}</h1>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="!list" class="muted">加载中…</p>

    <template v-else>
      <p class="meta">{{ list.kind === 'OFFICIAL' ? '官方清单' : '作者清单' }} · {{ list.items.length }} 项</p>
      <p v-if="!list.publishable" class="warn" data-testid="unpublishable">
        清单内商品全部不可用，暂不可新发布（U88）。
      </p>

      <ol class="items">
        <li v-for="it in list.items" :key="it.productId">
          <span class="ord">{{ it.ord + 1 }}</span>
          <div>
            <strong>{{ it.name }}</strong>
            <p class="reason">{{ it.reason }}</p>
            <p v-if="it.refState !== 'VISIBLE'" class="ref" data-testid="item-ref">
              {{ refStateCopy(it.refState === 'OFF_SHELF_BADGE' ? 'OFF_SHELF' : 'CLEARED').text }}
            </p>
          </div>
        </li>
      </ol>
    </template>
  </div>
</template>

<style scoped>
.list-detail { max-width: 640px; margin: 0 auto; padding: 24px 16px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.meta { color: #7a6e63; font-size: 14px; }
.warn { background: #fff1e8; color: #b45309; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.items { list-style: none; padding: 0; display: grid; gap: 12px; margin-top: 12px; }
.items li { display: flex; gap: 12px; background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; }
.ord { width: 28px; height: 28px; border-radius: 999px; background: #eaf1ff; color: #4d8dff; font-weight: 700; display: grid; place-items: center; flex: none; }
.reason { color: #7a6e63; font-size: 13px; margin: 4px 0 0; }
.ref { color: #b45309; font-size: 12px; margin: 4px 0 0; }
.muted { color: #7a6e63; }
.err { color: #ef4444; font-size: 13px; }
</style>
