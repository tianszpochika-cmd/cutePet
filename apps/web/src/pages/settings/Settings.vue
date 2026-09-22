<script setup lang="ts">
import { useRouter } from 'vue-router';
import { RULE_DOCS } from '../../domain/settings';

const router = useRouter();

const items = [
  { label: '资料与养宠标签', to: '/settings' },
  { label: '隐私设置', to: '/settings/privacy' },
  { label: '通知设置', to: '/settings/notifications' },
  { label: '数据导出', to: '/settings/export' },
  { label: '注销账号', to: '/settings/delete', danger: true },
];
</script>

<template>
  <div class="settings">
    <header>
      <button type="button" class="back" @click="router.push('/me')">‹ 我的</button>
      <h1>设置</h1>
    </header>

    <ul class="group">
      <li v-for="it in items" :key="it.to">
        <button type="button" :class="{ danger: (it as { danger?: boolean }).danger }" @click="router.push(it.to)">
          {{ it.label }}
        </button>
      </li>
    </ul>

    <ul class="group">
      <li v-for="r in RULE_DOCS" :key="r.slug">
        <button type="button" @click="router.push(`/legal/${r.slug}`)">规则 · {{ r.label }}</button>
      </li>
    </ul>

    <ul class="group">
      <li><button type="button" @click="router.push('/help')">帮助与客服</button></li>
      <li><button type="button" @click="router.push('/blocked')">受限账号状态（X15）</button></li>
    </ul>
  </div>
</template>

<style scoped>
.settings { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 14px; }
header { display: flex; gap: 12px; align-items: center; }
.back { background: none; border: none; color: #ff7a2f; }
.group { list-style: none; padding: 0; display: grid; gap: 8px; margin: 0; }
.group button { width: 100%; text-align: left; height: 48px; background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 12px; padding: 0 16px; font-size: 14px; color: #2b2118; }
.group .danger { color: #ef4444; }
</style>
