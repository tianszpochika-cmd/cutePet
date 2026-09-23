<script setup lang="ts">
import { nextTick, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getAccessToken } from '@cutepet/api-client';

const route = useRoute();
const menuOpen = ref(false);
const menuButton = ref<HTMLButtonElement | null>(null);
const menuPanel = ref<HTMLElement | null>(null);
const hasSession = ref(Boolean(getAccessToken()));
const navItems = [
  { to: '/', label: '首页' },
  { to: '/pets', label: '宠物管理' },
  { to: '/news', label: '资讯' },
  { to: '/explore', label: '探索' },
  { to: '/goods', label: '用品' },
];

function closeMenu(returnFocus = false) {
  menuOpen.value = false;
  if (returnFocus) void nextTick(() => menuButton.value?.focus());
}

function onMenuKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') { closeMenu(true); return; }
  if (event.key !== 'Tab' || !menuPanel.value) return;
  const focusable = [...menuPanel.value.querySelectorAll<HTMLElement>('a, button')];
  if (!focusable.length) return;
  if (event.shiftKey && document.activeElement === focusable[0]) {
    event.preventDefault(); focusable.at(-1)?.focus();
  } else if (!event.shiftKey && document.activeElement === focusable.at(-1)) {
    event.preventDefault(); focusable[0]?.focus();
  }
}

watch(() => route.fullPath, () => {
  hasSession.value = Boolean(getAccessToken());
  closeMenu();
});
watch(menuOpen, async (open) => {
  document.body.style.overflow = open ? 'hidden' : '';
  if (open) { await nextTick(); menuPanel.value?.querySelector<HTMLElement>('a')?.focus(); }
});
onUnmounted(() => { document.body.style.overflow = ''; });
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <header class="topbar">
      <div class="topbar-inner">
        <router-link class="brand" to="/" aria-label="cutePet，返回首页"><span class="brand-mark" aria-hidden="true">✦</span><span>cutePet</span></router-link>
        <nav class="desktop-nav" aria-label="主导航">
          <router-link v-for="item in navItems" :key="item.to" :to="item.to" :class="{ active: item.to === '/' ? route.path === '/' : route.path.startsWith(item.to) }">{{ item.label }}</router-link>
        </nav>
        <nav class="utility-nav" aria-label="个人导航">
          <router-link class="utility-link" to="/messages">消息</router-link>
          <router-link class="utility-link" to="/family">家庭</router-link>
          <router-link class="account-link" :to="hasSession ? '/me' : '/login'">{{ hasSession ? '我的' : '登录 / 注册' }}</router-link>
        </nav>
        <button ref="menuButton" class="menu-toggle" type="button" :aria-expanded="menuOpen" aria-controls="web-mobile-nav" aria-label="打开导航" @click="menuOpen = !menuOpen">☰</button>
      </div>
    </header>
    <main id="main-content" tabindex="-1"><router-view /></main>
    <footer class="app-footer"><span>cutePet · 认真记录每一天</span><nav aria-label="页脚导航"><router-link to="/help">帮助</router-link><router-link to="/legal/privacy">隐私</router-link><router-link to="/settings">设置</router-link></nav></footer>
    <div v-if="menuOpen" class="nav-backdrop" @click="closeMenu()">
      <nav id="web-mobile-nav" ref="menuPanel" class="mobile-nav" aria-label="移动导航" @click.stop @keydown="onMenuKeydown">
        <div class="mobile-nav-head"><strong>导航</strong><button type="button" aria-label="关闭导航" @click="closeMenu(true)">关闭</button></div>
        <router-link v-for="item in navItems" :key="item.to" :to="item.to">{{ item.label }}</router-link>
        <div class="mobile-nav-extra"><router-link to="/messages">消息</router-link><router-link to="/family">家庭</router-link><router-link :to="hasSession ? '/me' : '/login'">{{ hasSession ? '我的' : '登录 / 注册' }}</router-link></div>
      </nav>
    </div>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; flex-direction: column; }
.skip-link { position: fixed; left: 16px; top: -80px; z-index: 60; padding: 10px 16px; background: #fff; border-radius: 12px; }
.skip-link:focus { top: 12px; }
.topbar { position: sticky; top: 0; z-index: 30; background: rgba(255, 249, 243, .94); border-bottom: 1px solid var(--line); backdrop-filter: blur(18px); }
.topbar-inner { width: min(1320px, calc(100% - 64px)); min-height: 76px; margin: auto; display: flex; align-items: center; gap: clamp(18px, 3vw, 44px); }
.brand { display: inline-flex; align-items: center; gap: 10px; color: var(--ink); font-size: 24px; font-weight: 850; letter-spacing: -.05em; text-decoration: none; white-space: nowrap; }
.brand-mark { display: grid; place-items: center; width: 36px; height: 36px; color: #fff; background: var(--primary); border-radius: 12px; font-size: 23px; line-height: 1; }
.desktop-nav, .utility-nav { display: flex; align-items: center; gap: clamp(14px, 1.8vw, 26px); }
.desktop-nav a, .utility-nav a { display: inline-flex; align-items: center; justify-content: center; min-height: 44px; color: var(--ink-2); font-size: 14px; font-weight: 650; text-decoration: none; white-space: nowrap; }
.desktop-nav a:hover, .desktop-nav a.active, .utility-nav .utility-link:hover { color: var(--primary); }
.desktop-nav a.active { position: relative; }
.desktop-nav a.active::after { content: ''; position: absolute; left: 0; right: 0; bottom: 3px; height: 3px; background: var(--primary); border-radius: 3px; }
.utility-nav { margin-left: auto; gap: 18px; }
.utility-nav .account-link { padding: 0 19px; border: 1px solid var(--primary); border-radius: 999px; color: var(--primary); }
.utility-nav .account-link:hover { background: var(--primary-soft); }
.menu-toggle { display: none; margin-left: auto; width: 44px; height: 44px; border: 1px solid var(--line); border-radius: 12px; background: #fff; color: var(--ink); font-size: 21px; }
main { flex: 1; min-width: 0; }
.app-footer { width: min(1320px, calc(100% - 64px)); margin: 80px auto 0; padding: 24px 0 32px; border-top: 1px solid var(--line); display: flex; justify-content: space-between; gap: 16px; color: var(--ink-2); font-size: 13px; }
.app-footer nav { display: flex; flex-wrap: wrap; gap: 18px; }
.app-footer a { min-height: 40px; display: inline-flex; align-items: center; color: var(--ink-2); text-decoration: none; }
.app-footer a:hover { color: var(--primary); }
.nav-backdrop { position: fixed; inset: 0; z-index: 50; background: rgba(43, 33, 24, .45); }
.mobile-nav { position: absolute; right: 0; top: 0; bottom: 0; width: min(360px, 88vw); padding: 22px; overflow-y: auto; background: var(--bg); display: flex; flex-direction: column; gap: 4px; box-shadow: -20px 0 60px rgba(43,33,24,.14); }
.mobile-nav-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.mobile-nav-head button { min-width: 44px; min-height: 44px; border: 0; background: transparent; color: var(--primary); }
.mobile-nav a { display: flex; align-items: center; min-height: 52px; padding: 0 12px; border-radius: 12px; color: var(--ink); font-weight: 650; text-decoration: none; }
.mobile-nav a.router-link-active { background: var(--primary-soft); color: var(--primary); }
.mobile-nav-extra { display: grid; border-top: 1px solid var(--line); margin-top: 16px; padding-top: 12px; }
@media (max-width: 1050px) { .desktop-nav, .utility-nav { display: none; } .menu-toggle { display: block; } }
@media (max-width: 650px) { .topbar-inner, .app-footer { width: calc(100% - 32px); } .topbar-inner { min-height: 64px; } .app-footer { flex-direction: column; margin-top: 48px; } }
</style>
