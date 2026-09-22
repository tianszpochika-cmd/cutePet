<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { loginBlockersMobile } from '../domain/mobile';

const route = useRoute();
const router = useRouter();
const phone = ref('');
const agreedTerms = ref(false);
const agreedPrivacy = ref(false);
const error = ref('');

function submit() {
  const blockers = loginBlockersMobile({
    phone: phone.value,
    code: '123456',
    agreedTerms: agreedTerms.value,
    agreedPrivacy: agreedPrivacy.value,
  });
  if (blockers.length > 0) {
    error.value = blockers.join(' / ');
    return;
  }
  const back = String(route.query.return ?? '/');
  void router.replace(back.startsWith('/') && !back.startsWith('//') ? back : '/');
}
</script>

<template>
  <div class="m-login">
    <div class="brand">
      <span class="logo">🐾</span>
      <h1>cutePet</h1>
      <p>手机号验证码一键登录</p>
    </div>

    <label>手机号
      <input v-model="phone" inputmode="numeric" maxlength="11" placeholder="11 位手机号" data-testid="phone" />
    </label>

    <label class="agree"><input v-model="agreedTerms" type="checkbox" data-testid="terms" /> 我已阅读并同意《用户协议》</label>
    <label class="agree"><input v-model="agreedPrivacy" type="checkbox" data-testid="privacy" /> 我已阅读并同意《隐私政策》</label>

    <p v-if="error" class="err" data-testid="error">{{ error }}</p>

    <button type="button" class="primary" data-testid="get-code" @click="submit">获取验证码（dev: 123456）</button>

    <div class="links">
      <router-link to="/login/age">年龄分支/监护人（方案甲）</router-link>
      <router-link to="/">先逛逛</router-link>
    </div>
  </div>
</template>

<style scoped>
.m-login { min-height: 100vh; padding: 64px 24px calc(32px + env(safe-area-inset-bottom)); display: grid; gap: 16px; align-content: start; background: linear-gradient(180deg, #fff1e8, #f7f3ee 40%); }
.brand { text-align: center; display: grid; gap: 8px; margin-bottom: 18px; }
.logo { font-size: 56px; }
h1 { margin: 0; font-size: 26px; }
.brand p { margin: 0; color: #7a6e63; font-size: 14px; }
label { display: grid; gap: 8px; font-size: 14px; font-weight: 600; }
input[type='text'], input:not([type]) { height: 50px; border: 1px solid #f0e6dc; border-radius: 14px; padding: 0 16px; font-size: 16px; font-weight: 400; background: #fff; }
.agree { grid-template-columns: auto 1fr; align-items: center; font-weight: 400; color: #7a6e63; font-size: 13px; display: grid; grid-template-columns: auto 1fr; gap: 10px; }
.primary { height: 52px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-size: 16px; font-weight: 700; margin-top: 6px; min-height: 44px; }
.links { display: flex; justify-content: space-between; font-size: 13px; }
.links a { color: #ff7a2f; text-decoration: none; }
.err { color: #ef4444; font-size: 13px; margin: 0; }
</style>
