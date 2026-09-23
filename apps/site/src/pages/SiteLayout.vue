<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { SITE_NAV } from '../domain/site';

const route = useRoute();
const menuOpen = ref(false);
const menuButton = ref<HTMLButtonElement | null>(null);
const mobileMenu = ref<HTMLElement | null>(null);

async function openMenu() {
  menuOpen.value = true;
  document.body.style.overflow = 'hidden';
  await nextTick();
  mobileMenu.value?.querySelector<HTMLElement>('button')?.focus();
}

function closeMenu(restoreFocus = true) {
  menuOpen.value = false;
  document.body.style.overflow = '';
  if (restoreFocus) nextTick(() => menuButton.value?.focus());
}

function onKeydown(event: KeyboardEvent) {
  if (!menuOpen.value) return;
  if (event.key === 'Escape') { closeMenu(); return; }
  if (event.key !== 'Tab') return;
  const focusables = [...(mobileMenu.value?.querySelectorAll<HTMLElement>('button, a[href]') ?? [])];
  const first = focusables[0];
  const last = focusables[focusables.length - 1];
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last?.focus(); }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first?.focus(); }
}

function navigateFromMenu() {
  closeMenu(false);
  nextTick(() => document.getElementById('site-main')?.focus());
}

watch(() => route.fullPath, () => {
  if (menuOpen.value) closeMenu(false);
});
watch(() => route.path, async () => {
  await nextTick();
  document.getElementById('site-main')?.focus({ preventScroll: true });
});
onMounted(() => window.addEventListener('keydown', onKeydown));
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown);
  document.body.style.overflow = '';
});
</script>

<template>
  <div class="site-shell">
    <a class="skip-link" href="#site-main">跳到主要内容</a>
    <header class="site-header" :inert="menuOpen">
      <div class="site-wrap header-inner">
        <router-link class="brand" to="/" aria-label="cutePet 官网首页">
          <span class="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 32 32" fill="currentColor"><path d="M16 24.8c-3.4 0-4.5 2.2-7.1 1.1-2.4-1-2.8-4.1-.5-6.1 2.4-2.2 3.7-6.6 7.6-6.6s5.2 4.4 7.6 6.6c2.3 2 .8 5.1-1.5 6.1-2.6 1.1-3.8-1.1-6.8-1.1Z"/><ellipse cx="7.2" cy="10.4" rx="2.4" ry="3.5"/><ellipse cx="13" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="19.4" cy="6.6" rx="2.4" ry="3.5"/><ellipse cx="25" cy="10.4" rx="2.4" ry="3.5"/></svg>
          </span>
          <span>cutePet<span class="brand-dot">.</span></span>
        </router-link>
        <nav class="desktop-nav" aria-label="主导航">
          <router-link v-for="item in SITE_NAV.filter((entry) => entry.id !== 'home' && entry.id !== 'download')" :key="item.id" :to="item.to">{{ item.label }}</router-link>
        </nav>
        <router-link class="site-button header-cta" to="/download">开始使用 <span aria-hidden="true">↗</span></router-link>
        <button ref="menuButton" class="menu-trigger" type="button" :aria-expanded="menuOpen" aria-controls="site-mobile-menu" :aria-label="menuOpen ? '关闭导航菜单' : '打开导航菜单'" @click="menuOpen ? closeMenu() : openMenu()">
          <span></span><span></span><span></span>
        </button>
      </div>
    </header>

    <button v-if="menuOpen" class="menu-shade" type="button" aria-label="关闭导航菜单" @click="closeMenu()" />
    <nav id="site-mobile-menu" ref="mobileMenu" class="mobile-menu" :class="{ open: menuOpen }" :aria-hidden="!menuOpen" aria-label="移动端导航">
      <button class="mobile-close" type="button" aria-label="关闭导航菜单" :tabindex="menuOpen ? 0 : -1" @click="closeMenu()">×</button>
      <router-link v-for="item in SITE_NAV" :key="item.id" :to="item.to" :tabindex="menuOpen ? 0 : -1" @click="navigateFromMenu">{{ item.label }}<span aria-hidden="true">→</span></router-link>
    </nav>

    <main id="site-main" tabindex="-1" :inert="menuOpen"><router-view /></main>

    <footer class="site-footer" :inert="menuOpen">
      <div class="site-wrap footer-grid">
        <div class="footer-brand">
          <strong>cutePet<span>.</span></strong>
          <p>把每一份在意，变成有条理的照顾。</p>
          <small>品牌名称与标识仍待正式确认</small>
        </div>
        <div><h2>产品</h2><router-link to="/products">四大能力</router-link><router-link to="/family">家庭共享</router-link><router-link to="/services">使用场景</router-link></div>
        <div><h2>发现</h2><router-link to="/center">养宠资讯</router-link><router-link to="/events">本地活动</router-link><router-link to="/help">常见问题</router-link></div>
        <div><h2>支持与规则</h2><router-link to="/contact">联系我们</router-link><router-link to="/legal/terms">用户协议</router-link><router-link to="/legal/privacy">隐私政策</router-link><router-link to="/legal/report">投诉与版权</router-link></div>
      </div>
      <div class="site-wrap footer-bottom"><span>© 2026 cutePet · 产品官网</span><span>正式条款、联系渠道与备案信息待核实</span></div>
    </footer>
  </div>
</template>

<style scoped>
.skip-link{position:absolute;left:12px;top:-70px;z-index:100;padding:8px 14px;border-radius:9px;background:#fff;color:var(--site-action)}
.skip-link:focus{top:10px}
.site-header{position:sticky;top:0;z-index:30;border-bottom:1px solid rgba(234,223,212,.7);background:rgba(255,249,243,.94);backdrop-filter:blur(14px)}
.header-inner{display:flex;align-items:center;gap:28px;height:76px}
.brand{display:inline-flex;align-items:center;gap:11px;flex:none;color:var(--site-ink);font-size:23px;font-weight:850;letter-spacing:-.06em;text-decoration:none}
.brand-mark{display:grid;place-items:center;width:38px;height:38px;border-radius:13px;background:var(--site-action);color:#fff;transform:rotate(-8deg)}
.brand-mark svg{width:25px;height:25px}.brand-dot,.footer-brand strong span{color:var(--site-orange)}
.desktop-nav{display:flex;align-items:center;gap:clamp(14px,2vw,30px);margin-left:auto}
.desktop-nav a{display:inline-flex;align-items:center;min-height:44px;color:#65584e;font-size:14px;font-weight:700;text-decoration:none;white-space:nowrap}
.desktop-nav a.router-link-active{color:var(--site-action)}.header-cta{min-height:44px;padding-inline:19px;font-size:13px}
.menu-trigger{display:none;place-content:center;gap:5px;width:44px;height:44px;margin-left:auto;border:1px solid var(--site-line);border-radius:12px;background:#fff}
.menu-trigger span{display:block;width:19px;height:2px;border-radius:2px;background:var(--site-ink)}
.menu-shade{position:fixed;z-index:39;inset:0;width:100%;border:0;background:rgba(43,33,24,.42)}
.mobile-menu{position:fixed;z-index:40;top:0;right:0;bottom:0;display:flex;flex-direction:column;width:min(390px,87vw);padding:95px 24px 25px;overflow-y:auto;overscroll-behavior:contain;background:var(--site-bg);box-shadow:-20px 0 60px rgba(43,33,24,.14);transform:translateX(110%);transition:transform .3s var(--site-ease);visibility:hidden}
.mobile-menu.open{transform:translateX(0);visibility:visible}
.mobile-menu a{display:flex;align-items:center;justify-content:space-between;min-height:58px;border-bottom:1px solid var(--site-line);color:var(--site-ink);font-size:20px;font-weight:750;text-decoration:none}
.mobile-menu a:last-child{color:var(--site-action)}.mobile-close{position:absolute;top:29px;right:22px;width:44px;height:44px;border:1px solid var(--site-line);border-radius:50%;background:#fff;color:var(--site-ink);font-size:25px}
main{min-height:55vh}
.site-footer{margin-top:30px;padding:58px 0 24px;background:var(--site-ink);color:#fffaf5}
.footer-grid{display:grid;grid-template-columns:2fr repeat(3,1fr);gap:36px}
.footer-brand strong{font-size:26px;letter-spacing:-.055em}.footer-brand p{margin:13px 0 8px;color:#d3c6bb;font-size:14px}.footer-brand small{color:#a89b8f;font-size:12px}
.footer-grid h2{margin:2px 0 16px;font-size:14px}.footer-grid a{display:flex;align-items:center;min-height:44px;color:#d3c6bb;font-size:13px;text-decoration:none}
.footer-grid a:hover{text-decoration:underline;text-underline-offset:4px}
.footer-bottom{display:flex;justify-content:space-between;gap:16px;margin-top:34px;padding-top:18px;border-top:1px solid #55473c;color:#a89b8f;font-size:12px}
@media(max-width:980px){.desktop-nav,.header-cta{display:none}.menu-trigger{display:grid}}
@media(max-width:760px){.header-inner{height:66px}.footer-grid{grid-template-columns:1fr 1fr;gap:30px}.footer-brand{grid-column:1/-1}.footer-bottom{flex-direction:column}.site-footer{padding-top:42px}}
</style>
