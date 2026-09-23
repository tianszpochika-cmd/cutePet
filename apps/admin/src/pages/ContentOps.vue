<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { api } from '@cutepet/api-client';
import { bannerScheduleValid, pinAllowed, takedownBlockers, taxonomyTagValid } from '../domain/operations';

interface ArticleRow { id: number; slug: string; title: string; state: string; top: boolean; authorId: number; likes: number; comments: number }
const articles = ref<ArticleRow[]>([]);
const total = ref(0);
const loading = ref(true);
const error = ref('');
const selectedId = ref<number | null>(null);
const reason = ref('');
const newTag = ref('');
const banner = ref({ title: '', startsAt: '', endsAt: '', permanent: true });
const selected = computed(() => articles.value.find((row) => row.id === selectedId.value) ?? null);
const reasonReady = computed(() => !takedownBlockers(reason.value).length);
const tagReady = computed(() => taxonomyTagValid(newTag.value));
const bannerReady = computed(() => banner.value.permanent || bannerScheduleValid(banner.value.startsAt, banner.value.endsAt));
function selectArticle(id: number) { selectedId.value = id; reason.value = ''; }

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const result = (await api.articlesFeed({ query: { page: 1, size: 12 } })) as { items?: ArticleRow[]; total?: number };
    if (!result || !Array.isArray(result.items)) throw new Error('公开文章数据格式暂时不可用');
    articles.value = result.items.filter((row) => typeof row.id === 'number' && row.state === 'PUBLISHED');
    total.value = typeof result.total === 'number' ? result.total : articles.value.length;
    if (!articles.value.some((row) => row.id === selectedId.value)) selectedId.value = null;
  } catch (cause) {
    articles.value = [];
    total.value = 0;
    selectedId.value = null;
    error.value = cause instanceof Error ? cause.message : '公开文章暂时无法读取';
  } finally { loading.value = false; }
}
onMounted(load);
</script>

<template>
  <div class="ops-page">
    <div class="desktop-notice">管理操作请在电脑端查看。</div>
    <div class="workspace">
      <header class="page-head"><div><p class="eyebrow">CONTENT OPERATIONS</p><h1>文章与推荐位</h1><p>先核对正在公开的内容，再判断下架、置顶和配置可能影响的范围。</p></div><span class="preview-badge">只读界面预览</span></header>
      <div class="boundary"><strong>当前入口尚未连接管理员身份</strong><p>下方文章来自公开列表，只包含已发布内容。待审队列、完整版本、操作权限及传播回执尚不能核对，因此本页不会执行下架、置顶、标签或推荐位保存。</p></div>
      <div class="workgrid">
        <section class="panel"><div class="panel-head"><div><h2>当前公开文章</h2><p>公开视图 · 最多显示前 12 条 · 总量 {{ total }}</p></div><button type="button" class="subtle" :disabled="loading" @click="load">刷新</button></div>
          <div v-if="loading" class="state" role="status">正在读取公开文章…</div>
          <div v-else-if="error" class="state error" role="alert"><strong>读取失败</strong><p>{{ error }}</p><button type="button" @click="load">重试</button></div>
          <div v-else-if="!articles.length" class="state"><strong>当前没有公开文章</strong><p>这里不代表审核队列为空；可稍后刷新公开列表。</p></div>
          <div v-else class="table-scroll"><table><thead><tr><th>文章</th><th>公开状态</th><th>互动</th><th>查看</th></tr></thead><tbody><tr v-for="row in articles" :key="row.id" :class="{ selected: selectedId === row.id }"><td><strong>{{ row.title || `文章 #${row.id}` }}</strong><small>#{{ row.id }} · 作者 #{{ row.authorId }}</small></td><td><span class="status live">已发布</span><span v-if="row.top" class="status pinned">已置顶</span></td><td>{{ row.likes }} 赞 · {{ row.comments }} 评</td><td><button type="button" class="select" :aria-pressed="selectedId === row.id" @click="selectArticle(row.id)">核对影响</button></td></tr></tbody></table></div>
        </section>
        <aside class="detail"><p class="eyebrow">ACTION SCOPE</p><template v-if="selected"><h2>{{ selected.title }}</h2><p class="object">公开文章 #{{ selected.id }} · {{ selected.state }}</p><div class="divider"></div><h3>下架前需要核对</h3><ul><li>管理员真实身份和 <code>article.takedown</code> 权限</li><li>对象当前版本、待审版本与作者归属</li><li>搜索、专题、收藏与分享入口的传播清理</li><li>作者通知、失败待办及操作审计</li></ul><label class="field">下架原因 · 本地填写预览<textarea v-model="reason" rows="3" data-testid="takedown-reason" placeholder="写明可核实的问题与处理依据" /></label><p class="inline-status">{{ reasonReady ? '理由已填写；仍未核验权限和版本，不能下架。' : '正式下架必须填写具体原因。' }}</p><p class="inline-status">置顶条件：{{ pinAllowed(selected.state) ? '当前为已发布文章；还需核验权限、排期和服务端现状。' : '仅已发布文章可置顶。' }}</p><p class="no-action">此处只展示处理条件，不发送操作。</p></template><template v-else><h2>选择一篇文章</h2><p>从左侧公开列表选择对象，查看处理前要核对的范围。不会读取作者草稿或私有材料。</p></template></aside>
      </div>
      <div class="lower-grid">
        <section class="panel tool-panel"><div class="panel-head"><div><h2>分类与标签</h2><p>本地格式检查 · 未保存</p></div><span class="permission">taxonomy.manage</span></div><label class="field">新标签名称<input v-model="newTag" maxlength="24" data-testid="tag-input" placeholder="1–24 字" /></label><p v-if="newTag" class="inline-status">{{ tagReady ? '格式符合本地规则；重复合并与受影响文章仍需平台核对。' : '标签需为 1–24 字。' }}</p><p class="muted">正式创建或合并须取得权限、影响预览及平台确认。</p></section>
        <section class="panel tool-panel"><div class="panel-head"><div><h2>推荐位排期</h2><p>配置草稿预览 · 未保存</p></div><span class="permission">banner.manage</span></div><label class="field">标题<input v-model="banner.title" placeholder="推荐位标题" /></label><label class="check"><input v-model="banner.permanent" type="checkbox" />常驻展示</label><div v-if="!banner.permanent" class="date-row"><label class="field">开始<input v-model="banner.startsAt" type="date" /></label><label class="field">结束<input v-model="banner.endsAt" type="date" /></label></div><p v-if="!banner.permanent && (banner.startsAt || banner.endsAt)" class="inline-status">{{ bannerReady ? '时间顺序符合本地规则；排期冲突仍需平台核对。' : '请填写完整起止日期，结束时间晚于开始时间。' }}</p><p class="muted">关联文章必须保持公开；当前不提供保存入口。</p></section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ops-page{color:#2b2118}.workspace{max-width:1360px;margin:0 auto;display:grid;gap:20px}.desktop-notice{display:none}.page-head{display:flex;justify-content:space-between;align-items:start;gap:20px}.page-head h1{margin:0;font-size:clamp(29px,3vw,39px);letter-spacing:-.04em}.page-head p:last-child{margin:10px 0 0;color:#695d53}.eyebrow{margin:0 0 10px;color:#b85111;font-size:11px;font-weight:850;letter-spacing:.13em}.preview-badge{padding:7px 12px;border-radius:999px;background:#fff1e8;color:#8f3d0b;font-size:12px;font-weight:800;white-space:nowrap}.boundary{padding:17px 21px;border-left:4px solid #b85111;border-radius:12px;background:#fff1e8}.boundary strong{color:#8f3d0b}.boundary p{margin:5px 0 0;color:#695d53;font-size:13px;line-height:1.7}.workgrid{display:grid;grid-template-columns:minmax(0,1fr) 330px;gap:17px}.panel,.detail{min-width:0;border:1px solid #eadfd4;border-radius:17px;background:#fff;box-shadow:0 12px 30px rgba(83,55,28,.04)}.panel-head{display:flex;justify-content:space-between;align-items:start;gap:15px;padding:20px 22px;border-bottom:1px solid #eadfd4}.panel-head h2{margin:0;font-size:18px}.panel-head p{margin:5px 0 0;color:#695d53;font-size:12px}.subtle,.select{min-height:34px;padding:0 11px;border:1px solid #eadfd4;border-radius:9px;background:#fff;color:#b85111;font-size:12px;font-weight:750}.subtle:disabled{opacity:.5}.table-scroll{overflow-x:auto}table{width:100%;border-collapse:collapse;min-width:570px}th,td{padding:15px 16px;border-bottom:1px solid #f0e7df;text-align:left;font-size:12px}th{color:#695d53;font-weight:750;background:#fffdf9}td strong{display:block;max-width:320px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px}td small{display:block;margin-top:4px;color:#897669}.selected{background:#fff8f0}.status{display:inline-block;margin-right:5px;padding:4px 8px;border-radius:999px;font-size:11px;font-weight:750;white-space:nowrap}.live{background:#eaf5ed;color:#22674b}.pinned{background:#fff1e8;color:#8f3d0b}.detail{padding:24px}.detail h2{margin:0;font-size:23px;line-height:1.35}.detail h3{font-size:14px}.detail p{color:#695d53;font-size:13px;line-height:1.7}.detail .object{margin-top:8px}.divider{height:1px;margin:21px 0;background:#eadfd4}.detail ul{display:grid;gap:9px;padding-left:20px;color:#5d524a;font-size:12px;line-height:1.6}.detail code{font-size:11px}.field{display:grid;gap:7px;color:#51463c;font-size:12px;font-weight:750}.field input,.field textarea{width:100%;min-height:40px;padding:10px 12px;border:1px solid #dccfc3;border-radius:9px;background:#fff;font-size:13px;font-weight:400;resize:vertical}.inline-status{margin:10px 0;color:#8f3d0b;font-size:12px;line-height:1.6}.no-action{padding:11px;border-radius:9px;background:#f6f1ed;font-weight:700}.lower-grid{display:grid;grid-template-columns:1fr 1fr;gap:17px}.tool-panel{padding-bottom:21px}.tool-panel>.field,.tool-panel>.check,.tool-panel>.date-row,.tool-panel>.inline-status,.tool-panel>.muted{margin-left:22px;margin-right:22px}.tool-panel>.field{margin-top:17px}.permission{padding:5px 8px;border-radius:6px;background:#f6effa;color:#6f4788;font-size:10px;font-weight:800}.check{display:flex;align-items:center;gap:8px;margin-top:14px;color:#51463c;font-size:12px}.date-row{display:grid;grid-template-columns:1fr 1fr;gap:11px;margin-top:14px}.muted{color:#695d53;font-size:12px;line-height:1.6}.state{padding:27px;color:#695d53;font-size:13px}.state strong{color:#2b2118;font-size:15px}.state p{margin:7px 0 10px}.state button{border:0;background:none;color:#b85111;font-weight:750}.state.error{color:#ab3131}@media(max-width:1100px){.workgrid{grid-template-columns:1fr}.detail{max-width:none}}@media(max-width:800px){.workspace{display:none}.desktop-notice{display:block;padding:24px;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#695d53}}
</style>
