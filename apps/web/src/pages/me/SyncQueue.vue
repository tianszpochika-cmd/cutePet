<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { syncRowCopy, LOGOUT_SYNC_CHOICES, type SyncState } from '../../domain/closedLoop';

const router = useRouter();
const rows = ref<{ id: number; label: string; state: SyncState }[]>([
  { id: 1, label: '体重 12.5kg（2026-09-22）', state: 'PENDING' },
  { id: 2, label: '疫苗记录（2026-09-20）', state: 'FAILED' },
  { id: 3, label: '驱虫记录（2026-09-18）', state: 'SUCCESS' },
  { id: 4, label: '体检摘要（2026-09-01）', state: 'LOST_RIGHT' },
]);
const counts = computed(() => ({
  pending: rows.value.filter((r) => r.state === 'PENDING').length,
  failed: rows.value.filter((r) => r.state === 'FAILED').length,
  success: rows.value.filter((r) => r.state === 'SUCCESS').length,
}));

function retry(id: number) {
  const row = rows.value.find((r) => r.id === id);
  if (row && row.state === 'FAILED') row.state = 'SUCCESS';
}

function logoutChoice(choice: string) {
  alert(`已选择：${choice}`);
}
</script>

<template>
  <div class="x07">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>待同步记录（X07）</h1>
    </header>

    <p class="summary" data-testid="counts">
      待同步 {{ counts.pending }} · 失败 {{ counts.failed }} · 已完成 {{ counts.success }}
    </p>

    <ul class="list">
      <li v-for="r in rows" :key="r.id" class="row" :data-testid="`sync-${r.id}`">
        <div>
          <strong>{{ r.label }}</strong>
          <p class="meta" :class="{ warn: r.state === 'FAILED' || r.state === 'LOST_RIGHT' }">
            {{ syncRowCopy(r.state) }}
          </p>
        </div>
        <button v-if="r.state === 'FAILED'" type="button" class="ghost" @click="retry(r.id)">重试</button>
      </li>
    </ul>

    <section class="logout">
      <h2>登出前的选择（X07）</h2>
      <div class="actions">
        <button
          v-for="c in LOGOUT_SYNC_CHOICES"
          :key="c"
          type="button"
          class="ghost"
          @click="logoutChoice(c)"
        >
          {{ c }}
        </button>
      </div>
      <p class="meta">离线记录显示「待同步」而非「已完成」；同步失败保留全部输入。</p>
    </section>
  </div>
</template>

<style scoped>
.x07 { max-width: 640px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.summary { background: #fff1e8; color: #b45309; border-radius: 12px; padding: 10px 14px; font-size: 13px; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; margin: 0; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: flex; justify-content: space-between; align-items: center; }
.meta { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.meta.warn { color: #b45309; }
.logout { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.logout h2 { margin: 0; font-size: 15px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.ghost { height: 40px; padding: 0 14px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; }
.meta { color: #7a6e63; font-size: 12px; }
</style>
