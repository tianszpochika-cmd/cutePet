<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { BLOCKED_ALLOWED_ACTIONS, blockedCopy, restoredCapabilities } from '../../domain/settings';

const router = useRouter();
const copy = blockedCopy();
const action = ref<string | null>(null);
const queryState = ref(false);

// #56：各处罚独立计时（演示数据；真实状态随接口）
const banExpired = ref(false);
const muteExpired = ref(false);
const restored = () =>
  restoredCapabilities({ banExpired: banExpired.value, muteExpired: muteExpired.value, postRightSuspended: true });
</script>

<template>
  <div class="blocked">
    <h1 data-testid="blocked-title">{{ copy.title }}</h1>
    <p class="note">{{ copy.note }}</p>

    <!-- X15：受限动作仅四类，不进普通个人中心 -->
    <nav class="actions">
      <button
        v-for="a in BLOCKED_ALLOWED_ACTIONS"
        :key="a"
        type="button"
        :data-testid="`action-${a}`"
        @click="action = a"
      >
        {{ a }}
      </button>
    </nav>

    <section v-if="action === '申诉'" class="panel">
      <p>提交申诉（理由必填）→ 管理端 appeal.handle 处理 → 结果经本页与消息反馈（≤48h）。</p>
      <textarea rows="3" placeholder="说明申诉理由…" />
      <button type="button" class="primary" @click="alert('申诉已提交，可在此页查询进度（无需恢复账号）')">
        提交申诉
      </button>
    </section>

    <section v-else-if="action === '状态查询'" class="panel" data-testid="status">
      <p>处罚状态（各处罚独立计时，期满仅恢复对应能力 —— #56）：</p>
      <label><input v-model="banExpired" type="checkbox" /> 封禁已到期</label>
      <label><input v-model="muteExpired" type="checkbox" /> 禁言已到期</label>
      <p class="restored">已恢复能力：{{ restored().length > 0 ? restored().join('、') : '暂无（仍有生效处罚）' }}</p>
      <p v-if="queryState" class="meta">状态：查询完成</p>
    </section>

    <section v-else-if="action === '查看规则'" class="panel">
      <button type="button" class="link" @click="router.push('/legal/community')">查看《社区规范》处置阶梯 →</button>
    </section>

    <section v-else class="panel">
      <p>提交信息请求（更正/删除/复制个人信息）→ 工单流转，结果在本页可查。</p>
      <button type="button" class="primary" @click="alert('信息请求已提交')">提交</button>
    </section>
  </div>
</template>

<style scoped>
.blocked { max-width: 560px; margin: 48px auto; padding: 16px; display: grid; gap: 14px; text-align: center; }
.note { background: #fdecec; color: #b91c1c; border-radius: 12px; padding: 12px; font-size: 13px; line-height: 1.7; }
.actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.actions button { height: 48px; border: none; border-radius: 12px; background: #fff; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 14px; color: #2b2118; }
.panel { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 16px; display: grid; gap: 10px; text-align: left; font-size: 14px; color: #2b2118; }
label { display: flex; gap: 8px; align-items: center; font-size: 14px; }
.primary { height: 44px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.link { background: none; border: none; color: #ff7a2f; font-size: 14px; text-align: left; }
.restored { color: #22c55e; font-weight: 600; }
.meta { color: #7a6e63; font-size: 12px; }
textarea { border: 1px solid #f0e6dc; border-radius: 12px; padding: 10px; font-family: inherit; }
</style>
