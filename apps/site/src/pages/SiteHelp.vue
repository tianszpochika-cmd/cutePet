<script setup lang="ts">
const groups = [
  {
    id: 'start',
    title: '开始使用',
    questions: [
      { question: '现在可以从官网登录或下载吗？', answer: '官网负责介绍产品。电脑 Web 和手机 H5 的正式入口会在地址与发布状态核验后放在“开始使用”页；页面标为准备中时，请不要把它当作已可用的登录或安装入口。' },
      { question: '未满 14 周岁可以自行注册吗？', answer: '产品规划要求独立的监护人核验与同意流程。正式注册服务开放前，年龄判断、监护同意及相应规则仍需完成核验。' },
    ],
  },
  {
    id: 'care',
    title: '宠物与照护',
    questions: [
      { question: '记录与提醒有什么区别？', answer: '照护记录用于记下已经发生的事；提醒计划用于安排未来的事，待办对应其中某一次。健康类待办完成时，应关联有效记录，并以平台确认后的结果为准。' },
    ],
  },
  {
    id: 'family',
    title: '家庭协作',
    questions: [
      { question: '加入家庭后，家人会看到我全部宠物吗？', answer: '不会。每只宠物由所有者主动决定是否共享；新加入成员对已经共享的宠物默认只读。家庭管理员身份也不会自动取得其他成员宠物的管理权。' },
    ],
  },
  {
    id: 'content',
    title: '内容与活动',
    questions: [
      { question: '官网能直接投稿或报名活动吗？', answer: '不能。官网只展示可以公开的说明和内容。投稿、互动及活动报名应在可用的用户端完成登录、告知与确认，官网不会替你提交。' },
      { question: '养宠资讯可以代替兽医建议吗？', answer: '不能。健康类内容仅供科普参考；疾病、用药或紧急情况应咨询有资质的兽医。' },
    ],
  },
  {
    id: 'safety',
    title: '隐私与支持',
    questions: [
      { question: '如何举报、申诉或提出版权请求？', answer: '正式接收渠道和处理流程仍待公布。目前可先阅读规则页中的主题说明；联系页会显示渠道的真实开放状态，不会让你把材料发往示例地址。' },
    ],
  },
] as const;
</script>

<template>
  <div class="help site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">帮助中心</span>
    </nav>

    <header class="intro">
      <p class="site-eyebrow">HELP CENTER</p>
      <h1 class="site-section-title">遇到问题，先找到下一步。</h1>
      <p class="site-lead">按使用情境查看简明说明。关于正式联系、举报与版权渠道的状态，请以联系页为准。</p>
    </header>

    <section class="priority" aria-labelledby="priority-title">
      <div>
        <span class="priority-label">重要入口</span>
        <h2 id="priority-title">投诉、申诉与版权请求</h2>
        <p>了解需要准备的信息和渠道开放状态。当前没有经过核验的在线提交服务，本页不会声称请求已被接收。</p>
      </div>
      <router-link class="site-button outline" to="/contact">查看联系状态 <span aria-hidden="true">→</span></router-link>
    </section>

    <div class="help-grid">
      <nav class="directory" aria-label="帮助主题">
        <h2>帮助主题</h2>
        <a v-for="group in groups" :key="group.id" :href="'#' + group.id">{{ group.title }} <span aria-hidden="true">↗</span></a>
      </nav>

      <div class="faqs">
        <section v-for="group in groups" :id="group.id" :key="group.id" class="group" :aria-labelledby="'heading-' + group.id">
          <h2 :id="'heading-' + group.id">{{ group.title }}</h2>
          <details v-for="item in group.questions" :key="item.question" class="question">
            <summary>{{ item.question }}<span class="plus" aria-hidden="true">＋</span></summary>
            <p>{{ item.answer }}</p>
          </details>
        </section>
      </div>
    </div>

    <section class="next">
      <div>
        <h2>还想了解使用方式？</h2>
        <p>查看四项能力说明，或阅读规则主题。规则的正式文本尚未发布，页面会明确标注状态。</p>
      </div>
      <div class="next-links">
        <router-link class="site-text-link" to="/products">查看产品能力 <span aria-hidden="true">→</span></router-link>
        <router-link class="site-text-link" to="/legal/terms">查看规则主题 <span aria-hidden="true">→</span></router-link>
      </div>
    </section>
  </div>
</template>

<style scoped>
.help { padding-block: 28px 100px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.intro { padding: clamp(38px, 6vw, 72px) 0 36px; }
.priority { display: flex; align-items: center; justify-content: space-between; gap: 24px; padding: clamp(24px, 4vw, 38px); border: 1px solid #e6dbf6; border-radius: 22px; background: #f7f3fc; }
.priority-label { color: #5f427e; font-size: 12px; font-weight: 800; letter-spacing: .1em; }
.priority h2 { margin: 9px 0 5px; font-size: clamp(21px, 2.4vw, 27px); }
.priority p { max-width: 65ch; margin: 0; color: var(--site-muted); font-size: 14px; line-height: 1.8; }
.priority a { flex: none; }
.help-grid { display: grid; grid-template-columns: minmax(210px, 260px) minmax(0, 1fr); align-items: start; gap: 36px; padding-top: 62px; }
.directory { position: sticky; top: 110px; padding: 22px; border: 1px solid var(--site-line); border-radius: 18px; background: var(--site-paper); }
.directory h2 { margin: 0 0 12px; font-size: 16px; }
.directory a { display: flex; justify-content: space-between; align-items: center; min-height: 44px; padding: 8px 0; border-bottom: 1px solid var(--site-line); color: var(--site-muted); font-size: 14px; text-decoration: none; }
.directory a:last-child { border: 0; }
.directory a:hover { color: var(--site-action); }
.faqs { min-width: 0; }
.group { padding-bottom: 38px; scroll-margin-top: 96px; }
.group h2 { margin: 0 0 16px; font-size: 23px; }
.question { margin-bottom: 10px; border: 1px solid var(--site-line); border-radius: 15px; background: #fff; }
.question summary { display: flex; align-items: center; justify-content: space-between; gap: 15px; min-height: 58px; padding: 13px 19px; color: var(--site-ink); font-size: 15px; font-weight: 700; cursor: pointer; list-style: none; }
.question summary::-webkit-details-marker { display: none; }
.question[open] { border-color: #e6c9b5; }
.question[open] .plus { transform: rotate(45deg); }
.plus { flex: none; color: var(--site-action); font-size: 20px; font-weight: 400; transition: transform .2s var(--site-ease); }
.question p { margin: 0; padding: 0 19px 19px; color: var(--site-muted); font-size: 14px; line-height: 1.85; }
.next { display: flex; justify-content: space-between; gap: 24px; padding: 30px 0 0; border-top: 1px solid var(--site-line); }
.next h2 { margin: 0 0 6px; font-size: 22px; }
.next p { max-width: 58ch; margin: 0; color: var(--site-muted); font-size: 14px; }
.next-links { display: flex; flex-wrap: wrap; gap: 16px; align-content: start; }
@media (max-width: 760px) {
  .help { padding-block: 22px 68px; }
  .priority, .next { flex-direction: column; align-items: flex-start; }
  .help-grid { grid-template-columns: 1fr; gap: 32px; padding-top: 45px; }
  .directory { position: static; }
  .directory a { display: inline-flex; gap: 7px; margin-right: 17px; border: 0; }
}
</style>
