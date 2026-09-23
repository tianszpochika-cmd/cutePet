<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api, getAccessToken } from '@cutepet/api-client';
import { statusStepIndex, rejectCopy, withdrawCopy, STATUS_STEPS } from '../../domain/submission';
import { reviewStateCopy } from '../../domain/news';

interface SubmissionRow { articleId?: number; submissionId?: number; state: string; slug?: string; rejectCount?: number; note?: string; suspended?: boolean }
const route = useRoute();
const router = useRouter();
const authorized = Boolean(getAccessToken());
const rows = ref<SubmissionRow[]>([]);
const loading = ref(true);
const error = ref('');
const notice = ref('');
const withdrawing = ref<number | null>(null);

async function load() {
  if (!authorized) return;
  loading.value = true;
  error.value = '';
  try {
    const result = await api.submissionList();
    if (!Array.isArray(result)) throw new Error('投稿列表格式暂时不可用');
    rows.value = result as SubmissionRow[];
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '投稿暂时无法读取';
  } finally { loading.value = false; }
}
onMounted(() => {
  if (!authorized) void router.replace({ path: '/login', query: { return: route.fullPath, resume: 'submissions' } });
  else void load();
});
function steps(state: string) {
  const { done, current } = statusStepIndex(state);
  return STATUS_STEPS.map((label, index) => ({ label, cls: index < done ? 'done' : index === current ? 'current' : '' }));
}
async function withdraw(row: SubmissionRow) {
  if (!row.submissionId || withdrawing.value) return;
  const copy = withdrawCopy(row.state);
  if (!copy.allowed) return;
  if (!window.confirm(copy.note + ' 确定撤回吗？')) return;
  withdrawing.value = row.submissionId;
  error.value = '';
  notice.value = '';
  try {
    const result = (await api.submissionWithdraw({ params: { id: row.submissionId } })) as { id?: number; state?: string };
    if (result?.state !== 'WITHDRAWN' || result.id !== row.submissionId) throw new Error('未收到撤回确认');
    notice.value = '已撤回这篇投稿。';
    await load();
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '撤回失败';
  } finally { withdrawing.value = null; }
}
</script>

<template>
  <div class="submissions">
    <nav class="breadcrumb"><router-link to="/me">我的</router-link><span>／</span><span>创作中心</span></nav>
    <header><div><p class="eyebrow">YOUR STORIES</p><h1>我的投稿</h1><p>从一篇草稿开始，留下真实的照护经验。</p></div><router-link class="create" to="/write">＋ 写新内容</router-link></header>
    <div class="summary"><div><strong>{{ rows.length }}</strong><span>当前显示的投稿</span></div><p>文章提交后要经过审核。审核中的稿件可能暂时不会出现在这里；提交时请记下稿件编号。</p></div>
    <section v-if="!authorized" class="state">正在前往登录页…</section>
    <section v-else-if="loading" class="state" role="status">正在读取投稿记录…</section>
    <section v-else-if="error && !rows.length" class="state error" role="alert"><h2>暂时无法读取投稿</h2><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
    <template v-else>
      <p v-if="notice" class="notice" role="status">{{ notice }}</p><p v-if="error" class="error" role="alert">{{ error }}</p>
      <section v-if="!rows.length" class="state" data-testid="empty"><h2>还没有可读取的投稿</h2><p>现在可以写第一篇文章、评测或清单。保存后的稿件会在服务端确认后出现。</p><router-link to="/write">开始写作 →</router-link></section>
      <ul v-else class="list"><li v-for="(row, index) in rows" :key="`${row.articleId ?? 'v'}-${row.submissionId ?? index}`" class="card"><div class="card-top"><span :class="['badge', reviewStateCopy(row.state).tone]">{{ reviewStateCopy(row.state).label }}</span><span class="index">{{ String(index + 1).padStart(2, '0') }}</span></div><h2>{{ row.slug ? `文章 #${row.articleId ?? row.slug}` : `投稿 #${row.submissionId ?? row.articleId ?? '—'}` }}</h2><div class="steps"><span v-for="step in steps(row.state)" :key="step.label" :class="['step', step.cls]">{{ step.label }}</span></div><p v-if="row.state === 'REJECTED'" class="reject" data-testid="reject-copy">{{ rejectCopy(row.rejectCount ?? 1).message }}</p><p v-if="row.note && row.state === 'REJECTED'" class="note">编辑意见：{{ row.note }}</p><p v-if="row.suspended" class="reject">平台返回暂停标记，具体原因需核对；质量退修本身不应暂停投稿。</p><div class="actions"><router-link v-if="row.state === 'PUBLISHED' && row.slug" :to="`/news/${row.slug}`">阅读线上文章 →</router-link><button v-if="row.submissionId && withdrawCopy(row.state).allowed" type="button" :disabled="withdrawing === row.submissionId" :data-testid="`withdraw-${index}`" @click="withdraw(row)">{{ withdrawing === row.submissionId ? '撤回中…' : '撤回投稿' }}</button><span v-if="row.state === 'DRAFT' || row.state === 'REJECTED'">这篇稿件暂时无法在这里继续编辑。</span></div></li></ul>
    </template>
  </div>
</template>

<style scoped>
.submissions{width:min(100% - 32px,950px);margin:0 auto;padding:27px 0 90px}.breadcrumb{display:flex;gap:8px;color:var(--ink-2);font-size:13px}.breadcrumb a{text-decoration:none}.eyebrow{margin:0 0 10px;color:var(--primary);font-size:12px;font-weight:800;letter-spacing:.13em}header{display:flex;justify-content:space-between;align-items:end;gap:20px;margin:48px 0 26px}header h1{margin:0;font-size:clamp(35px,4vw,52px)}header p:not(.eyebrow){margin:13px 0 0;color:var(--ink-2)}.create{display:inline-flex;align-items:center;justify-content:center;min-height:46px;padding:0 22px;border-radius:999px;background:var(--primary);color:#fff;text-decoration:none;font-weight:750;white-space:nowrap}.summary{display:flex;align-items:center;gap:25px;padding:22px 25px;border-radius:19px;background:#f7e9df}.summary div{display:flex;flex-direction:column;min-width:165px}.summary strong{font-size:32px;line-height:1;color:var(--primary)}.summary span{margin-top:7px;font-size:12px;font-weight:700}.summary p{margin:0;color:var(--ink-2);font-size:13px;line-height:1.7}.state{margin-top:22px;padding:31px;border:1px solid var(--line);border-radius:20px;background:#fff;color:var(--ink-2)}.state h2{margin:0 0 9px;color:var(--ink);font-size:21px}.state p{margin:0 0 13px}.state a{font-weight:750;text-decoration:none}.state button{border:0;background:none;color:var(--primary);font-weight:750}.list{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:15px;list-style:none;margin:22px 0 0;padding:0}.card{display:flex;flex-direction:column;gap:13px;min-height:290px;padding:25px;border:1px solid var(--line);border-radius:20px;background:#fff}.card-top{display:flex;justify-content:space-between;align-items:center}.badge{padding:4px 11px;border-radius:999px;font-size:12px;font-weight:800}.badge.gray{background:#eeeae6;color:#64574d}.badge.orange{background:#fff1e8;color:#9b470e}.badge.blue{background:#eaf1ff;color:#366bc1}.badge.green{background:#e7f8ef;color:#176444}.badge.red{background:#fdecec;color:#a63322}.index{color:#bea898;font-size:13px;font-weight:800}.card h2{margin:0;font-size:22px}.id-line,.note{margin:0;color:var(--ink-2);font-size:13px}.steps{display:flex;flex-wrap:wrap;gap:6px}.step{padding:3px 9px;border-radius:999px;background:#f7f1ea;color:var(--ink-2);font-size:11px}.step.done{background:#e7f8ef;color:#176444}.step.current{background:#fff1e8;color:#9b470e;font-weight:800}.reject{margin:0;color:#a63322;font-size:13px}.actions{display:flex;flex-wrap:wrap;align-items:center;gap:12px;margin-top:auto;padding-top:12px;border-top:1px solid var(--line)}.actions a{font-size:13px;font-weight:750;text-decoration:none}.actions button{min-height:36px;padding:0 13px;border:1px solid var(--primary);border-radius:999px;background:#fff;color:var(--primary);font-size:13px;font-weight:750}.actions button:disabled{opacity:.5}.actions span{color:var(--ink-2);font-size:12px}.notice{color:#176444;font-size:13px}.error{color:#a63322;font-size:13px}@media(max-width:700px){header{align-items:start;flex-direction:column;margin-top:30px}.summary{align-items:start;flex-direction:column;gap:12px}.list{grid-template-columns:1fr}}
</style>
