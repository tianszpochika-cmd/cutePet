<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { channelBoundaryNote } from '../../../web/src/domain/news';

interface Channel { name: string; slug: string }
interface ArticleCard { id: number; slug: string; title: string; authorId?: number; likes?: number; comments?: number; top?: boolean; createdAt?: string }

const route = useRoute();
const router = useRouter();
const channels = ref<Channel[]>([]);
const channelsLoading = ref(true);
const channelsError = ref('');
const items = ref<ArticleCard[]>([]);
const total = ref(0);
const page = ref(1);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref('');
let latestFeed = 0;
let active = true;

const selected = computed(() => typeof route.query.ch === 'string' ? route.query.ch : '');
const currentChannel = computed(() => channels.value.find((item) => item.name === selected.value));
const validChannel = computed(() => !selected.value || Boolean(currentChannel.value));
const hasMore = computed(() => items.value.length < total.value);

async function loadChannels() {
  channelsLoading.value = true;
  channelsError.value = '';
  try {
    const response = await api.channelsList();
    if (!active) return;
    if (!Array.isArray(response)) throw new Error('频道数据格式不完整');
    channels.value = (response as Channel[]).filter((item) => typeof item?.name === 'string' && item.name.trim());
  } catch (cause) {
    if (active) channelsError.value = cause instanceof Error ? cause.message : '频道暂时无法读取';
  } finally { if (active) channelsLoading.value = false; }
}

async function load(nextPage = 1) {
  const request = ++latestFeed;
  if (nextPage === 1) { loading.value = true; items.value = []; total.value = 0; }
  else loadingMore.value = true;
  error.value = '';
  try {
    const result = await api.articlesFeed({ query: { channel: selected.value || undefined, page: nextPage, size: 10 } }) as { items?: ArticleCard[]; total?: number };
    if (!active || request !== latestFeed) return;
    if (!Array.isArray(result?.items)) throw new Error('资讯数据格式不完整');
    const received = result.items.filter((item) => typeof item?.id === 'number' && typeof item.slug === 'string' && typeof item.title === 'string');
    items.value = nextPage === 1 ? received : [...items.value, ...received];
    total.value = typeof result.total === 'number' ? result.total : items.value.length;
    page.value = nextPage;
  } catch (cause) {
    if (active && request === latestFeed) error.value = cause instanceof Error ? cause.message : '资讯暂时无法读取';
  } finally {
    if (active && request === latestFeed) { loading.value = false; loadingMore.value = false; }
  }
}

function pick(name: string) {
  void router.replace({ path: '/news', query: name ? { ch: name } : {} });
}
function write() {
  void router.push(getAccessToken() ? '/write' : { path: '/login', query: { return: '/write', resume: 'write' } });
}
function dateLabel(value?: string): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString('zh-CN');
}

void loadChannels();
watch(selected, () => void load(), { immediate: true });
onBeforeUnmount(() => { active = false; ++latestFeed; });
</script>

<template>
  <div class="news-page">
    <header class="hero">
      <div class="topline"><span class="eyebrow">CARE JOURNAL · 养宠资讯</span><button type="button" class="write-link" @click="write">写经验 ↗</button></div>
      <h1>认真照顾，<br>也一起学习。</h1>
      <p>阅读已公开的真实内容。遇到健康问题，及时咨询专业兽医。</p>
    </header>

    <nav class="chips" aria-label="资讯频道">
      <button type="button" :class="{ on: !selected }" :aria-current="!selected ? 'page' : undefined" @click="pick('')">推荐</button>
      <button v-for="item in channels" :key="item.slug || item.name" type="button" :class="{ on: selected === item.name }" :aria-current="selected === item.name ? 'page' : undefined" @click="pick(item.name)">{{ item.name }}</button>
    </nav>
    <p v-if="channelsLoading" class="quiet" role="status">正在读取频道…</p>
    <p v-else-if="channelsError" class="inline-state" role="alert">频道暂不可用：{{ channelsError }} <button type="button" @click="loadChannels">重试</button></p>
    <p v-if="selected && !validChannel && !channelsLoading && !channelsError" class="inline-state">当前频道已不可用。<button type="button" @click="pick('')">看推荐</button></p>
    <p v-if="selected && channelBoundaryNote(selected)" class="hint">{{ channelBoundaryNote(selected) }}</p>

    <div class="section-title"><h2>{{ selected || '最新发布' }}</h2><span v-if="!loading && !error">{{ total }} 篇</span></div>
    <div v-if="loading" class="state" role="status"><span class="state-icon" aria-hidden="true">◌</span><strong>正在读取资讯</strong><p>很快就好</p></div>
    <div v-else-if="error && !items.length" class="state error" role="alert"><span class="state-icon" aria-hidden="true">!</span><strong>资讯暂时无法读取</strong><p>{{ error }}</p><button type="button" @click="load()">重新加载</button></div>
    <div v-else-if="!items.length" class="state"><span class="state-icon" aria-hidden="true">✳</span><strong>暂时没有公开文章</strong><p>可以换一个频道看看。</p><button v-if="selected" type="button" @click="pick('')">看看推荐</button></div>
    <template v-else>
      <ul class="feed">
        <li v-for="(item, index) in items" :key="item.id">
          <router-link :to="{ path: `/news/${encodeURIComponent(item.slug)}`, query: selected ? { ch: selected } : {} }" class="article-card" :data-testid="`article-${item.slug}`">
            <span class="article-mark">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="article-body"><span class="article-kicker">{{ item.top ? '精选文章' : '已发布文章' }}</span><strong>{{ item.title }}</strong><span class="meta"><span v-if="item.createdAt">{{ dateLabel(item.createdAt) }}</span><span v-if="typeof item.likes === 'number'">{{ item.likes }} 人点赞</span></span></span>
            <span class="arrow" aria-hidden="true">↗</span>
          </router-link>
        </li>
      </ul>
      <div v-if="error" class="inline-state" role="alert">{{ error }} <button type="button" @click="load(page + 1)">重试</button></div>
      <button v-if="hasMore && !error" type="button" class="more" :disabled="loadingMore" @click="load(page + 1)">{{ loadingMore ? '读取中…' : '阅读更多' }}</button>
    </template>
  </div>
</template>

<style scoped>
.news-page{padding:16px 16px 28px;min-width:0}.hero{padding:21px 20px 23px;border-radius:24px;background:linear-gradient(135deg,#f6e5d5,#fff1e4 62%,#e4eee1);overflow:hidden}.topline{display:flex;justify-content:space-between;align-items:center;gap:8px}.eyebrow{font-size:10px;letter-spacing:.12em;color:#914514;font-weight:800}.write-link{border:1px solid #d4a784;background:#fffaf4;color:#96420b;border-radius:999px;padding:0 12px;font-size:12px;font-weight:750;white-space:nowrap}.hero h1{font-size:clamp(27px,8vw,36px);letter-spacing:-.04em;line-height:1.24;margin:22px 0 8px}.hero p{margin:0;max-width:31ch;color:#645649;font-size:13px;line-height:1.7}.chips{display:flex;gap:8px;overflow-x:auto;margin:20px -16px 0;padding:0 16px 8px;scrollbar-width:none}.chips::-webkit-scrollbar{display:none}.chips button{flex:none;border:1px solid #e4d5c7;background:#fff;color:#62564d;border-radius:999px;padding:0 16px;min-height:44px;font-size:13px;font-weight:700}.chips button.on{background:#b85111;border-color:#b85111;color:#fff}.section-title{display:flex;justify-content:space-between;align-items:baseline;margin:20px 1px 12px}.section-title h2{margin:0;font-size:19px}.section-title span,.quiet{color:#77695f;font-size:12px}.hint,.inline-state{margin:10px 0 0;border-radius:13px;padding:11px 13px;background:#fff1df;color:#75431c;font-size:12px;line-height:1.6}.inline-state button{border:0;background:none;color:#914514;text-decoration:underline;font-weight:800;padding:0 5px}.feed{list-style:none;padding:0;margin:0;display:grid;gap:10px}.article-card{display:flex;align-items:flex-start;gap:13px;min-width:0;padding:17px 16px;min-height:112px;border:1px solid #eadfd4;border-radius:18px;background:#fff;text-decoration:none;color:#2b2118}.article-mark{color:#b85111;font-size:12px;font-weight:900;margin-top:2px}.article-body{display:grid;gap:8px;min-width:0;flex:1}.article-kicker{color:#9e4a14;font-size:11px;font-weight:800}.article-body strong{font-size:16px;line-height:1.5;overflow-wrap:anywhere}.meta{display:flex;gap:11px;flex-wrap:wrap;color:#786b60;font-size:11px}.arrow{color:#a64e16;font-size:18px}.state{display:grid;justify-items:center;text-align:center;gap:8px;padding:32px 15px;border:1px dashed #dacbbc;border-radius:20px;background:#fff;color:#63594e}.state strong{color:#2b2118}.state p{margin:0;line-height:1.6;font-size:13px}.state-icon{display:grid;place-items:center;width:48px;height:48px;border-radius:16px;background:#fff0df;color:#b85111;font-size:25px}.state button,.more{border:1px solid #b85111;background:#b85111;color:#fff;border-radius:999px;padding:0 20px;font-weight:750;margin-top:8px;min-height:44px}.state.error{border-color:#e4b8af;background:#fff7f4}.more{display:block;margin:18px auto 0;background:#fff;color:#a54810;min-width:148px}button:disabled{opacity:.6}
</style>
