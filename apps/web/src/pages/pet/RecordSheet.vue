<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { RECORD_KINDS, recordFields, validateRecord } from '../../domain/pet';

const route = useRoute();
const router = useRouter();
const petId = String(route.params.id);

const kind = ref<string>('体重');
const values = reactive<Record<string, unknown>>({});
const attempted = ref(false);
const error = ref('');
const success = ref('');

const fields = computed(() => recordFields(kind.value));
const validationError = computed(() =>
  attempted.value ? validateRecord(kind.value, values) : null,
);

function pickKind(k: string) {
  kind.value = k;
  attempted.value = false;
  error.value = '';
  success.value = '';
  for (const key of Object.keys(values)) delete values[key];
}

async function save() {
  attempted.value = true;
  const invalid = validateRecord(kind.value, values);
  if (invalid) return;
  error.value = '';
  try {
    if (kind.value === '体重') {
      await api.recordCreate({
        path: { id: petId },
        body: {
          kind: '体重',
          eventDate: String(values.measuredAt ?? new Date().toISOString().slice(0, 10)),
          validFrom: undefined,
          validTo: undefined,
          org: undefined,
          doctor: undefined,
          note: String(values.note ?? ''),
          photo: undefined,
          nextReminderDate: undefined,
        },
      });
    } else {
      await api.recordCreate({
        path: { id: petId },
        body: {
          kind: kind.value,
          eventDate: String(values.eventDate ?? new Date().toISOString().slice(0, 10)),
          validFrom: (values.validFrom as string) ?? undefined,
          validTo: (values.validTo as string) ?? undefined,
          org: (values.org as string) ?? undefined,
          doctor: (values.doctor as string) ?? undefined,
          note: String(values.note ?? values.kindName ?? ''),
          photo: undefined,
          nextReminderDate: (values.nextReminderDate as string) ?? undefined,
        },
      });
    }
    success.value =
      values.nextReminderDate !== undefined
        ? '已保存，并自动生成下一次提醒'
        : '已保存';
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败（dev 需启动 pet-service）';
  }
}
</script>

<template>
  <div class="record">
    <header>
      <button type="button" class="back" @click="router.push(`/pets/${petId}`)">‹ 返回</button>
      <h1>＋ 记录</h1>
    </header>

    <div class="kinds">
      <button
        v-for="k in RECORD_KINDS"
        :key="k"
        type="button"
        :class="{ on: kind === k }"
        @click="pickKind(k)"
      >
        {{ k }}
      </button>
    </div>

    <form class="form" @submit.prevent="save">
      <label v-for="f in fields" :key="f.key">
        {{ f.label }}{{ f.required ? ' *' : '' }}
        <textarea
          v-if="f.type === 'textarea'"
          v-model="values[f.key] as string"
          rows="3"
          :data-testid="f.key"
        />
        <input
          v-else
          :data-testid="f.key"
          :type="f.type === 'date' ? 'date' : f.type === 'number' ? 'number' : 'text'"
          :step="f.type === 'number' ? '0.1' : undefined"
          :max="f.type === 'date' ? new Date().toISOString().slice(0, 10) : undefined"
          :value="(values[f.key] as string) ?? ''"
          @input="values[f.key] = ($event.target as HTMLInputElement).value"
        />
      </label>

      <p v-if="validationError" class="err" data-testid="record-error">
        {{ validationError.startsWith('REQUIRED:') ? `请填写 ${validationError.slice(9)}` : validationError }}
      </p>
      <p v-else-if="error" class="err">{{ error }}</p>
      <p v-if="success" class="ok" data-testid="record-success">{{ success }}</p>

      <div class="actions">
        <button type="button" class="ghost" @click="router.push(`/pets/${petId}`)">取消</button>
        <button type="submit" class="primary" data-testid="record-save">保存</button>
      </div>
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
  color: #ff7a2f;
  font-size: 16px;
}
.kinds {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.kinds button {
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  border: none;
  background: #f7f1ea;
  color: #7a6e63;
  cursor: pointer;
}
.kinds button.on {
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
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
  background: #ff7a2f;
  color: #fff;
}
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
  color: #22c55e;
  font-size: 13px;
}
</style>
