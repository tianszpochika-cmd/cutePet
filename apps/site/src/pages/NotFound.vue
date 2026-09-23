<template>
  <div class="not-found site-wrap">
    <div class="illustration" aria-hidden="true">
      <span class="orbit orbit-one"></span>
      <span class="orbit orbit-two"></span>
      <span class="paw">
        <svg viewBox="0 0 32 32" fill="currentColor"><path d="M16 24.8c-3.4 0-4.5 2.2-7.1 1.1-2.4-1-2.8-4.1-.5-6.1 2.4-2.2 3.7-6.6 7.6-6.6s5.2 4.4 7.6 6.6c2.3 2 .8 5.1-1.5 6.1-2.6 1.1-3.8-1.1-6.8-1.1Z"/><ellipse cx="7.2" cy="10.4" rx="2.4" ry="3.5"/><ellipse cx="13" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="19.4" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="25" cy="10.4" rx="2.4" ry="3.5"/></svg>
      </span>
    </div>
    <p class="code">404 · PAGE NOT FOUND</p>
    <h1>这页暂时找不到了。</h1>
    <p class="description">地址可能有误，也可能已经失效。你可以从首页重新找到产品介绍，或去帮助中心查看下一步。</p>
    <nav class="actions" aria-label="继续浏览">
      <router-link class="site-button" to="/">返回官网首页 <span aria-hidden="true">→</span></router-link>
      <router-link class="site-button outline" to="/products">了解产品能力</router-link>
      <router-link class="site-text-link" to="/help">查看帮助 <span aria-hidden="true">→</span></router-link>
    </nav>
  </div>
</template>

<style scoped>
.not-found { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: min(760px, 72vh); padding-block: 60px 95px; text-align: center; }
.illustration { position: relative; display: grid; place-items: center; width: 174px; height: 174px; margin-bottom: 15px; border-radius: 50%; background: #fff0e2; }
.orbit { position: absolute; border: 1px solid #f5d7bd; border-radius: 50%; }
.orbit-one { inset: 14px; }
.orbit-two { inset: 29px; }
.paw { display: grid; place-items: center; width: 74px; height: 74px; border-radius: 24px; background: var(--site-action); color: #fff; transform: rotate(-8deg); }
.paw svg { width: 43px; height: 43px; }
.code { margin: 0 0 9px; color: var(--site-action); font-size: 12px; font-weight: 800; letter-spacing: .16em; }
h1 { margin: 0; font-size: clamp(29px, 4vw, 43px); letter-spacing: -.05em; line-height: 1.3; }
.description { max-width: 48ch; margin: 13px 0 26px; color: var(--site-muted); font-size: 15px; line-height: 1.85; }
.actions { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 12px; }
@media (max-width: 760px) {
  .not-found { min-height: 64vh; padding-block: 50px 70px; }
  .illustration { width: 142px; height: 142px; }
  .actions { flex-direction: column; width: 100%; }
  .actions .site-button { width: min(100%, 300px); }
}
</style>
