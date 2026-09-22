<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { PRODUCTS } from '../domain/site';

const route = useRoute();
const product = computed(() => PRODUCTS.find((p) => p.id === String(route.params.id)) ?? PRODUCTS[0]!);

const DETAIL: Record<string, { highlights: string[]; scenes: string[] }> = {
  web: {
    highlights: ['L2 生命链：建档→记录→曲线→摘要', '7 频道资讯与创作中心（先审后发）', '探索地图/路线/活动报名与领养信息', '家庭共享与逐动作权限', '纯内容导购：商品/清单/评测'],
    scenes: ['多宠家庭的日常照护记录', '带崽出行前做功课', '养宠新手按频道学习'],
  },
  admin: {
    highlights: ['审核工作台：SLA 排序/处理中锁/模板驳回/批量/快捷键', '封禁 L1–L4 + 高危二次确认', '看板按域权限 + 审计 ≤90 天', '32 权限点 RBAC 与 6 预置角色', '运行异常待办与逾期升级'],
    scenes: ['内容日清：投稿/评论/评价/举报/申诉五队列', '合规处置：证据留痕与申诉闭环'],
  },
  site: {
    highlights: ['品牌与服务说明', '内容中心与先审后发同源', '下载落地页四端一致', '分享预览与不可用四态', '联系/法律/帮助直达'],
    scenes: ['公开分享与获客转化', '商务与媒体沟通'],
  },
  mobile: {
    highlights: ['4-Tab 移动体验（用品并入首页）', '与 Web 同一契约（153 路由）', 'L1 登录闸门与 L3 三卡片', '一键记录与提醒完成', 'reduced-motion 动效降级'],
    scenes: ['出门在外随手记录', '扫码进入活动/路线'],
  },
};
const detail = computed(() => DETAIL[product.value.id] ?? DETAIL.web!);
</script>

<template>
  <div class="p-detail">
    <p class="crumb"><router-link to="/products">产品与服务</router-link> / {{ product.label }}</p>
    <h1>{{ product.label }}</h1>
    <p class="lead">{{ product.desc }}</p>

    <div class="meta">
      <span>端口 {{ product.port }}</span>
      <span>设计预览版 · 运行验证在本地测试阶段</span>
    </div>

    <section class="cols">
      <article class="card">
        <h2>核心亮点</h2>
        <ul><li v-for="h in detail.highlights" :key="h">{{ h }}</li></ul>
      </article>
      <article class="card">
        <h2>典型场景</h2>
        <ul><li v-for="s in detail.scenes" :key="s">{{ s }}</li></ul>
      </article>
    </section>

    <div class="actions">
      <router-link class="primary" to="/download">{{ product.cta }} →</router-link>
      <router-link class="ghost" to="/services">看服务场景</router-link>
      <router-link v-if="product.id === 'web'" class="ghost" to="/family">家庭共享专页</router-link>
    </div>
  </div>
</template>

<style scoped>
.p-detail { max-width: 860px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 16px; }
.crumb { color: #7a6e63; font-size: 13px; margin: 0; }
.crumb a { color: #ff7a2f; text-decoration: none; }
h1 { font-size: 34px; margin: 0; }
.lead { color: #7a6e63; font-size: 17px; margin: 0; line-height: 1.8; }
.meta { display: flex; gap: 14px; flex-wrap: wrap; color: #4d8dff; font-size: 13px; font-weight: 600; }
.cols { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px; }
.card h2 { margin: 0 0 12px; font-size: 17px; }
ul { margin: 0; padding-left: 18px; display: grid; gap: 8px; font-size: 14px; color: #2b2118; line-height: 1.7; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 28px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 13px 28px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
@media (max-width: 720px) { .cols { grid-template-columns: 1fr; } }
</style>
