<script setup lang="ts">
import { FOUR_SECTIONS } from '../domain/site';

const steps = [
  { number: '01', title: '先看本次安排', copy: '提醒计划告诉你节奏，本次待办告诉你眼下要核对什么。' },
  { number: '02', title: '留下一份真实记录', copy: '把已经发生的健康事件记下来，日后查看变化有据可依。' },
  { number: '03', title: '和家人确认结果', copy: '按宠物授权共享，处理状态以平台确认结果为准。' },
];
</script>

<template>
  <div class="campaign">
    <section class="hero"><div class="site-wrap"><p class="site-eyebrow">EVERYDAY CARE</p><h1>照护小事，<br />值得被认真记住。</h1><p>从今天的待办，到下次的安排，用一条清楚的时间线照顾它。这是产品能力介绍，不是用户案例或活动报名页。</p><div class="actions"><router-link class="site-button" to="/products/manage">了解宠物管理</router-link><router-link class="site-button outline" to="/download">查看使用方式</router-link></div></div></section>
    <section class="site-wrap step-section"><p class="site-eyebrow">THREE CLEAR STEPS</p><h2 class="site-section-title">看清今天，也准备下一次。</h2><div class="steps"><article v-for="step in steps" :key="step.number"><span>{{ step.number }}</span><h3>{{ step.title }}</h3><p>{{ step.copy }}</p></article></div></section>
    <section class="site-wrap capabilities"><div><p class="site-eyebrow">BEYOND THE TIMELINE</p><h2 class="site-section-title">需要时，再探索更多。</h2></div><div class="links"><router-link v-for="section in FOUR_SECTIONS" :key="section.id" :to="section.to">{{ section.label }} <span aria-hidden="true">→</span></router-link></div></section>
  </div>
</template>

<style scoped>
.hero{padding:110px 0 105px;background:radial-gradient(circle at 80% 30%,#ffe2c7,transparent 31%),linear-gradient(135deg,#fff9f3,#fff1e8)}
.hero h1{margin:0;font-size:clamp(42px,5vw,65px);line-height:1.16;letter-spacing:-.065em}.hero p:not(.site-eyebrow){max-width:560px;margin:22px 0 0;color:var(--site-muted);font-size:17px}.actions{display:flex;flex-wrap:wrap;gap:13px;margin-top:30px}
.step-section{padding-block:95px}.steps{display:grid;grid-template-columns:repeat(3,1fr);gap:17px;margin-top:35px}.steps article{min-height:218px;padding:26px;border:1px solid var(--site-line);border-radius:22px;background:#fff}.steps span{color:var(--site-action);font-size:13px;font-weight:800}.steps h3{margin:25px 0 8px;font-size:21px}.steps p{margin:0;color:var(--site-muted);font-size:14px}
.capabilities{display:flex;justify-content:space-between;gap:32px;padding-block:30px 85px}.links{display:grid;grid-template-columns:1fr 1fr;gap:10px;min-width:420px}.links a{display:flex;align-items:center;justify-content:space-between;min-height:60px;padding:12px 18px;border:1px solid var(--site-line);border-radius:14px;background:#fff;color:var(--site-action);font-weight:750;text-decoration:none}
@media(max-width:760px){.hero{padding:76px 0}.step-section{padding-block:72px}.steps{grid-template-columns:1fr}.steps article{min-height:0}.capabilities{flex-direction:column}.links{min-width:0}}
@media(max-width:480px){.actions .site-button{width:100%}.links{grid-template-columns:1fr}}
</style>
