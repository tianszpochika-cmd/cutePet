<script setup lang="ts">
withDefaults(defineProps<{ padded?: boolean }>(), { padded: true });
</script>

<template>
  <div :class="['cp-card', { 'is-padded': padded }]">
    <slot />
  </div>
</template>

<style scoped>
.cp-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  overflow: hidden;
  transition: box-shadow 0.45s cubic-bezier(0.22, 1, 0.36, 1), transform 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
.cp-card:hover { box-shadow: 0 12px 32px rgba(43, 33, 24, 0.1); transform: translateY(-4px); }
.is-padded { padding: 16px; }
</style>
