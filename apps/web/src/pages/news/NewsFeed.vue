<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { CHANNELS, channelParam, channelBoundaryNote, type ArticleCard } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const channel = computed(() => {
  const value = typeof route.query.ch === 'string' ? route.query.ch : '推荐';
  return (CHANNELS as readonly string[]).includes(value) ? value : '推荐';
});
const items = ref<ArticleCard[]>([]);
const page = ref(1);
const total = ref(0);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref('');
let requestId = 0;
const hasMore = computed(() => items.value.length < total.value);
const boundary = computed(() => channelBoundaryNote(channel.value));

async function load(nextPage = 1) {
  const id = ++requestId;
  if (nextPage === 1) { loading.value = true; items.value = []; }
  else loadingMore.value = true;
  error.value = '';
  try {
    const result = (await api.articlesFeed({ query: { channel: channelParam(channel.value), page: nextPage, size: 12 } })) as { items?: ArticleCard[]; total?: number };
    if (id !== requestId) return;
    const newItems = Array.isArray(result?.items) ? result.items.filter((item) => typeof item.slug === 'string' && item.slug.length > 0) : [];
    items.value = nextPage === 1 ? newItems : [...items.value, ...newItems];
    page.value = nextPage;
    total.value = typeof result?.total === 'number' ? result.total : items.value.length;
  } catch (cause) {
    if (id === requestId) error.value = cause instanceof Error ? cause.message : '资讯暂时无法读取';
  } finally {
    if (id === requestId) { loading.value = false; loadingMore.value = false; }
  }
}
watch(channel, () => { void load(); }, { immediate: true });
function pick(value: string) { void router.replace({ path: '/news', query: value === '推荐' ? {} : { ch: value } }); }
function write() {
  const target = '/write';
  void router.push(getAccessToken() ? target : { path: '/login', query: { return: target, resume: 'write' } });
}
</script>

<template>
  <div class="feed">
    <section class="hero">
      <div><p class="eyebrow">CARE JOURNAL</p><h1>读懂它的每一天。</h1><p>从日常照护到健康科普，按需要慢慢找答案。这里展示已经公开的内容。</p><div class="hero-actions"><router-link to="/search">搜索想了解的话题 <span aria-hidden="true">↗</span></router-link><button type="button" @click="write">分享照护经验</button></div></div>
      <div class="hero-art" aria-hidden="true"><span>✦</span><i>01</i><strong>一篇有用的内容<br>从一个好问题开始。</strong></div>
    </section>
    <div class="section-head"><div><p class="eyebrow">EXPLORE BY TOPIC</p><h2>按主题阅读</h2></div><router-link to="/search">搜索全部资讯 →</router-link></div>
    <nav class="chips" aria-label="资讯频道"><button v-for="value in CHANNELS" :key="value" type="button" :class="{ on: channel === value }" :aria-current="channel === value ? 'page' : undefined" :data-testid="`ch-${value}`" @click="pick(value)">{{ value }}</button></nav>
    <p v-if="boundary" class="boundary">{{ boundary }}</p>
    <section v-if="loading" class="state" role="status">正在读取{{ channel }}资讯…</section>
    <section v-else-if="error && !items.length" class="state error" role="alert"><h3>资讯暂时无法读取</h3><p>{{ error }}</p><button type="button" @click="load()">重试</button></section>
    <section v-else-if="!items.length" class="state" data-testid="empty"><h3>{{ channel === '推荐' ? '暂时没有公开内容' : `${channel}频道还没有公开内容` }}</h3><p>可以换一个主题，或搜索具体问题。</p><router-link to="/search">去搜索 →</router-link></section>
    <template v-else>
      <ul class="cards"><li v-for="(item, index) in items" :key="item.id"><router-link class="card" :to="`/news/${item.slug}`"><span class="card-top"><span class="number">{{ String(index + 1).padStart(2, '0') }}</span><span v-if="item.top" class="pin">精选</span></span><strong>{{ item.title }}</strong><span class="meta">作者 #{{ item.authorId }} · {{ item.likes }} 人点赞 · {{ item.comments }} 条评论</span><span class="arrow" aria-hidden="true">↗</span></router-link></li></ul>
      <p v-if="error" class="load-error" role="alert">{{ error }} <button type="button" @click="load(page + 1)">重试</button></p>
      <button v-if="hasMore && !error" type="button" class="more" :disabled="loadingMore" @click="load(page + 1)">{{ loadingMore ? '正在加载…' : '阅读更多' }}</button>
    </template>
  </div>
</template>

<style scoped>
.feed{width:min(1320px,calc(100% - 64px));margin:0 auto;padding:34px 0 90px}.hero{min-height:350px;display:grid;grid-template-columns:1.1fr .9fr;gap:22px;margin-bottom:65px}.hero>div:first-child{display:flex;flex-direction:column;align-items:flex-start;justify-content:center;padding:clamp(28px,5vw,62px);border-radius:28px;background:#f7e8dc}.eyebrow{margin:0 0 12px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.15em}.hero h1{margin:0;font-size:clamp(36px,4vw,57px)}.hero p:not(.eyebrow){max-width:530px;color:var(--ink-2);line-height:1.8}.hero-actions{display:flex;gap:10px;flex-wrap:wrap;margin-top:16px}.hero-actions a,.hero-actions button{display:inline-flex;align-items:center;justify-content:center;gap:14px;min-height:46px;padding:0 21px;border-radius:999px;text-decoration:none;font-size:14px;font-weight:750}.hero-actions a{background:var(--primary);color:#fff}.hero-actions button{border:1px solid #d8bba7;background:#fff7f0;color:var(--primary)}.hero-art{position:relative;display:flex;flex-direction:column;justify-content:flex-end;overflow:hidden;padding:38px;border-radius:28px;background:radial-gradient(circle at 65% 30%,#fff2d4 0 20%,transparent 21%),linear-gradient(145deg,#e6ede0,#cbdcbf)}.hero-art span{position:absolute;top:13%;left:12%;font-size:150px;line-height:1;color:#fff9e8;opacity:.8}.hero-art i{font-size:12px;font-style:normal;font-weight:800;color:#416c4f}.hero-art strong{margin-top:12px;font-size:clamp(23px,2.5vw,37px);line-height:1.35}.section-head{display:flex;justify-content:space-between;align-items:end;gap:20px}.section-head h2{margin:0;font-size:clamp(27px,3vw,39px)}.section-head a{color:var(--primary);text-decoration:none;font-weight:700}.chips{display:flex;gap:9px;overflow-x:auto;margin:24px 0 17px;padding-bottom:6px}.chips button{min-height:40px;padding:0 18px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--ink-2);white-space:nowrap;font-weight:700}.chips button.on{border-color:var(--primary);background:var(--primary);color:#fff}.boundary{padding:12px 16px;border-radius:12px;background:#fff1e8;color:#8e3b0c;font-size:13px}.cards{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;list-style:none;margin:21px 0 0;padding:0}.card{min-height:230px;display:flex;flex-direction:column;gap:16px;position:relative;padding:24px;border:1px solid var(--line);border-radius:20px;background:#fff;color:var(--ink);text-decoration:none;transition:transform .2s var(--e-out)}.card:hover{transform:translateY(-3px)}.card-top{display:flex;align-items:center;justify-content:space-between}.number{color:var(--primary);font-size:13px;font-weight:800}.pin{padding:3px 10px;border-radius:999px;background:var(--primary-soft);color:var(--primary);font-size:12px;font-weight:750}.card strong{max-width:24ch;font-size:20px;line-height:1.45}.meta{margin-top:auto;color:var(--ink-2);font-size:12px}.arrow{position:absolute;right:22px;bottom:22px;color:var(--primary);font-size:18px}.state{padding:34px;border:1px solid var(--line);border-radius:20px;background:#fff;color:var(--ink-2)}.state h3{margin:0 0 8px;color:var(--ink);font-size:20px}.state p{margin:0 0 12px}.state a{font-weight:750;text-decoration:none}.state button,.load-error button{border:0;background:none;color:var(--primary);font-weight:750}.state.error,.load-error{color:#a63322}.load-error{margin-top:18px;font-size:13px}.more{display:block;min-height:44px;margin:28px auto 0;padding:0 30px;border:1px solid var(--primary);border-radius:999px;background:#fff;color:var(--primary);font-weight:750}@media(max-width:900px){.hero{grid-template-columns:1fr}.hero-art{min-height:180px}.cards{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:650px){.feed{width:calc(100% - 32px);padding-top:18px}.hero{margin-bottom:45px}.hero-art{display:none}.section-head{align-items:flex-start;flex-direction:column}.cards{grid-template-columns:1fr}.card{min-height:180px}}
</style>
