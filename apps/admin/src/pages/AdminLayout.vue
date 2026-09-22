<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { visibleNav, PERMISSIONS } from '../domain/workbench';

const router = useRouter();
// dev 会话：已登录角色权限（真实由 /me + 网关 JWT 提供，本地阶段接通）
const granted = ref<string[]>([...PERMISSIONS]);
const nav = computed(() => visibleNav(granted.value));
const collapsed = ref(false);

function demoteToReviewer() {
  granted.value = ['review.article', 'comment.manage', 'report.handle', 'user.view', 'dashboard.view.all'];
}
</script>

<template>
  <div class="layout" :class="{ collapsed }">
    <aside class="side">
      <div class="brand">🐾 cutePet · 运营工作台</div>
      <nav v-for="group in nav" :key="group.section" class="group">
        <p class="section">{{ group.section }}</p>
        <router-link v-for="item in group.items" :key="item.id" :to="item.to">
          {{ item.label }}
        </router-link>
      </nav>
      <button type="button" class="demo" @click="demoteToReviewer">切换为审核员（演示权限过滤）</button>
      <button type="button" class="demo" @click="router.push('/login')">退出</button>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout { display: flex; min-height: 100vh; }
.side { width: 220px; flex: none; background: #2b2118; color: #f7f1ea; padding: 16px 12px; display: grid; gap: 14px; align-content: start; }
.brand { font-weight: 700; font-size: 15px; padding: 0 8px; }
.group { display: grid; gap: 4px; }
.section { margin: 0; padding: 0 8px; font-size: 11px; color: #a89b8f; letter-spacing: .08em; }
.group a { color: #f7f1ea; text-decoration: none; font-size: 14px; padding: 8px; border-radius: 8px; }
.group a.router-link-active { background: #ff7a2f; color: #fff; font-weight: 600; }
.demo { margin-top: auto; height: 32px; border: none; border-radius: 8px; background: #463a30; color: #f7f1ea; font-size: 12px; }
.main { flex: 1; background: #faf7f3; padding: 24px; }
</style>
