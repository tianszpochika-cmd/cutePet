<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { activeAuthConfig, loginBlockers, safeReturnPath, startAuthFlow } from '../../domain/auth';

const route = useRoute();
const router = useRouter();
const config = activeAuthConfig();
const phone = ref('');
const agreedTerms = ref(false);
const agreedPrivacy = ref(false);
const error = ref('');

const returnPath = computed(() =>
  safeReturnPath(typeof route.query.return === 'string' ? route.query.return : '/'),
);
const resumeAction = computed(() =>
  typeof route.query.resume === 'string' ? route.query.resume : '',
);

function beginPreview() {
  error.value = '';
  startAuthFlow({
    mode: 'preview',
    phone: '',
    returnPath: returnPath.value,
    resumeAction: resumeAction.value,
    agreedTerms: false,
    agreedPrivacy: false,
  });
  void router.push('/login/age');
}

function continueLogin() {
  if (!config) {
    error.value = '正式协议版本与认证通道尚未接入，目前只能查看流程示意。';
    return;
  }
  const blockers = loginBlockers({
    phone: phone.value.trim(),
    agreedTerms: agreedTerms.value,
    agreedPrivacy: agreedPrivacy.value,
  });
  if (blockers.includes('PHONE_INVALID')) {
    error.value = '请输入正确的 11 位手机号';
    return;
  }
  if (blockers.includes('AGREEMENTS_REQUIRED')) {
    error.value = '请分别阅读并勾选正式用户协议与隐私政策';
    return;
  }
  error.value = '';
  startAuthFlow({
    mode: 'live',
    phone: phone.value.trim(),
    returnPath: returnPath.value,
    resumeAction: resumeAction.value,
    agreedTerms: agreedTerms.value,
    agreedPrivacy: agreedPrivacy.value,
  });
  void router.push('/login/age');
}
</script>

<template>
  <div class="auth-layout">
    <aside class="story">
      <p class="eyebrow">START WITH CARE</p>
      <h1>让照护，从这里接上。</h1>
      <p>记下已经做过的事，看清下一次安排；需要共同照顾时，再由宠物所有者决定共享范围。</p>
      <div class="steps" aria-label="登录后可继续的任务">
        <span>① 确认账号</span><span>② 建立档案</span><span>③ 留下记录</span>
      </div>
      <small>登录完成后会回到原页面；加入家庭、报名和发布仍需再次确认。</small>
    </aside>

    <section class="form-card" aria-labelledby="login-title">
      <p class="kicker">账号入口</p>
      <h2 id="login-title">登录或注册</h2>
      <p class="lead">公开内容可先浏览。写入宠物档案、收藏或参与活动时，需要完成账号核验。</p>

      <div v-if="!config" class="notice" role="status">
        <strong>认证暂未开放</strong>
        <p>正式协议文本与可验证的短信通道尚未同时就绪。下面可以查看年龄与监护流程示意，不会发送短信、创建账号或保存个人资料。</p>
      </div>

      <form @submit.prevent="continueLogin">
        <label class="field" for="login-phone">手机号
          <input
            id="login-phone"
            v-model="phone"
            type="tel"
            inputmode="numeric"
            autocomplete="tel"
            maxlength="11"
            placeholder="请输入 11 位手机号"
            data-testid="phone"
            :disabled="!config"
          />
        </label>
        <div class="agreements" role="group" aria-label="协议选择">
          <label><input v-model="agreedTerms" type="checkbox" :disabled="!config" />我已阅读并同意</label>
          <router-link to="/legal/terms">《用户协议》</router-link>
          <label><input v-model="agreedPrivacy" type="checkbox" :disabled="!config" />我已阅读并同意</label>
          <router-link to="/legal/privacy">《隐私政策》</router-link>
        </div>
        <p v-if="error" class="error" role="alert" data-testid="login-error">{{ error }}</p>
        <button class="primary" type="submit" data-testid="get-code" :disabled="!config">继续核对年龄</button>
      </form>

      <button class="preview" type="button" @click="beginPreview">查看流程示意 <span aria-hidden="true">→</span></button>
      <div class="bottom-links">
        <router-link :to="returnPath">先浏览公开内容</router-link>
        <router-link to="/guardian/control">监护人服务状态</router-link>
      </div>
      <p class="footnote">微信登录待正式接入；也须完成手机号、协议和适用的监护核验。</p>
    </section>
  </div>
</template>

<style scoped>
.auth-layout { width: min(100% - 32px, 980px); margin: 48px auto 88px; display: grid; grid-template-columns: minmax(0, .9fr) minmax(360px, 1.1fr); gap: 20px; }
.story, .form-card { border-radius: 26px; padding: clamp(25px, 4vw, 45px); }
.story { display: flex; flex-direction: column; min-height: 570px; background: linear-gradient(145deg, #ffe4cb, #fff1e7 64%, #fff9f3); }
.eyebrow, .kicker { margin: 0 0 14px; color: #92400e; font-size: 12px; font-weight: 800; letter-spacing: .12em; }
.story h1 { max-width: 10ch; margin: 0 0 16px; font-size: clamp(31px, 4vw, 44px); line-height: 1.22; letter-spacing: -.04em; }
.story p:not(.eyebrow) { max-width: 36ch; margin: 0; color: #665247; line-height: 1.85; }
.steps { display: grid; gap: 10px; margin-top: auto; padding-top: 32px; }
.steps span { display: block; padding: 11px 15px; border-radius: 12px; background: rgba(255, 255, 255, .72); color: #6b4934; font-size: 14px; font-weight: 700; }
.story small { margin-top: 22px; color: #6f5d50; line-height: 1.7; }
.form-card { display: flex; flex-direction: column; border: 1px solid #eadfd4; background: #fff; }
.form-card h2 { margin: 0; font-size: 32px; letter-spacing: -.04em; }
.lead { margin: 9px 0 25px; color: #64574d; line-height: 1.8; }
.notice { padding: 17px 18px; border-left: 4px solid #b85111; border-radius: 12px; background: #fff3e9; }
.notice strong { color: #8e3b0c; }
.notice p { margin: 4px 0 0; color: #64574d; font-size: 13px; line-height: 1.7; }
form { display: grid; gap: 16px; margin-top: 24px; }
.field { display: grid; gap: 7px; color: #2b2118; font-size: 14px; font-weight: 700; }
.field input { width: 100%; min-height: 48px; padding: 0 14px; border: 1px solid #dacbc0; border-radius: 12px; font-size: 16px; }
.field input:disabled { background: #f7f4f1; color: #8b817b; }
.agreements { display: grid; grid-template-columns: auto 1fr; align-items: center; column-gap: 4px; row-gap: 9px; color: #64574d; font-size: 13px; }
.agreements label { display: flex; align-items: center; gap: 8px; }
.agreements input { width: 18px; height: 18px; accent-color: #b85111; }
.agreements a { justify-self: start; color: #b85111; text-decoration: underline; text-underline-offset: 3px; }
.primary, .preview { min-height: 48px; border-radius: 999px; font-weight: 750; }
.primary { border: 0; background: #b85111; color: #fff; }
.primary:disabled { background: #cfc8c2; cursor: not-allowed; }
.preview { width: 100%; margin-top: 12px; border: 1px solid #b85111; background: #fff; color: #b85111; }
.bottom-links { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12px; margin-top: 24px; }
.bottom-links a { min-height: 44px; display: inline-flex; align-items: center; color: #9b470e; font-size: 13px; font-weight: 700; }
.footnote { margin: 12px 0 0; color: #7a6e63; font-size: 12px; line-height: 1.7; }
.error { margin: 0; color: #a32920; font-size: 13px; }
@media (max-width: 760px) { .auth-layout { grid-template-columns: 1fr; margin: 20px auto 64px; }.story { min-height: 0; }.steps { margin-top: 20px; }.form-card h2 { font-size: 27px; } }
</style>
