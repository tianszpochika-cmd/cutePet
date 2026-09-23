<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ageBand, beijingToday, getAuthFlow, setAuthBirthDate } from '../../domain/auth';

const router = useRouter();
const flow = getAuthFlow();
const birth = ref(flow?.birthDate ?? '');
const today = beijingToday();
const error = ref('');

function confirmAge() {
  if (!flow) {
    error.value = '当前步骤已过期，请从登录入口重新开始。';
    return;
  }
  if (!birth.value) {
    error.value = '请选择完整的出生日期';
    return;
  }
  try {
    const band = ageBand(birth.value, today);
    setAuthBirthDate(birth.value);
    error.value = '';
    void router.push(band === 'MINOR' ? '/login/guardian' : '/login/sms');
  } catch {
    error.value = '出生日期不合法，请重新选择';
  }
}

function backToLogin() {
  void router.push({
    path: '/login',
    query: flow ? { return: flow.returnPath, resume: flow.resumeAction } : {},
  });
}
</script>

<template>
  <div class="age-page">
    <button class="back" type="button" @click="backToLogin">← 返回登录入口</button>
    <div class="card">
      <p class="eyebrow">02 / 适用路径</p>
      <h1>确认适用的注册路径。</h1>
      <p class="lead">请填写完整出生日期，按北京时间计算实际周岁。未满 14 周岁需要独立的监护人核验与同意。</p>

      <div v-if="!flow" class="notice" role="status">
        此步骤没有可继续的登录状态，刷新页面后也不会保留手机号或生日。
      </div>
      <div v-else-if="flow.mode === 'preview'" class="notice" role="status">
        当前是流程示意。日期仅在此标签页内用于显示分支，不会发送到服务端、创建账号或记录监护同意。
      </div>

      <form v-if="flow" @submit.prevent="confirmAge">
        <label for="birth-date">出生日期</label>
        <input
          id="birth-date"
          v-model="birth"
          type="date"
          :max="today"
          autocomplete="bday"
          data-testid="birth"
          aria-describedby="birth-hint"
        />
        <p id="birth-hint" class="hint">填写日期只用于判断是否需要监护人流程；未确认前不会尝试登录。</p>
        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <button class="primary" type="submit" data-testid="confirm-age">查看下一步 <span aria-hidden="true">→</span></button>
      </form>
      <router-link v-else class="primary start" to="/login">重新开始</router-link>
    </div>
  </div>
</template>

<style scoped>
.age-page { width: min(100% - 32px, 600px); margin: 42px auto 90px; }
.back { display: inline-flex; align-items: center; min-height: 44px; margin-bottom: 15px; padding: 0 4px; border: 0; background: none; color: #9b470e; font-weight: 700; }
.card { padding: clamp(25px, 5vw, 46px); border: 1px solid #eadfd4; border-radius: 24px; background: #fff; }
.eyebrow { margin: 0 0 10px; color: #a0440b; font-size: 12px; font-weight: 800; letter-spacing: .12em; }
h1 { margin: 0; font-size: clamp(27px, 4vw, 36px); line-height: 1.3; letter-spacing: -.04em; }
.lead { margin: 12px 0 24px; color: #64574d; line-height: 1.85; }
.notice { margin-bottom: 24px; padding: 15px 17px; border-left: 4px solid #b85111; border-radius: 12px; background: #fff3e9; color: #704b37; font-size: 14px; line-height: 1.75; }
form { display: grid; gap: 11px; }
label { font-size: 14px; font-weight: 750; }
input { width: 100%; min-height: 48px; padding: 0 14px; border: 1px solid #dacbc0; border-radius: 12px; font-size: 16px; }
.hint { margin: 0 0 10px; color: #7a6e63; font-size: 13px; }
.error { margin: 0; color: #a32920; font-size: 13px; }
.primary { display: inline-flex; align-items: center; justify-content: center; min-height: 48px; padding: 0 24px; border: 0; border-radius: 999px; background: #b85111; color: #fff; font-weight: 750; text-decoration: none; }
.start { margin-top: 8px; }
</style>
