<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// T10.6 复用 #53 注销规则（Web settings 域）
import { deleteBlockers, cooldownState, COOLDOWN_DAYS, COOLDOWN_LOGIN_COPY } from '../../../web/src/domain/settings.ts';

const router = useRouter();
const phase = ref<'confirm' | 'cooldown'>('confirm');
const requestedAt = ref<string | null>(null);

const blockers = deleteBlockers({ ownedSharedPets: 1, isFamilyAdmin: false, activeSignups: 1, publishedActivities: 0 });
const cooldown = () => (requestedAt.value ? cooldownState(requestedAt.value, new Date().toISOString()) : null);

function submit() {
  if (blockers.length > 0) return;
  requestedAt.value = new Date().toISOString();
  phase.value = 'cooldown';
}

function cancel() {
  phase.value = 'confirm';
  requestedAt.value = null;
  alert('已确认撤销，账号恢复。');
}
</script>

<template>
  <div class="m-del">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>注销账号</h1>
    </header>

    <section v-if="phase === 'confirm'" class="card">
      <h2>依赖清单（#53 两维 + 活动报名）</h2>
      <ul data-testid="deps">
        <li v-for="b in blockers" :key="b.code">
          {{ b.reason }}
          <router-link :to="b.actionRoute">{{ b.actionLabel }} →</router-link>
        </li>
      </ul>
      <ul class="warn">
        <li>冷静期 {{ COOLDOWN_DAYS }} 天内可撤销</li>
        <li>已发布 UGC 匿名化保留（决议）</li>
      </ul>
      <button type="button" class="danger" :disabled="blockers.length > 0" data-testid="submit" @click="submit">
        {{ blockers.length > 0 ? '请先处理上方依赖' : '提交注销申请' }}
      </button>
    </section>

    <section v-else class="card cooldown" data-testid="cooldown">
      <h2>{{ COOLDOWN_LOGIN_COPY.title }}</h2>
      <p>{{ COOLDOWN_LOGIN_COPY.body }}</p>
      <p v-if="cooldown()?.cancellable" class="meta">剩余 {{ cooldown()!.daysLeft }} 天可撤销</p>
      <button type="button" class="primary" data-testid="cancel" @click="cancel">{{ COOLDOWN_LOGIN_COPY.confirmLabel }}</button>
    </section>
  </div>
</template>

<style scoped>
.m-del { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 17px; }
.card { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; }
.card h2 { margin: 0; font-size: 15px; }
.card ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; font-size: 13px; color: #2b2118; }
.card ul li { display: flex; gap: 8px; flex-wrap: wrap; }
.card a { color: #ff7a2f; text-decoration: none; font-weight: 600; }
.warn li { color: #7a6e63; }
.cooldown { background: #f5f3ff; }
.cooldown p { margin: 0; color: #5b4bc4; font-size: 13px; line-height: 1.7; }
.meta { color: #7a6e63; font-size: 13px; }
button { border: none; border-radius: 999px; min-height: 48px; font-weight: 700; font-size: 15px; }
.danger { background: #ef4444; color: #fff; }
.danger:disabled { opacity: 0.4; }
.primary { background: #22c55e; color: #fff; justify-self: start; }
</style>
