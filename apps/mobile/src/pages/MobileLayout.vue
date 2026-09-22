<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { TABS, safeAreaInsets, stackAction } from '../domain/mobile';
import { OFFLINE_COPY, pendingBadge, type OfflineStatus } from '../domain/mobileMotion';

const route = useRoute();
const router = useRouter();

const insets = safeAreaInsets({
  bottom: typeof window === 'undefined' ? 0 : 34, // env(safe-area-inset-bottom) 由 CSS 兜底
  top: 0,
});

const status = ref<OfflineStatus>('ONLINE');
const pending = ref(0);

const activeTab = computed(() => {
  const path = route.path;
  if (path.startsWith('/pets') || path.startsWith('/todos') || path.startsWith('/plans')) return 'pet';
  if (path.startsWith('/explore') || path.startsWith('/events')) return 'explore';
  if (path.startsWith('/me') || path.startsWith('/settings') || path.startsWith('/messages')) return 'me';
  return 'home';
});

function go(tabPath: string, tabId: string) {
  const action = stackAction(activeTab.value as never, tabId as never, tabPath);
  void router.push(tabPath);
  void action;
}

const banner = computed(() => OFFLINE_COPY[status.value].banner);
const badge = computed(() => pendingBadge(pending.value, status.value));

// 演示离线态（真实 online/offline 事件监听随本地阶段）
function cycleStatus() {
  const order: OfflineStatus[] = ['ONLINE', 'OFFLINE_PENDING', 'CONFLICT'];
  status.value = order[(order.indexOf(status.value) + 1) % order.length]!;
}
void cycleStatus;
</script>

<template>
  <div class="m-layout">
    <div v-if="banner" :class="['offline', OFFLINE_COPY[status].tone]" data-testid="offline-banner">
      {{ banner }}
      <button type="button" class="mini" @click="status = 'ONLINE'">恢复在线</button>
    </div>

    <main class="m-main">
      <router-view :offline-status="status" :pending="pending" @resolve-conflict="status = 'ONLINE'" />
    </main>

    <nav class="tabbar" :style="{ paddingBottom: `${insets.tabBottom}px` }" data-testid="tabbar">
      <button
        v-for="t in TABS"
        :key="t.id"
        type="button"
        :class="{ on: activeTab === t.id }"
        :data-testid="`tab-${t.id}`"
        @click="go(t.path, t.id)"
      >
        <span class="icon">{{ t.icon }}</span>
        <span>{{ t.label }}</span>
        <span v-if="t.id === 'me' && badge" class="badge">{{ badge }}</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.m-layout { min-height: 100vh; display: flex; flex-direction: column; background: #f7f3ee; }
.m-main { flex: 1; padding-bottom: calc(64px + env(safe-area-inset-bottom, 12px)); }
.offline { padding: 8px 14px; font-size: 13px; display: flex; gap: 10px; align-items: center; }
.offline.warn { background: #fff1e8; color: #b45309; }
.offline.error { background: #fdecec; color: #b91c1c; }
.offline.ok { background: #e7f8ef; color: #15803d; }
.mini { margin-left: auto; border: none; background: rgba(255,255,255,.7); border-radius: 999px; padding: 4px 10px; font-size: 12px; color: inherit; }
.tabbar {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  height: calc(64px + env(safe-area-inset-bottom, 12px));
  padding-bottom: env(safe-area-inset-bottom, 12px);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: rgba(255,255,255,.96);
  backdrop-filter: blur(12px);
  box-shadow: 0 -4px 16px rgba(43,33,24,.06);
  z-index: 40;
}
.tabbar button { position: relative; border: none; background: none; display: grid; place-items: center; gap: 2px; color: #7a6e63; font-size: 11px; padding-top: 8px; min-height: 44px; cursor: pointer; }
.tabbar button.on { color: #ff7a2f; font-weight: 700; }
.icon { font-size: 20px; }
.badge { position: absolute; top: 6px; right: 22%; background: #ef4444; color: #fff; border-radius: 999px; font-size: 10px; padding: 0 5px; }
</style>
