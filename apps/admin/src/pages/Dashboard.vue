<script setup lang="ts">
import { computed, ref } from 'vue';
import { dashboardWidgets, slaStatus } from '../domain/workbench';

const granted = ref<string[]>([
  'dashboard.view.all',
  'review.article',
  'product.create.edit',
  'poi.create.edit',
  'report.handle',
  'user.ban',
]);
const widgets = computed(() => dashboardWidgets(granted.value));

const metrics = ref({
  openReports: 12,
  oldestWaitHours: 30,
  backlog: 62,
  reportOnTime: 92.5,
  dau: 1280,
  weeklyActive: 5400,
  buildComplete: 312,
  reminderCompletion: 71.4,
  queueP50: 6.5,
  queueP90: 28,
});

const sla = computed(() => slaStatus(metrics.value.oldestWaitHours));
const backlogAlert = computed(() => metrics.value.backlog > 50);

const domainCards: Record<string, { label: string; value: string }[]> = {
  内容域: [
    { label: '审核队列 P50/P90(h)', value: `${metrics.value.queueP50} / ${metrics.value.queueP90}` },
    { label: '投稿通过率', value: '90%' },
  ],
  导购域: [
    { label: '商品浏览/收藏', value: '8.2k / 640' },
    { label: '参考价巡检', value: '本月已完成' },
  ],
  探索域: [
    { label: 'POI 覆盖', value: '200 / 5 城' },
    { label: '纠错平均处理时长', value: '5.4h' },
  ],
  治理域: [
    { label: '未结举报', value: String(metrics.value.openReports) },
    { label: '按时结案率', value: `${metrics.value.reportOnTime}%` },
  ],
  留存域: [
    { label: 'DAU / WAU', value: `${metrics.value.dau} / ${metrics.value.weeklyActive}` },
    { label: '建档完成 / 提醒完成率', value: `${metrics.value.buildComplete} / ${metrics.value.reminderCompletion}%` },
  ],
};
</script>

<template>
  <div class="dash">
    <div :class="['alert', sla.toLowerCase()]" data-testid="sla">
      最久未处理举报等待 {{ metrics.oldestWaitHours }}h · SLA：{{ sla }}
      <span v-if="backlogAlert" class="backlog-alert" data-testid="backlog">积压 {{ metrics.backlog }} ＞50 告警</span>
    </div>

    <section class="grid">
      <article v-for="w in widgets" :key="w" class="card" :data-testid="`widget-${w}`">
        <h3>{{ w }}</h3>
        <ul>
          <li v-for="c in domainCards[w]" :key="c.label">
            <span>{{ c.label }}</span>
            <strong>{{ c.value }}</strong>
          </li>
        </ul>
      </article>
    </section>

    <p class="meta">
      口径：零分母显示「—」不产生 0% 假象；跨日按自然日聚合；P50/P90 随跨服务聚合接入（本地阶段）。
      各运营仅见自己权限域的卡片（§6.1）。
    </p>
  </div>
</template>

<style scoped>
.dash { display: grid; gap: 14px; }
.alert { background: #fff1e8; color: #b45309; border-radius: 12px; padding: 12px 16px; font-size: 14px; display: flex; gap: 12px; align-items: center; }
.alert.escalate { background: #fdecec; color: #7c2d12; outline: 1px solid #ef4444; }
.backlog-alert { margin-left: auto; background: #ef4444; color: #fff; border-radius: 999px; padding: 2px 12px; font-size: 12px; font-weight: 600; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; }
.card h3 { margin: 0 0 10px; font-size: 14px; color: #7a6e63; }
ul { margin: 0; padding: 0; list-style: none; display: grid; gap: 8px; }
li { display: flex; justify-content: space-between; font-size: 13px; color: #2b2118; }
li strong { color: #ff7a2f; }
.meta { margin: 0; color: #7a6e63; font-size: 12px; line-height: 1.7; }
</style>
