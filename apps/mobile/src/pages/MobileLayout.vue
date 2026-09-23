<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { TABS, stackAction, type TabId } from '../domain/mobile';

const route = useRoute();
const router = useRouter();
const online = ref(typeof navigator === 'undefined' ? true : navigator.onLine);
const rootPaths: Record<TabId, string> = { home: '/', news: '/news', explore: '/explore', me: '/me' };
const lastPaths: Record<TabId, string> = { ...rootPaths };

function tabFor(path: string): TabId {
  if (path.startsWith('/news') || path.startsWith('/write')) return 'news';
  if (path.startsWith('/explore') || path.startsWith('/events')) return 'explore';
  if (path.startsWith('/me') || path.startsWith('/settings') || path.startsWith('/messages') || path.startsWith('/family')) return 'me';
  return 'home';
}
const activeTab = computed(() => tabFor(route.path));
watch(() => route.fullPath, (path) => { lastPaths[tabFor(route.path)] = path; }, { immediate: true });

function selectTab(target: TabId) {
  const action = stackAction(activeTab.value, target);
  const path = action === 'SWITCH_KEEP_STACK' ? lastPaths[target] : rootPaths[target];
  if (route.fullPath !== path) void router.push(path);
}

function updateConnection() { online.value = navigator.onLine; }
onMounted(() => {
  window.addEventListener('online', updateConnection);
  window.addEventListener('offline', updateConnection);
});
onUnmounted(() => {
  window.removeEventListener('online', updateConnection);
  window.removeEventListener('offline', updateConnection);
});
</script>

<template>
  <div class="m-layout">
    <a class="skip" href="#mobile-main">跳到主要内容</a>
    <div v-if="!online" class="offline-banner" role="status">设备当前离线。公开内容可能不可读取；未收到平台回执前，记录和待办都不算完成。</div>
    <main id="mobile-main" class="m-main" tabindex="-1"><router-view /></main>
    <nav class="tabbar" aria-label="主要导航" data-testid="tabbar">
      <button v-for="t in TABS" :key="t.id" type="button" :class="{ on: activeTab === t.id }" :aria-current="activeTab === t.id ? 'page' : undefined" :data-testid="`tab-${t.id}`" @click="selectTab(t.id)">
        <svg v-if="t.id === 'home'" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V10Z"/><path d="M9 21v-7h6v7"/></svg>
        <svg v-else-if="t.id === 'news'" viewBox="0 0 24 24" fill="none" aria-hidden="true"><rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8h8M8 12h8M8 16h5"/></svg>
        <svg v-else-if="t.id === 'explore'" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="m15.8 8.2-2.4 5.2-5.2 2.4 2.4-5.2 5.2-2.4Z"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.6-3.4 3.1-5.2 7-5.2s6.4 1.8 7 5.2"/></svg>
        <span>{{ t.label }}</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.m-layout { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #fff9f3; box-shadow: 0 0 40px #57453812; }
.skip { position: fixed; z-index: 100; top: -60px; left: max(calc((100vw - 480px) / 2 + 12px), 12px); padding: 8px 12px; border-radius: 8px; background: #fff; }
.skip:focus { top: 10px; }
.m-main { min-height: 100vh; padding-bottom: calc(77px + env(safe-area-inset-bottom, 0px)); }
.offline-banner { padding: 10px 16px; background: #fff0de; border-bottom: 1px solid #e8c9a5; color: #74401b; font-size: 12px; line-height: 1.55; }
.tabbar { position: fixed; z-index: 40; bottom: 0; left: 50%; transform: translateX(-50%); width: min(100%, 480px); min-height: calc(64px + env(safe-area-inset-bottom, 0px)); display: grid; grid-template-columns: repeat(4, 1fr); padding-bottom: max(8px, env(safe-area-inset-bottom, 0px)); border-top: 1px solid #eadfd4; background: #fffdfb; box-shadow: 0 -7px 28px #2b21180d; }
.tabbar button { position: relative; display: grid; justify-items: center; align-content: center; gap: 2px; min-height: 56px; border: 0; background: transparent; color: #786b60; font-size: 11px; font-weight: 600; }
.tabbar button.on { color: var(--primary); font-weight: 800; }
.tabbar button.on::before { content: ''; position: absolute; top: 0; width: 34px; height: 3px; border-radius: 0 0 4px 4px; background: var(--primary); }
.tabbar svg { width: 23px; height: 23px; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
@media (min-width: 481px) { .m-layout { border-left: 1px solid #eadfd4; border-right: 1px solid #eadfd4; } }
</style>
