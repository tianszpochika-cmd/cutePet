<script setup lang="ts">
import { computed } from 'vue';
import { buttonClass, type ButtonVariant, type ButtonSize } from '../logic';

const props = withDefaults(
  defineProps<{ variant?: ButtonVariant; size?: ButtonSize; disabled?: boolean; block?: boolean }>(),
  { variant: 'primary', size: 'm', disabled: false, block: false },
);
const cls = computed(() => [buttonClass(props.variant, props.size, props.disabled), { 'is-block': props.block }]);
</script>

<template>
  <button :class="cls" :disabled="disabled" type="button">
    <slot />
  </button>
</template>

<style scoped>
.cp-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  border-radius: 999px;
  font-weight: 600;
  transition: transform 0.15s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.15s ease, background 0.15s ease;
}
.cp-btn--m { height: 48px; padding: 0 24px; font-size: 16px; }
.cp-btn--s { height: 36px; padding: 0 16px; font-size: 14px; }
.cp-btn--primary { background: #ff7a2f; color: #fff; }
.cp-btn--primary:hover { background: #f26a1b; transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255, 122, 47, 0.28); }
.cp-btn--secondary { background: #fff; color: #ff7a2f; box-shadow: inset 0 0 0 1.5px #ff7a2f; }
.cp-btn--secondary:hover { background: #fff1e8; }
.cp-btn--ghost { background: transparent; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; }
.cp-btn:active { transform: scale(0.97); }
.is-disabled, .cp-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }
.is-block { display: flex; width: 100%; }
</style>
