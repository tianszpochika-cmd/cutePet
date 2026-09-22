<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

const route = useRoute();
const router = useRouter();
const phone = ref('');
const error = ref('');
const done = ref(false);

async function verifyAndConsent() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    error.value = '监护人手机号格式不正确';
    return;
  }
  error.value = '';
  try {
    await api.minorGuardianVerify({ body: { minorPhone: '', guardianPhone: phone.value } });
    await api.minorGuardianConsent({ body: { minorPhone: '', guardianPhone: phone.value } });
    done.value = true;
    setTimeout(() => {
      void router.replace({ path: '/login/sms', query: { birth: route.query.birth, guardian: '1' } });
    }, 800);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '监护人验证失败';
  }
}
</script>

<template>
  <div class="guardian">
    <h1>监护人同意</h1>
    <p class="hint">
      未满 14 周岁用户需由监护人验证手机号并同意《儿童个人信息处理规则》。监护人仅操作同意，不进入孩子账号。
    </p>
    <input v-model="phone" placeholder="监护人手机号" data-testid="guardian-phone" />
    <p v-if="error" class="err">{{ error }}</p>
    <p v-if="done" class="ok">已记录监护人同意，正在返回登录…</p>
    <button type="button" data-testid="consent" :disabled="done" @click="verifyAndConsent">
      验证并同意
    </button>
  </div>
</template>

<style scoped>
.guardian {
  max-width: 380px;
  margin: 64px auto;
  padding: 24px;
  display: grid;
  gap: 12px;
  text-align: center;
}
input {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
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
  opacity: 0.5;
}
.hint {
  color: #7a6e63;
  font-size: 13px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.ok {
  color: #22c55e;
  font-size: 13px;
}
</style>
