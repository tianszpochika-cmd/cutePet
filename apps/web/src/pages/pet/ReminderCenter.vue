<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface ReminderRow {
  id: number | string;
  type: string;
  title: string;
  state: string;
  nextDue: string | null;
}

const route = useRoute();
const router = useRouter();
const petId = String(route.params.id);
const rows = ref<ReminderRow[]>([]);
const error = ref('');
const loading = ref(true);

onMounted(() => { void reload(); });

async function reload() {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.reminderList({ path: { id: petId } });
    if (!Array.isArray(response) || !response.every((row) => row && typeof row === 'object' &&
        (typeof row.id === 'number' || typeof row.id === 'string') && typeof row.state === 'string' &&
        typeof row.type === 'string' && typeof row.title === 'string')) {
      throw new Error('提醒列表格式异常');
    }
    rows.value = response as ReminderRow[];
  } catch (e) {
    rows.value = [];
    error.value = e instanceof Error ? e.message : '提醒读取失败';
  } finally {
    loading.value = false;
  }
}

function tone(state: string): string {
  if (state === 'DONE') return 'green';
  if (state === 'EXPIRED') return 'red';
  if (state === 'CLOSED') return 'gray';
  return 'orange';
}
</script>

<template>
  <div class="reminders">
    <header>
      <button type="button" class="back" @click="router.push(`/pets/${petId}`)">‹ 返回</button>
      <h1>提醒计划</h1>
    </header>

    <p class="notice">这里显示平台返回的提醒计划。健康类待办需要与有效记录关联；当前完成接口尚不接收记录信息，完成操作暂未开放。</p>
    <p v-if="loading" class="muted" role="status">正在读取提醒…</p>
    <div v-else-if="error" class="error-state" role="alert">
      <p>{{ error }}</p>
      <button type="button" class="ghost" @click="reload">重试</button>
    </div>
    <div v-else-if="rows.length === 0" class="empty" data-testid="empty">
      <strong>目前没有提醒计划</strong>
      <p class="muted">健康记录中的下次日期只会生成建议。提醒创建入口接入前，请勿将建议视为已排期。</p>
      <button type="button" class="ghost" @click="router.push(`/pets/${petId}/record`)">去记录健康事项</button>
    </div>

    <ul v-else class="list">
      <li v-for="r in rows" :key="r.id" class="row">
        <div>
          <strong>{{ r.type }}</strong> <span :class="['tag', tone(r.state)]">{{ r.state }}</span>
          <p class="muted">{{ r.title }} · 下期 {{ r.nextDue ?? '—' }}</p>
        </div>
        <span v-if="r.state === 'ACTIVE'" class="pending-label">完成待接入</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.reminders {
  max-width: 560px;
  margin: 32px auto;
  padding: 16px;
  display: grid;
  gap: 12px;
}
header {
  display: flex;
  gap: 12px;
  align-items: center;
}
.back {
  background: none;
  border: none;
  color: #a8470c;
  min-height: 44px;
  cursor: pointer;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
}
.row {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.notice, .empty { background: #fff; border: 1px solid #f0e6dc; border-radius: 16px; padding: 16px; color: #604b3b; font-size: 14px; line-height: 1.6; }
.empty { display: grid; gap: 8px; }
.empty p { margin: 0; }
.error-state { color: #a12a28; background: #fff4f2; border-radius: 14px; padding: 14px; }
.ghost { min-height: 44px; border: 1px solid #dacabc; border-radius: 999px; background: #fff; color: #684b39; padding: 0 16px; cursor: pointer; }
.pending-label { color: #7b5d43; font-size: 12px; white-space: nowrap; }
.tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 2px 8px;
  font-weight: 600;
}
.tag.orange {
  background: #fff1e8;
  color: #9c3f0b;
}
.tag.green {
  background: #e7f8ef;
  color: #176336;
}
.tag.red {
  background: #fdecec;
  color: #a12a28;
}
.tag.gray {
  background: #f0e6dc;
  color: #7a6e63;
}
.back:focus-visible, .ghost:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
.muted {
  color: #7a6e63;
  font-size: 13px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
