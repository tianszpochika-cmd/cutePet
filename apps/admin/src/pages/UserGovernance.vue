<script setup lang="ts">
import { computed, ref } from 'vue';
import { BAN_ACTION_COPY, banBlockers, appealOverdue, FAMILY_DISPUTE_RULE, type BanLevel } from '../domain/governance';

const actorId = ref(1); // 当前操作者（演示）
const users = ref([
  { id: 1, nickname: '自己（管理员）', status: 'ACTIVE', posts: true },
  { id: 9, nickname: '广告账号', status: 'ACTIVE', posts: true },
]);
const message = ref('');

const banTarget = ref<number | null>(null);
const banLevel = ref<BanLevel>('L2');
const banReason = ref('');
const doubleConfirmed = ref(false);

const appeals = ref([
  { id: 51, userId: 9, actionType: 'MODERATION', createdAt: '2026-09-20T00:00:00Z', resolvedAt: null, reason: '误判为广告' },
  { id: 52, userId: 12, actionType: 'CUSTOMER_SERVICE', createdAt: '2026-09-22T00:00:00Z', resolvedAt: null, reason: '数据更正请求' },
]);
const disputes = ref([{ id: 61, familyId: 3, resolution: '' }]);
const resolution = ref('');

function submitBan() {
  if (banTarget.value === null) return;
  const blockers = banBlockers({
    actorId: actorId.value,
    targetId: banTarget.value,
    level: banLevel.value,
    reason: banReason.value,
    doubleConfirmed: doubleConfirmed.value,
  });
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  const u = users.value.find((x) => x.id === banTarget.value);
  if (u) u.status = banLevel.value === 'L4' ? 'BANNED' : 'MUTED';
  message.value = `#${banTarget.value} 处置：${BAN_ACTION_COPY[banLevel.value]}（高危二次确认+审计已留痕）`;
  banTarget.value = null;
  doubleConfirmed.value = false;
  banReason.value = '';
}

function resolveAppeal(id: number, accepted: boolean) {
  const a = appeals.value.find((x) => x.id === id);
  if (!a) return;
  if (!resolution.value.trim()) {
    message.value = 'RESOLUTION_REQUIRED：结论必填';
    return;
  }
  a.resolvedAt = new Date().toISOString();
  message.value = accepted
    ? `#${id} 申诉成立：撤销对应处置（剔除违规计数 U76），已通知用户`
    : `#${id} 申诉驳回：维持原处置，已通知用户`;
}

const appealOverdueFlag = computed(() =>
  appeals.value.filter((a) => appealOverdue(a.createdAt, a.resolvedAt, new Date().toISOString())),
);

function resolveDispute(id: number) {
  const d = disputes.value.find((x) => x.id === id);
  if (!d) return;
  if (!resolution.value.trim()) {
    message.value = 'RESOLUTION_REQUIRED：处理依据必填（留痕）';
    return;
  }
  d.resolution = resolution.value;
  message.value = `家庭纠纷 #${id} 已处理（${FAMILY_DISPUTE_RULE}）`;
}
</script>

<template>
  <div class="gov">
    <section class="card">
      <h2>用户与处置（user.view / user.ban）</h2>
      <table>
        <thead><tr><th>ID</th><th>昵称</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>#{{ u.id }}</td>
            <td>{{ u.nickname }}</td>
            <td>{{ u.status }}</td>
            <td>
              <button type="button" class="chip danger" @click="banTarget = u.id">封禁…</button>
              <button type="button" class="chip" @click="message = `#${u.id} 投稿资格已切换（user.post.right.cancel）`">
                投稿权
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="banTarget !== null" class="ban-form" data-testid="ban-form">
        <label>等级
          <select v-model="banLevel" data-testid="ban-level">
            <option value="L1">L1 — {{ BAN_ACTION_COPY.L1 }}</option>
            <option value="L2">L2 — {{ BAN_ACTION_COPY.L2 }}</option>
            <option value="L3">L3 — {{ BAN_ACTION_COPY.L3 }}</option>
            <option value="L4">L4 — {{ BAN_ACTION_COPY.L4 }}</option>
          </select>
        </label>
        <input v-model="banReason" placeholder="处置原因（必填，进入审计）" data-testid="ban-reason" />
        <label class="confirm">
          <input v-model="doubleConfirmed" type="checkbox" data-testid="double-confirm" />
          我已知悉这是高危操作（§6.2 双保险：二次确认 + 全量审计）
        </label>
        <div class="inline">
          <button type="button" class="chip danger" data-testid="ban-submit" @click="submitBan">执行处置</button>
          <button type="button" class="chip" @click="banTarget = null">取消</button>
        </div>
      </div>
    </section>

    <section class="card">
      <h2>工单分流（appeal.handle / report.handle 互不串）</h2>
      <label>处理结论（申诉/纠纷共用，必填）<input v-model="resolution" data-testid="resolution" /></label>
      <ul>
        <li v-for="a in appeals" :key="a.id">
          #{{ a.id }} [{{ a.actionType }}] {{ a.reason }}
          <span v-if="appealOverdueFlag.includes(a)" class="overdue">48h 超时</span>
          <button v-if="!a.resolvedAt" type="button" class="chip" data-testid="appeal-accept" @click="resolveAppeal(a.id, true)">成立</button>
          <button v-if="!a.resolvedAt" type="button" class="chip" @click="resolveAppeal(a.id, false)">驳回</button>
          <span v-else class="done">已结案</span>
        </li>
      </ul>
      <p class="meta">举报/版权走 report.handle 独立队列（/governance 集合入口）；不能用内容发布按钮处理工单（闭环 §3）。</p>
    </section>

    <section class="card">
      <h2>家庭纠纷（family.dispute）</h2>
      <p class="meta">{{ FAMILY_DISPUTE_RULE }}</p>
      <ul>
        <li v-for="d in disputes" :key="d.id">
          家庭 #{{ d.familyId }}
          <template v-if="!d.resolution">
            <button type="button" class="chip" data-testid="dispute-resolve" @click="resolveDispute(d.id)">按结论处理</button>
          </template>
          <span v-else class="done">已处理：{{ d.resolution }}</span>
        </li>
      </ul>
    </section>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.gov { display: grid; gap: 14px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 8px 10px; font-size: 13px; border-bottom: 1px solid #f0e6dc; }
th { color: #7a6e63; font-size: 12px; }
ul { margin: 0; padding-left: 16px; display: grid; gap: 8px; font-size: 13px; }
li { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.ban-form { background: #fdecec; border-radius: 12px; padding: 14px; display: grid; gap: 10px; }
.ban-form label { display: grid; gap: 6px; font-size: 13px; font-weight: 600; }
.ban-form input, .card select, .card > label input { height: 36px; border: 1px solid #f0e6dc; border-radius: 10px; padding: 0 10px; font-size: 13px; font-weight: 400; }
.confirm { display: flex; gap: 8px; align-items: center; font-weight: 400 !important; color: #7a6e63; }
.inline { display: flex; gap: 8px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 5px 12px; font-size: 12px; cursor: pointer; }
.chip.danger { background: #fdecec; color: #b91c1c; }
.overdue { background: #fdecec; color: #b91c1c; border-radius: 999px; padding: 2px 8px; font-size: 11px; }
.done { color: #22c55e; font-size: 12px; }
.meta { margin: 0; color: #7a6e63; font-size: 13px; }
.msg { background: #e7f8ef; color: #15803d; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>
