<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { todoConfirmSuccess, keepInputsOnFailure, type TodoConfirmModel } from '../../domain/closedLoop';

const route = useRoute();
const router = useRouter();
const todoId = String(route.params.id);

// X01：提醒卡 → 关联已有记录或新增 → 发生日期 + 下次计划预览 → 确认
const useExisting = ref(true);
const existingRecord = ref('健康记录·疫苗（2026-09-20）');
const occurredDate = ref(new Date().toISOString().slice(0, 10));
const nextDuePreview = ref('2027-03-21');
const handler = ref('我');
const result = ref<{ headline: string; lines: string[] } | null>(null);
const failed = ref(false);

function confirm() {
  // 模拟提交：失败保留输入（X01）
  if (failed.value) {
    keepInputsOnFailure();
    return;
  }
  const model: TodoConfirmModel = {
    petName: '旺财',
    existingRecord: useExisting.value ? existingRecord.value : null,
    occurredDate: occurredDate.value,
    nextDuePreview: nextDuePreview.value,
    handler: handler.value,
  };
  result.value = todoConfirmSuccess(model, 'COMPLETED');
}
</script>

<template>
  <div class="x01">
    <header>
      <button type="button" class="back" @click="router.back()">‹ 返回</button>
      <h1>完成待办（X01）</h1>
    </header>

    <template v-if="!result">
      <label class="row">
        <input v-model="useExisting" type="radio" :value="true" /> 关联已有记录：{{ existingRecord }}
      </label>
      <label class="row">
        <input v-model="useExisting" type="radio" :value="false" /> 新增一条健康记录
      </label>
      <label class="field">发生日期 <input v-model="occurredDate" type="date" data-testid="occurred" /></label>
      <label class="field">下次计划预览 <input v-model="nextDuePreview" type="date" /></label>

      <button type="button" class="primary" data-testid="confirm" @click="confirm">确认完成</button>
      <button type="button" class="ghost" @click="failed = !failed">{{ failed ? '模拟失败态（输入保留）' : '模拟提交失败' }}</button>
    </template>

    <section v-else class="success" data-testid="success">
      <h2>{{ result.headline }}</h2>
      <ul>
        <li v-for="l in result.lines" :key="l">{{ l }}</li>
      </ul>
      <button type="button" class="primary" @click="router.push('/pets')">返回宠物详情</button>
    </section>
  </div>
</template>

<style scoped>
.x01 { max-width: 520px; margin: 32px auto; padding: 16px; display: grid; gap: 12px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.row { background: #fff; border-radius: 12px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 14px; font-size: 14px; display: flex; gap: 8px; }
.field { display: grid; gap: 6px; font-size: 14px; font-weight: 600; }
.field input { height: 44px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 12px; }
.primary { height: 48px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.ghost { height: 40px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.success { background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px; display: grid; gap: 10px; }
.success h2 { margin: 0; color: #22c55e; font-size: 18px; }
.success ul { padding-left: 18px; display: grid; gap: 6px; color: #2b2118; font-size: 14px; }
</style>
