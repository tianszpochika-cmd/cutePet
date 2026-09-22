<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '@cutepet/api-client';
import { disclaimerFor, sharePath, validateComment, commentVisibility } from '../../domain/news';

const route = useRoute();
const router = useRouter();
const slug = String(route.params.slug);

interface ArticleDetail {
  id: number;
  title: string;
  body: string;
  channel: number;
  authorId: number;
  likes: number;
  commentCount: number;
}

const article = ref<ArticleDetail | null>(null);
const error = ref('');
const liked = ref(false);
const faved = ref(false);
const comment = ref('');
const commentError = ref('');
const progress = ref(0);

const disclaimer = computed(() => (article.value ? disclaimerFor(String(article.value.channel)) : null));

onMounted(async () => {
  try {
    article.value = (await api.articleGet({ path: { slug } })) as unknown as ArticleDetail;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '文章不可见（未发布或已下架）';
  }
  window.addEventListener('scroll', onScroll);
});

function onScroll() {
  const total = document.body.scrollHeight - window.innerHeight;
  progress.value = total > 0 ? Math.min(100, Math.round((window.scrollY / total) * 100)) : 0;
}

async function toggleLike() {
  try {
    if (liked.value) await api.articleUnlike({ path: { id: String(article.value!.id) } });
    else await api.articleLike({ path: { id: String(article.value!.id) } });
    liked.value = !liked.value;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败（未登录会进入登录闸门）';
  }
}

async function toggleFavorite() {
  try {
    if (faved.value) await api.articleUnfavorite({ path: { id: String(article.value!.id) } });
    else await api.articleFavorite({ path: { id: String(article.value!.id) } });
    faved.value = !faved.value;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败（未登录会进入登录闸门）';
  }
}

async function sendComment() {
  const invalid = validateComment(comment.value);
  if (invalid) {
    commentError.value = invalid === 'COMMENT_EMPTY' ? '评论不能为空' : '最多 500 字';
    return;
  }
  commentError.value = '';
  try {
    const res = (await api.commentCreate({
      path: { id: String(article.value!.id) },
      body: { parentId: null, content: comment.value.trim() },
    })) as unknown as { machineState?: string; visibleToOthers?: boolean };
    comment.value = '';
    if (res.machineState === 'SUSPECT') {
      commentError.value = '';
      alert('评论已提交（疑似内容仅你自己可见，审核通过后公开）');
    }
  } catch (e) {
    commentError.value = e instanceof Error ? e.message : '发送失败';
  }
}

function share() {
  const path = sharePath('article', slug);
  void navigator.clipboard?.writeText(`${location.origin}${path}`);
  alert('链接已复制');
}

// 供模板引用（分组可见性示例）
void commentVisibility;
</script>

<template>
  <div class="article">
    <div class="progress" :style="{ width: `${progress}%` }" data-testid="progress" />

    <p v-if="error" class="err">{{ error }}</p>
    <template v-else-if="article">
      <h1>{{ article.title }}</h1>
      <p class="meta">作者 #{{ article.authorId }} · 频道 #{{ article.channel }}</p>

      <section class="body">{{ article.body }}</section>

      <p v-if="disclaimer" class="disclaimer" data-testid="disclaimer">⚠️ {{ disclaimer }}</p>

      <div class="bar">
        <button type="button" :class="{ on: liked }" data-testid="like" @click="toggleLike">
          👍 {{ article.likes + (liked ? 1 : 0) }}
        </button>
        <button type="button" :class="{ on: faved }" data-testid="favorite" @click="toggleFavorite">
          ⭐ 收藏{{ faved ? '·已' : '' }}
        </button>
        <button type="button" @click="share">🔗 分享</button>
        <button type="button" class="report" @click="alert('举报面板：类型+说明 → 48h 内通知结果（合规 B2）')">
          🚩 举报
        </button>
      </div>

      <section class="comments">
        <h2>评论（{{ article.commentCount }}）</h2>
        <textarea v-model="comment" rows="2" placeholder="说点什么…（1–500 字）" data-testid="comment-input" />
        <p v-if="commentError" class="err">{{ commentError }}</p>
        <button type="button" class="primary" data-testid="send-comment" @click="sendComment">发送</button>
      </section>
    </template>

    <button type="button" class="back" @click="router.push('/news')">‹ 返回资讯</button>
  </div>
</template>

<style scoped>
.article {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px 64px;
}
.progress {
  position: fixed;
  top: 64px;
  left: 0;
  height: 3px;
  background: #ff7a2f;
  z-index: 20;
  transition: width 0.1s linear;
}
h1 {
  font-size: 26px;
  line-height: 1.4;
}
.meta {
  color: #7a6e63;
  font-size: 13px;
}
.body {
  line-height: 1.9;
  font-size: 16px;
  white-space: pre-wrap;
  margin: 16px 0;
}
.disclaimer {
  background: #fff1e8;
  color: #b45309;
  font-size: 13px;
  border-radius: 8px;
  padding: 10px 14px;
}
.bar {
  display: flex;
  gap: 10px;
  margin: 20px 0;
  flex-wrap: wrap;
}
.bar button {
  height: 36px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: #f7f1ea;
  color: #7a6e63;
  cursor: pointer;
}
.bar button.on {
  background: #fff1e8;
  color: #ff7a2f;
  font-weight: 600;
}
.bar .report {
  margin-left: auto;
}
.comments h2 {
  font-size: 17px;
}
.comments textarea {
  width: 100%;
  border: 1px solid #f0e6dc;
  border-radius: 12px;
  padding: 10px 14px;
  font-family: inherit;
}
.primary {
  margin-top: 8px;
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  background: #ff7a2f;
  color: #fff;
  font-weight: 600;
}
.back {
  margin-top: 24px;
  background: none;
  border: none;
  color: #ff7a2f;
  cursor: pointer;
}
.err {
  color: #ef4444;
  font-size: 13px;
}
</style>
