<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';

interface Article { id: number; slug: string; title: string; body: string; tags?: string | null; authorId?: number; likes?: number; commentCount?: number }
interface Comment { id: number; userId: number; content: string; machineState?: string; createdAt?: string }

const route = useRoute();
const router = useRouter();
const slug = computed(() => String(route.params.slug ?? ''));
const feedRoute = computed(() => ({ path: '/news', query: typeof route.query.ch === 'string' ? { ch: route.query.ch } : {} }));
function back() { void router.push(feedRoute.value); }
const article = ref<Article | null>(null);
const comments = ref<Comment[]>([]);
const loading = ref(true);
const error = ref('');
const commentsLoading = ref(false);
const commentsError = ref('');
let latest = 0;
let active = true;

const tagList = computed(() => article.value?.tags?.split(',').map((item) => item.trim()).filter(Boolean) ?? []);
function dateLabel(value?: string): string {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString('zh-CN');
}
async function loadComments(id: number, request: number) {
  commentsLoading.value = true;
  commentsError.value = '';
  try {
    const response = await api.commentsList({ params: { id } });
    if (!active || request !== latest) return;
    if (!Array.isArray(response)) throw new Error('评论数据格式不完整');
    comments.value = (response as Comment[]).filter((item) => typeof item?.id === 'number' && typeof item.content === 'string');
  } catch (cause) {
    if (active && request === latest) commentsError.value = cause instanceof Error ? cause.message : '评论暂时无法读取';
  } finally { if (active && request === latest) commentsLoading.value = false; }
}
async function load() {
  const request = ++latest;
  article.value = null;
  comments.value = [];
  loading.value = true;
  error.value = '';
  commentsError.value = '';
  try {
    const response = await api.articleGet({ params: { slug: slug.value } }) as Article;
    if (!active || request !== latest) return;
    if (typeof response?.id !== 'number' || typeof response.title !== 'string' || typeof response.body !== 'string') throw new Error('文章资料格式不完整');
    article.value = response;
    void loadComments(response.id, request);
  } catch (cause) {
    if (active && request === latest) error.value = cause instanceof Error ? cause.message : '文章不存在或已不可见';
  } finally { if (active && request === latest) loading.value = false; }
}
watch(slug, () => void load(), { immediate: true });
onBeforeUnmount(() => { active = false; ++latest; });
</script>

<template>
  <div class="article-page">
    <div class="topline"><button type="button" class="back" aria-label="返回资讯" @click="back">‹</button><span>文章详情</span><span class="spacer" /></div>
    <section v-if="loading" class="state" role="status"><span class="symbol">◌</span><h1>正在读取文章…</h1></section>
    <section v-else-if="error" class="state error" role="alert"><span class="symbol">!</span><h1>文章暂时无法阅读</h1><p>{{ error }}</p><button type="button" @click="load">重新加载</button><router-link :to="feedRoute">返回资讯</router-link></section>
    <template v-else-if="article">
      <header class="story-header"><span class="eyebrow">CARE JOURNAL · 已公开文章</span><h1>{{ article.title }}</h1><p class="meta"><span v-if="typeof article.authorId === 'number'">作者 #{{ article.authorId }}</span><span v-if="typeof article.likes === 'number'">{{ article.likes }} 人点赞</span></p><div v-if="tagList.length" class="tags"><span v-for="tag in tagList" :key="tag">{{ tag }}</span></div></header>
      <div class="story-body">{{ article.body }}</div>
      <p class="care-note">内容仅供交流与科普参考，不能代替专业兽医诊疗。出现明显异常时请及时就医。</p>
      <section class="comments" aria-labelledby="comments-title"><div class="section-head"><h2 id="comments-title">公开评论</h2><span v-if="typeof article.commentCount === 'number'">{{ article.commentCount }} 条</span></div><p v-if="commentsLoading" class="muted" role="status">正在读取评论…</p><p v-else-if="commentsError" class="inline-error" role="alert">{{ commentsError }} <button type="button" @click="loadComments(article.id, latest)">重试</button></p><p v-else-if="!comments.length" class="muted">目前没有公开评论。</p><ul v-else><li v-for="item in comments" :key="item.id"><div><strong>读者 #{{ item.userId }}</strong><time v-if="item.createdAt">{{ dateLabel(item.createdAt) }}</time></div><p>{{ item.content }}</p></li></ul></section>
      <router-link :to="feedRoute" class="all-news">继续阅读资讯 <span aria-hidden="true">↗</span></router-link>
    </template>
  </div>
</template>

<style scoped>
.article-page{padding:12px 16px 30px;min-width:0}.topline{display:grid;grid-template-columns:44px 1fr 44px;align-items:center;text-align:center;font-size:15px;font-weight:800}.back{width:44px;height:44px;border:1px solid #e8d9ca;border-radius:999px;background:#fff;color:#9f4d15;font-size:27px}.spacer{width:44px}.story-header{padding:28px 3px 24px;border-bottom:1px solid #eadfd4}.eyebrow{color:#a44f18;font-size:10px;letter-spacing:.14em;font-weight:850}.story-header h1{font-size:clamp(28px,8.6vw,38px);letter-spacing:-.045em;line-height:1.28;overflow-wrap:anywhere;margin:18px 0 17px}.meta{display:flex;flex-wrap:wrap;gap:12px;margin:0;color:#786b5e;font-size:12px}.tags{display:flex;gap:6px;flex-wrap:wrap;margin-top:17px}.tags span{padding:5px 9px;border-radius:999px;background:#fff0df;color:#8f4617;font-size:11px}.story-body{padding:28px 2px 23px;color:#332b25;font-size:16px;line-height:2;white-space:pre-wrap;overflow-wrap:anywhere}.care-note{padding:15px 16px;border-left:3px solid #b85111;border-radius:0 12px 12px 0;background:#fff1df;color:#70431f;font-size:12px;line-height:1.8}.comments{margin-top:33px}.section-head{display:flex;justify-content:space-between;align-items:baseline}.section-head h2{margin:0;font-size:20px}.section-head span{color:#7b7065;font-size:12px}.comments ul{list-style:none;margin:13px 0 0;padding:0;display:grid;gap:9px}.comments li{padding:15px;border:1px solid #eadfd4;border-radius:15px;background:#fff}.comments li div{display:flex;justify-content:space-between;gap:8px}.comments li strong{font-size:12px}.comments time{color:#8a7b6c;font-size:11px}.comments li p{margin:10px 0 0;white-space:pre-wrap;overflow-wrap:anywhere;font-size:13px;line-height:1.7}.muted{color:#7a6e62;font-size:12px;line-height:1.6}.inline-error{padding:12px;border-radius:12px;background:#fff1ee;color:#9a3123;font-size:12px}.inline-error button{border:0;background:transparent;color:inherit;text-decoration:underline;font-weight:800}.all-news{display:flex;justify-content:space-between;align-items:center;margin-top:25px;padding:0 17px;min-height:50px;border-radius:999px;background:#b85111;color:#fff;text-decoration:none;font-size:13px;font-weight:800}.state{display:grid;justify-items:center;text-align:center;gap:10px;margin-top:35px;padding:30px 18px;border:1px dashed #d9cabc;border-radius:19px;background:#fff}.state h1{font-size:19px;margin:0}.state p{margin:0;color:#6e6257;font-size:12px;line-height:1.7}.state button,.state a{display:inline-flex;justify-content:center;align-items:center;min-height:44px;padding:0 18px;border:1px solid #b85111;border-radius:999px;background:#b85111;color:#fff;text-decoration:none;font-size:12px;font-weight:800}.state a{background:#fff;color:#9b4714}.symbol{display:grid;place-items:center;width:49px;height:49px;border-radius:15px;background:#fff0df;color:#a94f16;font-size:25px}.state.error{background:#fff8f5;border-color:#e7c3ba}
</style>
