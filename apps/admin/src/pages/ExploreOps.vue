<script setup lang="ts">
import { ref } from 'vue';
import {
  correctionResolveBlockers,
  reviewHideBlockers,
  routeReviewBlockers,
  activityChangeOutcome,
} from '../domain/operations';

const message = ref('');

const pois = ref([
  { id: 1, name: '朝阳宠物公园', state: 'NORMAL' },
  { id: 2, name: '西城老店（停业）', state: 'CLOSED' },
]);
const corrections = ref([
  { id: 11, poi: '朝阳宠物公园', field: 'phone', proposed: '010-8888xxxx', merged: 0 },
  { id: 12, poi: '朝阳宠物公园', field: 'phone', proposed: '010-9999xxxx', merged: 11 },
]);
const hiddenReviews = ref([
  { id: 21, state: 'VISIBLE', avg: 5 },
  { id: 22, state: 'HIDDEN', avg: 5 },
]);
const routes = ref([
  { id: 31, name: '奥森环线', state: 'PENDING' },
  { id: 32, name: '亮马河夜走', state: 'PENDING' },
]);
const activities = ref([{ id: 41, title: '秋日遛宠会', state: 'CHANGED' }]);
const note = ref('');

function closePoi(id: number) {
  const p = pois.value.find((x) => x.id === id);
  if (p) p.state = 'CLOSED';
  message.value = `#${id} 已标记停业（poi.close，列表降权隐藏）`;
}

function resolveCorrection(id: number, accept: boolean) {
  const blockers = correctionResolveBlockers(accept, note.value);
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  const c = corrections.value.find((x) => x.id === id);
  corrections.value = corrections.value.filter((x) => x.id !== id && x.mergedInto !== id);
  message.value = accept
    ? `#${id} 采纳：回写 ${c?.field} 并通知贡献者（同类合并单已一并处理）`
    : `#${id} 已驳回（附理由，双向通知）`;
}

function hideReview(id: number, restore: boolean) {
  const r = hiddenReviews.value.find((x) => x.id === id);
  if (!r) return;
  const blockers = reviewHideBlockers(r.state, restore);
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  r.state = restore ? 'VISIBLE' : 'HIDDEN';
  message.value = `评价 #${id} → ${r.state}（均值已按有效样本重算 U81）`;
}

function reviewRoute(id: number, approve: boolean) {
  const route = routes.value.find((x) => x.id === id);
  if (!route) return;
  const blockers = routeReviewBlockers(approve, note.value, route.state);
  if (blockers.length > 0) {
    message.value = blockers.join(' / ');
    return;
  }
  route.state = approve ? 'PUBLISHED' : 'REJECTED';
  message.value = approve ? `路线 #${id} 已发布（先审后发）` : `路线 #${id} 已驳回：${note.value || '（需附意见）'}`;
}

function reviewActivityChange(id: number, approve: boolean) {
  const a = activities.value.find((x) => x.id === id);
  if (!a) return;
  const out = activityChangeOutcome(approve);
  a.state = out.nextState;
  message.value = `活动 #${id} 变更${approve ? '通过' : '驳回'}（${out.notify}，通知结果可跟踪）`;
}
</script>

<template>
  <div class="explore-ops">
    <section class="card">
      <h2>POI 管理（poi.create.edit / poi.close）</h2>
      <ul>
        <li v-for="p in pois" :key="p.id">
          {{ p.name }} · <span :class="['state', p.state.toLowerCase()]">{{ p.state }}</span>
          <button v-if="p.state === 'NORMAL'" type="button" class="chip danger" @click="closePoi(p.id)">标记停业</button>
        </li>
      </ul>
    </section>

    <section class="card">
      <h2>纠错工单（correction.handle · 同类合并）</h2>
      <input v-model="note" placeholder="处理说明（驳回必填 / 采纳回写值确认）" data-testid="corr-note" />
      <ul>
        <li v-for="c in corrections" :key="c.id">
          #{{ c.id }} {{ c.poi }} · {{ c.field }} → {{ c.proposed }}
          <span v-if="c.merged">（合并至 #{{ c.merged }}）</span>
          <button type="button" class="chip" data-testid="corr-accept" @click="resolveCorrection(c.id, true)">采纳</button>
          <button type="button" class="chip danger" @click="resolveCorrection(c.id, false)">驳回</button>
        </li>
      </ul>
    </section>

    <section class="card">
      <h2>评价抽审（ugv.review.hide）</h2>
      <ul>
        <li v-for="r in hiddenReviews" :key="r.id">
          评价 #{{ r.id }} · 均分 {{ r.avg }} · {{ r.state }}
          <button
            type="button"
            class="chip"
            @click="hideReview(r.id, r.state === 'HIDDEN')"
          >
            {{ r.state === 'HIDDEN' ? '恢复（重算均值）' : '隐藏（不计分）' }}
          </button>
        </li>
      </ul>
    </section>

    <section class="card">
      <h2>路线审核（route.approve）</h2>
      <ul>
        <li v-for="r in routes" :key="r.id">
          {{ r.name }} · {{ r.state }}
          <button type="button" class="chip" data-testid="route-approve" @click="reviewRoute(r.id, true)">通过</button>
          <button type="button" class="chip danger" @click="reviewRoute(r.id, false)">驳回</button>
        </li>
      </ul>
    </section>

    <section class="card">
      <h2>活动变更审核（activity.manage）</h2>
      <ul>
        <li v-for="a in activities" :key="a.id">
          {{ a.title }} · {{ a.state }}（核实中暂停新报名）
          <button type="button" class="chip" data-testid="activity-approve" @click="reviewActivityChange(a.id, true)">通过变更</button>
          <button type="button" class="chip danger" @click="reviewActivityChange(a.id, false)">驳回变更</button>
        </li>
      </ul>
    </section>

    <p v-if="message" class="msg" data-testid="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.explore-ops { display: grid; gap: 14px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
ul { margin: 0; padding-left: 16px; display: grid; gap: 8px; font-size: 13px; }
li { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
input { height: 36px; border: 1px solid #f0e6dc; border-radius: 10px; padding: 0 10px; font-size: 13px; }
.chip { border: none; background: #f7f1ea; color: #7a6e63; border-radius: 999px; padding: 4px 10px; font-size: 12px; cursor: pointer; }
.chip.danger { background: #fdecec; color: #b91c1c; }
.state { border-radius: 999px; padding: 2px 8px; font-size: 11px; font-weight: 600; }
.state.normal { background: #e7f8ef; color: #15803d; }
.state.closed { background: #fdecec; color: #b91c1c; }
.msg { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin: 0; }
</style>
