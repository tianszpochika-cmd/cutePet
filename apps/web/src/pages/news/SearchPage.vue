<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { highlight, type ArticleCard } from '../../domain/news';

interface SearchHit { title: string; score: number; slug?: string; id?: number }
const route = useRoute();
const router = useRouter();
const query = ref('');
const searchedFor = ref('');
const hits = ref<SearchHit[]>([]);
const recommend = ref<ArticleCard[]>([]);
const loading = ref(false);
const searched = ref(false);
const error = ref('');
const inputError = ref('');
let requestId = 0;

async function search(q: string) {
  const id = ++requestId;
  query.value = q;
  searchedFor.value = q;
  searched.value = true;
  loading.value = true;
  error.value = '';
  hits.value = [];
  recommend.value = [];
  try {
    const result = (await api.searchGet({ query: { q, limit: 20 } })) as { items?: SearchHit[]; recommend?: ArticleCard[] };
    if (id !== requestId) return;
    hits.value = Array.isArray(result?.items) ? result.items : [];
    recommend.value = Array.isArray(result?.recommend) ? result.recommend.filter((item) => !!item.slug) : [];
  } catch (cause) {
    if (id === requestId) error.value = cause instanceof Error ? cause.message : '搜索暂时不可用';
  } finally {
    if (id === requestId) loading.value = false;
  }
}

watch(() => route.query.q, (value) => {
  const q = typeof value === 'string' ? value.trim() : '';
  query.value = q;
  inputError.value = '';
  if (!q) { requestId++; searched.value = false; searchedFor.value = ''; hits.value = []; recommend.value = []; error.value = ''; loading.value = false; return; }
  if (q.length > 50) {
    requestId++;
    searched.value = false;
    hits.value = [];
    recommend.value = [];
    loading.value = false;
    error.value = '';
    inputError.value = '搜索词最多 50 字';
    return;
  }
  void search(q);
}, { immediate: true });

function submitSearch() {
  const q = query.value.trim();
  if (!q || q.length > 50) { inputError.value = !q ? '请输入想搜索的内容' : '搜索词最多 50 字'; return; }
  inputError.value = '';
  if (route.query.q === q) void search(q);
  else void router.replace({ path: '/search', query: { q } });
}
</script>

<template>
  <div class="search-page">
    <nav class="breadcrumb"><router-link to="/news">资讯</router-link><span>／</span><span>搜索</span></nav>
    <header><p class="eyebrow">FIND WHAT MATTERS</p><h1>找到想了解的照护知识。</h1><p>试着输入一个问题、主题或文章标题。</p></header>
    <form class="search-box" @submit.prevent="submitSearch"><label class="sr-only" for="search-input">搜索资讯</label><input id="search-input" v-model="query" data-testid="search-input" maxlength="50" placeholder="例如：换粮、幼猫、日常训练" /><button type="submit" data-testid="search-btn">搜索</button></form>
    <p v-if="inputError" class="error" role="alert">{{ inputError }}</p>
    <section v-if="loading" class="state" role="status">正在搜索「{{ searchedFor }}」…</section>
    <section v-else-if="error" class="state"><h2>搜索暂时无法完成</h2><p role="alert">{{ error }}</p><button type="button" @click="search(searchedFor)">重试</button></section>
    <section v-else-if="!searched" class="state"><h2>从一个问题开始</h2><p>公开文章可以直接阅读，搜索结果只包含仍可见的内容。</p><router-link to="/news">先浏览资讯 →</router-link></section>
    <template v-else>
      <div class="result-head"><h2>{{ hits.length ? '相关内容' : '暂时没有匹配的文章' }}</h2><span>搜索词：{{ searchedFor }}</span></div>
      <p v-if="!hits.length" class="suggestion" data-testid="empty">试试更短的词，或换一个频道浏览。</p>
      <ul v-else class="results"><li v-for="(item, index) in hits" :key="`${item.title}-${index}`"><router-link v-if="item.slug" class="result" :to="`/news/${item.slug}`"><span class="result-label">文章</span><strong><template v-for="(segment, i) in highlight(item.title, searchedFor)" :key="i"><mark v-if="segment.hit">{{ segment.text }}</mark><template v-else>{{ segment.text }}</template></template></strong><span class="arrow">阅读 →</span></router-link><div v-else class="result"><span class="result-label">文章</span><strong><template v-for="(segment, i) in highlight(item.title, searchedFor)" :key="i"><mark v-if="segment.hit">{{ segment.text }}</mark><template v-else>{{ segment.text }}</template></template></strong><span class="unavailable">此结果暂无法打开</span></div></li></ul>
      <section v-if="recommend.length" class="recommend"><h2>也可以看看这些文章</h2><div class="recommend-grid"><router-link v-for="item in recommend" :key="item.id" :to="`/news/${item.slug}`"><strong>{{ item.title }}</strong><span>阅读文章 →</span></router-link></div></section>
    </template>
  </div>
</template>

<style scoped>
.search-page{width:min(100% - 32px,880px);margin:0 auto;padding:27px 0 90px}.breadcrumb{display:flex;gap:8px;color:var(--ink-2);font-size:13px}.breadcrumb a{color:var(--primary);text-decoration:none}.eyebrow{margin:0 0 12px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.15em}header{padding:60px 0 26px}header h1{max-width:15ch;margin:0;font-size:clamp(37px,5vw,58px)}header p:not(.eyebrow){margin:18px 0 0;color:var(--ink-2)}.search-box{display:flex;gap:8px;padding:7px;border:1px solid var(--line);border-radius:17px;background:#fff;box-shadow:0 12px 40px rgba(115,74,46,.06)}.search-box input{flex:1;min-width:0;min-height:45px;padding:0 14px;border:0;outline:0;font-size:16px}.search-box button{min-width:110px;border:0;border-radius:12px;background:var(--primary);color:#fff;font-weight:750}.state{margin-top:31px;padding:27px;border:1px solid var(--line);border-radius:20px;background:#fff;color:var(--ink-2)}.state h2{margin:0 0 9px;color:var(--ink);font-size:20px}.state p{margin:0 0 9px}.state a{font-weight:750;text-decoration:none}.state button{border:0;background:none;color:var(--primary);font-weight:700}.error{color:#a63322;font-size:13px}.result-head{display:flex;justify-content:space-between;align-items:end;gap:10px;margin:39px 0 16px}.result-head h2,.recommend h2{margin:0;font-size:25px}.result-head span{color:var(--ink-2);font-size:13px}.suggestion{padding:24px;border:1px solid var(--line);border-radius:18px;background:#fff;color:var(--ink-2)}.results{display:grid;gap:11px;list-style:none;margin:0;padding:0}.result{display:flex;align-items:center;gap:16px;min-height:85px;padding:18px 22px;border:1px solid var(--line);border-radius:17px;background:#fff;color:var(--ink);text-decoration:none}.result-label{flex:none;color:var(--primary);font-size:12px;font-weight:800}.result strong{flex:1;font-size:17px}.arrow{color:var(--primary);font-size:13px;font-weight:750}.unavailable{color:var(--ink-2);font-size:12px}mark{padding:0 2px;border-radius:3px;background:#ffe0b9;color:inherit}.recommend{margin-top:44px}.recommend-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;margin-top:16px}.recommend-grid a{display:flex;flex-direction:column;justify-content:space-between;min-height:126px;padding:20px;border:1px solid var(--line);border-radius:17px;background:#fff;color:var(--ink);text-decoration:none}.recommend-grid span{color:var(--primary);font-size:13px;font-weight:750}.sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}@media(max-width:650px){header{padding-top:40px}.result-head{align-items:start;flex-direction:column}.result{align-items:flex-start;flex-wrap:wrap}.result strong{flex-basis:80%}.recommend-grid{grid-template-columns:1fr}.search-box button{min-width:75px}}
</style>
