<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

const router = useRouter();
const tab = ref<'我的报名' | '我发布的' | '机构认证'>('我的报名');
const signups = ref<{ id: number; activityId: number; state: string; consentVersion: string }[]>([]);
const published = ref<{ id: number; title: string; state: string }[]>([]);
const credState = ref('NONE');
const error = ref('');

async function load() {
  error.value = '';
  try {
    signups.value = [];
    published.value = [];
    // 明细接口随本地阶段接通；此处保证入口与状态结构完整（X08）
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败';
  }
}
void load;

const CRED_COPY: Record<string, string> = {
  NONE: '未申请 —— 提交材料后进入待核验（材料仅本人可见，不公开附件）',
  APPLY: '待核验',
  APPROVED: '已认证（可直发活动；领养仍统一人工审）',
  REJECTED: '已驳回 —— 修改后可重新申请',
  EXPIRED: '已到期 —— 请续期',
  REVOKED: '已被撤销 —— 可申诉',
};
</script>

<template>
  <div class="ac">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>活动中心</h1>
    </header>

    <nav class="tabs">
      <button
        v-for="t in ['我的报名', '我发布的', '机构认证'] as const"
        :key="t"
        type="button"
        :class="{ on: tab === t }"
        :data-testid="`tab-${t}`"
        @click="tab = t"
      >
        {{ t }}
      </button>
    </nav>

    <p v-if="error" class="err">{{ error }}</p>

    <!-- X08：无活动→发布入口；无报名→浏览活动 -->
    <div v-if="tab === '我的报名'">
      <p v-if="signups.length === 0" class="empty" data-testid="empty-signups">
        还没有报名 ——
        <button type="button" class="link" @click="router.push('/events')">去浏览活动</button>
      </p>
      <ul v-else class="list">
        <li v-for="s in signups" :key="s.id" class="row">
          活动 #{{ s.activityId }} · {{ s.state }} · 同意凭证 {{ s.consentVersion }}
          <button
            type="button"
            class="link"
            @click="alert('报名详情：查询编号/当前时间地点/变更历史/取消（取消前说明后果，X10）')"
          >
            详情与取消
          </button>
        </li>
      </ul>
    </div>

    <div v-else-if="tab === '我发布的'">
      <div class="create">
        <button type="button" class="link" @click="router.push('/events')">发布活动（X09：90天窗/名额/机构直发校验）</button>
        <button type="button" class="link" @click="router.push('/events')">发布领养（统一人工审）</button>
      </div>
      <p v-if="published.length === 0" class="empty">你发布的活动会显示在这里（含编辑/截止/取消/报名名单 X11）</p>
    </div>

    <div v-else class="cred" data-testid="cred">
      <p class="state">{{ CRED_COPY[credState] }}</p>
      <p class="meta">状态流转：申请 → 待核验 → 已认证 / 驳回修改 → 到期续期 / 撤销（X12）</p>
      <button
        type="button"
        class="primary"
        @click="alert('申请材料上传（附件仅本人与审核可见，不公开）→ 待核验')"
      >
        提交认证材料
      </button>
    </div>
  </div>
</template>

<style scoped>
.ac { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.tabs { display: flex; gap: 6px; }
.tabs button { height: 32px; padding: 0 14px; border: none; border-radius: 999px; background: #f7f1ea; color: #7a6e63; }
.tabs button.on { background: #ff7a2f; color: #fff; font-weight: 600; }
.list { list-style: none; padding: 0; display: grid; gap: 8px; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 12px 14px; font-size: 14px; display: grid; gap: 6px; }
.create { display: grid; gap: 8px; }
.empty { color: #7a6e63; font-size: 14px; }
.link { background: none; border: none; color: #ff7a2f; font-size: 14px; }
.cred { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px; display: grid; gap: 10px; }
.state { font-weight: 600; }
.meta { color: #7a6e63; font-size: 13px; }
.primary { height: 44px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.err { color: #ef4444; font-size: 13px; }
</style>
