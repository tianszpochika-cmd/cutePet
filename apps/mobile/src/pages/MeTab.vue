<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ME_GALLERY, galleryGate, unreadBadge } from '../../../web/src/domain/family.ts';

const router = useRouter();
const loggedIn = ref(true);
const unread = ref(3);

function nav(to: string) {
  void router.push(galleryGate(loggedIn.value, to)); // #45 未登录统一回跳
}
const ICONS: Record<string, string> = { pets: '🐾', family: '👨‍👩‍👧', creation: '✍️', favorites: '⭐', messages: '🔔', activity: '🎪' };
</script>

<template>
  <div class="m-me">
    <header class="profile">
      <div class="avatar">🐾</div>
      <div>
        <strong>{{ loggedIn ? '养宠的你' : '未登录' }}</strong>
        <p>{{ loggedIn ? '已登录 · 手机号 138****5678' : '登录后同步档案与家庭' }}</p>
      </div>
      <button v-if="!loggedIn" type="button" class="login" @click="nav('/login')">登录</button>
    </header>

    <!-- #45 宫格六项（含活动中心） -->
    <nav class="gallery" data-testid="gallery">
      <button v-for="g in ME_GALLERY" :key="g.id" type="button" @click="nav(g.to)">
        <span class="icon">{{ ICONS[g.id] }}</span>
        <span>{{ g.label }}</span>
        <span v-if="g.id === 'messages' && unread > 0" class="badge">{{ unreadBadge(unread) }}</span>
      </button>
    </nav>

    <ul class="menu">
      <li><button type="button" @click="nav('/settings')">设置与通知</button></li>
      <li><button type="button" @click="nav('/me/sync')">离线与同步（T10.6）</button></li>
      <li><button type="button" @click="nav('/settings/delete')">注销账号（冷静期 15 天）</button></li>
      <li><button type="button" class="danger" @click="nav('/login')">受限账号申诉（X15）</button></li>
    </ul>
  </div>
</template>

<style scoped>
.m-me { padding: 16px; display: grid; gap: 14px; }
.profile { background: linear-gradient(135deg, #fff1e8, #fff); border-radius: 20px; padding: 18px; display: flex; gap: 14px; align-items: center; }
.avatar { width: 54px; height: 54px; border-radius: 999px; background: #fff; display: grid; place-items: center; font-size: 26px; box-shadow: inset 0 0 0 1px #f0e6dc; }
.profile p { margin: 4px 0 0; color: #7a6e63; font-size: 13px; }
.login { margin-left: auto; border: none; background: #ff7a2f; color: #fff; border-radius: 999px; padding: 11px 20px; font-weight: 600; min-height: 44px; }
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.gallery button { position: relative; background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 18px; padding: 16px 8px; display: grid; gap: 6px; justify-items: center; font-size: 12px; color: #2b2118; min-height: 76px; cursor: pointer; }
.icon { font-size: 22px; }
.badge { position: absolute; top: 8px; right: 14px; background: #ef4444; color: #fff; border-radius: 999px; font-size: 10px; padding: 0 6px; }
.menu { list-style: none; margin: 0; padding: 0; display: grid; gap: 8px; }
.menu button { width: 100%; text-align: left; background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 14px; padding: 15px 16px; font-size: 14px; color: #2b2118; min-height: 48px; }
.menu .danger { color: #ef4444; }
</style>
