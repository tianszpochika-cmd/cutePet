<template>
  <div class="cases site-wrap">
    <nav class="crumb" aria-label="当前位置"><router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">使用案例</span></nav>
    <header><p class="site-eyebrow">REAL STORIES NEED REAL EVIDENCE</p><h1 class="site-section-title">真实故事，<br />等有依据后再分享。</h1><p class="site-lead">案例需要相关方授权、结果口径和可核对的出处。当前没有符合公开条件的案例。</p></header>
    <section class="empty" aria-labelledby="empty-title"><div class="empty-icon" aria-hidden="true">✦</div><div><span class="status">案例 · 待核验</span><h2 id="empty-title">暂时没有可公开的使用案例</h2><p>这里不会用虚构机构、提升比例或用户评价填充页面。你可以先从产品能力和使用场景了解能完成哪些任务。</p><div class="actions"><router-link class="site-button" to="/products">了解产品能力</router-link><router-link class="site-button outline" to="/services">看看使用场景</router-link></div></div></section>
  </div>
</template>

<style scoped>
.cases{padding-block:32px 90px}.crumb{display:flex;align-items:center;gap:10px;color:var(--site-muted);font-size:13px}.crumb a{color:inherit;text-decoration:none}
header{padding:clamp(48px,7vw,96px) 0 45px}.empty{display:grid;grid-template-columns:150px 1fr;align-items:center;gap:45px;padding:48px;border:1px solid var(--site-line);border-radius:28px;background:#fff}.empty-icon{display:grid;place-items:center;width:150px;height:150px;border-radius:32px;background:var(--site-soft);color:var(--site-action);font-size:76px}
.status{color:var(--site-action);font-size:12px;font-weight:800}.empty h2{margin:6px 0 9px;font-size:28px}.empty p{max-width:630px;margin:0;color:var(--site-muted);font-size:14px}.actions{display:flex;flex-wrap:wrap;gap:12px;margin-top:24px}
@media(max-width:760px){.cases{padding-block:22px 70px}.empty{grid-template-columns:1fr;gap:20px;padding:27px}.empty-icon{width:88px;height:88px;border-radius:20px;font-size:48px}.empty h2{font-size:23px}.actions .site-button{width:100%}}
</style>
