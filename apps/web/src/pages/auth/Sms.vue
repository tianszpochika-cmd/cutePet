<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, setAccessToken } from '@cutepet/api-client';
import { resendCountdownLeft, safeReturnPath } from '../../domain/auth';

const route = useRoute();
const router = useRouter();
const phone = String(route.query.phone ?? '');
const code = ref('');
const error = ref('');
const startedAt = ref(Date.now());
const elapsed = ref(0);

const countdown = computed(() => resendCountdownLeft(elapsed.value / 1000));

async function submit() {
  if (code.value.length !== 6) {
    error.value = '验证码为 6 位';
    return;
  }
  error.value = '';
  try {
    const res = await api.authSmsLogin({
      body: {
        phone,
        code: code.value,
        // dev 固定码由后端校验；无出生日期=ADULT；协议版本在真实注册流由登录页带入
        birthDate: undefined,
        guardianPhone: undefined,
        termsVersion: 'v1',
        privacyVersion: 'v1',
        device: 'web-dev',
      },
    });
    const data = res as unknown as { accessToken?: string };
    if (data?.accessToken) setAccessToken(data.accessToken);
    await router.replace(safeReturnPath(String(route.query.return ?? '/')));
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败';
  }
}

function resend() {
  if (countdown.value > 0) return;
  startedAt.value = Date.now();
  elapsed.value = 0;
  setInterval(() => {
    elapsed.value = Date.now() - startedAt.value;
  }, 500);
}
</script>

<template>
  <div class="sms">
    <h1>输入验证码</h1>
    <p class="phone">{{ phone }} <router-link :to="{ path: '/login', query: { return: route.query.return } }">更换</router-link></p>
    <input v-model="code" maxlength="6" inputmode="numeric" data-testid="code" placeholder="6 位验证码（dev: 123456）" />
    <p v-if="error" class="err">{{ error }}</p>
    <button type="button" data-testid="login" :disabled="code.length !== 6" @click="submit">登录</button>
    <button type="button" class="link" :disabled="countdown > 0" @click="resend">
      {{ countdown > 0 ? `${countdown}s 后重发` : '重新发送验证码' }}
    </button>
  </div>
</template>

<style scoped>
.sms {
  max-width: 380px;
  margin: 64px auto;
  padding: 24px;
  display: grid;
  gap: 12px;
}
input {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
  font-size: 18px;
  letter-spacing: 0.3em;
}
button {
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
button:disabled {
  opacity: 0.4;
}
button.link {
  background: none;
  color: #ff7a2f;
  height: 32px;
  font-weight: 400;
}
.phone {
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
