<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { RECORD_KINDS, recordFields, validateRecord } from '../../domain/pet';

const route = useRoute();
const router = useRouter();
const petId = computed(() => String(route.params.id));

const kind = ref<string>('体检');
const values = reactive<Record<string, unknown>>({});
const attempted = ref(false);
const error = ref('');
const success = ref('');
const saving = ref(false);
const needsReview = ref(false);
const suggestedReminder = ref(false);
const petLoading = ref(true);
const petWritable = ref(false);
const petError = ref('');
const now = new Date();
const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
let petCheckVersion = 0;

const fields = computed(() => {
  const available = kind.value === '体重' ? [] : recordFields(kind.value).filter((field) => field.key !== 'photo');
  return kind.value === '过敏'
    ? [{ key: 'eventDate', label: '发现日期', required: true, type: 'date' as const }, ...available]
    : available;
});
const effectiveEventDate = computed(() =>
  kind.value === '用药' ? String(values.validFrom ?? '') : String(values.eventDate ?? ''));
function recordValidation(): string | null {
  if (!effectiveEventDate.value) return 'REQUIRED:eventDate';
  if (effectiveEventDate.value > today) return 'EVENT_FUTURE_NOT_ALLOWED';
  return validateRecord(kind.value, { ...values, eventDate: effectiveEventDate.value });
}
const validationError = computed(() =>
  attempted.value ? recordValidation() : null,
);
const validationMessage = computed(() => {
  const code = validationError.value;
  if (!code) return '';
  if (code.startsWith('REQUIRED:')) {
    const key = code.slice(9);
    if (key === 'eventDate' && kind.value === '用药') return '请填写开始日期。';
    if (key === 'eventDate' && kind.value === '过敏') return '请填写发现日期。';
    return `请填写${recordFields(kind.value).find((field) => field.key === key)?.label ?? '必填项'}。`;
  }
  if (code === 'MED_COURSE_INVALID') return '结束日期不能早于开始日期。';
  if (code === 'EVENT_FUTURE_NOT_ALLOWED') return '已发生事项的日期不能晚于今天。';
  return '请检查本次记录信息。';
});

watch(() => route.params.id, () => {
  for (const key of Object.keys(values)) delete values[key];
  attempted.value = false;
  error.value = '';
  success.value = '';
  suggestedReminder.value = false;
  needsReview.value = false;
  void checkPet();
}, { immediate: true });

async function checkPet() {
  const version = ++petCheckVersion;
  const currentId = petId.value;
  petLoading.value = true;
  petWritable.value = false;
  petError.value = '';
  try {
    const response = (await api.petGet({ path: { id: currentId } })) as unknown;
    if (!response || typeof response !== 'object' || !('id' in response) || String(response.id) !== currentId ||
        !('state' in response) || response.state !== 'ACTIVE') {
      throw new Error('档案不可用或当前未启用');
    }
    if (version !== petCheckVersion) return;
    petWritable.value = true;
  } catch (e) {
    if (version !== petCheckVersion) return;
    petError.value = e instanceof Error ? e.message : '无法核对宠物档案';
  } finally {
    if (version === petCheckVersion) petLoading.value = false;
  }
}

function pickKind(k: string) {
  if (needsReview.value || saving.value) return;
  kind.value = k;
  attempted.value = false;
  error.value = '';
  success.value = '';
  suggestedReminder.value = false;
  for (const key of Object.keys(values)) delete values[key];
}

function setValue(key: string, value: string) {
  values[key] = value;
  if (!needsReview.value) error.value = '';
  success.value = '';
  suggestedReminder.value = false;
}

async function save() {
  if (saving.value || needsReview.value) return;
  if (!petWritable.value) {
    error.value = '宠物档案尚未核对，当前不能保存记录。';
    return;
  }
  if (kind.value === '体重') {
    error.value = '体重保存接口尚未纳入当前前端契约，请勿把这次输入视为已保存。';
    return;
  }
  attempted.value = true;
  const invalid = recordValidation();
  if (invalid) return;
  error.value = '';
  success.value = '';
  suggestedReminder.value = false;
  saving.value = true;
  const savedPetId = petId.value;
  try {
    const created = (await api.recordCreate({
        path: { id: savedPetId },
        body: {
          kind: kind.value,
          eventDate: effectiveEventDate.value,
          validFrom: (values.validFrom as string) ?? undefined,
          validTo: (values.validTo as string) ?? undefined,
          org: (values.org as string) ?? undefined,
          doctor: (values.doctor as string) ?? undefined,
          note: [values.kindName, values.note].filter(Boolean).join(' · '),
          photo: undefined,
          nextReminderDate: (values.nextReminderDate as string) ?? undefined,
        },
      })) as unknown;
    if (savedPetId !== petId.value) return;
    if (!created || typeof created !== 'object' || !('id' in created) ||
        typeof created.id !== 'number' || !Number.isSafeInteger(created.id) || created.id < 1) {
      error.value = '平台已响应，但没有返回记录编号。请先查看健康记录，避免重复保存。';
      needsReview.value = true;
      return;
    }
    suggestedReminder.value = 'suggestedReminder' in created && !!created.suggestedReminder;
    success.value = `记录 #${String(created.id)} 已由平台保存。`;
  } catch (e) {
    if (savedPetId !== petId.value) return;
    error.value = `保存结果尚未确认：${e instanceof Error ? e.message : '请求失败'}。输入仍在本页，请先查看记录列表再决定是否重试。`;
    needsReview.value = true;
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="record">
    <header>
      <button type="button" class="back" @click="router.push(`/pets/${petId}`)">‹ 返回</button>
      <h1>＋ 记录</h1>
    </header>
    <p v-if="petLoading" class="muted" role="status">正在核对宠物档案…</p>
    <div v-else-if="petError" class="pending" role="alert">{{ petError }} <button type="button" class="text-link" @click="checkPet">重试核对</button></div>

    <div class="kinds">
      <button
        v-for="k in RECORD_KINDS"
        :key="k"
        type="button"
        :class="{ on: kind === k }"
        :disabled="saving || needsReview"
        @click="pickKind(k)"
      >
        {{ k }}
      </button>
    </div>
    <p v-if="kind === '体重'" class="pending" role="status">体重与健康记录使用不同接口。当前体重写入尚未接入前端契约，本页不会保存体重；可以查看已有曲线。</p>
    <p v-else-if="kind === '疫苗' || kind === '驱虫'" class="pending">填写下次日期会在保存后获得提醒建议。提醒计划仍需单独创建，当前不会自动排期。</p>
    <p v-if="kind === '用药'" class="muted">开始日期会同时作为本次记录的发生日期。</p>
    <p v-if="kind === '疫苗' || kind === '驱虫' || kind === '用药' || kind === '过敏'" class="muted">名称将与备注合并保存；照片上传暂未接入。</p>

    <form class="form" @submit.prevent="save">
      <label v-for="f in fields" :key="f.key">
        {{ f.key === 'nextReminderDate' ? '下次日期（生成提醒建议）' : f.label }}{{ f.required ? ' *' : '' }}
        <textarea
          v-if="f.type === 'textarea'"
          :value="String(values[f.key] ?? '')"
          @input="setValue(f.key, ($event.target as HTMLTextAreaElement).value)"
          rows="3"
          :data-testid="f.key"
        />
        <input
          v-else
          :data-testid="f.key"
          :type="f.type === 'date' ? 'date' : f.type === 'number' ? 'number' : 'text'"
          :step="f.type === 'number' ? '0.1' : undefined"
          :max="f.type === 'date' && (f.key === 'eventDate' || f.key === 'measuredAt' || f.key === 'validFrom') ? today : undefined"
          :value="(values[f.key] as string) ?? ''"
          @input="setValue(f.key, ($event.target as HTMLInputElement).value)"
        />
      </label>

      <p v-if="validationMessage" class="err" role="alert" data-testid="record-error">{{ validationMessage }}</p>
      <p v-else-if="error" class="err" role="alert">{{ error }}</p>
      <p v-if="success" class="ok" role="status" data-testid="record-success">{{ success }}</p>
      <p v-if="suggestedReminder" class="pending" role="status">平台仅返回了提醒建议，尚未创建提醒。请到提醒中心核对。</p>

      <div class="actions">
        <button type="button" class="ghost" @click="router.push(`/pets/${petId}`)">取消</button>
        <button type="submit" class="primary" :disabled="petLoading || !petWritable || saving || needsReview || kind === '体重' || !!success" data-testid="record-save">{{ saving ? '保存中…' : needsReview ? '请先核对记录' : success ? '记录已保存' : kind === '体重' ? '体重保存待接入' : '保存记录' }}</button>
      </div>
      <button v-if="success" type="button" class="text-link" @click="router.push(`/pets/${petId}`)">查看宠物详情</button>
      <button v-if="needsReview" type="button" class="text-link" @click="router.push(`/pets/${petId}`)">先核对健康记录</button>
      <button v-if="kind === '体重'" type="button" class="text-link" @click="router.push(`/pets/${petId}/weights`)">查看已有体重曲线</button>
    </form>
  </div>
</template>

<style scoped>
.record {
  max-width: 520px;
  margin: 32px auto;
  padding: 16px;
  display: grid;
  gap: 16px;
}
header {
  display: flex;
  gap: 12px;
  align-items: center;
}
h1 {
  font-size: 20px;
}
.back {
  background: none;
  border: none;
  color: #a8470c;
  font-size: 16px;
  min-height: 44px;
  cursor: pointer;
}
.kinds {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.kinds button {
  min-height: 44px;
  padding: 0 14px;
  border-radius: 999px;
  border: none;
  background: #f7f1ea;
  color: #7a6e63;
  cursor: pointer;
}
.kinds button.on {
  background: #b85111;
  color: #fff;
  font-weight: 600;
}
.kinds button:disabled { opacity: .6; cursor: not-allowed; }
.form {
  display: grid;
  gap: 12px;
}
label {
  display: grid;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
}
input,
textarea {
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 15px;
  font-family: inherit;
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
  font-weight: 600;
}
.primary {
  background: #b85111;
  color: #fff;
}
.primary:disabled { opacity: .6; cursor: not-allowed; }
.pending { background: #fff4e9; border: 1px solid #f1d7c2; color: #754011; border-radius: 12px; padding: 12px; font-size: 13px; line-height: 1.6; }
.muted { color: #706255; font-size: 13px; }
.text-link { border: 0; background: transparent; color: #a8470c; min-height: 40px; justify-self: start; cursor: pointer; }
button:focus-visible, input:focus-visible, textarea:focus-visible { outline: 3px solid #6f320c; outline-offset: 2px; }
.ghost {
  background: #fff;
  color: #7a6e63;
  box-shadow: inset 0 0 0 1px #f0e6dc;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
.ok {
  color: #176336;
  font-size: 13px;
}
</style>
