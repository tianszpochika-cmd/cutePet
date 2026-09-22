<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { loginBlockers, safeReturnPath } from '../../domain/auth';

const route = useRoute();
const router = useRouter();

const phone = ref('');
const agreedTerms = ref(false); // 默认不勾选（决议）
const agreedPrivacy = ref(false);
const error = ref('');

async function submit() {
  const blockers = loginBlockers({
    phone: phone.value,
    code: '123456',
    agreedTerms: agreedTerms.value,
    agreedPrivacy: agreedPrivacy.value,
  });
  if (blockers.includes('PHONE_INVALID')) {
    error.value = '手机号格式不正确';
    return;
  }
  if (blockers.includes('AGREEMENTS_REQUIRED')) {
    error.value = '请先勾选《用户协议》与《隐私政策》';
    return;
  }
  error.value = '';
  // 进入验证码步骤（dev 固定码 123456 直接可过，真短信属本地阶段）
  const ret = safeReturnPath(String(route.query.return ?? ''));
  await router.push({ path: '/login/sms', query: { phone: phone.value, return: ret } });
}
</script>

<template>
  <div class="login">
    <h1>登录 / 注册</h1>
    <label>
      手机号
      <input v-model="phone" placeholder="11 位手机号" data-testid="phone" />
    </label>
    <p class="agree">
      <input id="terms" v-model="agreedTerms" type="checkbox" />
      <label for="terms">我已阅读并同意《用户协议》</label>
    </p>
    <p class="agree">
      <input id="privacy" v-model="agreedPrivacy" type="checkbox" />
      <label for="privacy">我已阅读并同意《隐私政策》</label>
    </p>
    <p v-if="error" class="err" data-testid="login-error">{{ error }}</p>
    <button type="button" data-testid="get-code" @click="submit">获取验证码</button>
    <p class="divider">或</p>
    <button type="button" class="wechat" @click="error = '微信登录需补齐手机验证与协议（U83），dev 环境请走验证码'">
      微信登录
    </button>
    <router-link class="back" :to="safeReturnPath(String(route.query.return))">先逛逛（返回）</router-link>
  </div>
</template>

<style scoped>
.login {
  max-width: 380px;
  margin: 64px auto;
  padding: 24px;
  display: grid;
  gap: 12px;
}
input[type='text'],
input:not([type]) {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
  font-size: 15px;
}
button {
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
button.wechat {
  background: #07c160;
}
.agree {
  font-size: 13px;
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.divider {
  text-align: center;
  color: #7a6e63;
}
.back {
  text-align: center;
  color: #ff7a2f;
  font-size: 13px;
}
</style>
