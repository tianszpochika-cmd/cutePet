<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// T10.6 复用 T7.11：同一 closedLoop 实现（跨端一致）
import { todoConfirmSuccess, type TodoConfirmModel } from '../../../web/src/domain/closedLoop.ts';

const route = useRoute();
const router = useRouter();
const existing = ref(true);
const occurred = ref(new Date().toISOString().slice(0, 10));
const nextDue = ref('2027-03-21');
const result = ref<{ headline: string; lines: string[] } | null>(null);
const alreadyDone = ref(false); // 家人已完成演示（X01）

function confirm() {
  const model: TodoConfirmModel = {
    petName: '旺财',
    existingRecord: existing.value ? '健康记录·疫苗' : null,
    occurredDate: occurred.value,
    nextDuePreview: nextDue.value,
    handler: '妈妈',
  };
  result.value = todoConfirmSuccess(model, alreadyDone.value ? 'ALREADY_COMPLETED' : 'COMPLETED');
}
void route;
</script>

<template>
  <div class="m-x01">
    <header>
      <button type="button" class="back" @click="router.back()">‹</button>
      <h1>完成待办（X01）</h1>
    </header>

    <template v-if="!result">
      <label class="row"><input v-model="existing" type="radio" :value="true" /> 关联已有记录：疫苗（2026-09-20）</label>
      <label class="row"><input v-model="existing" type="radio" :value="false" /> 新增健康记录</label>
      <label class="field">发生日期 <input v-model="occurred" type="date" data-testid="occurred" /></label>
      <label class="field">下次计划 <input v-model="nextDue" type="date" /></label>
      <label class="row"><input v-model="alreadyDone" type="checkbox" data-testid="already" /> 模拟家人已完成（幂等）</label>
      <button type="button" class="primary" data-testid="confirm" @click="confirm">确认完成</button>
    </template>

    <section v-else class="success" data-testid="success">
      <h2>{{ result.headline }}</h2>
      <ul><li v-for="l in result.lines" :key="l">{{ l }}</li></ul>
      <button type="button" class="primary" @click="router.push('/pets')">返回</button>
    </section>
  </div>
</template>

<style scoped>
.m-x01 { padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 10px; align-items: center; }
.back { border: none; background: #fff; width: 44px; height: 44px; border-radius: 999px; color: #ff7a2f; font-size: 20px; }
h1 { margin: 0; font-size: 17px; }
.row { background: #fff; border-radius: 14px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px 16px; font-size: 14px; display: flex; gap: 10px; align-items: center; min-height: 48px; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input { height: 48px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 12px; font-size: 16px; }
.primary { height: 52px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-size: 16px; font-weight: 700; min-height: 44px; }
.success { background: #fff; border-radius: 18px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 20px; display: grid; gap: 12px; }
.success h2 { margin: 0; color: #22c55e; font-size: 18px; }
.success ul { margin: 0; padding-left: 18px; display: grid; gap: 6px; font-size: 14px; color: #2b2118; }
</style>
