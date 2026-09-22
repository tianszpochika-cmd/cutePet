<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { MESSAGE_CATEGORIES, unreadBadge } from '../../domain/family';

const router = useRouter();
const tab = ref('REMINDER');
const rows = ref<{ id: number; title: string; body: string; deepLink?: string; state: string }[]>([]);
const unread = ref(0);
const error = ref('');

async function load() {
  error.value = '';
  try {
    const res = (await api.messagesList({ query: { category: undefined, state: undefined } })) as unknown as {
      items: typeof rows.value;
      unread: number;
    };
    rows.value = (res.items ?? []).filter((m) => m.category === undefined || true);
    unread.value = res.unread ?? 0;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（未登录会进入登录闸门）';
  }
}

async function readAll() {
  try {
    await api.messagesReadAll();
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

function open(m: { deepLink?: string }) {
  if (m.deepLink) void router.push(m.deepLink);
}

onMounted(load);
</script>

<template>
  <div class="msg">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>消息中心</h1>
      <span v-if="unread > 0" class="badge" data-testid="unread">{{ unreadBadge(unread) }}</span>
      <button type="button" class="read-all" @click="readAll">全部已读</button>
    </header>

    <nav class="tabs">
      <button
        v-for="c in MESSAGE_CATEGORIES"
        :key="c.id"
        type="button"
        :class="{ on: tab === c.id }"
        @click="tab = c.id"
      >
        {{ c.label }}
      </button>
    </nav>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="rows.length === 0" class="muted" data-testid="empty">暂无消息（提醒/互动/审核结果/家庭/系统五类）</p>
    <ul v-else class="list">
      <li v-for="m in rows" :key="m.id" class="row" @click="open(m)">
        <strong>{{ m.title }}</strong>
        <p>{{ m.body }}</p>
        <span :class="m.state === 'UNREAD' ? 'dot' : ''"></span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.msg { max-width: 640px; margin: 0 auto; padding: 24px 16px; }
header { display: flex; gap: 10px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.badge { background: #ef4444; color: #fff; border-radius: 999px; font-size: 12px; padding: 1px 8px; }
.read-all { margin-left: auto; background: none; border: none; color: #ff7a2f; font-size: 13px; }
.tabs { display: flex; gap: 6px; margin: 12px 0; overflow-x: auto; }
.tabs button { height: 32px; padding: 0 12px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; white-space: nowrap; }
.tabs button.on { background: #ff7a2f; color: #fff; font-weight: 600; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; }
.row { position: relative; background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px 14px; cursor: pointer; }
.row p { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.dot { position: absolute; top: 14px; right: 14px; width: 8px; height: 8px; border-radius: 999px; background: #ef4444; }
.muted { color: #7a6e63; font-size: 14px; }
.err { color: #ef4444; font-size: 13px; }
</style>
