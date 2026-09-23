<template>
  <div class="center site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">内容中心</span>
    </nav>

    <header class="intro">
      <p class="site-eyebrow">READ WITH CARE</p>
      <h1 class="site-section-title">值得读的内容，<br />也值得核对来源。</h1>
      <p class="site-lead">这里计划汇集养宠知识和用户创作。文章只有在完成审核、仍可公开时才会显示。</p>
    </header>

    <section class="empty" aria-labelledby="empty-title">
      <div class="empty-mark" aria-hidden="true">✦</div>
      <div>
        <p class="status">内容状态 · 待接入</p>
        <h2 id="empty-title">暂时没有可展示的公开文章</h2>
        <p>官网尚未接入可核验的已发布文章。我们不会用示例标题、作者或发布日期填充列表；等公开内容接入后，这里才能提供搜索和阅读。</p>
        <div class="actions">
          <router-link class="site-button" to="/products">了解产品能力</router-link>
          <router-link class="site-button outline" to="/help">查看使用帮助</router-link>
        </div>
      </div>
    </section>

    <section class="principles" aria-labelledby="principles-title">
      <div class="principles-heading">
        <p class="site-eyebrow">PUBLISHING PRINCIPLES</p>
        <h2 id="principles-title">内容展示会遵循这些原则</h2>
      </div>
      <div class="principle-grid">
        <article><span>01</span><h3>先审核，再公开</h3><p>草稿和未通过审核的投稿不会出现在公开列表。</p></article>
        <article><span>02</span><h3>交代作者与来源</h3><p>阅读时能看到必要的作者、日期和资料来源。</p></article>
        <article><span>03</span><h3>状态及时同步</h3><p>已下架或删除的内容不继续作为正常文章展示。</p></article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.center { padding-block: 32px 88px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.intro { padding: clamp(48px, 7vw, 96px) 0 46px; }
.intro h1 { max-width: 780px; }
.empty { display: grid; grid-template-columns: minmax(120px, .32fr) minmax(0, 1fr); gap: clamp(24px, 5vw, 72px); align-items: center; padding: clamp(28px, 5vw, 58px); background: #fff; border: 1px solid var(--site-line); border-radius: 28px; box-shadow: 0 20px 54px rgba(43, 33, 24, .05); }
.empty-mark { width: min(100%, 180px); aspect-ratio: 1; display: grid; place-items: center; border-radius: 32px; background: var(--site-soft); color: var(--site-action); font-size: clamp(56px, 8vw, 94px); }
.status { margin: 0 0 7px; color: var(--site-action); font-size: 12px; font-weight: 800; letter-spacing: .08em; }
.empty h2, .principles h2 { margin: 0; font-size: clamp(24px, 3vw, 34px); line-height: 1.3; }
.empty p:not(.status) { max-width: 680px; margin: 12px 0 0; color: var(--site-muted); line-height: 1.9; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 26px; }
.principles { padding-top: 82px; }
.principles-heading { margin-bottom: 24px; }
.principle-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.principle-grid article { min-height: 185px; padding: 26px; border: 1px solid var(--site-line); border-radius: 20px; background: rgba(255, 255, 255, .68); }
.principle-grid span { color: var(--site-action); font-size: 12px; font-weight: 800; letter-spacing: .12em; }
.principle-grid h3 { margin: 13px 0 6px; font-size: 18px; }
.principle-grid p { margin: 0; color: var(--site-muted); font-size: 14px; }
@media (max-width: 760px) {
  .center { padding-block: 22px 66px; }
  .intro { padding: 56px 0 32px; }
  .empty { grid-template-columns: 1fr; }
  .empty-mark { width: 92px; border-radius: 20px; font-size: 54px; }
  .principles { padding-top: 66px; }
  .principle-grid { grid-template-columns: 1fr; }
  .principle-grid article { min-height: 0; }
  .actions .site-button { width: 100%; }
}
</style>
