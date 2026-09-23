<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { RULE_DOCS } from '../../domain/settings';

const route = useRoute();
const router = useRouter();
const doc = computed(() => RULE_DOCS.find((item) => item.slug === route.params.doc));
</script>

<template>
  <div class="legal">
    <button type="button" class="back" @click="router.back()">‹ 返回</button>
    <template v-if="doc">
      <p class="eyebrow">RULES & PRIVACY</p>
      <h1>{{ doc.label }}</h1>
      <div class="notice" role="status">
        <strong>正式文本尚未发布</strong>
        <p>这份文件仍在审核中。正式版本、发布日期和适用范围确认前，这里不会展示占位条款，也不会要求你把示例文本当作已生效规则。</p>
        <router-link to="/help">需要协助？查看帮助与客服 →</router-link>
      </div>
    </template>
    <template v-else>
      <h1>未找到此规则页面</h1>
      <router-link to="/settings">返回设置</router-link>
    </template>
    <nav class="rule-nav" aria-label="其他规则"><router-link v-for="item in RULE_DOCS" :key="item.slug" :to="`/legal/${item.slug}`">{{ item.label }}</router-link></nav>
  </div>
</template>

<style scoped>
.legal { width: min(760px, calc(100% - 32px)); min-height: 50vh; margin: 48px auto; }
.back { min-height: 44px; padding: 0; border: 0; background: transparent; color: #b85111; font-weight: 700; }
.eyebrow { margin: 36px 0 10px; color: #b85111; font-size: 12px; font-weight: 800; letter-spacing: .14em; }
h1 { margin: 0 0 24px; font-size: clamp(31px, 5vw, 45px); }
.notice { padding: clamp(20px, 4vw, 34px); border: 1px solid #eadfd4; border-radius: 24px; background: #fff; }
.notice strong { font-size: 18px; }.notice p { max-width: 600px; color: #64574d; line-height: 1.8; }.notice a { display: inline-flex; align-items: center; min-height: 44px; color: #b85111; font-weight: 700; text-decoration: none; }
.rule-nav { display: flex; flex-wrap: wrap; gap: 10px 18px; margin-top: 30px; }.rule-nav a { display: inline-flex; align-items: center; min-height: 44px; color: #64574d; text-decoration: none; }.rule-nav a.router-link-active { color: #b85111; font-weight: 700; }
</style>
