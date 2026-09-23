<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <main class="todo-confirm">
    <button type="button" class="back" @click="router.back()">‹ 返回</button>
    <div class="heading">
      <span class="eyebrow">照护待办</span>
      <h1>确认本次完成</h1>
      <p>完成前需要核对本期待办、对应宠物与有效健康记录。</p>
    </div>

    <section class="status" role="status" aria-labelledby="unavailable-title">
      <span class="status-icon" aria-hidden="true">!</span>
      <div>
        <h2 id="unavailable-title">待办确认暂未接入</h2>
        <p>当前接口没有待办详情查询，也不接收关联的健康记录编号。无法核实本次执行与记录是否同成同败，因此这里不会提交完成或显示成功。</p>
      </div>
    </section>

    <section class="steps" aria-label="完成待办所需信息">
      <h2>接通后将核对</h2>
      <ol>
        <li>平台返回的宠物、待办和本次计划日期</li>
        <li>已有记录的有效性，或新记录的保存结果</li>
        <li>处理人、重复处理结果和下次排期</li>
      </ol>
    </section>

    <div class="actions">
      <button type="button" class="primary" disabled data-testid="confirm">确认完成 · 待接入</button>
      <button type="button" class="ghost" @click="router.push('/pets')">返回我的宠物</button>
    </div>
  </main>
</template>

<style scoped>
.todo-confirm { max-width: 620px; margin: 32px auto; padding: 16px; display: grid; gap: 20px; color: #2b2118; }
.back { justify-self: start; border: 0; min-height: 44px; background: transparent; color: #a8470c; cursor: pointer; }
.heading { display: grid; gap: 6px; }
.heading h1 { margin: 0; font-size: clamp(24px, 4vw, 32px); }
.heading p { margin: 0; color: #706255; line-height: 1.6; }
.eyebrow { color: #a8470c; font-size: 13px; font-weight: 700; }
.status { display: flex; gap: 14px; align-items: start; padding: 20px; border: 1px solid #ecd9c8; background: #fff7ee; border-radius: 18px; }
.status-icon { width: 26px; height: 26px; flex: none; display: grid; place-items: center; border-radius: 50%; background: #a8470c; color: #fff; font-weight: 800; }
.status h2, .steps h2 { margin: 0 0 7px; font-size: 18px; }
.status p { margin: 0; color: #604b3b; line-height: 1.7; font-size: 14px; }
.steps { background: #fff; border: 1px solid #f0e6dc; border-radius: 18px; padding: 20px; }
.steps ol { margin: 0; padding-left: 22px; display: grid; gap: 10px; line-height: 1.6; color: #604b3b; font-size: 14px; }
.actions { display: flex; gap: 10px; flex-wrap: wrap; }
.actions button { min-height: 48px; padding: 0 20px; border-radius: 999px; font-weight: 700; }
.primary { background: #b85111; color: #fff; border: 0; opacity: .58; cursor: not-allowed; }
.ghost { color: #684b39; background: #fff; border: 1px solid #dacabc; cursor: pointer; }
button:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
</style>
