<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { WIZARD_STEPS, SPECIES, validateWizardStep } from '../../domain/pet';

const router = useRouter();
const step = ref(0);
const info = reactive<{ species?: string; name?: string; breed?: string }>({});
const attempted = ref(false);
const submitError = ref('');
const saving = ref(false);
const needsReview = ref(false);
const validationMessages: Record<string, string> = {
  SPECIES_REQUIRED: '请选择宠物类型。',
  NAME_INVALID: '昵称需要 1–20 个字。',
  BREED_REQUIRED: '请填写品种。',
};
const validationError = computed(() => {
  if (!attempted.value) return '';
  if (step.value === 3) return '';
  const code = validateWizardStep(step.value, info);
  return validationMessages[code ?? ''] ?? '';
});

function next() {
  if (saving.value || needsReview.value) return;
  attempted.value = true;
  if (validationError.value) return;
  attempted.value = false;
  submitError.value = '';
  if (step.value < WIZARD_STEPS.length - 1) {
    step.value += 1;
    return;
  }
  void finish();
}

function prev() {
  if (saving.value) return;
  attempted.value = false;
  if (step.value > 0) step.value -= 1;
  else void router.push('/pets');
}

async function finish() {
  if (validateWizardStep(0, info) || validateWizardStep(1, info)) {
    submitError.value = '基础资料尚未填写完整，请返回核对。';
    return;
  }
  saving.value = true;
  submitError.value = '';
  try {
    const created = (await api.petCreate({
      body: {
        name: info.name!.trim(),
        species: info.species!,
        breed: info.breed!,
        sex: undefined,
        neutered: undefined,
        birthDate: undefined,
      },
    })) as unknown;
    if (!created || typeof created !== 'object' || !('id' in created) ||
        !Number.isSafeInteger(Number(created.id)) || Number(created.id) < 1) {
      submitError.value = '平台已响应，但没有返回可核对的档案编号。请先到宠物列表查看，避免重复建档。';
      needsReview.value = true;
      return;
    }
    await router.replace(`/pets/${encodeURIComponent(String(created.id))}`);
  } catch (e) {
    submitError.value = `建档结果尚未确认：${e instanceof Error ? e.message : '请求失败'}。请先到宠物列表核对，再决定是否重试。`;
    needsReview.value = true;
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="wizard">
    <header>
      <button type="button" class="back" @click="router.push('/pets')">‹ 我的宠物</button>
      <h1>新建宠物档案</h1>
      <p class="muted">完成基础资料后，平台确认保存才会进入宠物详情。</p>
    </header>
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
      <label>昵称（1–20 字）<input v-model="info.name" maxlength="20" autocomplete="off" data-testid="pet-name" /></label>
      <label>品种<input v-model="info.breed" data-testid="pet-breed" placeholder="如：柯基" /></label>
    </div>

    <!-- 步骤3：照片（可跳过） -->
    <div v-else-if="step === 2" class="pane">
      <p class="muted">照片上传尚未接入。可以继续建档，当前不会保存照片。</p>
      <button type="button" class="ghost" @click="next()">跳过，下一步</button>
    </div>

    <!-- 步骤4：体重基线暂未接入，先核对可保存的资料 -->
    <div v-else class="pane">
      <p class="pending">体重基线尚未接入当前前端契约。为避免填写后丢失，这一步先核对档案；体重不会写入曲线。</p>
      <div class="preview" aria-label="本次建档内容">
        <strong>即将保存的资料</strong>
        <span>{{ info.name?.trim() || '未填写昵称' }} · {{ info.species || '未选择类型' }} · {{ info.breed?.trim() || '未填写品种' }}</span>
      </div>
    </div>

    <p v-if="validationError || submitError" class="err" role="alert" data-testid="wizard-error">{{ validationError || submitError }}</p>
    <div class="actions">
      <button type="button" class="ghost" :disabled="saving" @click="prev()">{{ step === 0 ? '返回列表' : '上一步' }}</button>
      <button v-if="step !== 2" type="button" :disabled="saving || needsReview" data-testid="wizard-next" @click="next()">
        {{ saving ? '保存中…' : needsReview ? '请先核对列表' : step === 3 ? '确认并保存档案' : '下一步' }}
      </button>
    </div>
    <button v-if="submitError" type="button" class="text-link" @click="router.push('/pets')">先核对宠物列表</button>
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
header { display: grid; gap: 6px; }
h1 { margin: 0; font-size: 24px; color: #2b2118; }
.back, .text-link { justify-self: start; border: 0; padding: 8px 0; background: transparent; color: #a8470c; cursor: pointer; }
.preview { display: grid; gap: 5px; border-radius: 14px; padding: 14px; background: #fff; border: 1px solid #f0e6dc; font-size: 14px; }
.pending { margin: 0; border-radius: 14px; border: 1px solid #f1d7c2; background: #fff4e9; color: #754011; padding: 14px; font-size: 14px; line-height: 1.6; }
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
  background: #b85111;
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
.pane > .ghost { min-height: 44px; border: 1px solid #dacabc; border-radius: 999px; cursor: pointer; }
.choice {
  height: 56px;
  border-radius: 16px;
  border: 1px solid #f0e6dc;
  background: #fff;
  font-size: 17px;
  cursor: pointer;
}
.choice.on {
  border: 2px solid #b85111;
  background: #fff1e8;
  color: #9c3f0b;
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
  background: #b85111;
  color: #fff;
  font-weight: 600;
}
.actions button:disabled { opacity: .65; cursor: wait; }
.actions button:focus-visible, .choice:focus-visible, input:focus-visible, .back:focus-visible, .text-link:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
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
