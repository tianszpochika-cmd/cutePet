<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface ReminderRow {
  id: number;
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
const message = ref('');

onMounted(async () => {
  try {
    rows.value = (await api.reminderList({ path: { id: petId } })) as unknown as ReminderRow[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 pet-service）';
  }
});

async function complete(r: ReminderRow) {
  try {
    const res = (await api.reminderComplete({ path: { id: String(r.id) } })) as unknown as {
      outcome?: string;
      nextDue?: string;
    };
    message.value =
      res?.outcome === 'ALREADY_DONE'
        ? '该提醒已完成（幂等：显示已有结果，不重复动作）'
        : res?.nextDue
          ? `已完成，下期 ${res.nextDue}`
          : '已完成';
    await reload();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败';
  }
}

async function reload() {
  rows.value = (await api.reminderList({ path: { id: petId } })) as unknown as ReminderRow[];
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
      <h1>提醒中心</h1>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="message" class="ok" data-testid="reminder-msg">{{ message }}</p>
    <p v-if="rows.length === 0 && !error" class="muted" data-testid="empty">
      还没有提醒 —— 在健康记录里填「下次日期」会自动生成，或在档案页新建。
    </p>

    <ul class="list">
      <li v-for="r in rows" :key="r.id" class="row">
        <div>
          <strong>{{ r.type }}</strong> <span :class="['tag', tone(r.state)]">{{ r.state }}</span>
          <p class="muted">{{ r.title }} · 下期 {{ r.nextDue ?? '—' }}</p>
        </div>
        <button
          v-if="r.state === 'ACTIVE'"
          type="button"
          class="ok-btn"
          :data-testid="`complete-${r.id}`"
          @click="complete(r)"
        >
          完成
        </button>
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
  color: #ff7a2f;
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
}
.tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 2px 8px;
  font-weight: 600;
}
.tag.orange {
  background: #fff1e8;
  color: #ff7a2f;
}
.tag.green {
  background: #e7f8ef;
  color: #22c55e;
}
.tag.red {
  background: #fdecec;
  color: #ef4444;
}
.tag.gray {
  background: #f0e6dc;
  color: #7a6e63;
}
.ok-btn {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: #22c55e;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
.muted {
  color: #7a6e63;
  font-size: 13px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.ok {
  color: #22c55e;
  font-size: 13px;
}
</style>
