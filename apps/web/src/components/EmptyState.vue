<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{ kind?: 'lost' | 'dozing' | 'curious'; title?: string; actionLabel?: string; error?: string }>(),
  { kind: 'not-found' as never, title: '', actionLabel: '', error: '' },
);

const emoji = computed(() => {
  if (props.error) return '🌩';
  switch (props.kind) {
    case 'dozing':
      return '😴';
    case 'curious':
      return '👀';
    default:
      return '🐾';
  }
});

const text = computed(() => {
  if (props.error) return props.error;
  if (props.title) return props.title;
  switch (props.kind) {
    case 'dozing':
      return '加载中…';
    case 'curious':
      return '从这里开始吧';
    default:
      return '这里空空的';
  }
});

const emit = defineEmits<{ action: [] }>();
</script>

<template>
  <div class="empty" data-testid="empty-state">
    <span class="emoji" aria-hidden="true">{{ emoji }}</span>
    <p>{{ text }}</p>
    <button v-if="actionLabel" type="button" @click="emit('action')">{{ actionLabel }}</button>
  </div>
</template>

<style scoped>
.empty { display: grid; place-items: center; gap: 10px; padding: 48px 16px; text-align: center; }
.emoji { font-size: 56px; animation: float 3s ease-in-out infinite; }
p { color: #7a6e63; font-size: 15px; margin: 0; }
button { border: none; border-radius: 999px; background: #ff7a2f; color: #fff; height: 44px; padding: 0 24px; font-weight: 600; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
</style>
