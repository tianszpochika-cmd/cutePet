<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { FOUR_SECTIONS } from '../domain/site';

const DETAILS = {
  manage: {
    eyebrow: 'CARE, STEP BY STEP',
    title: '把上一次和下一次，连成一条线。',
    intro: '先建立宠物档案，再留下每次健康变化。提醒计划和本次待办分开看，家人也能在获得授权后接力照顾。',
    forWhom: '适合有固定疫苗、驱虫、体检或复诊安排的养宠人，以及共同照护家庭。',
    previewTitle: '照护路径',
    previewLines: ['宠物档案 · 了解基本情况', '健康记录 · 回看实际发生的事', '本次待办 · 处理前再核对', '下一次安排 · 日期由你确认'],
    steps: [
      { title: '建立档案', copy: '分步填写基础信息，照片可以稍后补充。' },
      { title: '留下记录', copy: '按实际发生时间记录体重、疫苗、驱虫等健康事项。' },
      { title: '安排提醒', copy: '明确计划与本次待办，核对周期和下一次日期。' },
      { title: '家人接力', copy: '只共享需要协作的宠物，查看由谁处理了哪件事。' },
    ],
    boundary: '疫苗、驱虫等健康待办须关联相应记录；记录和本次待办一起经平台确认后，才显示完成。健康摘要仅由宠物所有者核验生成。',
    faq: [
      { question: '建档后能直接设提醒吗？', answer: '可以从建档完成引导进入第一条提醒。日期和周期仍由你核对后确认。' },
      { question: '家人能看到我所有宠物吗？', answer: '不会。加入同一家庭不等于共享所有宠物，所有者需要逐只授权。' },
    ],
    related: 'family',
    relatedLabel: '了解家庭共享',
  },
  news: {
    eyebrow: 'READ, SAVE, SHARE',
    title: '需要知识的时候，找得到、看得懂。',
    intro: '按主题阅读公开内容，收藏以后可能还会用到的文章；想分享经验时，可以投稿并查看审核反馈。',
    forWhom: '适合想按主题查资料、整理收藏，或分享养宠经验的人。',
    previewTitle: '从阅读到分享',
    previewLines: ['主题频道 · 从关心的话题开始', '公开文章 · 阅读当前可展示版本', '我的收藏 · 方便日后回看', '投稿状态 · 看见审核反馈'],
    steps: [
      { title: '按主题查找', copy: '用犬、猫、营养等频道缩小范围。' },
      { title: '阅读公开内容', copy: '查看已审核并仍可公开展示的文章。' },
      { title: '收藏以后回看', copy: '将有用内容留在自己的收藏中。' },
      { title: '投稿并等反馈', copy: '文章、评测和清单进入审核流程，作者可查看状态。' },
    ],
    boundary: '投稿先审后发；未审核、已驳回或已下架的内容不作为公开推荐。涉及疾病和用药的文章只作科普参考，不能代替诊疗。',
    faq: [
      { question: '投稿后会立刻公开吗？', answer: '不会。投稿进入审核流程，通过后才公开展示；需要修改时会给作者反馈。' },
      { question: '官网能直接收藏和评论吗？', answer: '官网用于公开阅读；收藏、评论等个人操作要进入用户端，并按登录状态确认。' },
    ],
    related: 'goods',
    relatedLabel: '了解用品参考',
  },
  explore: {
    eyebrow: 'GO OUT TOGETHER',
    title: '出门之前，先把路线和去处看一看。',
    intro: '从城市与场所条件开始，查看宠物友好信息、遛宠路线和本地活动，为一起出门做准备。',
    forWhom: '适合需要查附近场所、计划散步路线或了解本地活动的养宠人。',
    previewTitle: '出门前的四件事',
    previewLines: ['选城市 · 不授权定位也能浏览', '看场所 · 核对设施与规则', '参考路线 · 了解路径和距离', '看活动 · 阅读条件再决定'],
    steps: [
      { title: '确定城市', copy: '允许定位时查看附近；拒绝定位时手动选城市。' },
      { title: '核对场所', copy: '用类型和友好属性筛选，再看当前详情。' },
      { title: '参考路线', copy: '浏览公开路线，出发前核实现场条件。' },
      { title: '了解活动', copy: '先看时间、地点和规则；报名进入用户端再次确认。' },
    ],
    boundary: '地点、路线和评价都是出行参考，不保证现场仍按同样规则开放。活动报名须在用户端阅读告知并确认；领养信息不提供交易入口。',
    faq: [
      { question: '不允许定位还能使用吗？', answer: '可以手动选择城市。地图或定位不可用时，应保留列表方式查看。' },
      { question: '看到活动就算报名了吗？', answer: '不算。只有进入用户端、阅读告知并提交后，才可能完成报名。' },
    ],
    related: 'news',
    relatedLabel: '了解养宠资讯',
  },
  goods: {
    eyebrow: 'CHOOSE WITH CONTEXT',
    title: '不急着买，先把差别看明白。',
    intro: '查看用品资料、相关评测与好物清单，把想比较的内容收藏起来，等需要时再回看。',
    forWhom: '适合想先做功课、比较用品信息，不希望被催促下单的人。',
    previewTitle: '用品内容参考',
    previewLines: ['用品资料 · 了解分类与参数', '公开评测 · 对照使用场景', '好物清单 · 集中比较理由', '收藏回看 · 不必当场决定'],
    steps: [
      { title: '查资料', copy: '先了解分类、参数和适用情况。' },
      { title: '读评测', copy: '查看经过审核的公开内容及其来源说明。' },
      { title: '看清单', copy: '比较商品与推荐理由，留意失效信息。' },
      { title: '收藏回访', copy: '把有用的参考保留，等需要时再看。' },
    ],
    boundary: '第一版只提供内容参考，不提供站内订单、支付或售后。兽药与处方类不进入用品内容库；资料、价格和适用性应以当前可靠来源核对。',
    faq: [
      { question: '可以在这里购买吗？', answer: '第一版没有站内购买、订单和支付流程。用品页提供资料、评测和清单。' },
      { question: '评测都是真实测试吗？', answer: '评测需要说明来源与利益关系；资料整理不能被标成实际测试。' },
    ],
    related: 'manage',
    relatedLabel: '了解宠物管理',
  },
} as const;

const route = useRoute();
const id = computed(() => String(route.params.id));
const section = computed(() => FOUR_SECTIONS.find((item) => item.id === id.value));
const detail = computed(() => DETAILS[id.value as keyof typeof DETAILS] ?? null);
const relatedTo = computed(() => detail.value?.related === 'family' ? '/family' : `/products/${detail.value?.related ?? 'manage'}`);
</script>

<template>
  <div v-if="section && detail" class="detail-page" :class="id">
    <section class="detail-hero">
      <div class="site-wrap">
        <p class="crumb"><router-link to="/products">四大能力</router-link><span aria-hidden="true"> / </span>{{ section.label }}</p>
        <div class="hero-grid">
          <div>
            <p class="site-eyebrow">{{ detail.eyebrow }}</p>
            <h1>{{ detail.title }}</h1>
            <p class="site-lead">{{ detail.intro }}</p>
            <div class="hero-actions"><a class="site-button" href="#how-it-works">看看如何使用 <span aria-hidden="true">↓</span></a><router-link class="site-button outline" to="/download">查看使用方式 <span aria-hidden="true">↗</span></router-link></div>
          </div>
          <div class="preview" role="group" aria-label="能力界面结构示意">
            <span class="preview-top">界面结构示意 · 无真实账户数据</span>
            <h2>{{ detail.previewTitle }}</h2>
            <ul><li v-for="(line, index) in detail.previewLines" :key="line"><span>{{ String(index + 1).padStart(2, '0') }}</span>{{ line }}</li></ul>
          </div>
        </div>
      </div>
    </section>

    <section class="site-wrap audience"><span>适合谁</span><p>{{ detail.forWhom }}</p></section>

    <section id="how-it-works" class="site-wrap how" aria-labelledby="how-heading">
      <div class="section-head"><div><p class="site-eyebrow">HOW IT WORKS</p><h2 id="how-heading" class="site-section-title">从一件事开始，<br />一步步往前走。</h2></div><p>以下是使用路径示意。实际保存、审核、报名或通知结果，以用户端和平台确认为准。</p></div>
      <ol class="steps"><li v-for="(step, index) in detail.steps" :key="step.title"><span>{{ String(index + 1).padStart(2, '0') }}</span><h3>{{ step.title }}</h3><p>{{ step.copy }}</p></li></ol>
    </section>

    <section class="boundary"><div class="site-wrap boundary-grid"><div><p class="site-eyebrow">GOOD TO KNOW</p><h2 class="site-section-title">能力的边界，<br />也值得先知道。</h2></div><p>{{ detail.boundary }}</p></div></section>

    <section class="site-wrap closing">
      <div class="faq"><h2>你可能还想知道</h2><details v-for="item in detail.faq" :key="item.question"><summary>{{ item.question }}</summary><p>{{ item.answer }}</p></details></div>
      <div class="more"><p class="site-eyebrow">KEEP EXPLORING</p><h2>把照护和生活连接起来。</h2><p>继续了解相关能力，或查看当前可用的 Web 与手机入口状态。</p><router-link class="site-text-link" :to="relatedTo">{{ detail.relatedLabel }} <span aria-hidden="true">→</span></router-link><router-link class="site-button" to="/download">查看使用方式 <span aria-hidden="true">↗</span></router-link></div>
    </section>
  </div>
  <section v-else class="site-wrap missing"><p class="site-eyebrow">PAGE NOT FOUND</p><h1>这项能力暂时找不到</h1><p>可以回到产品总览，查看宠物管理、资讯、探索与用品。</p><router-link class="site-button" to="/products">返回四大能力</router-link></section>
</template>

<style scoped>
.detail-page{--detail-tint:#fff1e8}.detail-page.news{--detail-tint:#fff1f4}.detail-page.explore{--detail-tint:#edf8f1}.detail-page.goods{--detail-tint:#edf3ff}.detail-hero{padding:28px 0 94px;background:radial-gradient(circle at 75% 20%,var(--detail-tint),transparent 50%),var(--site-bg)}.crumb{margin:0 0 50px;color:var(--site-muted);font-size:13px}.crumb a{color:var(--site-action);text-decoration:none}.hero-grid{display:grid;grid-template-columns:1fr .86fr;align-items:center;gap:9%}.hero-grid h1{max-width:660px;margin:0;font-size:clamp(38px,4.6vw,64px);line-height:1.18;letter-spacing:-.06em}.hero-grid .site-lead{margin-top:22px;font-size:17px}.hero-actions{display:flex;flex-wrap:wrap;gap:12px;margin-top:32px}.preview{padding:28px;border:1px solid var(--site-line);border-radius:27px;background:#fff;box-shadow:0 22px 50px rgba(74,44,24,.07)}.preview-top{color:var(--site-action);font-size:11px;font-weight:800;letter-spacing:.06em}.preview h2{margin:18px 0;font-size:23px}.preview ul{margin:0;padding:0;list-style:none}.preview li{display:flex;gap:14px;align-items:center;min-height:57px;border-top:1px solid var(--site-line);font-size:14px}.preview li span{color:var(--site-action);font-size:12px;font-weight:800}.audience{display:flex;align-items:center;gap:22px;padding-top:25px;padding-bottom:25px;border-top:1px solid var(--site-line);border-bottom:1px solid var(--site-line)}.audience span{flex:none;color:var(--site-action);font-size:12px;font-weight:800}.audience p{margin:0;font-size:15px}.how{padding-top:96px;padding-bottom:105px}.section-head{display:flex;align-items:end;justify-content:space-between;gap:30px}.section-head>p{max-width:365px;margin:0;color:var(--site-muted);font-size:14px}.steps{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px;margin:38px 0 0;padding:0;list-style:none}.steps li{min-height:235px;padding:24px;border:1px solid var(--site-line);border-radius:21px;background:#fff}.steps li>span{display:grid;place-items:center;width:41px;height:41px;border-radius:13px;background:var(--detail-tint);color:var(--site-action);font-size:13px;font-weight:800}.steps h3{margin:27px 0 8px;font-size:18px}.steps p{margin:0;color:var(--site-muted);font-size:13px}.boundary{padding:86px 0;background:#f6efe8}.boundary-grid{display:grid;grid-template-columns:1fr 1fr;gap:10%;align-items:center}.boundary-grid>p{margin:0;font-size:17px;line-height:1.9}.closing{display:grid;grid-template-columns:1fr .8fr;gap:10%;padding-top:95px;padding-bottom:110px}.faq h2,.more h2{margin:0 0 20px;font-size:clamp(25px,3vw,34px);line-height:1.25}.faq details{border-top:1px solid var(--site-line)}.faq details:last-child{border-bottom:1px solid var(--site-line)}.faq summary{display:flex;align-items:center;justify-content:space-between;min-height:68px;cursor:pointer;font-weight:700;list-style:none}.faq summary::after{content:'+';color:var(--site-action);font-size:23px}.faq details[open] summary::after{content:'−'}.faq details p{margin:0 0 22px;color:var(--site-muted);font-size:14px}.more{padding:29px;border-radius:25px;background:var(--detail-tint)}.more p:not(.site-eyebrow){color:var(--site-muted);font-size:14px}.more .site-text-link{display:flex}.more .site-button{margin-top:22px}.missing{padding-top:100px;padding-bottom:120px}.missing h1{margin:0;font-size:42px}.missing p:not(.site-eyebrow){color:var(--site-muted)}@media(max-width:1000px){.steps{grid-template-columns:repeat(2,minmax(0,1fr))}.hero-grid{gap:5%}}@media(max-width:760px){.detail-hero{padding-bottom:72px}.crumb{margin-bottom:35px}.hero-grid,.boundary-grid,.closing{grid-template-columns:1fr;gap:32px}.section-head{align-items:start;flex-direction:column}.how{padding-top:72px;padding-bottom:72px}.boundary{padding:72px 0}.closing{padding-top:72px;padding-bottom:72px}}@media(max-width:520px){.hero-actions .site-button{width:100%}.steps{grid-template-columns:1fr}.steps li{min-height:0}.audience{align-items:start;flex-direction:column;gap:5px}.missing h1{font-size:32px}}
</style>
