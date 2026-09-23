<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { FAVORITE_GROUPS } from '../../domain/family';

type FavoriteTab = typeof FAVORITE_GROUPS[number];
interface FavoriteRow {
  id: number;
  tab: string;
  targetType: string;
  targetId: number;
  groupName?: string | null;
  createdAt?: string | null;
}

const router = useRouter();
const tab = ref<FavoriteTab>('内容');
const rows = ref<FavoriteRow[]>([]);
const loading = ref(false);
const loaded = ref(false);
const error = ref('');
let requestId = 0;

const pending: Record<FavoriteTab, { text: string; to: string; action: string }> = {
  内容: {
    text: '内容收藏可从当前页面读取。',
    to: '/news',
    action: '浏览资讯',
  },
  商品: {
    text: '商品收藏由用品服务保存，尚未汇总到“我的收藏”。这里暂时无法判断你是否收藏了商品。',
    to: '/goods',
    action: '浏览用品',
  },
  清单: {
    text: '清单收藏的个人汇总尚未接通。这里暂时无法判断你是否收藏了清单。',
    to: '/goods',
    action: '浏览公开清单',
  },
  场所: {
    text: '场所收藏由探索服务保存，尚未汇总到“我的收藏”。这里暂时无法判断你是否收藏了场所。',
    to: '/explore',
    action: '浏览场所',
  },
  路线: {
    text: '路线收藏由探索服务保存，尚未汇总到“我的收藏”。这里暂时无法判断你是否收藏了路线。',
    to: '/explore/routes',
    action: '浏览路线',
  },
};

function formatDate(value?: string | null): string {
  if (!value) return '时间未提供';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value.replace('T', ' ') : date.toLocaleDateString('zh-CN');
}
function typeLabel(value: string): string {
  if (value === 'ARTICLE') return '文章';
  return '内容';
}
async function loadContent() {
  const current = ++requestId;
  rows.value = [];
  loaded.value = false;
  error.value = '';
  if (!getAccessToken()) return;
  loading.value = true;
  try {
    const response = await api.favoritesList({ query: { tab: '内容' } });
    if (current !== requestId) return;
    if (!Array.isArray(response) || response.some((row) =>
      !row || typeof row !== 'object' ||
      typeof (row as FavoriteRow).id !== 'number' ||
      typeof (row as FavoriteRow).targetId !== 'number' ||
      typeof (row as FavoriteRow).targetType !== 'string')) {
      throw new Error('收藏记录格式不完整，请稍后重试。');
    }
    rows.value = response as FavoriteRow[];
    loaded.value = true;
  } catch (cause) {
    if (current === requestId) error.value = cause instanceof Error ? cause.message : '收藏加载失败，请稍后重试。';
  } finally {
    if (current === requestId) loading.value = false;
  }
}
function pick(next: FavoriteTab) {
  if (tab.value === next) return;
  ++requestId;
  tab.value = next;
  loading.value = false;
  error.value = '';
  if (next === '内容') void loadContent();
}
onMounted(() => void loadContent());
</script>

<template>
  <div class="fav">
    <button type="button" class="back" @click="router.push('/me')">← 返回我的</button>
    <header class="heading">
      <p class="eyebrow">SAVED FOR LATER · 个人收藏</p>
      <h1>我的收藏</h1>
      <p>把值得回看的资料放在这里。每个分组只展示平台已确认的记录。</p>
    </header>

    <nav class="tabs" aria-label="收藏分组">
      <button v-for="group in FAVORITE_GROUPS" :id="'favorite-tab-' + group" :key="group" type="button"
        :class="{ on: tab === group }" :aria-pressed="tab === group" aria-controls="favorite-panel" @click="pick(group)">
        {{ group }}
      </button>
    </nav>

    <section id="favorite-panel" :aria-labelledby="'favorite-tab-' + tab" class="panel">
      <template v-if="tab === '内容'">
        <div class="section-heading"><h2>内容收藏</h2><span v-if="loaded">{{ rows.length }} 条记录</span></div>
        <div v-if="!getAccessToken()" class="notice">
          <strong>登录后查看</strong>
          <p>内容收藏属于个人资料，需要登录后读取。</p>
          <button type="button" @click="router.push({ path: '/login', query: { return: '/me/favorites' } })">前往登录</button>
        </div>
        <p v-else-if="loading" class="status" role="status">正在读取收藏记录…</p>
        <p v-else-if="error" class="status error" role="alert">{{ error }} <button type="button" @click="loadContent">重试</button></p>
        <template v-else-if="loaded">
          <ul v-if="rows.length" class="list">
            <li v-for="row in rows" :key="row.id" class="card">
              <span class="symbol" aria-hidden="true">✦</span>
              <div>
                <p class="kind">{{ typeLabel(row.targetType) }}收藏</p>
                <strong>{{ typeLabel(row.targetType) }}资料</strong>
                <p class="meta">收藏于 {{ formatDate(row.createdAt) }}<span v-if="row.groupName"> · {{ row.groupName }}分组</span></p>
                <p class="record-id">记录对象编号 {{ row.targetId }}</p>
              </div>
            </li>
          </ul>
          <div v-else class="empty" data-testid="empty">
            <strong>还没有内容收藏</strong>
            <p>读到想再看的文章时，可以在文章页点击收藏。</p>
            <button type="button" @click="router.push('/news')">去看资讯</button>
          </div>
          <p v-if="rows.length" class="footnote">目前收藏记录未提供文章标题与链接，因此暂不能从此处安全直达原文。可前往资讯页查看公开内容。</p>
        </template>
      </template>

      <div v-else class="notice pending">
        <span class="pending-symbol" aria-hidden="true">◇</span>
        <div>
          <h2>{{ tab }}汇总待接入</h2>
          <p>{{ pending[tab].text }}</p>
          <button type="button" @click="router.push(pending[tab].to)">{{ pending[tab].action }}</button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.fav{max-width:820px;margin:auto;padding:28px 20px 80px;color:#2b2118}.back{border:0;background:none;padding:0;color:#B85111;cursor:pointer;font:inherit}.heading{margin:30px 0 27px}.eyebrow{font-size:12px;font-weight:800;letter-spacing:.14em;color:#B85111;margin:0 0 8px}h1{font-size:clamp(29px,4vw,42px);letter-spacing:-.03em;margin:0}.heading>p:last-child{color:#71675e;line-height:1.7;margin:10px 0 0}.tabs{display:flex;gap:8px;overflow-x:auto;padding:0 0 12px}.tabs button{flex:none;border:1px solid #eadfd3;background:#fff;border-radius:999px;padding:10px 18px;color:#6e6053;white-space:nowrap;cursor:pointer;font:inherit}.tabs button.on{background:#B85111;color:#fff;border-color:#B85111;font-weight:700}.panel{min-height:210px}.section-heading{display:flex;justify-content:space-between;align-items:baseline;margin-bottom:15px}.section-heading h2{font-size:20px;margin:0}.section-heading span{font-size:13px;color:#827367}.list{list-style:none;margin:0;padding:0;display:grid;gap:10px}.card{display:flex;align-items:start;gap:15px;padding:18px;background:#fff;border:1px solid #eaded1;border-radius:16px;box-shadow:0 8px 28px #3a231308}.symbol{width:42px;height:42px;flex:none;border-radius:12px;background:#fff0dc;color:#af6a37;display:grid;place-items:center;font-size:22px}.kind{font-size:12px;font-weight:700;color:#B85111;margin:0 0 4px}.card strong{font-size:17px}.meta,.record-id{font-size:13px;color:#74695e;line-height:1.6;margin:6px 0 0}.record-id{font-size:12px;color:#97887b}.notice,.empty,.status{padding:24px;background:#f8f3ec;border:1px solid #ebdfd0;border-radius:16px;color:#63574d;line-height:1.7}.notice p,.empty p{margin:6px 0 16px}.notice button,.empty button{border:0;background:#B85111;color:#fff;border-radius:999px;padding:10px 17px;cursor:pointer;font:inherit}.pending{display:flex;align-items:start;gap:17px;background:#f5f7f2;border-color:#e1e8dd}.pending-symbol{width:44px;height:44px;flex:none;border-radius:12px;background:#e7efe2;color:#4d7752;display:grid;place-items:center;font-size:28px}.pending h2{font-size:20px;margin:0}.pending p{margin:7px 0 18px}.empty{text-align:center;background:#fff;border-style:dashed}.empty strong{font-size:17px}.error{background:#fff1ef;color:#a6372b;border-color:#f2d3cb}.error button{border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}.footnote{font-size:12px;color:#817367;line-height:1.7;margin:15px 0 0}@media(max-width:560px){.tabs button{padding:9px 15px}.pending{padding:18px}.card{padding:15px}}
</style>
