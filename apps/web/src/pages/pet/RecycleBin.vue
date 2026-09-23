<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { recycleRemainingDays, RESTORE_GUIDANCE } from '../../domain/closedLoop';

interface Row { id: number | string; name: string; state: 'DELETED' | 'ARCHIVED'; deletedAt?: string | null }
const router = useRouter();
const filter = ref<'DELETED' | 'ARCHIVED'>('DELETED');
const rows = ref<Row[]>([]);
const loading = ref(true);
const error = ref('');
const restoring = ref<number | string | null>(null);
const restored = ref(false);
const visibleRows = computed(() => rows.value.filter((row) => row.state === filter.value));

onMounted(() => { void loadRows(); });

async function loadRows() {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.petsList({ query: { all: true } });
    if (!Array.isArray(response)) throw new Error('归档列表格式异常');
    rows.value = response.filter((row): row is Row =>
      row && typeof row === 'object' && (typeof row.id === 'number' || typeof row.id === 'string') &&
      typeof row.name === 'string' &&
      (row.state === 'DELETED' || row.state === 'ARCHIVED'));
  } catch (e) {
    rows.value = [];
    error.value = e instanceof Error ? e.message : '归档列表读取失败';
  } finally {
    loading.value = false;
  }
}

async function restore(row: Row) {
  if (restoring.value !== null) return;
  restoring.value = row.id;
  error.value = '';
  restored.value = false;
  try {
    const response = (await api.petRestore({ path: { id: String(row.id) } })) as unknown;
    if (!response || typeof response !== 'object' || !('state' in response) || response.state !== 'ACTIVE' ||
        !('petId' in response) || String(response.petId) !== String(row.id)) {
      error.value = '平台已响应，但恢复结果尚未确认。请重新加载列表核对，避免重复操作。';
      return;
    }
    rows.value = rows.value.filter((item) => item.id !== row.id);
    restored.value = true;
  } catch (e) {
    error.value = `恢复结果尚未确认：${e instanceof Error ? e.message : '请求失败'}。请重新加载列表核对。`;
  } finally {
    restoring.value = null;
  }
}

function remaining(row: Row): string {
  if (!row.deletedAt) return '期限待平台核对';
  const days = recycleRemainingDays(row.deletedAt, new Date().toISOString());
  return Number.isFinite(days) ? `约剩 ${days} 天可恢复` : '期限待平台核对';
}
</script>

<template>
  <main class="recycle">
    <button type="button" class="back" @click="router.push('/pets')">‹ 我的宠物</button>
    <header>
      <span class="eyebrow">档案管理</span>
      <h1>归档与回收站</h1>
      <p>这里只显示平台返回的本人档案。恢复后，共享与提醒是否重新启用仍需逐项核对。</p>
    </header>

    <nav class="tabs" aria-label="档案状态">
      <button type="button" :class="{ on: filter === 'DELETED' }" @click="filter = 'DELETED'">回收站</button>
      <button type="button" :class="{ on: filter === 'ARCHIVED' }" @click="filter = 'ARCHIVED'">归档</button>
    </nav>

    <p v-if="loading" class="state" role="status">正在读取档案…</p>
    <div v-else-if="error" class="state error-state" role="alert">
      <p>{{ error }}</p><button type="button" class="ghost" @click="loadRows">重新加载</button>
    </div>
    <div v-else-if="visibleRows.length === 0" class="state empty" data-testid="empty">
      <h2>{{ filter === 'DELETED' ? '回收站是空的' : '没有归档的宠物' }}</h2>
      <p>当前没有可显示的{{ filter === 'DELETED' ? '软删除' : '归档' }}档案。</p>
    </div>
    <ul v-else class="list">
      <li v-for="row in visibleRows" :key="row.id" class="row" :data-testid="`row-${row.id}`">
        <div><strong>{{ row.name }}</strong><p>{{ row.state === 'DELETED' ? remaining(row) : '已归档' }}</p></div>
        <button type="button" class="restore" :disabled="restoring !== null" @click="restore(row)">{{ restoring === row.id ? '恢复中…' : '恢复档案' }}</button>
      </li>
    </ul>

    <section v-if="restored" class="guidance" role="status" data-testid="restore-guidance">
      <h2>平台已确认恢复</h2>
      <p v-for="item in RESTORE_GUIDANCE" :key="item">{{ item }}</p>
      <button type="button" class="ghost" @click="router.push('/pets')">返回宠物列表</button>
    </section>
    <p class="note">彻底删除接口尚未接入，页面不会在本地移除档案来模拟删除。</p>
  </main>
</template>

<style scoped>
.recycle { max-width: 720px; margin: 20px auto 56px; padding: 16px; display: grid; gap: 18px; color: #2b2118; }
.back { justify-self: start; min-height: 44px; padding: 0; border: 0; background: transparent; color: #a8470c; cursor: pointer; }
header { display: grid; gap: 6px; }
header h1 { margin: 0; font-size: clamp(24px, 4vw, 32px); }
header p { margin: 0; color: #706255; line-height: 1.6; }
.eyebrow { color: #a8470c; font-size: 13px; font-weight: 700; }
.tabs { display: flex; border-bottom: 1px solid #e6d8cb; }
.tabs button { min-height: 46px; padding: 0 22px; border: 0; border-bottom: 3px solid transparent; background: transparent; color: #706255; cursor: pointer; }
.tabs button.on { border-bottom-color: #b85111; color: #a8470c; font-weight: 700; }
.state, .row, .guidance { padding: 18px; background: #fff; border: 1px solid #f0e6dc; border-radius: 16px; }
.state h2, .state p { margin: 0 0 8px; }
.state p:last-child { margin-bottom: 0; }
.error-state { color: #a12a28; }
.list { list-style: none; padding: 0; margin: 0; display: grid; gap: 10px; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.row strong { font-size: 17px; }
.row p { margin: 5px 0 0; color: #706255; font-size: 13px; }
.restore, .ghost { min-height: 44px; padding: 0 18px; border-radius: 999px; cursor: pointer; font-weight: 600; }
.restore { border: 0; background: #b85111; color: #fff; }
.restore:disabled { opacity: .55; cursor: wait; }
.ghost { border: 1px solid #dacabc; background: #fff; color: #684b39; }
.guidance { display: grid; gap: 8px; background: #f0faf3; border-color: #c7e4d0; }
.guidance h2 { margin: 0; color: #176336; font-size: 17px; }
.guidance p { margin: 0; font-size: 14px; color: #2e6842; }
.guidance button { justify-self: start; }
.note { margin: 0; color: #706255; font-size: 13px; }
button:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
@media (max-width: 520px) { .row { align-items: stretch; flex-direction: column; } .restore { width: 100%; } }
</style>
