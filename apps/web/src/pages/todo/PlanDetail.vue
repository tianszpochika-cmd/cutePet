<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { PLAN_DETAIL_RULES, resumeNeedsFutureConfirm } from '../../domain/closedLoop';

const router = useRouter();
const paused = ref(false);
const resumeDate = ref('2026-10-01');
const confirmInfo = ref<{ required: boolean; copy: string } | null>(null);
const today = new Date().toISOString().slice(0, 10);

function checkResume() {
  confirmInfo.value = resumeNeedsFutureConfirm(resumeDate.value, today);
}
</script>

<template>
  <div class="x02">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>计划详情（X02）</h1>
    </header>

    <section class="card">
      <h2>本次待办</h2>
      <p>疫苗接种提醒 · 每 180 天 · 基准：计划日（plan）</p>
      <div class="actions">
        <button type="button" class="primary" @click="router.push('/todos/1/confirm')">去完成</button>
        <button type="button" class="ghost" @click="router.push('/todos/1/confirm')">跳过（填原因）</button>
      </div>
    </section>

    <section class="card">
      <h2>计划管理</h2>
      <p class="rule">{{ PLAN_DETAIL_RULES.editScope }}</p>
      <p class="rule">{{ PLAN_DETAIL_RULES.pauseVsSkip }}</p>
      <div class="actions">
        <button type="button" class="ghost" @click="paused = !paused">
          {{ paused ? '已暂停（点击恢复）' : '暂停计划' }}
        </button>
        <button type="button" class="ghost">编辑周期（仅影响未完成）</button>
      </div>
    </section>

    <section v-if="paused" class="card">
      <h2>恢复计划</h2>
      <label class="field">新的下期时间 <input v-model="resumeDate" type="date" data-testid="resume-date" /></label>
      <button type="button" class="ghost" @click="checkResume">校验恢复时间</button>
      <p v-if="confirmInfo" class="hint" data-testid="resume-confirm">{{ confirmInfo.copy }}</p>
    </section>

    <section class="card">
      <h2>历史执行</h2>
      <p class="muted">2026-03-21 已完成（处理人：妈妈）· 2025-09-22 已完成 · 2025-03-21 已跳过（原因：出差）</p>
    </section>
  </div>
</template>

<style scoped>
.x02 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
.card p { margin: 0; font-size: 14px; color: #2b2118; }
.rule { color: #7a6e63 !important; font-size: 13px !important; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.primary { height: 40px; padding: 0 18px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.ghost { height: 40px; padding: 0 14px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input { height: 44px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 12px; }
.hint { background: #fff1e8; color: #b45309; border-radius: 8px; padding: 8px 12px; font-size: 13px; }
.muted { color: #7a6e63; font-size: 13px; line-height: 1.8; }
</style>
