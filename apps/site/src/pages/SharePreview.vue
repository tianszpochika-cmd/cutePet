<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

type ShareKind = 'article' | 'event' | 'route' | 'summary';

const route = useRoute();

function shareKind(value: unknown): ShareKind | null {
  if (typeof value !== 'string') return null;
  if (value === 'article' || value === 'a') return 'article';
  if (value === 'event' || value === 'route' || value === 'summary') return value;
  return null;
}

const context = computed(() => {
  const kind = shareKind(route.params.kind);
  switch (kind) {
    case 'article':
      return { label: '文章分享', detail: '文章的当前公开状态、作者和来源尚无法核验。', destination: '/center', action: '返回内容中心' };
    case 'event':
      return { label: '活动分享', detail: '活动的时间、地点、主办方和报名状态尚无法核验。', destination: '/events', action: '查看活动栏目' };
    case 'route':
      return { label: '路线分享', detail: '路线的审核状态和公开范围尚无法核验。', destination: '/products', action: '了解产品能力' };
    case 'summary':
      return { label: '健康摘要分享', detail: '健康摘要涉及私人资料，目前无法核验分享权限，因此不展示任何健康信息。', destination: '/family', action: '了解家庭隐私' };
    default:
      return { label: '分享链接', detail: '链接类型无法识别，也无法核验其内容状态。', destination: '/', action: '返回官网首页' };
  }
});
</script>

<template>
  <div class="share site-wrap">
    <nav class="crumb" aria-label="当前位置">
      <router-link to="/">首页</router-link><span aria-hidden="true">/</span><span aria-current="page">分享内容</span>
    </nav>

    <section class="card" aria-labelledby="share-title" role="status" data-testid="share-card">
      <div class="mark" aria-hidden="true">⌁</div>
      <p class="site-eyebrow">{{ context.label }} · 暂不可用</p>
      <h1 id="share-title" class="site-section-title">这条分享，<br />暂时无法核验。</h1>
      <p class="lead">{{ context.detail }}为避免展示过期或未经发布的资料，官网不会仅凭链接地址显示标题、图片或“已发布”状态。</p>
      <div class="actions">
        <router-link class="site-button" :to="context.destination">{{ context.action }}</router-link>
        <router-link class="site-button outline" to="/help">查看帮助</router-link>
      </div>
    </section>

    <p class="footnote">请以原应用内的当前状态为准。若内容后续接入公开数据源，此页面仍需再次核验发布状态、可见范围和来源后才能展示。</p>
  </div>
</template>

<style scoped>
.share { padding-block: 32px 88px; }
.crumb { display: flex; align-items: center; gap: 10px; color: var(--site-muted); font-size: 13px; }
.crumb a { color: inherit; text-decoration: none; }
.crumb a:hover { color: var(--site-action); text-decoration: underline; }
.card { position: relative; overflow: hidden; max-width: 850px; margin: clamp(48px, 7vw, 96px) auto 0; padding: clamp(32px, 6vw, 68px); border: 1px solid var(--site-line); border-radius: 28px; background: #fff; box-shadow: 0 20px 54px rgba(43, 33, 24, .06); }
.card::after { content: ''; position: absolute; right: -100px; top: -120px; width: 300px; aspect-ratio: 1; border-radius: 50%; background: var(--site-soft); pointer-events: none; }
.card > * { position: relative; z-index: 1; }
.mark { display: grid; place-items: center; width: 62px; height: 62px; margin-bottom: 30px; border-radius: 18px; background: var(--site-soft); color: var(--site-action); font-size: 42px; }
.card h1 { max-width: 650px; }
.lead { max-width: 640px; margin: 20px 0 0; color: var(--site-muted); font-size: 16px; line-height: 1.9; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 30px; }
.footnote { max-width: 800px; margin: 26px auto 0; color: var(--site-muted); font-size: 13px; line-height: 1.8; }
@media (max-width: 760px) {
  .share { padding-block: 22px 66px; }
  .card { margin-top: 50px; }
  .card::after { width: 220px; right: -110px; top: -95px; }
  .actions .site-button { width: 100%; }
}
</style>
