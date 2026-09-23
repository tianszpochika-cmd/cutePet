<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { sharePath, validateComment } from '../../domain/news';

interface Article { id: number; slug: string; title: string; body: string; cover?: string | null; authorId: number; channelId: number; likes: number; commentCount: number }
interface Comment { id: number; userId: number; content: string; machineState: string }
const route = useRoute();
const router = useRouter();
const article = ref<Article | null>(null);
const comments = ref<Comment[]>([]);
const loading = ref(true);
const commentsLoading = ref(false);
const error = ref('');
const commentsError = ref('');
const actionError = ref('');
const notice = ref('');
const comment = ref('');
const commentError = ref('');
const liked = ref<boolean | null>(null);
const faved = ref<boolean | null>(null);
const busy = ref('');
const progress = ref(0);
const reportOpen = ref(false);
const reportReason = ref('');
const reportContent = ref('');
const reportError = ref('');
const disclaimer = '涉及健康与营养的内容仅供科普参考，不构成兽医诊疗建议；用药与诊疗请遵医嘱。';
let articleRequest = 0;

function loginFor(action: string): boolean {
  if (getAccessToken()) return false;
  void router.push({ path: '/login', query: { return: route.fullPath, resume: action } });
  return true;
}

async function loadComments(id: number) {
  commentsLoading.value = true;
  commentsError.value = '';
  try {
    const result = await api.commentsList({ params: { id } });
    const current = article.value;
    if (!current || current.id !== id) return;
    comments.value = Array.isArray(result) ? result as Comment[] : [];
    current.commentCount = comments.value.filter((item) => item.machineState !== 'SUSPECT').length;
  } catch (cause) {
    if (article.value?.id === id) commentsError.value = cause instanceof Error ? cause.message : '评论暂时无法读取';
  } finally { if (article.value?.id === id) commentsLoading.value = false; }
}

async function loadArticle(slug: string) {
  const currentRequest = ++articleRequest;
  loading.value = true;
  error.value = '';
  article.value = null;
  comments.value = [];
  commentsLoading.value = false;
  commentsError.value = '';
  notice.value = '';
  actionError.value = '';
  reportOpen.value = false;
  liked.value = null;
  faved.value = null;
  try {
    const detail = await api.articleGet({ params: { slug } });
    if (currentRequest !== articleRequest) return;
    const loaded = detail as Article;
    if (!loaded || typeof loaded.id !== 'number' || typeof loaded.title !== 'string') throw new Error('文章数据暂时不可用');
    article.value = loaded;
    void loadComments(loaded.id);
  } catch (cause) {
    if (currentRequest === articleRequest) error.value = cause instanceof Error ? cause.message : '文章不存在或已不可见';
  } finally { if (currentRequest === articleRequest) loading.value = false; }
}
watch(() => route.params.slug, (value) => { void loadArticle(String(value ?? '')); }, { immediate: true });
function onScroll() {
  const total = document.documentElement.scrollHeight - window.innerHeight;
  progress.value = total > 0 ? Math.min(100, Math.round(window.scrollY / total * 100)) : 0;
}
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }));
onUnmounted(() => window.removeEventListener('scroll', onScroll));

async function toggleLike() {
  const current = article.value;
  if (!current || busy.value || loginFor('like')) return;
  busy.value = 'like'; actionError.value = ''; notice.value = '';
  try {
    const result = (await (liked.value === true
      ? api.articleUnlike({ params: { id: current.id } })
      : api.articleLike({ params: { id: current.id } }))) as { liked?: boolean; count?: number };
    if (typeof result?.liked !== 'boolean') throw new Error('点赞状态未返回，请稍后刷新');
    liked.value = result.liked;
    if (typeof result.count === 'number') current.likes = result.count;
    notice.value = result.liked ? '已点赞' : '已取消点赞';
  } catch (cause) { actionError.value = cause instanceof Error ? cause.message : '点赞操作失败'; }
  finally { busy.value = ''; }
}

async function toggleFavorite() {
  const current = article.value;
  if (!current || busy.value || loginFor('favorite')) return;
  busy.value = 'favorite'; actionError.value = ''; notice.value = '';
  try {
    const result = (await (faved.value === true
      ? api.articleUnfavorite({ params: { id: current.id } })
      : api.articleFavorite({ params: { id: current.id } }))) as { favorited?: boolean };
    if (typeof result?.favorited !== 'boolean') throw new Error('收藏状态未返回，请稍后刷新');
    faved.value = result.favorited;
    notice.value = result.favorited ? '已收藏到内容' : '已取消收藏';
  } catch (cause) { actionError.value = cause instanceof Error ? cause.message : '收藏操作失败'; }
  finally { busy.value = ''; }
}

async function sendComment() {
  const current = article.value;
  if (!current || busy.value || loginFor('comment')) return;
  const invalid = validateComment(comment.value);
  if (invalid) { commentError.value = invalid === 'COMMENT_EMPTY' ? '先写下评论内容' : '评论最多 500 字'; return; }
  busy.value = 'comment'; commentError.value = ''; notice.value = '';
  try {
    const result = (await api.commentCreate({ params: { id: current.id }, body: { parentId: null, content: comment.value.trim() } })) as { id?: number; machineState?: string };
    if (typeof result?.id !== 'number') throw new Error('未收到评论确认，请稍后重试');
    comment.value = '';
    notice.value = result.machineState === 'SUSPECT' ? '评论已送审，目前仅你可见。' : '评论已发表。';
    await loadComments(current.id);
  } catch (cause) { commentError.value = cause instanceof Error ? cause.message : '评论发送失败'; }
  finally { busy.value = ''; }
}

async function share() {
  const current = article.value;
  if (!current) return;
  actionError.value = ''; notice.value = '';
  try {
    if (!navigator.clipboard?.writeText) throw new Error('当前浏览器无法直接复制，请使用地址栏分享');
    await navigator.clipboard.writeText(`${location.origin}${sharePath('article', current.slug)}`);
    notice.value = '文章链接已复制';
  } catch (cause) { actionError.value = cause instanceof Error ? cause.message : '复制失败'; }
}

function openReport() {
  if (!article.value || loginFor('report')) return;
  reportError.value = '';
  reportOpen.value = true;
}
async function submitReport() {
  const current = article.value;
  if (!current || busy.value) return;
  if (!reportReason.value) { reportError.value = '请选择举报原因'; return; }
  busy.value = 'report'; reportError.value = '';
  try {
    const result = (await api.reportsCreate({ body: { targetType: 'ARTICLE', targetId: current.id, reason: reportReason.value, content: reportContent.value.trim() } })) as { id?: number; state?: string };
    if (typeof result?.id !== 'number' || result.state !== 'OPEN') throw new Error('未收到举报受理确认');
    reportOpen.value = false;
    reportReason.value = ''; reportContent.value = '';
    notice.value = `举报已受理，编号 #${result.id}`;
  } catch (cause) { reportError.value = cause instanceof Error ? cause.message : '举报提交失败'; }
  finally { busy.value = ''; }
}
</script>

<template>
  <div class="article-page">
    <div class="progress" :style="{ width: `${progress}%` }" data-testid="progress" />
    <nav class="breadcrumb"><router-link to="/news">资讯</router-link><span>／</span><span>正文</span></nav>
    <section v-if="loading" class="state" role="status">正在读取文章…</section>
    <section v-else-if="error" class="state"><h1>这篇文章暂时无法阅读</h1><p role="alert">{{ error }}</p><router-link to="/news">返回资讯首页 →</router-link></section>
    <template v-else-if="article">
      <article class="article-shell">
        <header class="article-header"><p class="eyebrow">宠物资讯</p><h1>{{ article.title }}</h1><p class="meta">作者 #{{ article.authorId }} · 文章 #{{ article.id }}</p></header>
        <img v-if="article.cover" class="cover" :src="article.cover" alt="文章封面" />
        <div class="body">{{ article.body }}</div>
        <p v-if="disclaimer" class="disclaimer" data-testid="disclaimer">{{ disclaimer }}</p>
        <div class="bar" aria-label="文章操作">
          <button type="button" :class="{ on: liked === true }" :disabled="!!busy" data-testid="like" @click="toggleLike">{{ liked === true ? '已点赞' : '点赞' }} · {{ article.likes }}</button>
          <button type="button" :class="{ on: faved === true }" :disabled="!!busy" data-testid="favorite" @click="toggleFavorite">{{ faved === true ? '已收藏' : '收藏' }}</button>
          <button type="button" @click="share">分享链接</button><button type="button" @click="openReport">举报</button>
        </div>
        <p v-if="notice" class="notice" role="status">{{ notice }}</p><p v-if="actionError" class="error" role="alert">{{ actionError }}</p>
      </article>
      <section id="comments" class="comments" aria-labelledby="comments-title">
        <div class="section-head"><p class="eyebrow">DISCUSSION</p><h2 id="comments-title">一起交流 <small>{{ article.commentCount }}</small></h2></div>
        <div class="comment-compose"><label for="comment-input">写下你的想法</label><textarea id="comment-input" v-model="comment" rows="3" maxlength="500" placeholder="分享经验，也请尊重彼此的照护选择" data-testid="comment-input" /><div class="compose-foot"><span>{{ comment.length }}/500</span><button type="button" class="primary" :disabled="!!busy" data-testid="send-comment" @click="sendComment">{{ busy === 'comment' ? '发送中…' : '发送评论' }}</button></div><p v-if="commentError" class="error" role="alert">{{ commentError }}</p></div>
        <p v-if="commentsLoading" class="muted">正在读取评论…</p><div v-else-if="commentsError" class="inline-state"><p>{{ commentsError }}</p><button type="button" @click="loadComments(article.id)">重试</button></div><p v-else-if="!comments.length" class="muted">还没有可见评论，欢迎分享第一条经验。</p>
        <ul v-else class="comment-list"><li v-for="item in comments" :key="item.id"><div class="comment-meta"><strong>用户 #{{ item.userId }}</strong><span v-if="item.machineState === 'SUSPECT'">仅自己可见 · 待审核</span></div><p>{{ item.content }}</p></li></ul>
      </section>
      <div v-if="reportOpen" class="dialog-backdrop" @click.self="reportOpen = false"><form class="report-dialog" aria-label="举报文章" @submit.prevent="submitReport"><h2>举报这篇文章</h2><p>请选择原因并补充有助于核实的信息。</p><label>举报原因<select v-model="reportReason"><option value="">请选择</option><option value="MISINFORMATION">信息不准确</option><option value="HARMFUL">可能造成伤害</option><option value="COPYRIGHT">版权问题</option><option value="OTHER">其他</option></select></label><label>补充说明<textarea v-model="reportContent" rows="3" maxlength="500" placeholder="可选，最多 500 字" /></label><p v-if="reportError" class="error" role="alert">{{ reportError }}</p><div class="dialog-actions"><button type="button" class="secondary" @click="reportOpen = false">取消</button><button type="submit" class="primary" :disabled="!!busy">{{ busy === 'report' ? '提交中…' : '提交举报' }}</button></div></form></div>
    </template>
  </div>
</template>

<style scoped>
.article-page{width:min(100% - 32px,900px);margin:0 auto;padding:25px 0 80px}.progress{position:fixed;top:76px;left:0;height:3px;background:var(--primary);z-index:31;transition:width .1s linear}.breadcrumb{display:flex;gap:8px;align-items:center;margin-bottom:30px;color:var(--ink-2);font-size:13px}.breadcrumb a{color:var(--primary);text-decoration:none}.article-shell,.comments,.state{background:#fff;border:1px solid var(--line);border-radius:25px;padding:clamp(22px,5vw,52px)}.article-header{padding-bottom:26px;border-bottom:1px solid var(--line)}.eyebrow{margin:0 0 12px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.13em}.article-header h1{max-width:22ch;margin:0;font-size:clamp(31px,4vw,49px);line-height:1.28}.meta{margin:17px 0 0;color:var(--ink-2);font-size:13px}.cover{display:block;width:100%;max-height:450px;margin:28px 0 0;border-radius:18px;object-fit:cover}.body{white-space:pre-wrap;overflow-wrap:anywhere;margin:32px 0;color:var(--ink);font-size:17px;line-height:2}.disclaimer{padding:16px 18px;border-radius:12px;background:#fff2e8;color:#8e3b0c;font-size:13px;line-height:1.7}.bar{display:flex;flex-wrap:wrap;gap:9px;padding-top:22px;border-top:1px solid var(--line)}.bar button,.secondary{min-height:42px;padding:0 17px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--ink-2);font-weight:700}.bar button.on{background:var(--primary-soft);border-color:var(--primary-soft);color:var(--primary)}button:disabled{opacity:.55;cursor:not-allowed}.notice,.error{margin:16px 0 0;font-size:13px}.notice{color:#176444}.error{color:#ab3223}.comments{margin-top:20px}.section-head h2{margin:0;font-size:25px}.section-head small{margin-left:6px;color:var(--ink-2);font-size:15px;font-weight:500}.comment-compose{display:grid;gap:9px;margin:23px 0;padding:20px;border-radius:17px;background:#faf5f0}.comment-compose label{font-weight:750}.comment-compose textarea,.report-dialog textarea,.report-dialog select{width:100%;padding:12px 14px;border:1px solid #d9c9bd;border-radius:11px;background:#fff;resize:vertical}.compose-foot{display:flex;justify-content:space-between;align-items:center;gap:12px;color:var(--ink-2);font-size:12px}.primary{min-height:43px;padding:0 19px;border:0;border-radius:999px;background:var(--primary);color:#fff;font-weight:750}.muted{color:var(--ink-2);font-size:14px}.inline-state{display:flex;align-items:center;gap:14px;color:#ab3223}.inline-state button{border:0;background:none;color:var(--primary);font-weight:700}.comment-list{display:grid;gap:0;list-style:none;margin:0;padding:0}.comment-list li{padding:19px 0;border-top:1px solid var(--line)}.comment-meta{display:flex;gap:12px;align-items:center;font-size:13px}.comment-meta span{color:#9b5b17}.comment-list p{margin:8px 0 0;white-space:pre-wrap;overflow-wrap:anywhere}.state{min-height:220px}.state h1{margin:0;font-size:28px}.state p{color:var(--ink-2)}.state a{display:inline-flex;margin-top:10px;text-decoration:none;font-weight:700}.dialog-backdrop{position:fixed;inset:0;z-index:50;display:grid;place-items:center;padding:16px;background:rgba(43,33,24,.5)}.report-dialog{width:min(100%,470px);display:grid;gap:15px;padding:25px;border-radius:22px;background:#fff}.report-dialog h2{margin:0}.report-dialog p{margin:0;color:var(--ink-2);font-size:13px}.report-dialog label{display:grid;gap:7px;font-size:13px;font-weight:700}.dialog-actions{display:flex;justify-content:flex-end;gap:9px}@media(max-width:650px){.progress{top:64px}.article-page{padding-top:18px}.body{font-size:16px}.bar button{flex:1}.report-dialog{max-height:90vh;overflow:auto}}
</style>
