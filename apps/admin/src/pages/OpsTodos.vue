<script setup lang="ts">
import { ref } from 'vue';
import { escalateOpsTodo, OPS_TODO_LABELS, type OpsTodo, type OpsTodoKind } from '../domain/governance';

const nowIso = () => new Date().toISOString();

const todos = ref<OpsTodo[]>([
  { kind: 'FAILED_NOTIFICATION', refId: 'msg-88', owner: 'op-notify', createdAtIso: '2026-09-20T00:00:00Z', resolved: false },
  { kind: 'CLEANUP_FAILED', refId: 'signup-9', owner: 'op-data', createdAtIso: '2026-09-22T08:00:00Z', resolved: false },
  { kind: 'PRO_REVIEW_PENDING', refId: 'article-31', owner: 'editor-2', createdAtIso: '2026-09-22T10:00:00Z', resolved: false },
  { kind: 'ACCOUNT_CLEANUP', refId: 'user-77', owner: 'op-iam', createdAtIso: '2026-09-21T00:00:00Z', resolved: true },
]);
const message = ref('');

function escalate(t: OpsTodo) {
  const result = escalateOpsTodo(t, nowIso());
  message.value = result.escalated
    ? result.visible
    : `${OPS_TODO_LABELS[t.kind]}：未逾期（24h 阈值），无需升级`;
}

function resolve(t: OpsTodo) {
  t.resolved = true;
  message.value = `${OPS_TODO_LABELS[t.kind]}（${t.refId}）已处理`;
}
</script>

<template>
  <div class="ops-todos">
    <p class="rule">闭环 §3：各业务域待办列表 + 主管逾期升级；不得仅展示后台任务「已运行」。</p>

    <ul class="list">
      <li v-for="t in todos" :key="t.refId + t.kind" :class="{ done: t.resolved }" :data-testid="`todo-${t.refId}`">
        <div>
          <strong>{{ OPS_TODO_LABELS[t.kind] }}</strong>
          <p class="meta">{{ t.refId }} · 负责人 {{ t.owner }} · 创建 {{ t.createdAtIso.slice(0, 16).replace('T', ' ') }}</p>
        </div>
        <div class="ops">
          <template v-if="!t.resolved">
            <button type="button" class="chip" @click="escalate(t)">逾期检查/升级</button>
            <button type="button" class="chip primary" @click="resolve(t)">标记处理</button>
          </template>
          <span v-else class="done">✅ 已完成</span>
        </div>
      </li>
    </ul>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.ops-todos { display: grid; gap: 12px; }
.rule { background: #f5f3ff; color: #6d5bd0; border-radius: 12px; padding: 12px 14px; font-size: 13px; margin: 0; }
.list { list-style: none; padding: 0; display: grid; gap: 10px; margin: 0; }
.list li { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; display: flex; justify-content: space-between; align-items: center; }
.list li.done { opacity: .6; }
.meta { margin: 4px 0 0; color: #7a6e63; font-size: 12px; }
.ops { display: flex; gap: 6px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.chip.primary { background: #22c55e; color: #fff; }
.done { color: #22c55e; font-size: 13px; }
.msg { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>
