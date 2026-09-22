<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ME_GALLERY, galleryGate, RULE_DOCS } from '../../domain/settings';
import { ME_GALLERY as GALLERY, galleryGate as gate } from '../../domain/family';

const router = useRouter();
const loggedIn = ref(false); // 会话态接线随本地阶段（#45 未登录统一回跳）

function nav(to: string) {
  void router.push(gate(loggedIn.value, to));
}
void GALLERY;
void gate;
void ME_GALLERY;
</script>

<template>
  <div class="me">
    <header v-if="loggedIn" class="profile">
      <div class="avatar">🐾</div>
      <div>
        <strong>养宠的你</strong>
        <p class="meta">已登录 · 资料编辑见设置</p>
      </div>
    </header>
    <header v-else class="login-guide">
      <p>登录后管理宠物档案、家庭、投稿与活动</p>
      <button type="button" class="primary" @click="nav('/login')">登录 / 注册</button>
    </header>

    <!-- #45 宫格六项（含活动中心） -->
    <nav class="gallery" data-testid="me-gallery">
      <button v-for="g in ME_GALLERY" :key="g.id" type="button" @click="nav(g.to)">
        <span class="icon">{{ { pets: '🐾', family: '👨‍👩‍👧', creation: '✍️', favorites: '⭐', messages: '🔔', activity: '🎪' }[g.id] }}</span>
        {{ g.label }}
      </button>
    </nav>

    <ul class="menu">
      <li><button type="button" @click="nav('/settings')">设置</button></li>
      <li><button type="button" @click="nav('/help')">帮助与客服</button></li>
      <li>
        <button type="button" @click="nav(`/legal/${RULE_DOCS[0].slug}`)">规则（协议/隐私/社区规范…）</button>
      </li>
      <li><button type="button" class="danger" @click="nav('/blocked')">受限账号状态查询（X15）</button></li>
    </ul>
  </div>
</template>

<style scoped>
.me { max-width: 640px; margin: 0 auto; padding: 24px 16px; display: grid; gap: 16px; }
.profile { display: flex; gap: 12px; align-items: center; }
.avatar { width: 56px; height: 56px; border-radius: 999px; background: #fff1e8; display: grid; place-items: center; font-size: 26px; }
.meta { color: #7a6e63; font-size: 13px; margin: 4px 0 0; }
.login-guide { background: linear-gradient(180deg, #fff1e8, #fff); border-radius: 20px; padding: 24px; text-align: center; display: grid; gap: 12px; }
.primary { height: 44px; border: none; border-radius: 999px; background: #ff7a2f; color: #fff; font-weight: 600; }
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.gallery button { background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 16px; padding: 16px 8px; display: grid; gap: 6px; justify-items: center; font-size: 13px; color: #2b2118; cursor: pointer; }
.icon { font-size: 22px; }
.menu { list-style: none; padding: 0; display: grid; gap: 8px; }
.menu button { width: 100%; text-align: left; height: 48px; background: #fff; border: none; box-shadow: inset 0 0 0 1px #f0e6dc; border-radius: 12px; padding: 0 16px; font-size: 14px; color: #2b2118; }
.menu .danger { color: #ef4444; }
.meta + .meta { margin-top: 0; }
</style>
