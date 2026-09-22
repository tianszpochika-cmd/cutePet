<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ageBand } from '../../domain/auth';

const router = useRouter();
const birth = ref('');
const today = new Date().toISOString().slice(0, 10);
const error = ref('');

async function confirm() {
  if (!birth.value) {
    error.value = '请选择出生日期';
    return;
  }
  try {
    const band = ageBand(birth.value, today);
    error.value = '';
    if (band === 'MINOR') {
      await router.push({ path: '/login/guardian', query: { birth: birth.value } });
    } else {
      // ADULT → 回到短信步骤完成注册（带出生日期）
      await router.push({ path: '/login/sms', query: { birth: birth.value } });
    }
  } catch {
    error.value = '出生日期不合法';
  }
}
</script>

<template>
  <div class="age">
    <h1>你的出生日期是？</h1>
    <p class="hint">方案甲：满 14 周岁自主注册；未满 14 周岁进入监护人同意流程。</p>
    <input v-model="birth" type="date" :max="today" data-testid="birth" />
    <p v-if="error" class="err">{{ error }}</p>
    <button type="button" data-testid="confirm-age" @click="confirm">确认</button>
  </div>
</template>

<style scoped>
.age {
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
.hint {
  color: #7a6e63;
  font-size: 13px;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
