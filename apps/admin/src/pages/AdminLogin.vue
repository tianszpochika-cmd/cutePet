<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { enterPreview } from '../adminPreview';

const route = useRoute();
const router = useRouter();
const returnPath = computed(() => {
  const value = route.query.return;
  return typeof value === 'string' && value.startsWith('/') && !value.startsWith('//') && !value.startsWith('/login')
    ? value : '/dashboard';
});

function openPreview() {
  enterPreview();
  void router.replace(returnPath.value);
}
</script>

<template>
  <div class="login-page">
    <div class="login-wrap">
      <div class="intro">
        <p class="eyebrow">cutePet · 运营管理</p>
        <h1>每一项判断，<br /><em>都要有依据。</em></h1>
        <p>从待办与审核开始，按对象、状态和影响范围逐步核对。界面预览可用于检查导航和交互逻辑。</p>
        <div class="steps" aria-label="正式管理入口条件">
          <span>01 身份核验</span><span>02 权限确认</span><span>03 可追溯处理</span>
        </div>
      </div>

      <main class="login-card" id="main-content">
        <span class="paw" aria-hidden="true">✿</span>
        <p class="kicker">内部工作台</p>
        <h2>管理端访问</h2>
        <p class="description">管理员身份核验与权限获取尚未接入。当前仅开放只读界面预览，不会建立登录会话或授予管理权限。</p>
        <div class="locked" role="status">
          <span class="locked-icon" aria-hidden="true">🔒</span>
          <div><strong>真实登录暂未开放</strong><small>需要服务端账号核验、权限回传与审计后才能启用。</small></div>
        </div>
        <button type="button" class="primary" data-testid="preview" @click="openPreview">查看只读工作台预览 <span aria-hidden="true">→</span></button>
        <p class="footnote">预览资料只供设计和逻辑核对；刷新页面即退出预览。所有审核、处罚、导入和授权操作均不会生效。</p>
      </main>
    </div>
  </div>
</template>

<style scoped>
.login-page { min-height: 100vh; display: grid; place-items: center; padding: 48px 24px; background: radial-gradient(circle at 16% 18%, #ffe8d2, transparent 32%), #fff9f3; }
.login-wrap { width: min(1100px, 100%); display: grid; grid-template-columns: minmax(0, 1fr) 430px; gap: clamp(32px, 7vw, 110px); align-items: center; }
.eyebrow, .kicker { color: var(--primary); font-size: 13px; font-weight: 800; letter-spacing: .13em; }
h1 { margin: 22px 0; font-size: clamp(40px, 5vw, 68px); line-height: 1.1; letter-spacing: -.065em; }
h1 em { color: var(--primary); font-style: normal; }
.intro > p:not(.eyebrow) { color: var(--muted); max-width: 450px; font-size: 17px; line-height: 1.9; }
.steps { display: flex; gap: 14px; flex-wrap: wrap; margin-top: 42px; color: var(--ink); font-size: 12px; font-weight: 700; }
.steps span { border-left: 2px solid var(--primary); padding-left: 12px; }
.login-card { padding: 42px; background: #fff; border: 1px solid var(--line); border-radius: 28px; box-shadow: 0 24px 80px rgba(78, 46, 25, .09); }
.paw { display: grid; place-items: center; width: 52px; height: 52px; border-radius: 18px; background: #fff0e2; color: var(--primary); font-size: 30px; }
.kicker { margin: 32px 0 7px; }
h2 { margin: 0 0 12px; font-size: 30px; }
.description { color: var(--muted); font-size: 14px; line-height: 1.8; }
.locked { display: flex; gap: 12px; align-items: center; margin: 24px 0; padding: 16px; background: #faf6f1; border: 1px solid var(--line); border-radius: 14px; }
.locked-icon { font-size: 22px; }
.locked strong, .locked small { display: block; }
.locked strong { font-size: 14px; }
.locked small { margin-top: 4px; color: var(--muted); line-height: 1.6; }
.primary { width: 100%; min-height: 48px; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; border: 0; border-radius: 13px; background: var(--primary); color: #fff; font-weight: 700; }
.primary:hover { background: #99410e; }
.footnote { margin: 15px 0 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
@media (max-width: 820px) { .login-wrap { grid-template-columns: 1fr; max-width: 560px; } .intro h1 { font-size: 42px; } .steps { margin-top: 24px; } }
@media (max-width: 520px) { .login-page { padding: 24px 16px; } .login-card { padding: 28px 22px; } }
</style>
