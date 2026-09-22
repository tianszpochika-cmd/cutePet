<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { FAVORITE_GROUPS } from '../../domain/family';

const router = useRouter();
const tab = ref<string>('内容');
const rows = ref<Record<string, unknown>[]>([]);
const error = ref('');

async function load() {
  error.value = '';
  try {
    rows.value = (await api.favoritesList({ query: { tab: tab.value } })) as unknown as Record<string, unknown>[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（未登录会进入登录闸门）';
  }
}

function pick(t: string) {
  tab.value = t;
  void load();
}

onMounted(load);
</script>

<template>
  <div class="fav">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>我的收藏</h1>
    </header>

    <nav class="tabs">
      <button v-for="t in FAVORITE_GROUPS" :key="t" type="button" :class="{ on: tab === t }" @click="pick(t)">
        {{ t }}
      </button>
    </nav>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="rows.length === 0" class="muted" data-testid="empty">「{{ tab }}」分组暂无收藏（左滑/长按整理随移动端）</p>
    <ul v-else class="list">
      <li v-for="(r, i) in rows" :key="i" class="row">{{ JSON.stringify(r).slice(0, 80) }}…</li>
    </ul>
  </div>
</template>

<style scoped>
.fav { max-width: 640px; margin: 0 auto; padding: 24px 16px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.tabs { display: flex; gap: 6px; margin: 12px 0; overflow-x: auto; }
.tabs button { height: 32px; padding: 0 14px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; white-space: nowrap; }
.tabs button.on { background: #ff7a2f; color: #fff; font-weight: 600; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px 14px; font-size: 13px; color: #7a6e63; }
.muted { color: #7a6e63; font-size: 14px; }
.err { color: #ef4444; font-size: 13px; }
</style>
