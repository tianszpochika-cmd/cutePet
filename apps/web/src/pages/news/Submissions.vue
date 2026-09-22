<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { statusStepIndex, rejectCopy, withdrawCopy, STATUS_STEPS } from '../../domain/submission';
import { reviewStateCopy } from '../../domain/news';

interface SubmissionRow {
  articleId?: number;
  submissionId?: number;
  state: string;
  slug?: string;
  rejectCount?: number;
  note?: string;
  suspended?: boolean;
}

const router = useRouter();
const rows = ref<SubmissionRow[]>([]);
const error = ref('');

onMounted(async () => {
  try {
    rows.value = (await api.submissionList()) as unknown as SubmissionRow[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 content-service）';
  }
});

function steps(state: string): { label: string; cls: string }[] {
  const { done, current } = statusStepIndex(state);
  return STATUS_STEPS.map((label, i) => ({
    label,
    cls: i < done ? 'done' : i === current ? 'current' : '',
  }));
}

async function withdraw(row: SubmissionRow) {
  const id = row.submissionId ?? row.articleId;
  if (!id) return;
  const copy = withdrawCopy(row.state);
  if (!copy.allowed) {
    alert(copy.note);
    return;
  }
  if (!confirm(copy.note)) return;
  try {
    await api.submissionWithdraw({ path: { id: String(id) } });
    rows.value = (await api.submissionList()) as unknown as SubmissionRow[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '撤回失败';
  }
}
</script>

<template>
  <div class="subs">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>我的投稿</h1>
      <button type="button" class="primary" @click="router.push('/write')">＋ 写新内容</button>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="rows.length === 0 && !error" class="muted" data-testid="empty">
      还没有投稿 —— 点「写新内容」创建第一篇（文章/评测/清单）。
    </p>

    <ul class="list">
      <li v-for="(r, i) in rows" :key="i" class="card">
        <div class="head">
          <span :class="['badge', reviewStateCopy(r.state).tone]">
            {{ reviewStateCopy(r.state).label }}
          </span>
          <strong>{{ r.slug ?? `#${r.submissionId ?? r.articleId}` }}</strong>
        </div>

        <div class="steps">
          <span v-for="s in steps(r.state)" :key="s.label" :class="['step', s.cls]">{{ s.label }}</span>
        </div>

        <p v-if="r.state === 'REJECTED'" class="err" data-testid="reject-copy">
          {{ rejectCopy(r.rejectCount ?? 1).message }}
        </p>
        <p v-if="r.note && r.state === 'REJECTED'" class="note">编辑意见：{{ r.note }}</p>
        <p v-if="r.suspended" class="err">⏸ 投稿资格暂停中</p>

        <div class="actions">
          <button
            v-if="r.state === 'REJECTED' || r.state === 'DRAFT'"
            type="button"
            @click="router.push(`/write/${r.submissionId ?? r.articleId}`)"
          >
            修改
          </button>
          <button
            v-if="['PENDING', 'REVIEWING', 'PUBLISHED'].includes(r.state)"
            type="button"
            class="ghost"
            :data-testid="`withdraw-${i}`"
            @click="withdraw(r)"
          >
            撤回
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.subs {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
}
header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back {
  background: none;
  border: none;
  color: #ff7a2f;
}
.primary {
  margin-left: auto;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 12px;
}
.card {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 16px;
  display: grid;
  gap: 10px;
}
.head {
  display: flex;
  gap: 10px;
  align-items: center;
}
.badge {
  font-size: 12px;
  border-radius: 999px;
  padding: 2px 10px;
  font-weight: 600;
}
.badge.gray {
  background: #f0e6dc;
  color: #7a6e63;
}
.badge.orange {
  background: #fff1e8;
  color: #ff7a2f;
}
.badge.blue {
  background: #eaf1ff;
  color: #4d8dff;
}
.badge.green {
  background: #e7f8ef;
  color: #22c55e;
}
.badge.red {
  background: #fdecec;
  color: #ef4444;
}
.steps {
  display: flex;
  gap: 6px;
}
.step {
  font-size: 12px;
  color: #7a6e63;
  background: #f7f1ea;
  border-radius: 999px;
  padding: 3px 10px;
}
.step.done {
  background: #e7f8ef;
  color: #22c55e;
}
.step.current {
  background: #fff1e8;
  color: #ff7a2f;
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 8px;
}
.actions button {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
}
.actions button.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.note {
  color: #7a6e63;
  font-size: 13px;
}
.muted {
  color: #7a6e63;
}
</style>
