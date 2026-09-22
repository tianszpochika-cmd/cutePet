<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{ modelValue: string; label?: string; error?: string; placeholder?: string; maxlength?: number }>(),
  { label: '', error: '', placeholder: '', maxlength: undefined },
);
const emit = defineEmits<{ 'update:modelValue': [value: string] }>();
const cls = computed(() => ['cp-input', { 'is-error': !!props.error }]);
</script>

<template>
  <label class="cp-field">
    <span v-if="label" class="cp-label">{{ label }}</span>
    <input
      :class="cls"
      :value="modelValue"
      :placeholder="placeholder"
      :maxlength="maxlength"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <span v-if="error" class="cp-error">{{ error }}</span>
  </label>
</template>

<style scoped>
.cp-field { display: flex; flex-direction: column; gap: 6px; }
.cp-label { font-size: 14px; font-weight: 600; color: #2b2118; }
.cp-input {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
  font-size: 15px;
  color: #2b2118;
  background: #fff;
  outline: none;
  transition: border 0.15s ease, box-shadow 0.15s ease;
}
.cp-input:focus { border: 1.5px solid #ff7a2f; box-shadow: 0 0 0 3px #fff1e8; }
.cp-input.is-error { border-color: #ef4444; }
.cp-error { font-size: 12px; color: #ef4444; }
</style>
