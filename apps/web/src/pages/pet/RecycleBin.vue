<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { recycleRemainingDays, restoreAllowed, RESTORE_GUIDANCE } from '../../domain/closedLoop';

const router = useRouter();
const filter = ref<'回收站' | '归档'>('回收站');

interface Row {
  id: number;
  name: string;
  state: 'DELETED' | 'ARCHIVED';
  deletedAt: string;
  isOwner: boolean;
}

const rows = ref<Row[]>([
  { id: 90, name: '小灰（示例）', state: 'DELETED', deletedAt: new Date(Date.now() - 2 * 86400000).toISOString(), isOwner: true },
  { id: 91, name: '咪咪（归档）', state: 'ARCHIVED', deletedAt: new Date().toISOString(), isOwner: true },
]);
const guidance = ref<string[]>([]);
const nowIso = () => new Date().toISOString();

function restore(row: Row) {
  if (!restoreAllowed(row.isOwner)) return; // 仅所有者
  if (row.state === 'DELETED' && recycleRemainingDays(row.deletedAt, nowIso()) <= 0) {
    alert('超过 30 天软删窗口，无法普通恢复（联系客服）。');
    return;
  }
  row.state = 'ARCHIVED';
  row.deletedAt = nowIso();
  guidance.value = [...RESTORE_GUIDANCE]; // 恢复成功 → 引导，不自动执行
}

function remove(id: number) {
  rows.value = rows.value.filter((r) => r.id !== id);
}
</script>

<template>
  <div class="x06">
    <header>
      <button type="button" class="back" @click="router.push('/pets')">‹ 我的宠物</button>
      <h1>归档与回收站（X06）</h1>
    </header>

    <nav class="tabs">
      <button type="button" :class="{ on: filter === '回收站' }" @click="filter = '回收站'">回收站</button>
      <button type="button" :class="{ on: filter === '归档' }" @click="filter = '归档'">归档</button>
    </nav>

    <ul class="list">
      <li
        v-for="row in rows.filter((r) => (filter === '回收站' ? r.state === 'DELETED' : r.state === 'ARCHIVED'))"
        :key="row.id"
        class="row"
        :data-testid="`row-${row.id}`"
      >
        <div>
          <strong>{{ row.name }}</strong>
          <p class="meta">
            <template v-if="row.state === 'DELETED'">
              剩余恢复 {{ recycleRemainingDays(row.deletedAt, nowIso()) }} 天
            </template>
            <template v-else>已归档（历史记录保留）</template>
            · 所有者：{{ row.isOwner ? '我' : '他人' }}
          </p>
        </div>
        <div class="actions">
          <button type="button" class="ghost" @click="restore(row)">恢复</button>
          <button v-if="row.state === 'DELETED'" type="button" class="ghost" @click="remove(row.id)">彻底删除</button>
        </div>
      </li>
      <li v-if="rows.filter((r) => (filter === '回收站' ? r.state === 'DELETED' : r.state === 'ARCHIVED')).length === 0" class="muted">
        {{ filter }}为空
      </li>
    </ul>

    <div v-if="guidance.length > 0" class="guidance" data-testid="restore-guidance">
      <h2>恢复成功（未自动执行以下操作）</h2>
      <p v-for="g in guidance" :key="g">· {{ g }}</p>
      <button type="button" class="primary" @click="router.push('/pets')">前往处理</button>
    </div>
  </div>
</template>

<style scoped>
.x06 { max-width: 640px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.tabs { display: flex; gap: 6px; background: #f7f1ea; border-radius: 999px; padding: 4px; width: fit-content; }
.tabs button { border: none; background: none; height: 32px; padding: 0 18px; border-radius: 999px; color: #7a6e63; }
.tabs button.on { background: #fff; color: #ff7a2f; font-weight: 600; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; margin: 0; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: flex; justify-content: space-between; align-items: center; }
.meta { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.actions { display: flex; gap: 6px; }
.ghost { height: 34px; padding: 0 12px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; }
.guidance { background: #e7f8ef; border-radius: 16px; padding: 16px; display: grid; gap: 8px; }
.guidance h2 { margin: 0; font-size: 15px; color: #15803d; }
.guidance p { margin: 0; font-size: 13px; color: #166534; }
.primary { height: 40px; padding: 0 18px; border: none; border-radius: 999px; background: #22c55e; color: #fff; font-weight: 600; }
.muted { color: #7a6e63; font-size: 13px; list-style: none; }
</style>
