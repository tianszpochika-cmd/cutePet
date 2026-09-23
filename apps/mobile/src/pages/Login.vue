<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { safeReturnPath } from '../../../web/src/domain/auth.ts';

const route = useRoute();
const showingAge = ref(false);
const returnPath = computed(() => {
  const target = safeReturnPath(typeof route.query.return === 'string' ? route.query.return : '/');
  return /^\/login(?:[/?#]|$)/i.test(target) ? '/' : target;
});
</script>

<template>
  <div class="login-page">
    <div class="hero" aria-hidden="true"><span>✿</span><span>♡</span><span>✦</span></div>
    <header class="brand">
      <p class="eyebrow">CUTE PET · ACCOUNT</p>
      <h1>一起，把照护的每一步记清楚。</h1>
      <p>公开资讯和探索内容可先浏览。建立档案、加入家庭和收藏需要核验账号。</p>
    </header>

    <section class="card" aria-labelledby="login-title">
      <div class="step"><span>01</span> 手机号登录或注册</div>
      <h2 id="login-title">账号入口准备中</h2>
      <p>正式用户协议、隐私政策版本和可验证的短信通道尚未同时接通。当前不会发送验证码、创建账号或把输入的号码当作登录凭据。</p>
      <label class="field" for="login-phone">手机号
        <input id="login-phone" type="tel" inputmode="numeric" autocomplete="tel" placeholder="正式登录开放后填写" disabled />
      </label>
      <div class="agreements" aria-label="协议状态">
        <div><span aria-hidden="true">○</span><span>用户协议全文与有效版本待发布</span></div>
        <div><span aria-hidden="true">○</span><span>隐私政策全文与有效版本待发布</span></div>
      </div>
      <button class="primary" type="button" disabled data-testid="get-code">发送验证码待接入</button>
      <p class="small">微信授权同样需要手机号、正式协议和适用的监护核验，当前暂不可用。</p>
    </section>

    <section class="flow" aria-labelledby="flow-title">
      <button type="button" class="flow-toggle" :aria-expanded="showingAge" aria-controls="age-flow" @click="showingAge = !showingAge">
        <span><strong id="flow-title">年龄与监护流程</strong><small>查看未满 14 周岁的适用路径</small></span>
        <span aria-hidden="true">{{ showingAge ? '−' : '＋' }}</span>
      </button>
      <div v-if="showingAge" id="age-flow" class="flow-body" role="status">
        <ol>
          <li>完成正式协议选择后，按北京时间核对完整出生日期。</li>
          <li>未满 14 周岁时，需要独立验证监护人手机号并取得监护人同意。</li>
          <li>只有服务端确认身份、年龄分支与同意记录后，才能建立账号并回到原任务。</li>
        </ol>
        <p>本页不收集出生日期或监护人号码；加入家庭、报名和发布仍需本人返回原任务再次确认。</p>
      </div>
    </section>

    <p v-if="returnPath !== '/'" class="return-note">原任务仅作为将来完成正式登录后的回跳意图；当前游客浏览不会进入私人页面。</p>
    <router-link class="browse" to="/">先浏览公开首页 <span aria-hidden="true">→</span></router-link>
  </div>
</template>

<style scoped>
.login-page{width:min(100%,560px);min-height:100vh;margin:auto;padding:calc(24px + env(safe-area-inset-top,0px)) 16px calc(38px + env(safe-area-inset-bottom,0px));display:grid;align-content:start;gap:16px;color:#2b2118;background:radial-gradient(circle at 20% 5%,#ffe3cb,transparent 42%),#faf7f3}.hero{height:82px;position:relative;display:flex;align-items:center;justify-content:center;color:#b85111}.hero span:first-child{font-size:70px;line-height:1}.hero span:nth-child(2){position:absolute;left:25%;top:8px;font-size:25px}.hero span:last-child{position:absolute;right:22%;bottom:5px;font-size:25px}.brand{text-align:center;padding:0 10px 7px}.eyebrow{margin:0 0 9px;color:#a34810;font-size:11px;font-weight:850;letter-spacing:.16em}.brand h1{max-width:12ch;margin:0 auto;font-size:clamp(27px,8vw,38px);line-height:1.23;letter-spacing:-.045em}.brand>p:last-child{max-width:36ch;margin:13px auto 0;color:#685b4e;font-size:13px;line-height:1.8}.card,.flow{border:1px solid #eadfd4;border-radius:22px;background:#fff;box-shadow:0 12px 28px #53371c0a}.card{padding:22px;display:grid;gap:14px}.step{color:#9b470e;font-size:11px;font-weight:800;letter-spacing:.08em}.step span{display:inline-grid;place-items:center;width:27px;height:27px;margin-right:6px;border-radius:8px;background:#fff0e2}.card h2{margin:0;font-size:22px;letter-spacing:-.03em}.card>p{margin:0;color:#695d53;font-size:13px;line-height:1.75}.field{display:grid;gap:7px;font-size:13px;font-weight:750}.field input{width:100%;min-height:48px;padding:0 13px;border:1px solid #eadfd4;border-radius:11px;background:#f6f3f0;color:#7d726a;font-size:16px}.agreements{display:grid;gap:8px;padding:13px;border-radius:12px;background:#faf7f3;color:#716458;font-size:12px}.agreements div{display:flex;gap:9px;align-items:center}.agreements div span:first-child{color:#ad7760;font-size:17px}.primary{min-height:48px;border:0;border-radius:12px;background:#e3dbd4;color:#695d53;font-weight:800}.small{font-size:11px!important}.flow{overflow:hidden}.flow-toggle{width:100%;min-height:64px;display:flex;align-items:center;justify-content:space-between;gap:14px;padding:12px 20px;border:0;background:#fff;color:#2b2118;text-align:left}.flow-toggle strong,.flow-toggle small{display:block}.flow-toggle strong{font-size:14px}.flow-toggle small{margin-top:3px;color:#796b5f;font-size:11px}.flow-toggle>span:last-child{color:#b85111;font-size:22px}.flow-body{padding:0 20px 19px;border-top:1px solid #f1e8e0;color:#65584b;font-size:12px;line-height:1.75}.flow-body ol{padding-left:19px}.flow-body li{margin:8px 0}.flow-body p{margin:10px 0 0}.return-note{margin:0;color:#78675a;font-size:11px;line-height:1.6}.browse{min-height:48px;display:flex;align-items:center;justify-content:space-between;padding:0 18px;border:1px solid #dfc7b5;border-radius:13px;background:#fff;color:#9a430e;text-decoration:none;font-size:14px;font-weight:750}@media(max-width:360px){.card{padding:18px}.brand h1{font-size:27px}}
</style>
