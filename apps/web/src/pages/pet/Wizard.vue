<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { WIZARD_STEPS, SPECIES, validateWizardStep, wizardComplete } from '../../domain/pet';

const router = useRouter();
const step = ref(0);
const info = reactive<{ species?: string; name?: string; breed?: string; weightKg?: number }>({});
const attempted = ref(false);
const error = computed(() => (attempted.value ? validateWizardStep(step.value, info) : null));

function next() {
  attempted.value = true;
  if (validateWizardStep(step.value, info) !== null) return;
  attempted.value = false;
  if (step.value < WIZARD_STEPS.length - 1) {
    step.value += 1;
    return;
  }
  void finish();
}

function prev() {
  attempted.value = false;
  if (step.value > 0) step.value -= 1;
  else void router.push('/pets');
}

async function finish() {
  if (!wizardComplete(info)) return;
  try {
    await api.petCreate({
      body: {
        name: info.name!.trim(),
        species: info.species!,
        breed: info.breed!,
        sex: undefined,
        neutered: undefined,
        birthDate: undefined,
      },
    });
    // 基线体重 → 记录页（L1：完成后引导首条记录/设提醒）
    await router.replace('/pets');
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败';
  }
}
</script>

<template>
  <div class="wizard">
    <div class="steps">
      <span
        v-for="(s, i) in WIZARD_STEPS"
        :key="s"
        :class="['dot', { active: i === step, done: i < step }]"
      />
    </div>
    <p class="step-name">第 {{ step + 1 }} 步 / 4 · {{ WIZARD_STEPS[step] }}</p>

    <!-- 步骤1：物种 -->
    <div v-if="step === 0" class="pane">
      <button
        v-for="s in SPECIES"
        :key="s"
        type="button"
        :class="['choice', { on: info.species === s }]"
        @click="info.species = s"
      >
        {{ s }}
      </button>
    </div>

    <!-- 步骤2：基础信息 -->
    <div v-else-if="step === 1" class="pane">
      <label>昵称（1–20 字）<input v-model="info.name" data-testid="pet-name" /></label>
      <label>品种<input v-model="info.breed" data-testid="pet-breed" placeholder="如：柯基" /></label>
    </div>

    <!-- 步骤3：照片（可跳过） -->
    <div v-else-if="step === 2" class="pane">
      <p class="muted">上传一张照片（可跳过，之后在档案里补）</p>
      <button type="button" class="ghost" @click="next()">跳过，下一步</button>
    </div>

    <!-- 步骤4：体重基线 -->
    <div v-else class="pane">
      <label>体重基线 (kg)
        <input v-model.number="info.weightKg" type="number" step="0.1" data-testid="pet-weight" />
      </label>
      <p class="muted">0.1–200kg；用于曲线与 10% 波动二次确认</p>
    </div>

    <p v-if="error" class="err" data-testid="wizard-error">{{ error }}</p>
    <div class="actions">
      <button type="button" class="ghost" @click="prev()">上一步</button>
      <button v-if="step !== 2" type="button" data-testid="wizard-next" @click="next()">
        {{ step === 3 ? '完成建档' : '下一步' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.wizard {
  max-width: 480px;
  margin: 48px auto;
  padding: 24px;
  display: grid;
  gap: 16px;
}
.steps {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.dot {
  width: 40px;
  height: 6px;
  border-radius: 999px;
  background: #f0e6dc;
}
.dot.active {
  background: #ff7a2f;
}
.dot.done {
  background: #ffd0b3;
}
.step-name {
  text-align: center;
  color: #7a6e63;
  font-size: 13px;
}
.pane {
  display: grid;
  gap: 12px;
}
.choice {
  height: 56px;
  border-radius: 16px;
  border: 1px solid #f0e6dc;
  background: #fff;
  font-size: 17px;
  cursor: pointer;
}
.choice.on {
  border: 2px solid #ff7a2f;
  background: #fff1e8;
  color: #ff7a2f;
  font-weight: 600;
}
label {
  display: grid;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
}
input {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
  font-size: 15px;
}
.actions {
  display: flex;
  gap: 12px;
}
.actions button {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.actions button.ghost,
.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.muted {
  color: #7a6e63;
  font-size: 13px;
}
</style>
