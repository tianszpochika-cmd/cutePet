<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { deleteBlockers, cooldownState, COOLDOWN_DAYS, COOLDOWN_LOGIN_COPY } from '../../domain/settings';

const router = useRouter();

// #53：两维依赖 + 活动与报名（数值随接口；此处为状态结构演示）
const deps = { ownedSharedPets: 0, isFamilyAdmin: false, activeSignups: 0, publishedActivities: 0 };
const blockers = deleteBlockers(deps);

const phase = ref<'confirm' | 'cooldown'>('confirm');
const requestedAt = ref<string | null>(null);
const error = ref('');

async function submit() {
  if (blockers.length > 0) return; // 先处理依赖
  error.value = '';
  try {
    const res = (await api.meDelete()) as unknown as { expireAt?: string };
    requestedAt.value = new Date().toISOString();
    phase.value = 'cooldown';
    void res;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '申请失败（未登录会进入登录闸门）';
  }
}

function cancelDeletion() {
  // #53：冷静期登录 = 明确撤销确认（点击才撤销）
  void api.meCancelDelete().then(() => {
    phase.value = 'confirm';
    requestedAt.value = null;
    alert('已确认撤销，账号恢复。');
  });
}

const cooldown = () =>
  requestedAt.value ? cooldownState(requestedAt.value, new Date().toISOString()) : null;
</script>

<template>
  <div class="del">
    <header>
      <button type="button" class="back" @click="router.push('/settings')">‹ 设置</button>
      <h1>注销账号</h1>
    </header>

    <!-- 依赖清单（#53 两维 + 活动报名，给实际入口） -->
    <section v-if="blockers.length > 0" class="deps" data-testid="deps">
      <h2>注销前需要先处理（{{ blockers.length }}）</h2>
      <ul>
        <li v-for="b in blockers" :key="b.code">
          <span>{{ b.reason }}</span>
          <button type="button" class="link" @click="router.push(b.actionRoute)">{{ b.actionLabel }} →</button>
        </li>
      </ul>
    </section>

    <!-- 冷静期：明确撤销确认页 -->
    <section v-if="phase === 'cooldown'" class="cooldown" data-testid="cooldown">
      <h2>{{ COOLDOWN_LOGIN_COPY.title }}</h2>
      <p>{{ COOLDOWN_LOGIN_COPY.body }}</p>
      <p v-if="cooldown()?.cancellable" class="meta" data-testid="days-left">
        剩余 {{ cooldown()!.daysLeft }} 天可撤销
      </p>
      <div class="actions">
        <button type="button" class="primary" data-testid="cancel-deletion" @click="cancelDeletion">
          {{ COOLDOWN_LOGIN_COPY.confirmLabel }}
        </button>
        <button type="button" class="ghost" disabled>{{ COOLDOWN_LOGIN_COPY.keepLabel }}（冷静期结束后生效）</button>
      </div>
    </section>

    <template v-else>
      <ul class="warn-list">
        <li>将失去：宠物档案、家庭关系（作为所有者/管理员需先处理）、投稿与报名记录</li>
        <li>冷静期 {{ COOLDOWN_DAYS }} 天，期内随时可撤销</li>
        <li>已发布 UGC 将匿名化保留（决议：不删除）</li>
      </ul>
      <p v-if="error" class="err">{{ error }}</p>
      <button
        type="button"
        class="danger"
        :disabled="blockers.length > 0"
        data-testid="submit-delete"
        @click="submit"
      >
        {{ blockers.length > 0 ? '请先处理上方依赖' : '提交注销申请' }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.del { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.deps { background: #fdecec; border-radius: 16px; padding: 16px; display: grid; gap: 10px; }
.deps h2 { font-size: 15px; color: #b91c1c; margin: 0; }
.deps ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; font-size: 14px; }
.link { background: none; border: none; color: #ff7a2f; font-size: 13px; padding: 0; }
.cooldown { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px; display: grid; gap: 10px; }
.cooldown h2 { margin: 0; font-size: 16px; }
.cooldown p { margin: 0; font-size: 14px; color: #7a6e63; line-height: 1.7; }
.actions { display: flex; gap: 10px; flex-wrap: wrap; }
.warn-list { padding-left: 18px; display: grid; gap: 8px; font-size: 14px; color: #2b2118; }
.primary { height: 44px; padding: 0 20px; border: none; border-radius: 999px; background: #22c55e; color: #fff; font-weight: 600; }
.ghost { height: 44px; padding: 0 16px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; }
.danger { height: 48px; border: none; border-radius: 999px; background: #ef4444; color: #fff; font-weight: 600; }
.danger:disabled { opacity: 0.4; }
.meta { color: #7a6e63; font-size: 13px; }
.err { color: #ef4444; font-size: 13px; }
</style>
