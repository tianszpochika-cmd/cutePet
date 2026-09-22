<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const doc = computed(() => String(route.params.doc ?? 'terms'));

const DOCS: Record<string, { title: string; sections: { h: string; p: string }[] }> = {
  terms: {
    title: '用户协议',
    sections: [
      { h: '一、服务内容', p: '宠物档案管理、资讯、探索与用品导购（不含交易）等能力。' },
      { h: '二、账号规范', p: '后台实名（手机号）、前台自愿；禁止违规内容与商业灌水。' },
      { h: '三、责任与限制', p: '不可抗力与第三方服务中断在合理范围内免责；处置阶梯见社区规范。' },
    ],
  },
  privacy: {
    title: '隐私政策',
    sections: [
      { h: '一、收集的信息', p: '账号信息、宠物档案、日志与设备信息；相机/定位按需授权。' },
      { h: '二、使用目的', p: '核心功能、安全风控与经同意的匿名统计（未同意不加载可选能力）。' },
      { h: '三、你的权利', p: '查阅、更正、导出（JSON）、撤回同意、注销（15 天冷静期可撤销）。' },
      { h: '四、共享与委托', p: '活动报名信息经单独同意提供给发布方，保存 ≤90 天。' },
    ],
  },
  community: {
    title: '社区规范',
    sections: [
      { h: '一、内容规范', p: '禁止色情低俗、政治敏感、广告引流、诈骗与违禁品信息；兽药/处方类禁入。' },
      { h: '二、处置阶梯', p: 'L1 删内容 → L2 禁言 7 天 → L3 禁言 30 天 → L4 永久封禁；质量退修与违规分开。' },
      { h: '三、申诉', p: '受限账号页可申诉与查询状态，48 小时反馈。' },
    ],
  },
  children: {
    title: '儿童个人信息处理规则',
    sections: [
      { h: '一、适用范围', p: '不满 14 周岁用户。' },
      { h: '二、监护人同意', p: '监护人手机号验证同意；可随时撤回（撤回限制会话与推送）。' },
      { h: '三、我们的承诺', p: '不向儿童推送商业广告，不默认开放位置共享。' },
    ],
  },
  report: {
    title: '投诉举报与版权通道',
    sections: [
      { h: '一、举报', p: '站内任意内容/用户/场所可举报，48 小时反馈。' },
      { h: '二、版权通知-删除', p: '合格通知立即下架涉嫌内容；作者可反通知申诉。' },
      { h: '三、联系方式', p: 'copyright@example.com（占位，上线前替换正式邮箱）。' },
    ],
  },
};
const current = computed(() => DOCS[doc.value] ?? DOCS.terms!);
</script>

<template>
  <div class="site-legal">
    <nav class="toc" aria-label="规则目录">
      <router-link
        v-for="(v, k) in DOCS"
        :key="k"
        :to="`/legal/${k}`"
        :class="{ on: doc === k }"
      >
        {{ v.title }}
      </router-link>
    </nav>

    <article class="doc">
      <h1>{{ current.title }}</h1>
      <p class="meta">版本 v1.0 · 更新 2026-09-22 · 法务定稿前为产品占位文本</p>
      <section v-for="s in current.sections" :key="s.h">
        <h2>{{ s.h }}</h2>
        <p>{{ s.p }}</p>
      </section>
    </article>
  </div>
</template>

<style scoped>
.site-legal { max-width: 820px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 20px; }
.toc { display: flex; gap: 8px; flex-wrap: wrap; }
.toc a { background: #fff; color: #7a6e63; border-radius: 999px; padding: 8px 16px; text-decoration: none; font-size: 13px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.toc a.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.doc { background: #fff; border-radius: 24px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 36px 32px; }
.doc h1 { margin: 0 0 8px; font-size: 26px; }
.meta { color: #7a6e63; font-size: 13px; margin: 0 0 20px; }
.doc section { margin-bottom: 20px; }
.doc h2 { font-size: 17px; margin: 0 0 8px; }
.doc section p { margin: 0; color: #2b2118; line-height: 1.9; font-size: 15px; }
</style>
