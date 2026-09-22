<script setup lang="ts">
import { ref } from 'vue';

const SECTIONS = [
  { id: 'account', label: '账号与登录', qs: [
    { q: '如何登录？', a: '手机号验证码一键登录（dev 固定码 123456）；微信登录需补手机验证与协议（U83）。' },
    { q: '忘记账号了？', a: '同一手机号即账号；换绑见应用内设置。' },
  ] },
  { id: 'pet', label: '宠物与记录', qs: [
    { q: '删掉的宠物能恢复吗？', a: '软删 30 天内可恢复；归档随时恢复；超期联系人工。' },
    { q: '提醒怎么自动生成？', a: '疫苗/驱虫记录里的「下次提醒」日期会自动建计划。' },
  ] },
  { id: 'content', label: '内容与投稿', qs: [
    { q: '投稿多久有结果？', a: '先审后发，SLA 24 小时；超时看板标红催办。' },
    { q: '被驳回会封号吗？', a: '质量退修与违规分开计数：3 次退修暂停 7 天，不计违规。' },
  ] },
  { id: 'family', label: '家庭与共享', qs: [
    { q: '家人能看到我全部宠物吗？', a: '不能。只有你主动共享的宠物可见，且默认只读。' },
    { q: '能同时加两个家庭吗？', a: '不能，一人限一个家庭。' },
  ] },
  { id: 'safety', label: '安全与合规', qs: [
    { q: '如何举报？', a: '内容/用户/场所均有举报入口，48 小时内反馈结果。' },
    { q: '未成年人可以使用吗？', a: '满 14 周岁自主注册；未满 14 走监护人同意流程（方案甲）。' },
    { q: '被封禁了怎么办？', a: '受限账号页可申诉与查询，无需恢复账号（X15）。' },
  ] },
];
const open = ref('account');
</script>

<template>
  <div class="site-help">
    <h1>帮助中心</h1>

    <router-link class="appeal" to="/contact">⚖️ 处置申诉与投诉举报通道 →</router-link>

    <nav class="tabs">
      <button
        v-for="s in SECTIONS"
        :key="s.id"
        type="button"
        :class="{ on: open === s.id }"
        @click="open = s.id"
      >
        {{ s.label }}
      </button>
    </nav>

    <section class="faq">
      <article v-for="qa in SECTIONS.find((s) => s.id === open)!.qs" :key="qa.q">
        <strong>{{ qa.q }}</strong>
        <p>{{ qa.a }}</p>
      </article>
    </section>

    <p class="meta">没解决？应用内「帮助与客服 → 提交工单」（48h 反馈）；或联系页留言。</p>
  </div>
</template>

<style scoped>
.site-help { max-width: 800px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 18px; }
h1 { font-size: 32px; margin: 0; }
.appeal { background: #f5f3ff; color: #6d5bd0; border-radius: 14px; padding: 16px 20px; text-decoration: none; font-weight: 600; font-size: 15px; }
.tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.tabs button { height: 34px; padding: 0 16px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 13px; cursor: pointer; }
.tabs button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
.faq { display: grid; gap: 12px; }
.faq article { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px 20px; display: grid; gap: 8px; }
.faq p { margin: 0; color: #7a6e63; font-size: 14px; line-height: 1.8; }
.meta { color: #7a6e63; font-size: 13px; }
</style>
