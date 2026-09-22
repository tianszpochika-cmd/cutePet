<script setup lang="ts">
withDefaults(defineProps<{ section?: 'home' | 'feed' | 'list' | 'detail'; title?: string }>(), {
  section: 'list',
  title: '',
});
const rowsBySection = { home: 6, feed: 12, list: 8, detail: 4 } as const;
</script>

<template>
  <div class="skeleton" aria-busy="true" aria-label="加载中">
    <div
      v-for="i in rowsBySection[section]"
      :key="i"
      class="row shimmer"
      :style="{ animationDelay: `${Math.min(i, 8) * 80}ms` }"
    />
    <p v-if="title" class="label">{{ title }}</p>
  </div>
</template>

<style scoped>
.skeleton { display: grid; gap: 10px; padding: 8px 0; }
.row { height: 64px; border-radius: 12px; background: linear-gradient(90deg, #f4eee8 25%, #fbf7f2 50%, #f4eee8 75%); background-size: 200% 100%; animation: shimmer 1.2s linear infinite; }
@keyframes shimmer { from { background-position: 200% 0; } to { background-position: -200% 0; } }
.label { color: #7a6e63; font-size: 12px; }
</style>
