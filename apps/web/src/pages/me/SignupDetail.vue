<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { SIGNUP_DETAIL_FIELDS, cancelSignupConfirmCopy } from '../../domain/closedLoop';

const route = useRoute();
const router = useRouter();
const signupId = String(route.params.id);
const state = ref('ACTIVE');
const changes = ref([
  { at: '2026-10-01 14:00', what: '活动地点由 A 更新为 B（已通过核实）' },
]);

function cancelSignup() {
  if (!confirm(cancelSignupConfirmCopy('秋日遛宠会'))) return; // 取消前说明后果（X10）
  state.value = 'CANCELLED';
}
</script>

<template>
  <div class="x10">
    <header>
      <button type="button" class="back" @click="router.push('/me/activity-center')">‹ 活动中心</button>
      <h1>报名详情（X10）</h1>
    </header>

    <section class="card">
      <p class="code">查询编号：SQ-{{ signupId }}-2026</p>
      <p>秋日遛宠会 · 2026-12-01 10:00 · 朝阳公园南门</p>
      <span :class="['badge', state === 'ACTIVE' ? 'ok' : 'off']" data-testid="state">{{ state === 'ACTIVE' ? '有效' : '已取消' }}</span>
    </section>

    <section class="card">
      <h2>变更历史（{{ changes.length }}）</h2>
      <p v-for="c in changes" :key="c.at" class="change">{{ c.at }} — {{ c.what }}</p>
    </section>

    <template v-if="state === 'ACTIVE'">
      <button type="button" class="danger" data-testid="cancel" @click="cancelSignup">取消报名</button>
      <p class="hint">满员/截止后不可新报名；取消后名额立即释放。</p>
    </template>
    <p v-else class="off-copy">已取消（名额已释放，重新报名视名额而定）</p>
  </div>
</template>

<style scoped>
.x10 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 8px; }
.card h2 { margin: 0; font-size: 15px; }
.card p { margin: 0; font-size: 14px; color: #2b2118; }
.code { color: #4d8dff; font-weight: 600; font-size: 13px; }
.badge { align-self: start; border-radius: 999px; padding: 2px 10px; font-size: 12px; font-weight: 600; }
.badge.ok { background: #e7f8ef; color: #22c55e; }
.badge.off { background: #f0e6dc; color: #7a6e63; }
.change { color: #7a6e63; font-size: 13px; }
.danger { height: 48px; border: none; border-radius: 999px; background: #ef4444; color: #fff; font-weight: 600; }
.hint { color: #7a6e63; font-size: 12px; }
.off-copy { color: #7a6e63; font-size: 14px; text-align: center; }
</style>
