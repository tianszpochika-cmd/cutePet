<script setup lang="ts">
import { FOUR_SECTIONS } from '../domain/site';

const capabilityDetails = {
  manage: { number: '01', task: '回看上一次照护，知道下一次何时处理。', points: ['宠物档案与健康时间线', '提醒计划与本次待办', '按权限邀请家人协作'], cue: '从记录开始' },
  news: { number: '02', task: '有疑问时按主题阅读，也能分享自己的经验。', points: ['按主题查找公开内容', '收藏值得回看的文章', '投稿状态与审核反馈'], cue: '从主题开始' },
  explore: { number: '03', task: '带宠出门前，先看看地点、路线与活动信息。', points: ['地图与列表查看场所', '核对宠物友好条件', '参考路线和本地活动'], cue: '从城市开始' },
  goods: { number: '04', task: '比较用品资料、评测与清单，慢慢做选择。', points: ['查看分类与资料', '阅读公开评测', '收藏清单以后回看'], cue: '从资料开始' },
} as const;
</script>

<template>
  <div class="products-page">
    <section class="intro">
      <div class="site-wrap intro-grid">
        <div>
          <p class="site-eyebrow">CARE, IN ONE PLACE</p>
          <h1>它的每一天，<br /><span>都有迹可循。</span></h1>
          <p class="site-lead">从第一份宠物档案，到每次健康记录、下一次提醒和家人的接力照顾。需要阅读、出门或挑选用品时，也能找到清楚的参考。</p>
          <div class="intro-actions">
            <a class="site-button" href="#abilities">认识四大能力 <span aria-hidden="true">↓</span></a>
            <router-link class="site-button outline" to="/services">看看使用场景 <span aria-hidden="true">↗</span></router-link>
          </div>
        </div>
        <div class="care-preview" role="group" aria-label="照护路径示意：档案、健康记录、本次待办、下一次安排">
          <span class="preview-label">照护路径 · 界面示意</span>
          <div class="preview-pet"><span aria-hidden="true">♡</span><div><strong>先认识它</strong><small>基础信息与健康记录放在一起</small></div></div>
          <ol>
            <li><span>01</span><strong>建立档案</strong><small>随时补充</small></li>
            <li><span>02</span><strong>留下记录</strong><small>按时间回看</small></li>
            <li><span>03</span><strong>确认本次待办</strong><small>处理结果清楚</small></li>
            <li><span>04</span><strong>安排下一次</strong><small>日期由你确认</small></li>
          </ol>
          <p>这里展示使用方式，不连接任何人的真实宠物资料。</p>
        </div>
      </div>
    </section>

    <section id="abilities" class="site-wrap abilities" aria-labelledby="abilities-heading">
      <div class="section-heading">
        <div><p class="site-eyebrow">FOUR THINGS, ONE EVERYDAY LIFE</p><h2 id="abilities-heading" class="site-section-title">照顾它的四件事，<br />都能从这里开始。</h2></div>
        <p class="site-lead">先把重要的照护做清楚。想看知识、出门走走或比较用品时，再按自己的需要往下看。</p>
      </div>
      <div class="capability-grid">
        <article v-for="section in FOUR_SECTIONS" :key="section.id" class="capability" :class="section.id">
          <div class="capability-top"><span>{{ capabilityDetails[section.id].number }} / 04</span><span class="capability-cue">{{ capabilityDetails[section.id].cue }}</span></div>
          <h3>{{ section.label }}</h3>
          <p class="task">{{ capabilityDetails[section.id].task }}</p>
          <ul><li v-for="point in capabilityDetails[section.id].points" :key="point">{{ point }}</li></ul>
          <router-link class="site-text-link" :to="section.to">了解{{ section.label }} <span aria-hidden="true">→</span></router-link>
        </article>
      </div>
    </section>

    <section class="promise">
      <div class="site-wrap promise-grid">
        <div><p class="site-eyebrow">CLEAR BY DESIGN</p><h2 class="site-section-title">能做什么，<br />由谁操作，都说清楚。</h2></div>
        <div class="promise-list">
          <article><span>01</span><div><h3>照护结果有依据</h3><p>健康记录与本次待办相互关联；下一次日期和周期由用户核对。平台确认之前，不把操作写成“已完成”。</p></div></article>
          <article><span>02</span><div><h3>家庭共享有范围</h3><p>宠物所有者逐只决定共享对象和权限。加入家庭不会自动开放私有档案。</p></div></article>
          <article><span>03</span><div><h3>阅读与选购有边界</h3><p>公开投稿先审核；用品提供资料、评测和清单作为参考，第一版没有站内订单与支付。</p></div></article>
        </div>
      </div>
    </section>

    <section class="site-wrap next-step">
      <div><p class="site-eyebrow">CARE TOGETHER</p><h2>一个人照顾，或一家人接力。</h2><p>把需要协作的宠物共享给家人，分清可管理与只读；敏感健康摘要仍由宠物所有者处理。</p></div>
      <router-link class="site-button outline" to="/family">了解家庭共享 <span aria-hidden="true">→</span></router-link>
    </section>

    <section class="site-wrap ending"><p>官网用于介绍能力和公开信息。建档、记录、投稿与活动报名需要进入用户端，并由你再次确认。</p><router-link class="site-button" to="/download">查看使用方式 <span aria-hidden="true">↗</span></router-link></section>
  </div>
</template>

<style scoped>
.products-page{overflow:hidden}.intro{padding:82px 0 96px;background:radial-gradient(circle at 84% 18%,#ffe6d1,transparent 35%),linear-gradient(145deg,#fff9f3,#fff2e8)}.intro-grid{display:grid;grid-template-columns:1fr .86fr;gap:8%;align-items:center}.intro h1{margin:0;font-size:clamp(42px,5vw,70px);font-weight:850;line-height:1.15;letter-spacing:-.07em}.intro h1 span{color:var(--site-action)}.intro .site-lead{max-width:600px;margin-top:26px;font-size:17px}.intro-actions{display:flex;flex-wrap:wrap;gap:12px;margin-top:32px}.care-preview{position:relative;max-width:490px;padding:28px;border:1px solid #f1d9c5;border-radius:28px;background:rgba(255,255,255,.94);box-shadow:0 24px 64px rgba(77,45,25,.08);transform:rotate(2deg)}.preview-label{color:var(--site-action);font-size:12px;font-weight:800;letter-spacing:.08em}.preview-pet{display:flex;align-items:center;gap:14px;margin:23px 0;padding:16px;border-radius:17px;background:#fff1e8}.preview-pet>span{display:grid;place-items:center;width:46px;height:46px;border-radius:15px;background:#fff;color:var(--site-action);font-size:27px}.preview-pet strong,.preview-pet small{display:block}.preview-pet small{margin-top:3px;color:var(--site-muted);font-size:12px}.care-preview ol{margin:0;padding:0;list-style:none}.care-preview li{display:flex;align-items:center;gap:12px;min-height:53px;border-top:1px solid var(--site-line)}.care-preview li span{color:var(--site-action);font-size:12px;font-weight:800}.care-preview li strong{font-size:14px}.care-preview li small{margin-left:auto;color:var(--site-muted);font-size:12px}.care-preview>p{margin:15px 0 0;color:var(--site-muted);font-size:12px}.abilities{padding-top:100px;padding-bottom:100px}.section-heading{display:flex;justify-content:space-between;align-items:end;gap:32px;margin-bottom:36px}.section-heading .site-lead{max-width:340px}.capability-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px}.capability{display:flex;flex-direction:column;min-height:380px;padding:25px;border:1px solid var(--site-line);border-radius:24px;background:#fff}.capability.manage{background:#fff1e8}.capability.news{background:#fff1f4}.capability.explore{background:#edf8f1}.capability.goods{background:#edf3ff}.capability-top{display:flex;justify-content:space-between;gap:8px;color:var(--site-muted);font-size:11px;font-weight:800}.capability-cue{padding:4px 9px;border-radius:999px;background:#fff}.capability h3{margin:38px 0 8px;font-size:22px;line-height:1.3}.capability .task{min-height:72px;margin:0;color:var(--site-muted);font-size:14px}.capability ul{display:grid;gap:8px;margin:18px 0 23px;padding-left:18px;font-size:13px}.capability li::marker{color:var(--site-action)}.capability .site-text-link{margin-top:auto;font-size:13px}.promise{padding:92px 0;background:#f6efe8}.promise-grid{display:grid;grid-template-columns:.78fr 1.22fr;gap:10%;align-items:start}.promise-list{display:grid;gap:22px}.promise-list article{display:flex;gap:20px;padding-bottom:20px;border-bottom:1px solid #ddcfc2}.promise-list article>span{color:var(--site-action);font-size:13px;font-weight:800}.promise-list h3{margin:0 0 7px;font-size:18px}.promise-list p{margin:0;color:var(--site-muted);font-size:14px}.next-step{display:flex;justify-content:space-between;align-items:center;gap:24px;margin-top:96px;padding:42px;border-radius:28px;background:#f2ecfb}.next-step h2{margin:0;font-size:clamp(26px,3vw,38px);line-height:1.25}.next-step p:last-child{max-width:640px;margin:12px 0 0;color:#5d4c6d}.next-step .site-button{flex:none}.ending{display:flex;justify-content:space-between;align-items:center;gap:22px;padding-top:48px;padding-bottom:96px}.ending p{max-width:600px;margin:0;color:var(--site-muted)}.ending .site-button{flex:none}@media(max-width:1000px){.capability-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.capability{min-height:350px}.intro-grid{gap:5%}}@media(max-width:760px){.intro{padding:58px 0 70px}.intro-grid,.promise-grid{grid-template-columns:1fr;gap:42px}.care-preview{max-width:none;transform:none}.section-heading,.next-step,.ending{align-items:start;flex-direction:column}.abilities{padding-top:72px;padding-bottom:72px}.promise{padding:72px 0}.next-step{margin-top:72px;padding:30px}.ending{padding-bottom:72px}}@media(max-width:520px){.intro-actions .site-button{width:100%}.capability-grid{grid-template-columns:1fr}.capability{min-height:320px}.capability h3{margin-top:24px}.capability .task{min-height:0}.next-step .site-button,.ending .site-button{width:100%}}
</style>
