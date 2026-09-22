<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { notificationWarnings } from '../../domain/settings';

const router = useRouter();
const state = reactive({ reminderPush: true, interactionPush: true, 免打扰: false });
const warnings = () => notificationWarnings(state);
</script>

<template>
  <div class="notif">
    <header>
      <button type="button" class="back" @click="router.push('/settings')">‹ 设置</button>
      <h1>通知设置</h1>
    </header>

    <ul class="list">
      <li><span>提醒推送（疫苗/驱虫）</span><input v-model="state.reminderPush" type="checkbox" data-testid="reminder-push" /></li>
      <li><span>互动通知（赞/评/关注）</span><input v-model="state.interactionPush" type="checkbox" /></li>
      <li><span>免打扰时段（23:00–08:00）</span><input v-model="state.免打扰" type="checkbox" data-testid="dnd" /></li>
    </ul>

    <p v-for="(w, i) in warnings()" :key="i" class="warn" data-testid="warn">{{ w }}</p>
    <p class="meta">#34：推送通道未就绪时，提醒明确显示「仅站内提醒」；免打扰时段内提醒顺延送达。</p>
  </div>
</template>

<style scoped>
.notif { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; }
.list li { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.warn { background: #fdecec; color: #b91c1c; border-radius: 12px; padding: 10px 12px; font-size: 13px; margin: 0; }
.meta { color: #7a6e63; font-size: 12px; }
</style>
