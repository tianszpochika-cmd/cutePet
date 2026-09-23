<template>
  <div class="contact site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">联系与支持</span>
    </nav>

    <header class="intro">
      <p class="site-eyebrow">HELP & CONTACT</p>
      <h1 class="site-section-title">遇到问题，<br />先找到合适的入口。</h1>
      <p class="site-lead">不同问题有不同处理流程。当前页面提供帮助与规则说明；正式联系渠道和在线提交服务仍待核验。</p>
    </header>

    <section class="notice" aria-labelledby="contact-status-title" role="status">
      <span class="notice-mark" aria-hidden="true">!</span>
      <div>
        <h2 id="contact-status-title">在线联系暂未开放</h2>
        <p>目前没有已核验的收件邮箱或可追踪的表单服务。此页不会收集称呼、联系方式或留言，也不会显示未经实际接收的“已收到”提示。</p>
      </div>
    </section>

    <section class="channels" aria-labelledby="channels-title">
      <div class="heading">
        <p class="site-eyebrow">FIND YOUR PATH</p>
        <h2 id="channels-title">按问题类型查看下一步</h2>
      </div>
      <div class="channel-grid">
        <article>
          <span class="number">01 / 使用帮助</span>
          <h3>账号、档案与功能</h3>
          <p>先查看帮助中心中的常见问题和操作说明。帮助内容不等于工单已提交。</p>
          <router-link class="site-text-link" to="/help">打开帮助中心 <span aria-hidden="true">→</span></router-link>
        </article>
        <article>
          <span class="number">02 / 举报与版权</span>
          <h3>了解处理流程</h3>
          <p>举报、版权通知和申诉有各自的材料与处理要求。正式接收渠道尚未公布，当前页面仅供了解流程。</p>
          <router-link class="site-text-link" to="/legal/report">查看流程说明 <span aria-hidden="true">→</span></router-link>
        </article>
        <article>
          <span class="number">03 / 商务与媒体</span>
          <h3>联系渠道待确认</h3>
          <p>经核验的合作邮箱与媒体联系信息公布前，这里不提供占位地址或无法送达的提交按钮。</p>
          <router-link class="site-text-link" to="/about">了解项目 <span aria-hidden="true">→</span></router-link>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.contact { padding-block: 32px 88px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.intro { padding: clamp(48px, 7vw, 96px) 0 46px; }
.intro h1 { max-width: 800px; }
.notice { display: flex; align-items: flex-start; gap: 20px; padding: clamp(24px, 4vw, 38px); background: #fff; border: 1px solid #e8d2bd; border-left: 5px solid var(--site-action); border-radius: 20px; }
.notice-mark { display: grid; place-items: center; width: 38px; height: 38px; flex: none; border-radius: 50%; background: var(--site-soft); color: var(--site-action); font-weight: 800; }
.notice h2 { margin: 0; font-size: clamp(21px, 2.4vw, 27px); }
.notice p { max-width: 760px; margin: 8px 0 0; color: var(--site-muted); line-height: 1.9; }
.channels { padding-top: 82px; }
.heading h2 { margin: 0 0 24px; font-size: clamp(24px, 3vw, 34px); }
.channel-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.channel-grid article { display: flex; flex-direction: column; align-items: flex-start; min-height: 280px; padding: 28px; border: 1px solid var(--site-line); border-radius: 20px; background: rgba(255, 255, 255, .75); }
.number { color: var(--site-action); font-size: 12px; font-weight: 800; letter-spacing: .08em; }
.channel-grid h3 { margin: 22px 0 6px; font-size: 19px; }
.channel-grid p { margin: 0; color: var(--site-muted); font-size: 14px; line-height: 1.8; }
.channel-grid a { margin-top: auto; padding-top: 20px; }
@media (max-width: 760px) {
  .contact { padding-block: 22px 66px; }
  .intro { padding: 56px 0 32px; }
  .notice { gap: 12px; }
  .channels { padding-top: 66px; }
  .channel-grid { grid-template-columns: 1fr; }
  .channel-grid article { min-height: 0; }
}
</style>
