<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { homeSections, reminderBarModel, unfinishedWizardHint } from '../../domain/home';

const router = useRouter();
const loggedIn = ref(false); // 本地会话态；真实接线随本地阶段（dev 登录后置 true）
const petCount = ref(0);
const hasUnfinishedWizard = ref(false);
const pendingReminders = ref<{ petName: string; type: string; due: string }[]>([]);

const sections = computed(() =>
  homeSections({
    loggedIn: loggedIn.value,
    petCount: petCount.value,
    hasUnfinishedWizard: hasUnfinishedWizard.value,
    pendingReminders: pendingReminders.value.length,
  }),
);
const visible = computed(() => sections.value.filter((s) => s.visible).map((s) => s.id));
const reminderBar = computed(() => reminderBarModel(pendingReminders.value));
const wizardHint = computed(() =>
  unfinishedWizardHint({
    loggedIn: loggedIn.value,
    petCount: petCount.value,
    hasUnfinishedWizard: hasUnfinishedWizard.value,
    pendingReminders: pendingReminders.value.length,
  }),
);

function goLogin() {
  void router.push('/login?return=%2F');
}
function goPets() {
  void router.push(petCount.value > 0 ? '/pets' : '/pets/new');
}
</script>

<template>
  <div class="home">
    <!-- 提醒条（L2 置顶） -->
    <div v-if="visible.includes('reminderBar') && reminderBar" class="reminder" data-testid="reminder-bar">
      ⏰ {{ reminderBar.text }}<span class="badge">{{ reminderBar.count }}</span>
    </div>

    <!-- 登录引导（L1 闸门） -->
    <section v-if="visible.includes('loginGuide')" class="hero" data-testid="login-guide">
      <h1>让每一天，都更懂你的宠物</h1>
      <p>管理 · 资讯 · 探索 · 用品，一站式宠物生活服务。</p>
      <button type="button" @click="goLogin">开始记录</button>
    </section>

    <!-- 我的宠物 -->
    <section v-if="visible.includes('myPets')" class="pets" data-testid="my-pets">
      <h2>我的宠物</h2>
      <button type="button" @click="goPets">管理宠物档案</button>
      <p v-if="wizardHint" class="hint">{{ wizardHint }}</p>
    </section>

    <!-- 快速记录（L2 一步可达） -->
    <section v-if="visible.includes('quickRecord')" class="quick" data-testid="quick-record">
      <button type="button" @click="router.push('/pets')">＋ 记录</button>
    </section>

    <!-- 用品频道（4-Tab 决议：用品并入首页） -->
    <section v-if="visible.includes('goods')" class="block" data-testid="goods">
      <h2>逛好物</h2>
      <p class="muted">分类 · 评测 · 我的收藏（T7.7 落地）</p>
    </section>

    <section v-if="visible.includes('nearbyExplore')" class="block" data-testid="nearby">
      <h2>附近探索</h2>
      <p class="muted">宠物友好场所（T7.6 落地）</p>
    </section>

    <section v-if="visible.includes('feed')" class="block" data-testid="feed">
      <h2>精选资讯</h2>
      <p class="muted">七频道内容流（T7.4 落地）</p>
    </section>
  </div>
</template>

<style scoped>
.home {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px 64px;
  display: grid;
  gap: 24px;
}
.reminder {
  background: #fff1e8;
  color: #ff7a2f;
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
}
.badge {
  background: #ff7a2f;
  color: #fff;
  border-radius: 999px;
  padding: 0 8px;
  font-size: 12px;
}
.hero {
  text-align: center;
  padding: 48px 16px;
  background: linear-gradient(180deg, #fff1e8, #fff9f3);
  border-radius: 24px;
}
.hero button,
.pets button,
.quick button {
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  height: 48px;
  padding: 0 24px;
  font-weight: 600;
  cursor: pointer;
}
.block,
.pets,
.quick {
  background: #fff;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  padding: 16px;
}
.muted {
  color: #7a6e63;
  font-size: 14px;
}
.hint {
  color: #ff7a2f;
  font-size: 13px;
}
</style>
