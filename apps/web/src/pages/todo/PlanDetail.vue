<script setup lang="ts">
import { useRouter } from 'vue-router';
import { PLAN_DETAIL_RULES } from '../../domain/closedLoop';

const router = useRouter();
</script>

<template>
  <main class="plan-detail">
    <button type="button" class="back" @click="router.back()">‹ 返回</button>
    <div class="heading">
      <span class="eyebrow">照护计划</span>
      <h1>计划详情</h1>
      <p>计划、每期待办与处理记录需要从平台分别核对。</p>
    </div>

    <section class="unavailable" role="status">
      <h2>计划详情暂不可查看</h2>
      <p>当前接口只有按宠物查询计划列表，没有按计划编号读取详情或历史待办的入口。本页无法确认这项计划属于哪只宠物，也无法展示真实日期与处理人。</p>
      <button type="button" @click="router.push('/pets')">查看我的宠物</button>
    </section>

    <section class="rules" aria-label="计划管理规则">
      <h2>计划管理规则</h2>
      <p>{{ PLAN_DETAIL_RULES.editScope }}</p>
      <p>{{ PLAN_DETAIL_RULES.pauseVsSkip }}</p>
      <p>编辑、暂停、恢复和跳过需要平台提供当前状态与相应操作结果，接通前暂不开放。</p>
    </section>
  </main>
</template>

<style scoped>
.plan-detail { max-width: 620px; margin: 32px auto; padding: 16px; display: grid; gap: 20px; color: #2b2118; }
.back { justify-self: start; border: 0; min-height: 44px; background: transparent; color: #a8470c; cursor: pointer; }
.heading { display: grid; gap: 6px; }
.heading h1 { margin: 0; font-size: clamp(24px, 4vw, 32px); }
.heading p { margin: 0; color: #706255; line-height: 1.6; }
.eyebrow { color: #a8470c; font-size: 13px; font-weight: 700; }
.unavailable, .rules { padding: 20px; border-radius: 18px; line-height: 1.7; }
.unavailable { background: #fff7ee; border: 1px solid #ecd9c8; }
.rules { background: #fff; border: 1px solid #f0e6dc; }
h2 { margin: 0 0 8px; font-size: 18px; }
p { margin: 0 0 8px; font-size: 14px; color: #604b3b; }
p:last-child { margin-bottom: 0; }
.unavailable button { min-height: 44px; margin-top: 8px; padding: 0 18px; border: 0; border-radius: 999px; color: #fff; background: #b85111; font-weight: 700; cursor: pointer; }
button:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
</style>
