<script setup lang="ts">
import { ref } from 'vue';
import { auditQueryBlockers, redactValue } from '../domain/workbench';

const from = ref('');
const to = ref('');
const action = ref('');
const error = ref('');
const rows = ref([
  {
    id: 1,
    operator: 'op-admin',
    ip: '127.0.0.1',
    action: 'user.ban',
    target: 'USER#9',
    before: '{"status":"ACTIVE","phone":"13812345678"}',
    after: '{"status":"MUTED"}',
    createdAt: '2026-09-22 10:00',
    highRisk: true,
  },
  {
    id: 2,
    operator: 'op-editor',
    ip: '10.0.0.8',
    action: 'article.takedown',
    target: 'ARTICLE#3',
    before: '{"state":"PUBLISHED"}',
    after: '{"state":"TAKEDOWN"}',
    createdAt: '2026-09-22 09:30',
    highRisk: false,
  },
]);

function query() {
  const blockers = auditQueryBlockers(from.value, to.value);
  if (blockers.length > 0) {
    error.value = blockers.join(' / ') + '（起≤止且跨度≤90 天）';
    return;
  }
  error.value = '';
}
</script>

<template>
  <div class="audit">
    <section class="card filters">
      <label>起 <input v-model="from" type="date" data-testid="from" /></label>
      <label>止 <input v-model="to" type="date" data-testid="to" /></label>
      <label>动作 <input v-model="action" placeholder="如 user.ban" /></label>
      <button type="button" class="chip primary" data-testid="query" @click="query">查询（≤90 天）</button>
    </section>

    <p v-if="error" class="err" data-testid="error">{{ error }}</p>

    <table>
      <thead>
        <tr><th>时间</th><th>操作者</th><th>IP</th><th>动作</th><th>对象</th><th>前值 → 后值</th></tr>
      </thead>
      <tbody>
        <tr v-for="r in rows" :key="r.id" :data-testid="`log-${r.id}`">
          <td>{{ r.createdAt }}</td>
          <td>{{ r.operator }}</td>
          <td>{{ r.ip }}</td>
          <td>
            {{ r.action }}
            <span v-if="r.highRisk" class="risk">高危</span>
          </td>
          <td>{{ r.target }}</td>
          <td class="values">
            <code>{{ redactValue(r.before) }}</code> → <code>{{ redactValue(r.after) }}</code>
          </td>
        </tr>
      </tbody>
    </table>

    <p class="meta">读出即脱敏（手机号/邮箱）；区间 ≤90 天；IP 为必录字段（决议）。高危动作：封禁/权限变更/系统设置。</p>
  </div>
</template>

<style scoped>
.audit { display: grid; gap: 14px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; }
.filters { display: flex; gap: 12px; align-items: end; flex-wrap: wrap; }
.filters label { display: grid; gap: 4px; font-size: 12px; color: #7a6e63; }
.filters input { height: 36px; border: 1px solid #f0e6dc; border-radius: 10px; padding: 0 10px; font-size: 13px; }
.chip { height: 36px; border: none; border-radius: 999px; padding: 0 16px; font-size: 13px; cursor: pointer; }
.chip.primary { background: #ff7a2f; color: #fff; font-weight: 600; }
table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
th, td { text-align: left; padding: 10px 12px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { background: #faf7f3; color: #7a6e63; font-size: 12px; }
.risk { margin-left: 6px; background: #fdecec; color: #b91c1c; border-radius: 999px; padding: 1px 8px; font-size: 11px; }
.values code { background: #faf7f3; border-radius: 6px; padding: 2px 6px; font-size: 12px; }
.meta { margin: 0; color: #7a6e63; font-size: 12px; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
</style>
