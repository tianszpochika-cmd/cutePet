<script setup lang="ts">
import { ref } from 'vue';
import { CENTER_NOTICE } from '../domain/site';

interface ArticleRow {
  slug: string;
  title: string;
  channel: string;
  author: string;
  updatedAt: string;
}

// 与用户端资讯同源（先审后发）——仅 PUBLISHED 内容（U94）
const items = ref<ArticleRow[]>([
  { slug: 'puppy-food', title: '幼猫换粮的七个误区', channel: '猫', author: '编辑部', updatedAt: '2026-09-22' },
  { slug: 'summer-water', title: '夏季饮水注意事项', channel: '营养', author: '编辑部', updatedAt: '2026-09-21' },
  { slug: 'walk-map', title: '城市遛宠路线指南', channel: '行业快讯', author: '运营组', updatedAt: '2026-09-20' },
]);
const q = ref('');
const filtered = () =>
  q.value.trim() ? items.value.filter((i) => i.title.includes(q.value.trim())) : items.value;
</script>

<template>
  <div class="center">
    <h1>内容中心</h1>
    <p class="notice">{{ CENTER_NOTICE }}</p>

    <div class="search">
      <input v-model="q" placeholder="搜索标题（1–50 字）" data-testid="center-search" />
      <span class="meta">{{ filtered().length }} 篇已发布</span>
    </div>

    <ul class="list">
      <li v-for="a in filtered()" :key="a.slug">
        <router-link :to="`/share/a/${a.slug}`" class="row" :data-testid="`row-${a.slug}`">
          <strong>{{ a.title }}</strong>
          <span class="meta">{{ a.channel }} · {{ a.author }} · {{ a.updatedAt }}</span>
        </router-link>
      </li>
      <li v-if="filtered().length === 0" class="empty">未找到匹配内容，试试其它关键词。</li>
    </ul>
  </div>
</template>

<style scoped>
.center { max-width: 860px; margin: 0 auto; padding: 40px 24px; display: grid; gap: 18px; }
h1 { font-size: 32px; margin: 0; }
.notice { background: #f5f3ff; color: #6d5bd0; border-radius: 12px; padding: 12px 16px; font-size: 13px; line-height: 1.7; margin: 0; }
.search { display: flex; gap: 12px; align-items: center; }
.search input { flex: 1; height: 46px; border: 1px solid #f0e6dc; border-radius: 12px; padding: 0 16px; font-size: 15px; background: #fff; }
.list { list-style: none; padding: 0; margin: 0; display: grid; gap: 10px; }
.row { display: grid; gap: 6px; background: #fff; border-radius: 16px; box-shadow: inset 0 0 0 1px #f0e6dc; padding: 18px 20px; text-decoration: none; color: #2b2118; }
.row:hover { box-shadow: inset 0 0 0 1px #ffb98a; }
.meta { color: #7a6e63; font-size: 13px; }
.empty { color: #7a6e63; font-size: 14px; text-align: center; padding: 32px 0; }
</style>
