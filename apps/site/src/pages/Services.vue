<script setup lang="ts">
import { FOUR_SECTIONS } from '../domain/site';

const SCENES = [
  { title: '日常照护', steps: ['早晨查看提醒条', '记录喂养与体重', '曲线确认 10% 波动', '疫苗到期自动提醒'], tag: '管理' },
  { title: '就医前后', steps: ['病历与用药时间线', '下次复诊排期', '分享摘要给家人', '分享面板不生成实时私有链接'], tag: '管理' },
  { title: '出行前做功课', steps: ['附近友好场所筛选', '看真实评价与属性', '报名遛宠活动', '按路线打卡'], tag: '探索' },
  { title: '养前功课', steps: ['看 7 频道资讯', '用品评测与清单', '领养信息（无交易按钮）', '举报售卖嫌疑'], tag: '资讯/用品' },
];
</script>

<template>
  <div class="services">
    <h1>服务场景</h1>
    <p class="lead">按真实养宠节奏组织：每个场景都有可执行的闭环步骤。</p>

    <section class="grid">
      <article v-for="s in SCENES" :key="s.title" class="card">
        <span class="tag">{{ s.tag }}</span>
        <h2>{{ s.title }}</h2>
        <ol>
          <li v-for="(step, i) in s.steps" :key="i">{{ step }}</li>
        </ol>
      </article>
    </section>

    <section class="four">
      <article v-for="f in FOUR_SECTIONS" :key="f.id">
        <h3>{{ f.label }}</h3>
        <p>{{ f.desc }}</p>
      </article>
    </section>

    <router-link class="cta" to="/download">选择你的端 →</router-link>
  </div>
</template>

<style scoped>
.services { max-width: 1120px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 28px; }
h1 { font-size: 34px; margin: 0; }
.lead { color: #7a6e63; font-size: 16px; margin: 0; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: 20px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 24px; display: grid; gap: 10px; }
.tag { justify-self: start; background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 3px 12px; font-size: 12px; font-weight: 600; }
.card h2 { margin: 0; font-size: 18px; }
ol { margin: 0; padding-left: 18px; display: grid; gap: 8px; color: #2b2118; font-size: 14px; }
.four { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
.four article { background: #fff9f3; border-radius: 16px; padding: 18px; }
.four h3 { margin: 0 0 6px; font-size: 15px; }
.four p { margin: 0; color: #7a6e63; font-size: 13px; line-height: 1.7; }
.cta { justify-self: center; background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 32px; text-decoration: none; font-weight: 600; }
</style>
