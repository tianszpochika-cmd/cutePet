<script setup lang="ts">
const EVENTS = [
  { id: 1, date: '2026-12-01', title: '秋日遛宠嘉年华', city: '北京', type: '活动', note: '报名将于开始前 90 天窗口开放，名额 20' },
  { id: 2, date: '2026-11-15', title: '爱心领养日', city: '上海', type: '领养', note: '领养信息仅展示与联系——无报名/无交易按钮' },
];
</script>

<template>
  <div class="site-events">
    <h1>本地活动</h1>
    <p class="lead">活动与领养共用阅读组件、行为分开：活动可报名，领养仅联系与举报。</p>

    <ul class="list">
      <li v-for="e in EVENTS" :key="e.id" class="row" :data-testid="`event-${e.id}`">
        <div class="date">{{ e.date.slice(5) }}</div>
        <div>
          <strong>{{ e.title }}</strong>
          <p class="meta">{{ e.city }} · {{ e.type }} · {{ e.note }}</p>
        </div>
        <span class="tag">{{ e.type }}</span>
      </li>
    </ul>

    <div class="actions">
      <router-link class="primary" to="/download">应用内报名 / 查看详情 →</router-link>
      <router-link class="ghost" to="/center">看相关资讯</router-link>
    </div>
    <p class="note">售卖信息？官网页脚「投诉举报与版权通道」直达；应用内举报 48h 反馈。</p>
  </div>
</template>

<style scoped>
.site-events { max-width: 820px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 18px; }
h1 { font-size: 32px; margin: 0; }
.lead { color: #7a6e63; font-size: 15px; margin: 0; }
.list { list-style: none; padding: 0; margin: 0; display: grid; gap: 12px; }
.row { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 20px; display: flex; gap: 18px; align-items: center; }
.date { background: #fff1e8; color: #ff7a2f; font-weight: 700; border-radius: 14px; padding: 10px 14px; font-size: 15px; }
.row strong { font-size: 17px; }
.meta { margin: 6px 0 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.tag { margin-left: auto; background: #eaf1ff; color: #4d8dff; border-radius: 999px; padding: 4px 14px; font-size: 13px; font-weight: 600; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 28px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 13px 28px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
.note { color: #7a6e63; font-size: 13px; margin: 0; }
</style>
