<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { RULE_DOCS } from '../../domain/settings';

const route = useRoute();
const router = useRouter();
const slug = computed(() => String(route.params.doc ?? 'terms'));
const doc = computed(() => RULE_DOCS.find((d) => d.slug === slug.value) ?? RULE_DOCS[0]);

const SECTIONS: Record<string, { h: string; p: string }[]> = {
  terms: [
    { h: '一、服务内容', p: '平台提供宠物档案管理、资讯、探索与用品导购（不含交易）等能力。' },
    { h: '二、账号规范', p: '后台实名（手机号）、前台自愿；禁止违规内容与商业灌水。' },
    { h: '三、责任与限制', p: '因不可抗力或第三方服务导致的中断，将在合理范围内免责。' },
  ],
  privacy: [
    { h: '一、收集的信息', p: '账号信息、宠物档案（属你的宠物数据）、日志与设备信息；相机/定位按需授权。' },
    { h: '二、使用目的', p: '提供核心功能、安全风控与（经同意的）匿名统计。' },
    { h: '三、你的权利', p: '查阅、更正、导出（JSON）、撤回同意、注销（15 天冷静期）。' },
    { h: '四、共享与委托', p: '活动报名信息经你单独同意后提供给活动发布方；保存 ≤90 天。' },
  ],
  community: [
    { h: '一、内容规范', p: '禁止色情低俗、政治敏感、广告引流、诈骗与违禁品信息。' },
    { h: '二、处置阶梯', p: 'L1 删内容 → L2 禁言 7 天 → L3 禁言 30 天 → L4 永久封禁；质量退修与违规分开计数。' },
    { h: '三、申诉', p: '对处置有异议可在受限账号页申诉，48 小时内反馈。' },
  ],
  children: [
    { h: '一、适用范围', p: '不满 14 周岁用户的个人信息处理规则。' },
    { h: '二、监护人同意', p: '注册时经监护人手机号验证同意；监护人可随时撤回（撤回将限制会话与推送）。' },
    { h: '三、我们的承诺', p: '不向儿童推送商业广告，不默认开放位置共享。' },
  ],
  report: [
    { h: '一、举报', p: '站内任意内容/用户/场所可举报，48 小时内处理并通知结果。' },
    { h: '二、版权通知-删除', p: '权利人提交合格通知后立即下架涉嫌内容；作者可反通知申诉。' },
    { h: '三、联系方式', p: 'copyright@example.com（示例占位，上线前替换为正式邮箱）。' },
  ],
};

const sections = computed(() => SECTIONS[slug.value] ?? SECTIONS.terms!);
</script>

<template>
  <div class="legal">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>{{ doc.label }}</h1>
    </header>
    <p class="meta">版本 v1.0 · 更新日期 2026-09-22 · 法务定稿前为产品占位文本</p>

    <nav class="toc" aria-label="目录">
      <a v-for="(s, i) in sections" :key="s.h" :href="`#sec-${i}`">{{ s.h }}</a>
    </nav>

    <section v-for="(s, i) in sections" :id="`sec-${i}`" :key="s.h" class="sec">
      <h2>{{ s.h }}</h2>
      <p>{{ s.p }}</p>
    </section>
  </div>
</template>

<style scoped>
.legal { max-width: 720px; margin: 0 auto; padding: 24px 16px 64px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.meta { color: #7a6e63; font-size: 12px; }
.toc { position: sticky; top: 72px; display: flex; gap: 10px; flex-wrap: wrap; background: #fff9f3; padding: 10px 0; border-bottom: 1px solid #f0e6dc; margin: 12px 0; }
.toc a { color: #ff7a2f; font-size: 13px; text-decoration: none; }
.sec h2 { font-size: 17px; margin: 20px 0 8px; }
.sec p { color: #2b2118; line-height: 1.9; font-size: 15px; }
</style>
