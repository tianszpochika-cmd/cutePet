<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { onlyOwnActivities, maskName, SIGNUP_VIEW_AUDIT, cancelActivityNotifyProgress } from '../../domain/closedLoop';

const route = useRoute();
const router = useRouter();
const activityId = String(route.params.id);
const actorId = ref(1);

// X11：列表只见本人活动
const all = [
  { id: Number(activityId), orgUserId: 1, title: '秋日遛宠会', quota: 20 },
  { id: 999, orgUserId: 2, title: '别人的活动', quota: 5 },
];
const own = () => onlyOwnActivities(all, actorId.value);

const showRoster = ref(false);
const rosterViewedAt = ref<string | null>(null); // 查看留痕
const notified = ref('');
const canceled = ref(false);

function viewRoster() {
  showRoster.value = true;
  rosterViewedAt.value = new Date().toLocaleTimeString(); // U72：查看留痕
}

function cancelActivity() {
  if (!confirm('取消活动后：有效报名将转为「活动取消」并通知参与者；普通编辑不可恢复该活动。确认取消？')) return;
  canceled.value = true;
  notified.value = cancelActivityNotifyProgress(1); // 1 条失败进接手队列
}

const roster = [
  { name: '王小明', phone: '138****5678' },
  { name: '李四', phone: '139****1234' },
];
void maskName;
</script>

<template>
  <div class="x11">
    <header>
      <button type="button" class="back" @click="router.push('/me/activity-center')">‹ 活动中心</button>
      <h1>活动管理与名单（X11）</h1>
    </header>

    <section class="card">
      <h2>{{ own()[0]?.title ?? '（不在你的活动中）' }}</h2>
      <div class="actions">
        <button type="button" class="ghost" @click="alert('编辑时间地点：有报名时进入核实中（U70）')">编辑</button>
        <button type="button" class="ghost" @click="viewRoster" data-testid="view-roster">报名名单</button>
        <button type="button" class="ghost" @click="alert('导出按钮仅 activity.export 权限可见——普通组织者无导出（U72）')">
          导出（需权限）
        </button>
        <button v-if="!canceled" type="button" class="danger" data-testid="cancel-activity" @click="cancelActivity">
          取消活动
        </button>
      </div>
      <p v-if="canceled" class="ok" data-testid="cancel-result">{{ notified }}</p>
    </section>

    <section v-if="showRoster" class="card" data-testid="roster">
      <h2>报名名单（实名字段默认脱敏）</h2>
      <p v-if="rosterViewedAt" class="audit">查看留痕：{{ rosterViewedAt }} · 操作者 #{{ actorId }} · 范围=本活动名单</p>
      <ul>
        <li v-for="r in roster" :key="r.phone">{{ r.name }} · {{ r.phone }}</li>
      </ul>
      <p class="hint">{{ SIGNUP_VIEW_AUDIT }}</p>
    </section>

    <p v-if="own().length === 0" class="muted">（切换 actorId 可验证「仅本人活动」过滤）</p>
  </div>
</template>

<style scoped>
.x11 { max-width: 640px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 16px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.ghost { height: 36px; padding: 0 14px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; }
.danger { height: 36px; padding: 0 14px; border: none; border-radius: 999px; background: #fdecec; color: #b91c1c; font-size: 13px; }
.audit { background: #eaf1ff; color: #2563eb; border-radius: 8px; padding: 8px 10px; font-size: 12px; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 6px; font-size: 14px; color: #2b2118; }
.hint { color: #7a6e63; font-size: 12px; }
.ok { color: #22c55e; font-size: 13px; }
.muted { color: #7a6e63; font-size: 13px; }
</style>
