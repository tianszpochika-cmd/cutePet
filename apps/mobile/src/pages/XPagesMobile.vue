<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// T10.6 复用 T7.11（同一 closedLoop）
import {
  visibleShares,
  OWNER_VERIFY_COPY,
  OWNERSHIP_TRANSFER_IMPACTS,
  recycleRemainingDays,
  restoreAllowed,
  RESTORE_GUIDANCE,
  type ShareMemberView,
} from '../../../web/src/domain/closedLoop.ts';

const router = useRouter();

// X03 共享授权（只读视角演示）
const views = ref<ShareMemberView[]>([
  { userId: 1, petId: 10, share: 'MANAGE' },
  { userId: 2, petId: 10, share: 'READONLY' },
]);
const visible = () => visibleShares(views.value, 2, false); // 成员 #2 只见自己

// X04 转移影响清单（发送前确认）
const impacts = ref(OWNERSHIP_TRANSFER_IMPACTS);

// X06 回收站
const deletedAt = new Date(Date.now() - 3 * 86400000).toISOString();
const daysLeft = recycleRemainingDays(deletedAt, new Date().toISOString());
const guidance = ref<string[]>([]);
function restore() {
  if (!restoreAllowed(true)) return;
  guidance.value = [...RESTORE_GUIDANCE]; // 引导不自动执行
}
</script>

<template>
  <div class="m-x-pages">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>闭环操作（X03/X04/X06 · 复用 T7.11）</h1>
    </header>

    <section class="card" data-testid="x03">
      <h2>X03 共享授权</h2>
      <p class="note">{{ OWNER_VERIFY_COPY }}</p>
      <p class="meta">成员视角（仅见自己）：</p>
      <ul>
        <li v-for="v in visible()" :key="v.userId">成员 #{{ v.userId }} · {{ v.share === 'MANAGE' ? '可管理' : '只读' }}</li>
      </ul>
    </section>

    <section class="card" data-testid="x04">
      <h2>X04 转移所有权（24h）</h2>
      <ul><li v-for="i in impacts" :key="i">{{ i }}</li></ul>
      <button type="button" class="danger" @click="alert('已发送转移申请（失败/撤销不变更原所有权）')">发送申请</button>
    </section>

    <section class="card" data-testid="x06">
      <h2>X06 回收站</h2>
      <p class="meta">小灰（示例）· 剩余恢复 {{ daysLeft }} 天 · 仅所有者可恢复</p>
      <button v-if="guidance.length === 0" type="button" class="primary" data-testid="restore" @click="restore">恢复</button>
      <template v-else>
        <p class="guide">恢复成功（未自动执行）：</p>
        <ul><li v-for="g in guidance" :key="g">{{ g }}</li></ul>
      </template>
    </section>
  </div>
</template>

<style scoped>
.m-x-pages { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 16px; }
.card { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; color: #ff7a2f; }
.card ul { margin: 0; padding-left: 18px; display: grid; gap: 6px; font-size: 13px; color: #2b2118; }
.note { background: #f5f3ff; color: #6d5bd0; border-radius: 10px; padding: 10px 12px; font-size: 12px; margin: 0; line-height: 1.7; }
.meta { color: #7a6e63; font-size: 13px; margin: 0; }
.guide { color: #15803d; font-size: 13px; margin: 0; font-weight: 600; }
button { border: none; border-radius: 999px; min-height: 44px; font-weight: 600; font-size: 14px; padding: 0 18px; }
.primary { background: #22c55e; color: #fff; justify-self: start; }
.danger { background: #fdecec; color: #b91c1c; justify-self: start; }
</style>
