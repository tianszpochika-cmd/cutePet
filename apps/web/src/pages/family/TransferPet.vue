<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import {
  OWNERSHIP_TRANSFER_IMPACTS,
  transferStateV7,
  TRANSFER_TTL_HOURS,
} from '../../domain/closedLoop';

const router = useRouter();
const receiver = ref(2);
const confirmed = ref(false);
const sent = ref<{ createdAt: string; state: string } | null>(null);
const error = ref('');

const stateCopy = computed(() =>
  sent.value ? transferStateV7(sent.value.createdAt, new Date().toISOString(), false) : null,
);

async function send() {
  if (!confirmed.value) return;
  error.value = '';
  try {
    await api.familyTransferOwner({ body: { kind: 'PET_OWNER', toUserId: receiver.value, refId: 10 } });
    sent.value = { createdAt: new Date().toISOString(), state: 'PENDING' };
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发送失败（失败不变更原所有权）';
  }
}
</script>

<template>
  <div class="x04">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>转移宠物所有权（X04）</h1>
    </header>

    <section class="danger-zone">
      <h2>危险区操作</h2>
      <label class="field">
        接收者（须为同家庭成员）
        <select v-model="receiver">
          <option :value="2">成员 #2（豆豆爸）</option>
          <option :value="3">成员 #3</option>
        </select>
      </label>
    </section>

    <section class="card">
      <h2>影响清单</h2>
      <ul>
        <li v-for="i in OWNERSHIP_TRANSFER_IMPACTS" :key="i">{{ i }}</li>
      </ul>
    </section>

    <label class="confirm">
      <input v-model="confirmed" type="checkbox" data-testid="impact-ack" />
      我已阅读影响清单，确认发送转移申请（{{ TRANSFER_TTL_HOURS }}h 内可撤销）
    </label>

    <p v-if="error" class="err">{{ error }}</p>

    <template v-if="sent">
      <p class="pending" data-testid="pending">
        转移申请已发送 · 状态：{{ stateCopy }} · 接收方确认后：共享重置为只读、所有权变更生效
      </p>
      <button type="button" class="ghost" @click="alert('已撤销转移（失败/撤销均不变更原所有权）')">
        撤销转移
      </button>
    </template>
    <button v-else type="button" class="danger" :disabled="!confirmed" data-testid="send" @click="send">
      发送转移申请
    </button>
  </div>
</template>

<style scoped>
.x04 { max-width: 560px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.danger-zone { background: #fdecec; border-radius: 16px; padding: 16px; display: grid; gap: 10px; }
.danger-zone h2 { margin: 0; font-size: 15px; color: #b91c1c; }
.card { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; }
.card h2 { margin: 0 0 8px; font-size: 15px; }
.card ul { margin: 0; padding-left: 18px; display: grid; gap: 6px; font-size: 13px; color: #2b2118; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
select { height: 44px; border-radius: 12px; border: 1px solid #f0e6dc; padding: 0 12px; }
.confirm { display: flex; gap: 8px; font-size: 13px; color: #7a6e63; align-items: center; }
.danger { height: 48px; border: none; border-radius: 999px; background: #ef4444; color: #fff; font-weight: 600; }
.danger:disabled { opacity: 0.4; }
.ghost { height: 44px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.pending { background: #fff1e8; color: #b45309; border-radius: 8px; padding: 10px 12px; font-size: 13px; }
.err { color: #ef4444; font-size: 13px; }
</style>
