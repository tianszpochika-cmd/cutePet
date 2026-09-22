<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import {
  QUEUE_TABS,
  slaStatus,
  queueSort,
  canOperateRow,
  reviewActionBlockers,
  REJECT_TEMPLATES,
  batchEligible,
  REVIEW_SHORTCUTS,
  type QueueRow,
  type QueueTab,
} from '../domain/workbench';
import { versionDiffRequired, reviewDisposition } from '../domain/governance';

const tab = ref<QueueTab>('SUBMISSIONS');
const operatorId = ref(7);
const rows = ref<QueueRow[]>([
  { id: 1, type: 'ARTICLE', title: '幼猫换粮的七个误区', submitter: 'u-101', submittedAt: '2026-09-21T09:00', waitedHours: 40, reports: 0, claimedBy: null },
  { id: 2, type: 'REVIEW', title: '无谷粮评测（含来源材料）', submitter: 'u-102', submittedAt: '2026-09-22T08:00', waitedHours: 30, reports: 0, claimedBy: 7 },
  { id: 3, type: 'LIST', title: '幼猫好物清单', submitter: 'u-103', submittedAt: '2026-09-20T08:00', waitedHours: 60, reports: 2, claimedBy: null },
  { id: 4, type: 'ARTICLE', title: '夏季饮水注意', submitter: 'u-104', submittedAt: '2026-09-22T12:00', waitedHours: 3, reports: 0, claimedBy: null },
]);
const selected = ref<number[]>([]);
const active = ref<QueueRow | null>(null);
const note = ref('');
const message = ref('');

const sorted = computed(() => queueSort(rows.value.filter((r) => r.claimedBy === null || r.claimedBy === operatorId.value)));
const eligibleTabs = () => QUEUE_TABS.filter((t) => true); // 权限过滤由侧边栏承担；此处展示全部可用项

function openRow(r: QueueRow) {
  active.value = r;
  note.value = '';
  message.value = '';
}

function claim(r: QueueRow) {
  const verdict = canOperateRow(r, operatorId.value);
  if (!verdict.allowed) {
    message.value = verdict.reason;
    return;
  }
  r.claimedBy = operatorId.value;
  message.value = `已领取 #${r.id}（处理中锁生效）`;
}

function act(action: 'approve' | 'reject') {
  if (!active.value) return;
  const blockers = reviewActionBlockers(action, note.value);
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  message.value =
    action === 'approve'
      ? `#${active.value.id} 通过并发布${versionDiffRequired(active.value.type) ? '（当前版本生效，旧版归档）' : ''}`
      : `#${active.value.id} 驳回：${reviewDisposition({ kind: 'quality', rejectCount: 1 }).label}（模板：${REJECT_TEMPLATES[0]}…）`;
  rows.value = rows.value.filter((r) => r.id !== active.value!.id);
  active.value = null;
}

function toggleSelect(id: number) {
  selected.value = selected.value.includes(id)
    ? selected.value.filter((x) => x !== id)
    : [...selected.value, id];
}

const canBatch = computed(() =>
  batchEligible(
    rows.value.filter((r) => selected.value.includes(r.id)),
    selected.value.length > 0,
  ),
);

function batchApprove() {
  if (!canBatch.value) return;
  const n = selected.value.length;
  rows.value = rows.value.filter((r) => !selected.value.includes(r.id));
  selected.value = [];
  message.value = `批量通过 ${n} 条（同队列、无锁定行、二次确认口径）`;
}

onMounted(() => {
  // 快捷键：Enter 打开 / A 通过 / R 驳回 / Esc 关闭（交互设计 §5）
  window.addEventListener('keydown', (e) => {
    const key = e.key === 'Enter' ? 'Enter' : e.key.toLowerCase();
    const action = REVIEW_SHORTCUTS[e.key === 'Escape' ? 'Escape' : key];
    if (action === 'close') active.value = null;
    if (action === 'approve' && active.value) act('approve');
    if (action === 'reject' && active.value) act('reject');
  });
});
</script>

<template>
  <div class="bench">
    <nav class="tabs">
      <button
        v-for="t in eligibleTabs()"
        :key="t.id"
        type="button"
        :class="{ on: tab === t.id }"
        :data-testid="`tab-${t.id}`"
        @click="tab = t.id"
      >
        {{ t.label }}
      </button>
    </nav>

    <div class="toolbar">
      <span class="hint">排序：SLA（24h 红 / 48h 升级）→ 举报数 · 快捷键 {{ Object.entries(REVIEW_SHORTCUTS).map(([k, v]) => `${k}=${v}`).join(' · ') }}</span>
      <button type="button" :disabled="!canBatch" data-testid="batch" @click="batchApprove">
        批量通过（{{ selected.value === undefined ? 0 : selected.length }}）
      </button>
    </div>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>

    <div class="cols">
      <table>
        <thead>
          <tr><th /><th>类型</th><th>标题</th><th>提交人</th><th>等待</th><th>SLA</th><th>锁</th></tr>
        </thead>
        <tbody>
          <tr
            v-for="r in sorted"
            :key="r.id"
            :class="{ active: active?.id === r.id }"
            :data-testid="`row-${r.id}`"
            @click="openRow(r)"
          >
            <td @click.stop><input type="checkbox" :checked="selected.includes(r.id)" @change="toggleSelect(r.id)" /></td>
            <td>{{ r.type }}</td>
            <td>{{ r.title }}<span v-if="r.reports > 0" class="badge">{{ r.reports }} 举报</span></td>
            <td>{{ r.submitter }}</td>
            <td>{{ r.waitedHours }}h</td>
            <td>
              <span :class="['sla', slaStatus(r.waitedHours).toLowerCase()]">{{ slaStatus(r.waitedHours) }}</span>
            </td>
            <td>{{ r.claimedBy ? `#${r.claimedBy}` : '—' }}</td>
          </tr>
        </tbody>
      </table>

      <aside v-if="active" class="drawer" data-testid="drawer">
        <h3>#{{ active.id }} {{ active.title }}</h3>
        <p class="meta">类型 {{ active.type }} · 提交人 {{ active.submitter }} · 已等待 {{ active.waitedHours }}h</p>
        <p class="meta">内容预览（差异对比随接口渲染；审核仅对当前提交版本生效）</p>
        <div class="lock">
          <span>{{ active.claimedBy ? `处理中锁：#${active.claimedBy}` : '未领取' }}</span>
          <button v-if="!active.claimedBy" type="button" class="ghost" data-testid="claim" @click="claim(active)">
            领取（{{ operatorId }}）
          </button>
        </div>
        <textarea v-model="note" rows="3" placeholder="意见（驳回必填；可点模板）" data-testid="note" />
        <div class="tpl">
          <button v-for="t in REJECT_TEMPLATES" :key="t" type="button" class="chip" @click="note = t">{{ t }}</button>
        </div>
        <div class="actions">
          <button type="button" class="primary" data-testid="approve" @click="act('approve')">通过（A）</button>
          <button type="button" class="danger" data-testid="reject" @click="act('reject')">驳回（R）</button>
          <button type="button" class="ghost" @click="active = null">关闭（Esc）</button>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.bench { display: grid; gap: 14px; }
.tabs { display: flex; gap: 6px; overflow-x: auto; }
.tabs button { height: 32px; padding: 0 12px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; white-space: nowrap; font-size: 13px; }
.tabs button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.hint { color: #7a6e63; font-size: 12px; }
.toolbar button { height: 34px; padding: 0 14px; border: none; border-radius: 999px; background: #4d8dff; color: #fff; font-size: 13px; }
.toolbar button:disabled { opacity: .4; }
.msg { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 8px 12px; font-size: 13px; margin: 0; }
.cols { display: grid; grid-template-columns: 1.4fr 1fr; gap: 14px; align-items: start; }
table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
th, td { text-align: left; padding: 10px 12px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { background: #faf7f3; color: #7a6e63; font-size: 12px; }
tbody tr { cursor: pointer; }
tbody tr.active { background: #fff1e8; }
.badge { margin-left: 8px; background: #fdecec; color: #b91c1c; border-radius: 999px; padding: 1px 8px; font-size: 11px; }
.sla { border-radius: 999px; padding: 2px 8px; font-size: 11px; font-weight: 600; }
.sla.ok { background: #e7f8ef; color: #15803d; }
.sla.breach { background: #fdecec; color: #b91c1c; }
.sla.escalate { background: #fdecec; color: #7c2d12; outline: 1px solid #ef4444; }
.drawer { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.drawer h3 { margin: 0; font-size: 15px; }
.meta { margin: 0; color: #7a6e63; font-size: 13px; }
.lock { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: #7a6e63; }
textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 10px; font-family: inherit; font-size: 13px; }
.tpl { display: flex; gap: 6px; flex-wrap: wrap; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 4px 10px; font-size: 12px; cursor: pointer; }
.actions { display: flex; gap: 8px; }
.actions button { flex: 1; height: 40px; border: none; border-radius: 999px; font-weight: 600; font-size: 14px; }
.primary { background: #22c55e; color: #fff; }
.danger { background: #fdecec; color: #b91c1c; }
.ghost { background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
</style>
