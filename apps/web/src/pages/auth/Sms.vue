<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, setAccessToken } from '@cutepet/api-client';
import {
  activeAuthConfig,
  ageBand,
  beijingToday,
  clearAuthFlow,
  gateReturnTarget,
  getAuthFlow,
  resendCountdownLeft,
} from '../../domain/auth';

const router = useRouter();
const flow = getAuthFlow();
const config = activeAuthConfig();
const code = ref('');
const error = ref('');
const state = ref<'idle' | 'sending' | 'sent' | 'demo' | 'submitting'>('idle');
const elapsed = ref(0);
const startedAt = ref(0);
let timer: number | undefined;

const adultFlow = computed(() => {
  if (!flow?.birthDate) return false;
  try {
    return ageBand(flow.birthDate, beijingToday()) === 'ADULT';
  } catch {
    return false;
  }
});
const ready = computed(() =>
  flow?.mode === 'live' &&
  !!flow.phone &&
  flow.agreedTerms &&
  flow.agreedPrivacy &&
  !!config &&
  adultFlow.value,
);
const countdown = computed(() => startedAt.value > 0 ? resendCountdownLeft(elapsed.value / 1000) : 0);
const codeReady = computed(() => /^[0-9]{6}$/.test(code.value));
const maskedPhone = computed(() => flow?.phone ? flow.phone.slice(0, 3) + '****' + flow.phone.slice(-4) : '');

function stopTimer() {
  if (timer !== undefined) window.clearInterval(timer);
  timer = undefined;
}

function startTimer() {
  stopTimer();
  startedAt.value = Date.now();
  elapsed.value = 0;
  timer = window.setInterval(() => {
    elapsed.value = Date.now() - startedAt.value;
    if (countdown.value === 0) stopTimer();
  }, 500);
}

async function sendCode() {
  if (!ready.value || !flow || state.value === 'sending' || state.value === 'submitting') return;
  if (state.value === 'sent' && countdown.value > 0) return;
  error.value = '';
  state.value = 'sending';
  try {
    const response = await api.authSmsSend({ body: { phone: flow.phone } }) as {
      sent?: boolean;
      devHint?: unknown;
    };
    if (!response?.sent) throw new Error('短信接口未确认发送');
    if ('devHint' in response) {
      state.value = 'demo';
      error.value = '当前接口只提供本地开发验证码，不能证明手机号持有人身份。已停止登录；正式短信核验待接入。';
      return;
    }
    state.value = 'sent';
    code.value = '';
    startTimer();
  } catch (e) {
    state.value = 'idle';
    error.value = e instanceof Error ? e.message : '发送失败，请稍后重试';
  }
}

async function submit() {
  if (!ready.value || !flow || !config || state.value !== 'sent') return;
  if (!codeReady.value) {
    error.value = '请输入收到的 6 位数字验证码';
    return;
  }
  error.value = '';
  state.value = 'submitting';
  try {
    const response = await api.authSmsLogin({
      body: {
        phone: flow.phone,
        code: code.value,
        birthDate: flow.birthDate,
        guardianPhone: undefined,
        termsVersion: config.termsVersion,
        privacyVersion: config.privacyVersion,
        device: 'web',
      },
    }) as { accessToken?: unknown; ageBand?: unknown };
    if (response.ageBand !== 'ADULT' || typeof response.accessToken !== 'string' || !response.accessToken) {
      throw new Error('服务端未确认可用的成人账号状态');
    }
    setAccessToken(response.accessToken);
    const target = gateReturnTarget(flow.returnPath, flow.resumeAction);
    clearAuthFlow();
    stopTimer();
    await router.replace(target);
  } catch (e) {
    state.value = 'sent';
    error.value = e instanceof Error ? e.message : '登录失败，请保留输入并重试';
  }
}

onBeforeUnmount(stopTimer);
</script>

<template>
  <div class="sms-page">
    <button class="back" type="button" @click="router.push('/login/age')">← 返回年龄步骤</button>
    <div class="card">
      <p class="eyebrow">03 / 手机核验</p>
      <h1>确认手机号码。</h1>
      <p class="lead" v-if="maskedPhone">准备发送至 {{ maskedPhone }}。</p>
      <p class="lead" v-else>请从登录入口开始，不在链接中传递手机号或生日。</p>

      <div v-if="!flow" class="notice" role="status">当前步骤已过期。刷新页面后需重新开始，手机号与生日不会被保留。</div>
      <div v-else-if="flow.mode === 'preview'" class="notice" role="status">这是验证码界面示意。不会发送短信，也不会接受本地示例验证码或创建登录态。</div>
      <div v-else-if="!adultFlow" class="notice" role="alert">未满 14 周岁或出生日期无效，不能从成人短信入口继续。请先完成监护流程。</div>
      <div v-else-if="!config" class="notice" role="status">正式协议版本和认证通道尚未启用，请返回登录入口。</div>
      <div v-else-if="state === 'demo'" class="notice" role="alert">开发验证码已被识别为演示结果；本次不会发放登录态。</div>

      <template v-if="ready">
        <button class="primary send" type="button" :disabled="state === 'sending' || state === 'submitting' || state === 'demo' || countdown > 0" @click="sendCode">
          {{ state === 'sending' ? '正在请求发送…' : countdown > 0 ? countdown + ' 秒后可重发' : state === 'sent' ? '重新发送验证码' : '发送验证码' }}
        </button>
        <form v-if="state === 'sent' || state === 'submitting'" @submit.prevent="submit">
          <label for="sms-code">6 位验证码</label>
          <input
            id="sms-code"
            v-model="code"
            type="text"
            inputmode="numeric"
            autocomplete="one-time-code"
            maxlength="6"
            placeholder="请输入短信中的数字"
            data-testid="code"
            :disabled="state === 'submitting'"
          />
          <p class="hint">验证码真假由服务端确认。这里不会显示或接受开发提示码作为真实核验。</p>
          <button class="primary" type="submit" data-testid="login" :disabled="state === 'submitting' || !codeReady">
            {{ state === 'submitting' ? '正在核验…' : '确认并继续' }}
          </button>
        </form>
      </template>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <router-link v-if="!flow || !ready" class="return-link" to="/login">返回登录入口 <span aria-hidden="true">→</span></router-link>
    </div>
  </div>
</template>

<style scoped>
.sms-page { width: min(100% - 32px, 600px); margin: 42px auto 90px; }
.back { display: inline-flex; align-items: center; min-height: 44px; margin-bottom: 15px; padding: 0 4px; border: 0; background: none; color: #9b470e; font-weight: 700; }
.card { padding: clamp(25px, 5vw, 46px); border: 1px solid #eadfd4; border-radius: 24px; background: #fff; }
.eyebrow { margin: 0 0 10px; color: #a0440b; font-size: 12px; font-weight: 800; letter-spacing: .12em; }
h1 { margin: 0; font-size: clamp(27px, 4vw, 36px); letter-spacing: -.04em; }
.lead { margin: 12px 0 24px; color: #64574d; line-height: 1.8; }
.notice { padding: 17px 19px; border-left: 4px solid #b85111; border-radius: 12px; background: #fff3e9; color: #704b37; line-height: 1.8; }
.send { width: 100%; margin: 22px 0 4px; }
form { display: grid; gap: 11px; margin-top: 22px; }
label { font-size: 14px; font-weight: 750; }
input { width: 100%; min-height: 48px; padding: 0 15px; border: 1px solid #dacbc0; border-radius: 12px; font-size: 18px; letter-spacing: .16em; }
.hint { margin: 0 0 10px; color: #7a6e63; font-size: 13px; }
.primary { min-height: 48px; padding: 0 22px; border: 0; border-radius: 999px; background: #b85111; color: #fff; font-weight: 750; }
.primary:disabled { background: #cfc8c2; cursor: not-allowed; }
.error { margin: 16px 0 0; color: #a32920; font-size: 13px; line-height: 1.7; }
.return-link { display: inline-flex; align-items: center; min-height: 44px; margin-top: 20px; color: #9b470e; font-weight: 700; }
</style>
