<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { shareCardModel, shareLoginHref, type SharePayload } from '../domain/site';

const route = useRoute();
const router = useRouter();

// /share/:kind/:id —— dev 演示态；真实元数据随接口（先审后发同源）
const kindParam = String(route.params.kind ?? 'article');
const id = String(route.params.id ?? '');
const state = ref<'PUBLISHED' | 'DRAFT' | 'REJECTED' | 'TAKEDOWN' | 'DELETED'>(
  (String(route.query.state ?? 'PUBLISHED')) as never,
);

const payload = computed<SharePayload>(() => ({
  kind: kindParam === 'summary' ? 'summary' : kindParam === 'route' ? 'route' : kindParam === 'event' ? 'event' : 'article',
  idOrSlug: id,
  title: id.includes('food') || id === 'puppy-food' ? '幼猫换粮的七个误区' : `内容 ${id}`,
  channelLabel: '猫',
  authorLabel: '编辑部',
  state: state.value,
}));

const card = computed(() => shareCardModel(payload.value));
const loginHref = () => shareLoginHref(card.value.loginReturn);

function demoSet(next: typeof state.value) {
  void router.replace({ query: { state: next } });
  state.value = next;
}
</script>

<template>
  <div class="share">
    <section class="card" data-testid="share-card">
      <div class="head">
        <span class="chip">{{ card.chip }}</span>
        <span v-if="!card.visible" class="unavail-tag">不可用</span>
      </div>
      <h1 :class="{ muted: !card.visible }">{{ card.title }}</h1>
      <p class="meta">{{ card.author }} · cutePet 内容中心</p>
      <p class="source" data-testid="source">来源：编辑部整理 · 发布于 2026-09-22（公开分享与来源标注强制展示 —— T9.6/U92）</p>

      <div v-if="card.visible" class="body">
        <p>这是分享预览页：卡片结构与用户端文章卡同构（标题 / 频道 chip / 作者 / 操作区）。</p>
        <div class="actions">
          <a class="primary" :href="loginHref()" data-testid="login-cta">登录后查看完整内容</a>
          <router-link class="ghost" :to="`/center`">返回内容中心</router-link>
        </div>
      </div>

      <!-- T9.6 不可用四态 -->
      <div v-else class="unavailable" data-testid="unavailable">
        <p class="copy">{{ card.unavailable }}</p>
        <div class="actions">
          <router-link class="ghost" to="/center">看看其他内容</router-link>
          <router-link v-if="state === 'TAKEDOWN'" class="ghost" to="/contact">申诉/举报入口</router-link>
        </div>
      </div>
    </section>

    <section class="demo">
      <span>演示态切换（真实内容由先审后发状态决定）：</span>
      <button
        v-for="s in ['PUBLISHED', 'DRAFT', 'REJECTED', 'TAKEDOWN', 'DELETED'] as const"
        :key="s"
        type="button"
        :class="{ on: state === s }"
        :data-testid="`state-${s}`"
        @click="demoSet(s)"
      >
        {{ s }}
      </button>
    </section>
  </div>
</template>

<style scoped>
.share { max-width: 720px; margin: 40px auto; padding: 0 24px; display: grid; gap: 18px; }
.card { background: #fff; border-radius: 28px; box-shadow: 0 16px 40px rgba(43,33,24,.08); padding: 36px 32px; display: grid; gap: 14px; }
.head { display: flex; gap: 10px; align-items: center; }
.chip { background: #fff1e8; color: #ff7a2f; border-radius: 999px; padding: 4px 14px; font-size: 13px; font-weight: 600; }
.unavail-tag { background: #f0e6dc; color: #7a6e63; border-radius: 999px; padding: 4px 12px; font-size: 12px; }
h1 { margin: 0; font-size: 28px; line-height: 1.4; }
h1.muted { color: #a89b8f; text-decoration: line-through; text-decoration-color: #e5ddd3; }
.meta { margin: 0; color: #7a6e63; font-size: 14px; }
.source { margin: 0; color: #a89b8f; font-size: 12px; padding-top: 8px; border-top: 1px dashed #f0e6dc; }
.body p { color: #7a6e63; font-size: 15px; line-height: 1.8; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; }
.primary { background: #ff7a2f; color: #fff; border-radius: 999px; padding: 13px 26px; text-decoration: none; font-weight: 600; }
.ghost { background: #fff; color: #7a6e63; border-radius: 999px; padding: 13px 26px; text-decoration: none; box-shadow: inset 0 0 0 1px #f0e6dc; }
.unavailable { background: #faf7f3; border-radius: 16px; padding: 20px; display: grid; gap: 14px; }
.copy { margin: 0; color: #7a6e63; font-size: 15px; line-height: 1.8; }
.demo { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; color: #7a6e63; font-size: 13px; }
.demo button { height: 30px; padding: 0 12px; border: none; border-radius: 999px; background: #fff; color: #7a6e63; box-shadow: inset 0 0 0 1px #f0e6dc; font-size: 12px; cursor: pointer; }
.demo button.on { background: #ff7a2f; color: #fff; font-weight: 600; box-shadow: none; }
</style>
