<template>
  <div class="guardian-control">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/login">登录入口</router-link><span aria-hidden="true">/</span><span aria-current="page">监护人服务</span>
    </nav>
    <header>
      <p class="eyebrow">GUARDIAN SERVICE</p>
      <h1>监护人服务</h1>
      <p>监护人需要能够独立核验身份、查看同意状态并撤回同意。这些动作不能由儿童账号代为确认。</p>
    </header>

    <section class="notice" aria-labelledby="control-status" role="status">
      <strong id="control-status">在线控制暂未开放</strong>
      <p>当前服务尚未提供可验证的监护人身份确认与完整撤回结果。此页不会查询儿童账号是否存在，不会收集手机号，也不会显示“已核验”“已撤回”等未经平台确认的状态。</p>
    </section>

    <section class="tasks" aria-labelledby="tasks-title">
      <h2 id="tasks-title">正式入口需要完成的三件事</h2>
      <ol>
        <li><span>01</span><div><strong>独立核验</strong><p>确认操作人为监护人；仅填写手机号不足以证明身份。</p></div></li>
        <li><span>02</span><div><strong>查看同意</strong><p>仅在核验通过后展示与本人相关的同意状态，不泄露无关联儿童账号。</p></div></li>
        <li><span>03</span><div><strong>撤回并查看结果</strong><p>由平台确认撤回，说明对儿童账号会话、提醒和资料可见性的影响。</p></div></li>
      </ol>
    </section>

    <div class="actions">
      <router-link class="primary" to="/help">查看帮助与联系信息</router-link>
      <router-link class="secondary" to="/legal/children">阅读儿童规则状态</router-link>
    </div>
  </div>
</template>

<style scoped>
.guardian-control { width: min(100% - 32px, 760px); margin: 34px auto 92px; }
.crumb { display: flex; gap: 9px; align-items: center; color: #7a6e63; font-size: 13px; }
.crumb a { color: #9b470e; }
header { padding: 55px 0 30px; }
.eyebrow { margin: 0 0 10px; color: #a0440b; font-size: 12px; font-weight: 800; letter-spacing: .12em; }
h1 { margin: 0; font-size: clamp(29px, 4vw, 40px); letter-spacing: -.04em; }
header p:last-child { max-width: 62ch; margin: 11px 0 0; color: #64574d; line-height: 1.85; }
.notice { padding: 22px 24px; border: 1px solid #e8d2bd; border-left: 4px solid #b85111; border-radius: 16px; background: #fff7ef; }
.notice strong { color: #8e3b0c; font-size: 18px; }
.notice p { margin: 7px 0 0; color: #704b37; font-size: 14px; line-height: 1.8; }
.tasks { margin-top: 36px; padding: 26px 28px; border: 1px solid #eadfd4; border-radius: 20px; background: #fff; }
.tasks h2 { margin: 0 0 23px; font-size: 21px; }
.tasks ol { display: grid; gap: 18px; margin: 0; padding: 0; list-style: none; }
.tasks li { display: flex; gap: 14px; }
.tasks li > span { color: #a0440b; font-size: 13px; font-weight: 800; }
.tasks strong { font-size: 15px; }
.tasks li p { margin: 4px 0 0; color: #7a6e63; font-size: 13px; line-height: 1.75; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 26px; }
.primary, .secondary { display: inline-flex; align-items: center; justify-content: center; min-height: 48px; padding: 0 22px; border: 1px solid #b85111; border-radius: 999px; font-weight: 750; text-decoration: none; }
.primary { background: #b85111; color: #fff; }
.secondary { background: #fff; color: #b85111; }
@media (max-width: 600px) { .actions { flex-direction: column; }.actions a { width: 100%; }.tasks { padding: 22px; } }
</style>
