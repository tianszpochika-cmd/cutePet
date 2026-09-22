<script setup lang="ts">
import { statusTone } from '../logic';

const props = withDefaults(defineProps<{ status: string; label?: string }>(), { label: '' });
</script>

<template>
  <span :class="['cp-badge', `tone-${statusTone(props.status)}`]">{{ label || status }}</span>
</template>

<style scoped>
.cp-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.tone-gray { background: #f0e6dc; color: #7a6e63; }
.tone-orange { background: #fff1e8; color: #ff7a2f; }
.tone-blue { background: #eaf1ff; color: #4d8dff; }
.tone-green { background: #e7f8ef; color: #22c55e; }
.tone-red { background: #fdecec; color: #ef4444; }
</style>
