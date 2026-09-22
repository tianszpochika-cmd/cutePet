<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { signupBlockers, signupDenyCopyFallback, CONSENT_TEXT } from '../../domain/exploreSignupPage';

const router = useRouter();

interface Activity {
  id: number;
  title: string;
  city: string;
  type: string;
  beginsAt: string;
  quota: number;
  state: string;
}

const items = ref<Activity[]>([]);
const error = ref('');
const tab = ref<'活动' | '领养'>('活动');
const consentOpen = ref<number | null>(null);
const consentChecked = ref(false);
const message = ref('');

const loggedIn = ref(false); // 会话态接线随本地阶段

onMounted(async () => {
  try {
    items.value = (await api.activitiesList()) as unknown as Activity[];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 explore-service）';
  }
});

function beginSignup(a: Activity) {
  const deny = signupBlockers({ loggedIn: loggedIn.value, state: a.state, beginsAt: a.beginsAt });
  if (deny) {
    message.value = signupDenyCopyFallback(deny);
    return;
  }
  consentChecked.value = false;
  consentOpen.value = a.id;
}

async function confirmSignup() {
  const a = items.value.find((x) => x.id === consentOpen.value);
  if (!a || !consentChecked.value) return;
  try {
    await api.activitySignup({
      path: { id: String(a.id) },
      body: {
        name: '我',
        phone: '13800000000',
        consentVersion: 'v1-2026',
        consentAt: new Date().toISOString(),
      },
    });
    message.value = '报名成功（同意凭证已记录），凭证见消息中心。';
    consentOpen.value = null;
  } catch (e) {
    message.value = e instanceof Error ? e.message : '报名失败';
  }
}

const activeTab = computed(() => tab.value);
void activeTab;
</script>

<template>
  <div class="events">
    <header>
      <h1>本地活动</h1>
      <nav class="tabs">
        <button type="button" :class="{ on: tab === '活动' }" @click="tab = '活动'">活动</button>
        <button type="button" :class="{ on: tab === '领养' }" data-testid="adoption-tab" @click="tab = '领养'">
          领养
        </button>
      </nav>
    </header>

    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="message" class="msg" data-testid="signup-msg">{{ message }}</p>

    <!-- 活动列表 -->
    <ul v-if="tab === '活动'" class="list">
      <li v-for="a in items" :key="a.id" class="card">
        <div class="date">{{ a.beginsAt.slice(5, 10) }}</div>
        <div>
          <strong>{{ a.title }}</strong>
          <p class="meta">{{ a.city }} · {{ a.type }} · 报名至开始前（90 天窗口规则）</p>
        </div>
        <button type="button" class="primary" :data-testid="`signup-${a.id}`" @click="beginSignup(a)">
          立即报名
        </button>
      </li>
      <li v-if="items.length === 0 && !error" class="muted">暂无已发布活动</li>
    </ul>

    <!-- 领养（U90：无报名/交易按钮） -->
    <div v-else class="adoption">
      <p class="notice" data-testid="adoption-notice">
        ⚠️ 领养信息仅作展示与联系——<strong>无报名、无交易按钮</strong>；请核验检疫证明、防范诈骗，建议签订领养协议。
      </p>
      <p class="muted">领养列表随接口填充；过 30 天未确认自动停止公开（U90）。发现售卖信息请走举报。</p>
      <button
        type="button"
        class="ghost"
        @click="alert('举报面板：SELLING_SUSPECTED → 处置可追溯（U90）')"
      >
        🚩 举报售卖嫌疑
      </button>
    </div>

    <!-- 报名：先弹告知同意（合规 B4） -->
    <div v-if="consentOpen !== null" class="mask" @click.self="consentOpen = null">
      <div class="dialog" data-testid="consent-card">
        <h3>报名信息告知同意</h3>
        <p>{{ CONSENT_TEXT }}</p>
        <label class="agree">
          <input v-model="consentChecked" type="checkbox" data-testid="consent-check" />
          我已阅读并同意
        </label>
        <div class="actions">
          <button type="button" class="ghost" @click="consentOpen = null">取消</button>
          <button
            type="button"
            class="primary"
            :disabled="!consentChecked"
            data-testid="confirm-signup"
            @click="confirmSignup"
          >
            确认报名
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.events {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
}
header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.tabs {
  display: flex;
  gap: 4px;
  background: #f7f1ea;
  border-radius: 999px;
  padding: 4px;
}
.tabs button {
  border: none;
  background: none;
  height: 30px;
  padding: 0 14px;
  border-radius: 999px;
  color: #7a6e63;
}
.tabs button.on {
  background: #fff;
  color: #ff7a2f;
  font-weight: 600;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
  margin-top: 12px;
}
.card {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 14px 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.date {
  background: #fff1e8;
  color: #ff7a2f;
  font-weight: 700;
  border-radius: 12px;
  padding: 8px 10px;
  font-size: 14px;
}
.primary {
  margin-left: auto;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.primary:disabled {
  opacity: 0.4;
}
.meta {
  color: #7a6e63;
  font-size: 13px;
}
.notice {
  background: #fdecec;
  color: #b91c1c;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.7;
}
.muted {
  color: #7a6e63;
  font-size: 14px;
}
.msg {
  color: #22c55e;
  font-size: 13px;
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(43, 33, 24, 0.45);
  display: grid;
  place-items: center;
  z-index: 50;
}
.dialog {
  width: min(420px, calc(100vw - 48px));
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  display: grid;
  gap: 12px;
}
.agree {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 14px;
}
.actions {
  display: flex;
  gap: 12px;
}
.actions button {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 999px;
  font-weight: 600;
}
.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.actions .primary:disabled {
  opacity: 0.4;
}
.ghost:not(.actions .ghost) {
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  background: #fff;
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
