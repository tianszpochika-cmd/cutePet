<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ADMIN_NAV } from '../domain/workbench';
import { leavePreview } from '../adminPreview';

const route = useRoute();
const router = useRouter();
const nav = ADMIN_NAV.map((group) => ({
  ...group,
  items: group.items.filter((item, index, items) => items.findIndex((candidate) => candidate.to === item.to) === index),
}));
const pageTitle = computed(() => nav.flatMap((group) => group.items).find((item) => item.to === route.path)?.label ?? '运营工作台');

function closePreview() {
  leavePreview();
  void router.replace('/login');
}
</script>

<template>
  <a class="skip-link" href="#main-content">跳到主要内容</a>
  <div class="layout">
    <aside class="sidebar" aria-label="工作台导航">
      <router-link class="brand" to="/dashboard"><span class="brand-mark" aria-hidden="true">✿</span><span>cutePet <small>运营工作台</small></span></router-link>
      <div class="preview-label">只读界面预览</div>
      <nav class="nav" aria-label="管理端页面">
        <div v-for="group in nav" :key="group.section" class="nav-group">
          <p>{{ group.section }}</p>
          <router-link v-for="item in group.items" :key="item.id" :to="item.to" :aria-current="route.path === item.to ? 'page' : undefined">
            <span class="nav-dot" aria-hidden="true"></span>{{ item.label }}
          </router-link>
        </div>
      </nav>
      <div class="side-bottom"><p>当前没有管理员会话<br />页面不会执行管理操作</p><button type="button" @click="closePreview">退出预览 <span aria-hidden="true">↗</span></button></div>
    </aside>

    <div class="work-area">
      <header class="topbar"><div><span class="top-kicker">OPERATIONS / PREVIEW</span><strong>{{ pageTitle }}</strong></div><span class="status"><i aria-hidden="true"></i> 只读预览</span></header>
      <div class="preview-banner" role="status"><strong>界面预览</strong><span>这里不代表登录成功、拥有后台权限或业务数据已接入。每一项结果请以服务端回执和审计为准。</span></div>
      <main id="main-content" class="main-content" tabindex="-1"><router-view /></main>
    </div>
  </div>
  <div class="desktop-notice" role="status"><div class="notice-card"><span aria-hidden="true">✿</span><h1>请在电脑端查看管理工作台</h1><p>管理任务包含多列数据、对象详情与核对信息，需要较宽的工作区。请使用桌面浏览器打开。</p><button type="button" @click="closePreview">退出预览</button></div></div>
</template>

<style scoped>
.skip-link { position: fixed; z-index: 100; left: 18px; top: -70px; padding: 10px 16px; background: #fff; border-radius: 8px; }
.skip-link:focus { top: 12px; }
.layout { display: flex; min-height: 100vh; }
.sidebar { width: 238px; flex: none; display: flex; flex-direction: column; padding: 24px 15px 18px; background: #2b2118; color: #fff6ed; }
.brand { display: flex; gap: 10px; align-items: center; margin: 0 8px; color: #fff6ed; text-decoration: none; font-size: 20px; font-weight: 800; line-height: 1.15; }
.brand small { display: block; margin-top: 4px; color: #d5c7b8; font-size: 11px; font-weight: 500; letter-spacing: .08em; }
.brand-mark { display: grid; place-items: center; width: 38px; height: 38px; border-radius: 12px; color: #fff; background: var(--primary); font-size: 27px; }
.preview-label { margin: 26px 8px 16px; border: 1px solid #67594a; padding: 9px 12px; border-radius: 9px; color: #e7d1bc; font-size: 12px; }
.nav { overflow-y: auto; }
.nav-group { margin-bottom: 19px; }
.nav-group p { margin: 0 12px 5px; color: #b3a291; font-size: 11px; font-weight: 700; letter-spacing: .13em; }
.nav-group a { min-height: 40px; display: flex; align-items: center; gap: 11px; padding: 8px 12px; border-radius: 10px; color: #f4eae0; text-decoration: none; font-size: 13px; }
.nav-group a:hover { background: #49382b; }
.nav-group a[aria-current='page'] { background: #fff1e4; color: #733409; font-weight: 800; }
.nav-dot { width: 6px; height: 6px; border: 1px solid currentColor; border-radius: 50%; flex: none; }
.side-bottom { margin-top: auto; border-top: 1px solid #594a3d; padding: 16px 8px 0; }
.side-bottom p { margin: 0 0 12px; color: #c5b6a8; font-size: 11px; line-height: 1.7; }
.side-bottom button { width: 100%; display: flex; justify-content: space-between; align-items: center; min-height: 40px; padding: 0 12px; border: 1px solid #725d4b; border-radius: 10px; color: #fff6ed; background: transparent; }
.work-area { min-width: 0; flex: 1; }
.topbar { min-height: 79px; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 16px clamp(22px, 3vw, 42px); background: #fff; border-bottom: 1px solid var(--line); }
.topbar > div { display: grid; gap: 1px; }
.top-kicker { color: var(--muted); font-size: 10px; font-weight: 700; letter-spacing: .14em; }
.topbar strong { font-size: 18px; }
.status { display: flex; gap: 7px; align-items: center; white-space: nowrap; color: #705338; font-size: 12px; font-weight: 700; }
.status i { width: 8px; height: 8px; border-radius: 50%; background: #d59548; }
.preview-banner { display: flex; gap: 10px; align-items: flex-start; margin: 22px clamp(22px, 3vw, 42px) 0; padding: 12px 15px; border: 1px solid #ead1b9; border-radius: 12px; background: #fff5ea; color: #754316; font-size: 12px; line-height: 1.6; }
.preview-banner strong { flex: none; }
.main-content { min-height: calc(100vh - 170px); padding: 26px clamp(22px, 3vw, 42px) 60px; }
.desktop-notice { display: none; }
@media (max-width: 800px) { .layout, .skip-link { display: none; } .desktop-notice { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: #fff9f3; } .notice-card { max-width: 390px; padding: 30px; border: 1px solid var(--line); border-radius: 22px; background: #fff; } .notice-card > span { color: var(--primary); font-size: 44px; } .notice-card h1 { font-size: 25px; } .notice-card p { color: var(--muted); line-height: 1.8; } .notice-card button { min-height: 42px; padding: 0 16px; border: 1px solid var(--line); border-radius: 10px; background: #fff; } }
</style>
