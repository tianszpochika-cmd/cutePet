<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { ADMIN_TRANSFER_SEPARATION, transferStateV7 } from '../../domain/closedLoop';

const router = useRouter();
const target = ref(2);
const sentAt = ref<string | null>(null);
const exited = ref(false);
const error = ref('');

const invalidated = ref(false);
function onExitConflict() {
  exited.value = true;
  invalidated.value = sentAt.value !== null; // X05：退出冲突使申请失效
}

async function send() {
  error.value = '';
  try {
    await api.familyTransferAdmin({ body: { kind: 'FAMILY_ADMIN', toUserId: target.value, refId: 1 } });
    sentAt.value = new Date().toISOString();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发送失败';
  }
}
</script>

<template>
  <div class="x05">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>转交家庭管理员（X05）</h1>
    </header>

    <p class="sep">{{ ADMIN_TRANSFER_SEPARATION }}</p>

    <label class="field">
      选择成员
      <select v-model="target">
        <option :value="2">成员 #2</option>
        <option :value="3">成员 #3</option>
      </select>
    </label>

    <p v-if="error" class="err">{{ error }}</p>

    <template v-if="sentAt && !invalidated">
      <p class="pending" data-testid="admin-pending">
        待双方确认 · 状态：{{ transferStateV7(sentAt, new Date().toISOString(), false) }} ·
        待接受期间原管理员仍有效
      </p>
      <div class="actions">
        <button type="button" class="ghost" @click="alert('接收方确认完成（双方确认闭环）')">模拟接收方确认</button>
        <button type="button" class="ghost" @click="onExitConflict()">模拟接收方退出</button>
      </div>
    </template>

    <p v-if="invalidated" class="invalid" data-testid="invalidated">
      接收方已退出家庭 —— 该转交申请已失效，原管理员身份不变。
    </p>

    <button v-if="!sentAt" type="button" class="primary" data-testid="send" @click="send">发送转交申请</button>
  </div>
</template>

<style scoped>
.x05 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.sep { background: #f5f3ff; color: #6d5bd0; border-radius: 12px; padding: 12px; font-size: 13px; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
select { height: 44px; border-radius: 12px; border: 1px solid #f0e6dc; padding: 0 12px; }
.primary { height: 48px; border: none; border-radius: 999px; background: #9b8cff; color: #fff; font-weight: 600; }
.ghost { height: 40px; padding: 0 14px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.actions { display: flex; gap: 8px; }
.pending { background: #fff1e8; color: #b45309; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.invalid { background: #fdecec; color: #b91c1c; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.err { color: #ef4444; font-size: 13px; }
</style>
