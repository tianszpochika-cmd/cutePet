<template>
  <div class="events site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">本地活动</span>
    </nav>

    <header class="intro">
      <p class="site-eyebrow">GO OUT TOGETHER</p>
      <h1 class="site-section-title">出门的计划，<br />从可靠的信息开始。</h1>
      <p class="site-lead">了解宠物友好活动与领养信息时，时间、地点、主办方和参与方式都需要逐项核实。</p>
    </header>

    <section class="empty" aria-labelledby="events-empty-title">
      <div class="empty-mark" aria-hidden="true">↗</div>
      <div>
        <p class="status">公开活动 · 待接入</p>
        <h2 id="events-empty-title">目前没有可核验的活动信息</h2>
        <p>官网尚未接入可公开的活动或领养资料，因此暂不展示城市、日期、名额和报名状态。浏览产品能力不代表已经报名。</p>
        <div class="actions">
          <router-link class="site-button" to="/products">了解产品能力</router-link>
          <router-link class="site-button outline" to="/services">看看使用场景</router-link>
        </div>
      </div>
    </section>

    <section class="paths" aria-labelledby="paths-title">
      <p class="site-eyebrow">TWO DISTINCT PATHS</p>
      <h2 id="paths-title">活动与领养，参与方式不同</h2>
      <div class="path-grid">
        <article>
          <span class="path-icon" aria-hidden="true">01</span>
          <h3>本地活动</h3>
          <p>当真实活动发布后，可先核对主办方、地点和条件；报名须前往用户端阅读告知并确认。</p>
        </article>
        <article>
          <span class="path-icon" aria-hidden="true">02</span>
          <h3>领养信息</h3>
          <p>领养信息以了解和联系为主，不提供一键报名或交易入口；发布前同样需要人工审核。</p>
        </article>
      </div>
      <router-link class="site-text-link" to="/help">关于探索与活动的常见问题 <span aria-hidden="true">→</span></router-link>
    </section>
  </div>
</template>

<style scoped>
.events { padding-block: 32px 88px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.intro { padding: clamp(48px, 7vw, 96px) 0 46px; }
.intro h1 { max-width: 820px; }
.empty { display: grid; grid-template-columns: minmax(120px, .32fr) minmax(0, 1fr); gap: clamp(24px, 5vw, 72px); align-items: center; padding: clamp(28px, 5vw, 58px); background: #fff; border: 1px solid var(--site-line); border-radius: 28px; box-shadow: 0 20px 54px rgba(43, 33, 24, .05); }
.empty-mark { width: min(100%, 180px); aspect-ratio: 1; display: grid; place-items: center; border-radius: 32px; background: #e8f5ee; color: #26714d; font-size: clamp(56px, 8vw, 94px); }
.status { margin: 0 0 7px; color: #26714d; font-size: 12px; font-weight: 800; letter-spacing: .08em; }
.empty h2, .paths h2 { margin: 0; font-size: clamp(24px, 3vw, 34px); line-height: 1.3; }
.empty p:not(.status) { max-width: 680px; margin: 12px 0 0; color: var(--site-muted); line-height: 1.9; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 26px; }
.paths { padding-top: 82px; }
.path-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin: 24px 0 14px; }
.path-grid article { padding: 28px; border: 1px solid var(--site-line); border-radius: 20px; background: rgba(255, 255, 255, .68); }
.path-icon { color: var(--site-action); font-size: 12px; font-weight: 800; letter-spacing: .12em; }
.path-grid h3 { margin: 13px 0 6px; font-size: 19px; }
.path-grid p { margin: 0; color: var(--site-muted); font-size: 14px; }
@media (max-width: 760px) {
  .events { padding-block: 22px 66px; }
  .intro { padding: 56px 0 32px; }
  .empty { grid-template-columns: 1fr; }
  .empty-mark { width: 92px; border-radius: 20px; font-size: 54px; }
  .paths { padding-top: 66px; }
  .path-grid { grid-template-columns: 1fr; }
  .actions .site-button { width: 100%; }
}
</style>
