<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const documents = [
  {
    slug: 'terms',
    title: '用户协议',
    intro: '了解账号、服务范围与使用规则将如何说明。',
    sections: [
      { id: 'service', title: '服务范围', body: '正式版本将说明宠物档案、照护记录、内容阅读、附近探索和用品资料等功能，以及各项功能的适用条件。用品内容仅供参考，站内不提供购买、订单或支付。' },
      { id: 'account', title: '账号与使用', body: '正式版本将说明注册、登录、账号管理、内容发布和违规处置的规则。未成年人使用服务的条件将与儿童个人信息处理规则一同列明。' },
    ],
  },
  {
    slug: 'privacy',
    title: '隐私政策',
    intro: '了解个人信息、宠物资料与设备权限将如何被说明。',
    sections: [
      { id: 'information', title: '信息与用途', body: '正式版本将逐项说明账号信息、宠物档案、照护记录及设备信息的处理目的、范围和保存期限。定位、相机等权限只应在对应功能需要时请求。' },
      { id: 'family', title: '家庭共享', body: '加入家庭不应自动公开个人宠物。正式版本将说明宠物所有者主动共享后的可见范围、成员权限和撤销方式。' },
      { id: 'rights', title: '个人信息权利', body: '正式版本将列明查阅、更正、导出、撤回同意与注销等操作的条件、处理路径和联系渠道。' },
    ],
  },
  {
    slug: 'community',
    title: '社区规范',
    intro: '了解公开内容、投稿和社区互动的治理方向。',
    sections: [
      { id: 'content', title: '内容边界', body: '正式版本将说明内容发布、评测来源披露、广告或利益关系标注，以及违法和有害内容的处理标准。健康资讯属于科普，不替代兽医诊疗。' },
      { id: 'review', title: '审核与申诉', body: '投稿质量退修与违规处置应分别说明。正式版本将列明审核结果、处置原因、可申诉情形和实际反馈渠道。' },
    ],
  },
  {
    slug: 'children',
    title: '儿童个人信息处理规则',
    intro: '了解未满 14 周岁用户的使用和监护要求。',
    sections: [
      { id: 'scope', title: '适用对象', body: '正式版本将面向未满 14 周岁用户及其监护人，说明年龄判断、独立的监护人核验与同意流程。' },
      { id: 'guardian', title: '监护人权利', body: '正式版本将说明监护人如何查看同意状态、提出信息请求和撤回同意，以及撤回后对儿童账号的影响。' },
    ],
  },
  {
    slug: 'report',
    title: '投诉举报与版权',
    intro: '了解举报、申诉与版权通知所需的处理路径。',
    sections: [
      { id: 'reporting', title: '举报与申诉', body: '正式版本将说明可举报的内容、需要提供的材料、处理状态与结果通知方式。站内入口和接收渠道须在可用后公布。' },
      { id: 'copyright', title: '版权通知', body: '正式版本将说明权利证明、被投诉内容定位、通知与反通知的材料要求，以及可核验的接收渠道。' },
    ],
  },
] as const;

const slug = computed(() => String(route.params.doc ?? ''));
const current = computed(() => documents.find((item) => item.slug === slug.value));
</script>

<template>
  <div class="legal site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span>
      <router-link to="/help">帮助与规则</router-link><span aria-hidden="true">/</span>
      <span aria-current="page">{{ current?.title ?? '规则页面不可用' }}</span>
    </nav>

    <header class="intro">
      <p class="site-eyebrow">RULES & TRUST</p>
      <h1 class="site-section-title">{{ current?.title ?? '没有找到这份规则' }}</h1>
      <p class="site-lead">{{ current?.intro ?? '这个规则地址可能有误。你可以查看现有规则目录，或返回帮助中心。' }}</p>
    </header>

    <div class="legal-grid">
      <nav class="directory" aria-label="规则目录">
        <h2>规则目录</h2>
        <router-link
          v-for="item in documents"
          :key="item.slug"
          :to="'/legal/' + item.slug"
          :aria-current="item.slug === slug ? 'page' : undefined"
        >
          {{ item.title }}<span aria-hidden="true">→</span>
        </router-link>
      </nav>

      <div class="content">
        <template v-if="current">
          <div class="status" role="note">
            <strong>正式文本尚未发布</strong>
            <p>以下是拟说明主题，供了解产品方向；没有生效版本、发布日期或可用的法律文件接收渠道。请勿将本页作为注册同意或提交投诉的依据。</p>
          </div>

          <nav class="section-nav" aria-label="本页主题">
            <a v-for="section in current.sections" :key="section.id" :href="'#' + section.id">{{ section.title }}</a>
          </nav>

          <section v-for="section in current.sections" :id="section.id" :key="section.id" class="topic">
            <h2>{{ section.title }}</h2>
            <p>{{ section.body }}</p>
          </section>

          <div class="next">
            <div>
              <h2>需要进一步了解？</h2>
              <p>帮助中心提供当前可阅读的使用说明。正式支持与投诉渠道公布前，联系页会明确标出状态。</p>
            </div>
            <div class="actions">
              <router-link class="site-button outline" to="/help">查看帮助</router-link>
              <router-link class="site-text-link" to="/contact">联系渠道状态 <span aria-hidden="true">→</span></router-link>
            </div>
          </div>
        </template>
        <div v-else class="missing">
          <p>请从左侧目录选择需要查看的主题。</p>
          <router-link class="site-button" to="/help">返回帮助中心</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.legal { padding-block: 28px 100px; }
.crumb { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.intro { padding: clamp(38px, 6vw, 72px) 0 34px; }
.legal-grid { display: grid; grid-template-columns: minmax(220px, 280px) minmax(0, 1fr); align-items: start; gap: 24px; }
.directory, .content { border: 1px solid var(--site-line); border-radius: 22px; background: var(--site-paper); }
.directory { padding: 22px; }
.directory h2 { margin: 0 0 14px; font-size: 16px; }
.directory a { display: flex; justify-content: space-between; align-items: center; gap: 10px; min-height: 46px; padding: 10px 12px; border-radius: 11px; color: var(--site-muted); font-size: 14px; text-decoration: none; }
.directory a:hover, .directory a[aria-current="page"] { background: var(--site-soft); color: var(--site-action); }
.directory a[aria-current="page"] { font-weight: 750; }
.content { min-width: 0; padding: clamp(22px, 3.6vw, 42px); }
.status { padding: 20px 23px; border: 1px solid #e8d2bd; border-left: 4px solid var(--site-action); border-radius: 14px; background: #fffaf5; }
.status strong { font-size: 18px; }
.status p { margin: 5px 0 0; color: var(--site-muted); font-size: 14px; line-height: 1.8; }
.section-nav { display: flex; flex-wrap: wrap; gap: 8px; padding: 22px 0 4px; }
.section-nav a { display: inline-flex; align-items: center; min-height: 44px; padding: 8px 14px; border: 1px solid var(--site-line); border-radius: 999px; color: var(--site-action); font-size: 13px; font-weight: 700; text-decoration: none; }
.section-nav a:hover { background: var(--site-soft); }
.topic { padding: 25px 0; border-bottom: 1px solid var(--site-line); scroll-margin-top: 90px; }
.topic h2 { margin: 0 0 8px; font-size: 21px; }
.topic p { max-width: 66ch; margin: 0; color: var(--site-muted); line-height: 1.9; }
.next { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 24px; padding-top: 30px; }
.next h2 { margin: 0 0 6px; font-size: 20px; }
.next p { max-width: 50ch; margin: 0; color: var(--site-muted); font-size: 14px; }
.actions { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }
.missing { display: grid; justify-items: start; gap: 18px; }
.missing p { margin: 0; color: var(--site-muted); }
@media (max-width: 760px) {
  .legal { padding-block: 22px 68px; }
  .legal-grid { grid-template-columns: 1fr; gap: 14px; }
  .directory { padding: 18px; }
  .directory a { display: inline-flex; min-height: 44px; margin-right: 4px; }
  .directory a span { display: none; }
  .content { padding: 21px; }
}
</style>
