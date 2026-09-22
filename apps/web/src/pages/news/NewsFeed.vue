<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { CHANNELS, channelParam, channelBoundaryNote, type ArticleCard } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const channel = ref(String(route.query.ch ?? '推荐'));
const items = ref<ArticleCard[]>([]);
const loading = ref(true);
const error = ref('');
const boundary = ref(channelBoundaryNote(channel.value));

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const res = (await api.articlesFeed({
      query: { channel: channelParam(channel.value), page: 1, size: 12 },
    })) as unknown as { items: ArticleCard[] };
    items.value = res.items ?? [];
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败（dev 需启动 content-service）';
  } finally {
    loading.value = false;
  }
}

function pick(ch: string) {
  channel.value = ch;
  void router.replace({ path: '/news', query: ch === '推荐' ? {} : { ch } });
}

watch(channel, () => {
  boundary.value = channelBoundaryNote(channel.value);
});
onMounted(load);
watch(() => route.query.ch, load);
</script>

<template>
  <div class="feed">
    <header>
      <h1>宠物资讯</h1>
      <router-link class="search" to="/search">🔍 搜索</router-link>
    </header>

    <nav class="chips">
      <button
        v-for="c in CHANNELS"
        :key="c"
        type="button"
        :class="{ on: channel === c }"
        :data-testid="`ch-${c}`"
        @click="pick(c)"
      >
        {{ c }}
      </button>
    </nav>

    <p v-if="boundary" class="boundary">{{ boundary }}</p>
    <p v-if="error" class="err">{{ error }}</p>
    <p v-else-if="loading" class="muted">加载中…</p>
    <p v-else-if="items.length === 0" class="muted" data-testid="empty">该频道暂无内容</p>

    <ul v-else class="cards">
      <li v-for="a in items" :key="a.id">
        <button type="button" class="card" @click="router.push(`/news/${a.slug}`)">
          <span v-if="a.top" class="pin">置顶</span>
          <strong>{{ a.title }}</strong>
          <span class="meta">👍 {{ a.likes }} · 💬 {{ a.comments }}</span>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.feed {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search {
  color: #ff7a2f;
  text-decoration: none;
  font-size: 14px;
}
.chips {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 12px 0;
}
.chips button {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: #f7f1ea;
  color: #7a6e63;
  white-space: nowrap;
  cursor: pointer;
}
.chips button.on {
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.boundary {
  background: #fff1e8;
  color: #ff7a2f;
  font-size: 12px;
  border-radius: 8px;
  padding: 8px 12px;
}
.cards {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 12px;
}
.card {
  width: 100%;
  text-align: left;
  background: #fff;
  border: none;
  box-shadow: inset 0 0 0 1px #f0e6dc;
  border-radius: 16px;
  padding: 16px;
  display: grid;
  gap: 6px;
  cursor: pointer;
}
.pin {
  color: #ff7a2f;
  font-size: 12px;
  font-weight: 600;
}
.meta {
  color: #7a6e63;
  font-size: 13px;
}
.muted {
  color: #7a6e63;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
