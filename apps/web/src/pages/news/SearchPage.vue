<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { highlight, emptySearchCopy, type ArticleCard } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const query = ref(String(route.query.q ?? ''));
const items = ref<ArticleCard[]>([]);
const empty = ref(false);
const searched = ref(false);
const loading = ref(false);

async function search() {
  const q = query.value.trim();
  if (q.length === 0 || q.length > 50) return;
  loading.value = true;
  searched.value = true;
  void router.replace({ path: '/search', query: { q } });
  try {
    const res = (await api.searchGet({ query: { q, limit: 20 } })) as unknown as {
      items: { title: string; score: number }[];
      empty: boolean;
      recommend?: ArticleCard[];
    };
    items.value = (res.items ?? []).map((it, idx) => ({
      id: idx,
      slug: '',
      title: it.title,
      channel: '',
      authorId: 0,
      likes: 0,
      comments: 0,
    }));
    if (res.empty && res.recommend) items.value = res.recommend;
    empty.value = Boolean(res.empty && (!res.recommend || res.recommend.length === 0));
  } catch (e) {
    items.value = [];
    empty.value = true;
    void e;
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.query.q,
  (v) => {
    if (typeof v === 'string' && v) {
      query.value = v;
      void search();
    }
  },
);
</script>

<template>
  <div class="search">
    <h1>站内搜索</h1>
    <div class="box">
      <input
        v-model="query"
        data-testid="search-input"
        placeholder="搜索文章 / 作者 / 商品（1–50 字）"
        @keyup.enter="search"
      />
      <button type="button" data-testid="search-btn" @click="search">搜索</button>
    </div>

    <p v-if="loading" class="muted">搜索中…</p>
    <p v-else-if="searched && empty" class="muted" data-testid="empty">{{ emptySearchCopy(query) }}</p>

    <ul v-else class="results">
      <li v-for="it in items" :key="it.id">
        <template v-if="it.slug">
          <button type="button" class="item" @click="router.push(`/news/${it.slug}`)">
            <template v-for="(seg, i) in highlight(it.title, query)" :key="i">
              <mark v-if="seg.hit">{{ seg.text }}</mark>
              <template v-else>{{ seg.text }}</template>
            </template>
          </button>
        </template>
        <span v-else class="item plain">
          <template v-for="(seg, i) in highlight(it.title, query)" :key="i">
            <mark v-if="seg.hit">{{ seg.text }}</mark>
            <template v-else>{{ seg.text }}</template>
          </template>
        </span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.search {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px;
}
.box {
  display: flex;
  gap: 8px;
}
.box input {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  border: 1px solid #f0e6dc;
  padding: 0 16px;
  font-size: 15px;
}
.box button {
  height: 44px;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.results {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
  margin-top: 16px;
}
.item {
  display: block;
  width: 100%;
  text-align: left;
  background: #fff;
  border: none;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  border-radius: 12px;
  padding: 14px 16px;
  font-size: 15px;
  cursor: pointer;
}
.item.plain {
  cursor: default;
}
mark {
  background: #ffe9a8;
  color: inherit;
  border-radius: 3px;
  padding: 0 2px;
}
.muted {
  color: #7a6e63;
  margin-top: 16px;
}
</style>
