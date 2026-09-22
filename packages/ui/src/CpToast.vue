<script setup lang="ts">
import { createToastStore, type ToastTone } from '../logic';

const store = createToastStore();
function fire(message: string, tone: ToastTone = 'info') {
  const id = store.push(message, tone);
  setTimeout(() => store.dismiss(id), 2500);
}
defineExpose({ fire });
</script>

<template>
  <Teleport to="body">
    <div class="cp-toast-layer" aria-live="polite">
      <div v-for="t in store.items" :key="t.id" :class="['cp-toast', `tone-${t.tone}`]">{{ t.message }}</div>
    </div>
  </Teleport>
</template>

<style scoped>
.cp-toast-layer { position: fixed; top: 24px; left: 50%; transform: translateX(-50%); z-index: 3000; display: grid; gap: 8px; }
.cp-toast {
  padding: 10px 20px;
  border-radius: 999px;
  color: #fff;
  font-size: 14px;
  background: #2b2118;
  box-shadow: 0 12px 32px rgba(43, 33, 24, 0.2);
  animation: cp-toast-in 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}
.tone-success { background: #22c55e; }
.tone-error { background: #ef4444; }
@keyframes cp-toast-in { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }
</style>
