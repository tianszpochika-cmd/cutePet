<script setup lang="ts">
import { emptyCopy, type EmptyKind } from '../logic';

const props = withDefaults(defineProps<{ kind?: EmptyKind; title?: string }>(), { kind: 'not-found', title: '' });
const copy = emptyCopy(props.kind);
</script>

<template>
  <div class="cp-empty">
    <div class="cp-illu" aria-hidden="true">{{ copy.illustration === 'lost' ? '🐾' : copy.illustration === 'curious' ? '👀' : '😴' }}</div>
    <p class="cp-title">{{ title || copy.title }}</p>
    <div class="cp-action"><slot /></div>
  </div>
</template>

<style scoped>
.cp-empty { display: grid; place-items: center; gap: 12px; padding: 48px 16px; text-align: center; }
.cp-illu { font-size: 56px; }
.cp-title { color: #7a6e63; font-size: 15px; }
</style>
