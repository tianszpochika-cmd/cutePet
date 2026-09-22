<script setup lang="ts">
withDefaults(defineProps<{ open: boolean; title?: string; danger?: boolean }>(), { title: '', danger: false });
const emit = defineEmits<{ confirm: []; cancel: [] }>();
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="cp-dialog-layer" @click.self="emit('cancel')">
      <div class="cp-dialog" role="dialog" aria-modal="true">
        <h3 class="cp-title">{{ title }}</h3>
        <div class="cp-body"><slot /></div>
        <div class="cp-actions">
          <button class="cp-cancel" type="button" @click="emit('cancel')">取消</button>
          <button :class="['cp-confirm', { 'is-danger': danger }]" type="button" @click="emit('confirm')">确认</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.cp-dialog-layer { position: fixed; inset: 0; background: rgba(43, 33, 24, 0.45); display: grid; place-items: center; z-index: 2000; animation: cp-fade 0.15s ease; }
.cp-dialog { width: 280px; background: #fff; border-radius: 16px; overflow: hidden; text-align: center; animation: cp-pop 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cp-title { font-size: 17px; font-weight: 600; margin: 20px 16px 8px; }
.cp-body { font-size: 14px; color: #7a6e63; padding: 0 16px 16px; }
.cp-actions { display: flex; border-top: 1px solid #f0e6dc; }
.cp-actions button { flex: 1; height: 44px; border: none; background: none; font-size: 15px; cursor: pointer; }
.cp-cancel { color: #7a6e63; border-right: 1px solid #f0e6dc; }
.cp-confirm { color: #ff7a2f; font-weight: 600; }
.cp-confirm.is-danger { color: #ef4444; }
@keyframes cp-fade { from { opacity: 0; } to { opacity: 1; } }
@keyframes cp-pop { from { transform: scale(0.94); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>
