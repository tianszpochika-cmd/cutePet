<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import {
  QUEUE_TABS,
  REJECT_TEMPLATES,
  REVIEW_SHORTCUTS,
  batchEligible,
  canOperateRow,
  queueMatches,
  queueSla,
  queueSort,
  queueStateOf,
  queueTabOf,
  reviewActionBlockers,
  type QueueRow,
  type QueueSlaFilter,
  type QueueStateFilter,
  type QueueTab,
} from '../domain/workbench';

interface DemoRow extends QueueRow {
  code: string;
  version: string;
  onlineVersion: string;
  preview: string;
  current: string;
  source: string;
  risk: string;
  history: string;
  riskLevel: 'LOW' | 'CHECK' | 'HIGH';
}

/** 纯虚构的设计样例；没有管理员身份、真实队列或审核请求。 */
const demoRows: DemoRow[] = [
  { id: 1, code: 'AR-2409', type: 'ARTICLE', title: '幼猫换粮的七个误区', submitter: '样例作者 A', submittedAt: '样例时间', waitedHours: 53, reports: 0, claimedBy: null, version: '待审 v3', onlineVersion: '线上 v2', preview: '作者补充了换粮步骤和观察提示。', current: 'v3 新增诊疗用药相关判断，必须先核对来源。', source: '样例资料：参考依据 2 份', risk: '涉及诊疗判断，专业复核尚未完成。', history: '质量退修 1 次；违规处理 0 次', riskLevel: 'HIGH', batchable: false, requiresProfessionalReview: true },
  { id: 2, code: 'AR-2410', type: 'ARTICLE', title: '周末带狗散步前的准备', submitter: '样例作者 B', submittedAt: '样例时间', waitedHours: 8, reports: 0, claimedBy: null, version: '首次投稿 v1', onlineVersion: '无线上版本', preview: '出门前检查牵引绳、饮水、温度和返程安排。', current: '待审 v1：清单结构与来源说明已填写。', source: '样例资料：参考依据 1 份', risk: '低风险样例；仍须逐篇核对正文。', history: '质量退修 0 次；违规处理 0 次', riskLevel: 'LOW', batchable: true },
  { id: 3, code: 'AR-2411', type: 'ARTICLE', title: '雨天遛宠的装备清单', submitter: '样例作者 C', submittedAt: '样例时间', waitedHours: 6, reports: 0, claimedBy: null, version: '首次投稿 v1', onlineVersion: '无线上版本', preview: '按出门和回家两个阶段整理雨天用品。', current: '待审 v1：内容与频道一致。', source: '样例资料：参考依据 1 份', risk: '低风险样例；可与同类型事项核对批量条件。', history: '质量退修 0 次；违规处理 0 次', riskLevel: 'LOW', batchable: true },
  { id: 4, code: 'LI-2063', type: 'LIST', title: '幼猫用品入门清单', submitter: '样例作者 D', submittedAt: '样例时间', waitedHours: 29, reports: 2, claimedBy: null, version: '待审 v2', onlineVersion: '线上 v1', preview: '新增两件用品和利益关系说明。', current: '待审 v2：商品来源和禁入品类须核查。', source: '样例资料：来源与利益声明待核对', risk: '含举报和商品关联，不能参加低风险批量处理。', history: '质量退修 2 次；违规处理 0 次', riskLevel: 'CHECK', batchable: false },
  { id: 5, code: 'AR-2412', type: 'ARTICLE', title: '养宠家庭的日常整理', submitter: '样例作者 E', submittedAt: '样例时间', waitedHours: 13, reports: 0, claimedBy: 7, version: '待审 v1', onlineVersion: '无线上版本', preview: '整理饮食、清洁和外出备忘。', current: '待审 v1：无真实正文或来源文件。', source: '仅供布局预览的虚构摘要', risk: '样例标为低风险，正式处理仍须读取当前版本。', history: '无可验证的历史记录', riskLevel: 'LOW', batchable: false },
  { id: 6, code: 'CM-6182', type: 'COMMENT', title: '评论疑似包含站外联系方式', submitter: '样例用户 F', submittedAt: '样例时间', waitedHours: 11, reports: 1, claimedBy: 8, version: '评论 v1', onlineVersion: '尚未公开', preview: '机审命中疑似联系方式，等待人工判断。', current: '评论 v1：原文和上下文尚未接入。', source: '样例资料：机审疑似标签', risk: '疑似不等于违规；样例显示由其他人领取。', history: '无可验证的历史记录', riskLevel: 'CHECK', batchable: false },
  { id: 7, code: 'PV-7022', type: 'POI_REVIEW', title: '场所评价抽审', submitter: '样例用户 G', submittedAt: '样例时间', waitedHours: 12, reports: 0, claimedBy: null, version: '评价 v1', onlineVersion: '当前状态待核对', preview: '评价摘要仅用于界面预览。', current: '平台原文与评分联动尚未读取。', source: '样例资料：抽审规则', risk: '隐藏评价可能影响公开分值，须由专门接口确认。', history: '无可验证的历史记录', riskLevel: 'CHECK', batchable: false },
  { id: 8, code: 'RP-4201', type: 'REPORT', title: '路线图片举报', submitter: '样例举报人', submittedAt: '样例时间', waitedHours: 46, reports: 3, claimedBy: null, version: '工单待受理', onlineVersion: '路线当前状态未读取', preview: '举报指向一条公开路线的图片。', current: '需核查证据和对象，不展开私人宠物资料。', source: '样例资料：附件 2 件', risk: '普通举报按 48 小时反馈口径处理，与投稿审核分开。', history: '同对象近 30 天另有样例举报 1 件', riskLevel: 'CHECK', batchable: false },
  { id: 9, code: 'CP-5020', type: 'COPYRIGHT', title: '文章封面版权通知', submitter: '样例权利人', submittedAt: '样例时间', waitedHours: 2, reports: 0, claimedBy: null, version: '通知待核查', onlineVersion: '文章公开状态未读取', preview: '权利材料指向某篇文章的封面。', current: '须核对通知有效性、下架及反通知路径。', source: '样例资料：权利证明摘要', risk: '合格版权通知应立即处理；本页不能执行下架。', history: '首次样例通知', riskLevel: 'HIGH', batchable: false },
  { id: 10, code: 'AP-6113', type: 'APPEAL', title: '评论禁言申诉', submitter: '样例申请人', submittedAt: '样例时间', waitedHours: 51, reports: 0, claimedBy: null, version: '申诉待复核', onlineVersion: '原处置未读取', preview: '申请人对禁言提出异议。', current: '须核对原处置依据并保留受限入口反馈。', source: '样例资料：原处置单号', risk: '申诉结果和通知须独立留痕。', history: '原处置：样例 L2', riskLevel: 'CHECK', batchable: false },
  { id: 11, code: 'AR-2408', type: 'ARTICLE', title: '已处理状态展示样例', submitter: '样例作者 H', submittedAt: '样例时间', waitedHours: 9, reports: 0, claimedBy: null, state: 'HANDLED', version: '历史 v1', onlineVersion: '线上状态未读取', preview: '仅用于检查已处理筛选的布局。', current: '本页没有真实处理结果或审计回执。', source: '无平台数据', risk: '不提供任何再次操作。', history: '无平台记录', riskLevel: 'LOW', batchable: false },
  { id: 12, code: 'RV-2413', type: 'REVIEW', title: '宠物用品体验评测', submitter: '样例作者 I', submittedAt: '样例时间', waitedHours: 18, reports: 0, claimedBy: null, version: '待审 v1', onlineVersion: '无线上版本', preview: '记录一件日常用品的实际使用体验。', current: '需核查商品来源、评测依据与利益关系声明。', source: '样例资料：来源说明 1 份', risk: '评测稿与场所评价抽审分属不同权限队列。', history: '无可验证的历史记录', riskLevel: 'CHECK', batchable: false },
];

const sampleOperatorId = 7;
const typeLabels: Record<string, string> = {
  ARTICLE: '投稿文章', REVIEW: '评测投稿', LIST: '好物清单', COMMENT: '评论疑似',
  POI_REVIEW: '场所评价', REPORT: '举报工单', COPYRIGHT: '版权通知', APPEAL: '处置申诉',
};
const stateOptions: { id: QueueStateFilter; label: string }[] = [
  { id: 'ALL', label: '全部' }, { id: 'UNCLAIMED', label: '待领取' },
  { id: 'MINE', label: '样例已领取' }, { id: 'OTHER', label: '他人处理中' },
  { id: 'HANDLED', label: '已处理样例' },
];

const rootEl = ref<HTMLElement | null>(null);
const detailEl = ref<HTMLElement | null>(null);
const searchEl = ref<HTMLInputElement | null>(null);
const noteEl = ref<HTMLTextAreaElement | null>(null);
const approveEl = ref<HTMLButtonElement | null>(null);
const batchDialogEl = ref<HTMLDialogElement | null>(null);
const tab = ref<QueueTab>('SUBMISSIONS');
const stateFilter = ref<QueueStateFilter>('UNCLAIMED');
const typeFilter = ref('ALL');
const slaFilter = ref<QueueSlaFilter>('ALL');
const query = ref('');
const activeId = ref<number | null>(null);
const selected = ref<number[]>([]);
const notes = reactive<Record<number, string>>({});
const dispositions = reactive<Record<number, 'quality' | 'violation'>>({});
const message = ref('');
const showShortcuts = ref(false);
const batchAction = ref<'approve' | 'reject'>('approve');
const batchTemplate = ref('');
const batchAcknowledged = ref(false);
let lastRowTrigger: HTMLElement | null = null;
let batchTrigger: HTMLElement | null = null;

const active = computed(() => demoRows.find((row) => row.id === activeId.value) ?? null);
const note = computed({
  get: () => activeId.value === null ? '' : notes[activeId.value] ?? '',
  set: (value: string) => { if (activeId.value !== null) notes[activeId.value] = value; },
});
const disposition = computed({
  get: () => activeId.value === null ? 'quality' : dispositions[activeId.value] ?? 'quality',
  set: (value: 'quality' | 'violation') => { if (activeId.value !== null) dispositions[activeId.value] = value; },
});
const availableTypes = computed(() => [...new Set(demoRows.filter((row) => queueTabOf(row) === tab.value).map((row) => row.type))]);
const visibleRows = computed(() => queueSort(demoRows.filter((row) => queueMatches(row, {
  tab: tab.value, state: stateFilter.value, type: typeFilter.value, sla: slaFilter.value,
  query: query.value, sampleOperatorId,
}))) as DemoRow[]);
const selectedRows = computed(() => demoRows.filter((row) => selected.value.includes(row.id)));
const canBatch = computed(() => selectedRows.value.length === selected.value.length && batchEligible(selectedRows.value, true));
const tabRows = computed(() => demoRows.filter((row) => queueTabOf(row) === tab.value));
const sampleCounts = computed(() => ({
  unclaimed: tabRows.value.filter((row) => queueStateOf(row, sampleOperatorId) === 'UNCLAIMED').length,
  breach: tabRows.value.filter((row) => row.state !== 'HANDLED' && queueSla(row).tier === 'BREACH').length,
  escalate: tabRows.value.filter((row) => row.state !== 'HANDLED' && ['ESCALATE', 'URGENT'].includes(queueSla(row).tier)).length,
  mine: tabRows.value.filter((row) => queueStateOf(row, sampleOperatorId) === 'MINE').length,
}));

watch([tab, stateFilter, typeFilter, slaFilter, query], () => {
  selected.value = [];
  if (activeId.value !== null && !visibleRows.value.some((row) => row.id === activeId.value)) activeId.value = null;
  message.value = '';
});

function pickTab(next: QueueTab) {
  tab.value = next;
  typeFilter.value = 'ALL';
  stateFilter.value = 'UNCLAIMED';
  slaFilter.value = 'ALL';
}
function openRow(row: DemoRow, event?: Event) {
  if (event?.currentTarget instanceof HTMLElement) lastRowTrigger = event.currentTarget;
  activeId.value = row.id;
  message.value = '';
  void nextTick(() => detailEl.value?.focus());
}
function closeDetail() {
  activeId.value = null;
  message.value = '';
  void nextTick(() => lastRowTrigger?.isConnected && lastRowTrigger.focus());
}
function toggleSelect(row: DemoRow) {
  if (!batchEligible([row], true)) return;
  if (selected.value.includes(row.id)) {
    selected.value = selected.value.filter((id) => id !== row.id);
    return;
  }
  if (selectedRows.value.length && selectedRows.value[0]?.type !== row.type) {
    message.value = '批量核对只能选择同一类型的低风险事项。';
    return;
  }
  selected.value = [...selected.value, row.id];
  message.value = '';
}
function previewClaim() {
  if (!active.value) return;
  const state = queueStateOf(active.value, sampleOperatorId);
  message.value = state === 'UNCLAIMED'
    ? active.value.code + ' 仅符合样例待领取条件。真实领取须由平台验证管理员身份、权限、当前版本和跨人锁；本页未领取。'
    : '此样例当前并非待领取状态。本页不会改动处理锁。';
}
function previewAction(action: 'approve' | 'reject') {
  const row = active.value;
  if (!row) return;
  if (queueTabOf(row) !== 'SUBMISSIONS') {
    message.value = '此队列需要独立的处理动作与权限，本页不复用投稿通过或驳回。';
    return;
  }
  if (row.state === 'HANDLED') { message.value = '已处理样例只可查看，本页不会重复执行。'; return; }
  const lock = canOperateRow(row, sampleOperatorId);
  if (!lock.allowed) { message.value = lock.reason + '（样例，未执行）'; return; }
  if (row.claimedBy !== sampleOperatorId) {
    message.value = '需要先取得平台确认的处理锁并重新读取当前版本。本页没有真实领取能力，未执行审核。';
    return;
  }
  if (action === 'approve' && row.requiresProfessionalReview) {
    message.value = '专业复核尚未完成，不能通过此版本；本页未执行审核。';
    return;
  }
  const blockers = reviewActionBlockers(action, note.value);
  if (blockers.length) {
    message.value = blockers.includes('REJECT_NOTE_REQUIRED') ? '驳回需填写具体意见；本页未执行审核。' : '意见最多 500 字；本页未执行审核。';
    if (action === 'reject') void nextTick(() => noteEl.value?.focus());
    return;
  }
  if (action === 'reject' && disposition.value === 'violation') {
    message.value = '人工确认违规还需可核对证据、独立权限和处置范围；本页无法完成该条件检查，未执行审核。';
    return;
  }
  message.value = row.code + ' 的' + (action === 'approve' ? '通过' : '驳回') +
    '输入已通过本地条件检查。正式提交还须重新核验身份、锁、当前版本及审计回执；样例状态未改变。';
}
function openBatch(action: 'approve' | 'reject', event: Event) {
  if (!canBatch.value) {
    message.value = '只可核对同类型、待处理、低风险、零举报且未领取的事项。';
    return;
  }
  batchAction.value = action;
  batchTemplate.value = '';
  batchAcknowledged.value = false;
  batchTrigger = event.currentTarget instanceof HTMLElement ? event.currentTarget : null;
  batchDialogEl.value?.showModal();
}
function closeBatch() { batchDialogEl.value?.close(); }
function afterBatchClose() { void nextTick(() => batchTrigger?.isConnected && batchTrigger.focus()); }
function confirmBatchPreview() {
  if (!canBatch.value) { message.value = '样例选择已变化，请重新核对范围。'; closeBatch(); return; }
  if (batchAction.value === 'reject' && !batchTemplate.value) { message.value = '批量驳回须选择统一意见模板。'; return; }
  if (!batchAcknowledged.value) { message.value = '请先核对数量、类型与影响。'; return; }
  message.value = '已核对 ' + selectedRows.value.length + ' 条' + (typeLabels[selectedRows.value[0]!.type] ?? '事项') +
    '的批量' + (batchAction.value === 'approve' ? '通过' : '驳回') + '条件。未提交、未处理，样例状态不变。';
  closeBatch();
}
function focusVisibleRow(direction: -1 | 1) {
  if (!visibleRows.value.length) return;
  const current = visibleRows.value.findIndex((row) => row.id === activeId.value);
  const next = current < 0
    ? direction === 1 ? 0 : visibleRows.value.length - 1
    : (current + direction + visibleRows.value.length) % visibleRows.value.length;
  const row = visibleRows.value[next]!;
  activeId.value = row.id;
  void nextTick(() => rootEl.value?.querySelector<HTMLElement>('[data-row-id="' + row.id + '"]')?.focus());
}
function onKeydown(event: KeyboardEvent) {
  if (event.altKey || event.ctrlKey || event.metaKey || event.isComposing || batchDialogEl.value?.open) return;
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  if (event.key === 'Escape' && rootEl.value?.contains(target)) {
    if (showShortcuts.value) showShortcuts.value = false;
    else if (activeId.value !== null) closeDetail();
    return;
  }
  if (target.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) return;
  if (event.key === '/') { event.preventDefault(); searchEl.value?.focus(); return; }
  if (event.key === '?') { event.preventDefault(); showShortcuts.value = true; return; }
  if (!rootEl.value?.contains(target)) return;
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault();
    focusVisibleRow(event.key === 'ArrowDown' ? 1 : -1);
    return;
  }
  if (event.key === 'Enter' && target.hasAttribute('data-row-id')) {
    const row = visibleRows.value.find((item) => String(item.id) === target.getAttribute('data-row-id'));
    if (row) { event.preventDefault(); lastRowTrigger = target; openRow(row); }
    return;
  }
  const shortcut = REVIEW_SHORTCUTS[event.key.toLowerCase()];
  if (shortcut === 'approve' && active.value) { event.preventDefault(); approveEl.value?.focus(); }
  if (shortcut === 'reject' && active.value) { event.preventDefault(); noteEl.value?.focus(); }
}
onMounted(() => window.addEventListener('keydown', onKeydown));
onUnmounted(() => window.removeEventListener('keydown', onKeydown));
</script>

<template>
  <div ref="rootEl" class="bench">
    <header class="page-head">
      <div><p class="eyebrow">REVIEW DESK · INTERFACE PREVIEW</p><h1>审核工作台</h1><p>先定位待处理对象，再核对当前版本、风险与处理范围。</p></div>
      <span class="preview-pill">只读样例</span>
    </header>
    <div class="boundary" role="status">
      <strong>当前为只读界面预览</strong>
      <span>下方人物、内容、时长和状态均为虚构样例。没有真实管理员身份、权限、审核队列或跨人锁；检查按钮只核对本地输入，不会领取、审核、发布或写入审计。</span>
    </div>
    <div class="desktop-note" role="status"><strong>请在电脑端使用审核工作台</strong><p>此页面按宽屏列表与详情并排设计。请使用更宽的电脑窗口查看完整样例。</p></div>

    <div class="desktop-content">
      <section class="stats" aria-label="当前样例队列概况">
        <div><span>待领取样例</span><strong>{{ sampleCounts.unclaimed }}</strong></div>
        <div><span>超 24 小时样例</span><strong>{{ sampleCounts.breach }}</strong></div>
        <div><span>超时 / 立即样例</span><strong>{{ sampleCounts.escalate }}</strong></div>
        <div><span>样例审核员 #7 领取</span><strong>{{ sampleCounts.mine }}</strong></div>
      </section>

      <nav class="queue-tabs" aria-label="审核队列类型">
        <button v-for="item in QUEUE_TABS" :key="item.id" type="button" :class="{ active: tab === item.id }" :aria-current="tab === item.id ? 'page' : undefined" :data-testid="'tab-' + item.id" @click="pickTab(item.id)">{{ item.label }}</button>
      </nav>

      <div class="workspace">
        <section class="queue-panel" aria-labelledby="queue-title">
          <div class="panel-head"><div><p class="eyebrow">QUEUE · SAMPLE DATA</p><h2 id="queue-title">统一待办队列</h2></div><span>{{ visibleRows.length }} 条符合筛选的样例</span></div>
          <p class="panel-note">按各类时限、举报数和等待时长排序。版权通知、普通举报与投稿采用不同的处理时限。</p>
          <div class="state-tabs" role="group" aria-label="处理状态">
            <button v-for="item in stateOptions" :key="item.id" type="button" :class="{ active: stateFilter === item.id }" :aria-pressed="stateFilter === item.id" @click="stateFilter = item.id">{{ item.label }}</button>
          </div>
          <div class="filters">
            <label>搜索标题或编号<input ref="searchEl" v-model="query" type="search" placeholder="按 / 快速聚焦" /></label>
            <label>类型<select v-model="typeFilter"><option value="ALL">全部类型</option><option v-for="kind in availableTypes" :key="kind" :value="kind">{{ typeLabels[kind] ?? kind }}</option></select></label>
            <label>时限<select v-model="slaFilter"><option value="ALL">全部时限</option><option value="URGENT">立即核查</option><option value="ESCALATE">已超 48 小时</option><option value="BREACH">已超 24 小时</option><option value="OK">目标时限内</option><option value="UNKNOWN">时限待核对</option></select></label>
          </div>
          <div class="table-wrap">
            <table aria-label="虚构审核事项队列"><thead><tr><th scope="col">批量</th><th scope="col">类型 / 编号</th><th scope="col">标题 / 当前版本</th><th scope="col">样例提交者</th><th scope="col">时限</th><th scope="col">处理状态</th></tr></thead>
              <tbody><tr v-for="row in visibleRows" :key="row.id" :data-row-id="row.id" :data-testid="'row-' + row.id" :aria-label="'查看样例 ' + row.code + ' ' + row.title" :class="{ active: activeId === row.id }" tabindex="0" @click="openRow(row, $event)">
                <td @click.stop><input type="checkbox" :aria-label="'选择样例 ' + row.code" :checked="selected.includes(row.id)" :disabled="!batchEligible([row], true) || (selectedRows.length > 0 && selectedRows[0]?.type !== row.type && !selected.includes(row.id))" @change="toggleSelect(row)" /></td>
                <td><strong>{{ typeLabels[row.type] ?? row.type }}</strong><small>{{ row.code }}</small></td>
                <td><strong>{{ row.title }}</strong><small>{{ row.version }}<span v-if="row.reports"> · {{ row.reports }} 件举报</span></small></td>
                <td>{{ row.submitter }}</td>
                <td><span :class="['sla', queueSla(row).tier.toLowerCase()]">{{ row.waitedHours }}h · {{ queueSla(row).label }}</span></td>
                <td>{{ stateOptions.find((item) => item.id === queueStateOf(row, sampleOperatorId))?.label }}</td>
              </tr></tbody>
            </table>
          </div>
          <div v-if="!visibleRows.length" class="empty"><strong>{{ tabRows.length ? '此筛选下暂无样例事项' : '此队列暂无样例事项' }}</strong><p>可换状态、类型或搜索词继续查看。此处不代表真实待办数量。</p></div>
          <div class="queue-foot"><span>{{ selected.length ? '已选 ' + selected.length + ' 条样例；仅可核对同类型低风险事项' : '勾选同类型、零举报且未领取的样例，查看批量核对步骤' }}</span><div><button type="button" class="secondary" :disabled="!canBatch" @click="openBatch('reject', $event)">批量驳回条件</button><button type="button" class="primary" :disabled="!canBatch" data-testid="batch" @click="openBatch('approve', $event)">批量通过条件</button></div></div>
        </section>

        <aside ref="detailEl" class="detail-panel" tabindex="-1" aria-label="样例审核详情" data-testid="drawer">
          <div v-if="!active" class="detail-empty"><span aria-hidden="true">◫</span><h2>选择一条事项</h2><p>这里会展开当前版本、原版本、风险与处理条件。键盘可用 ↑↓ 切换队列行、Enter 打开。</p></div>
          <template v-else>
            <div class="detail-top"><div><span class="eyebrow">{{ active.code }} · {{ typeLabels[active.type] ?? active.type }}</span><h2>{{ active.title }}</h2></div><button type="button" class="close" aria-label="关闭详情" @click="closeDetail">×</button></div>
            <p class="detail-sub">{{ active.version }} · 样例等待 {{ active.waitedHours }} 小时</p>
            <dl class="meta-grid"><div><dt>提交者</dt><dd>{{ active.submitter }}</dd></div><div><dt>处理标识</dt><dd>{{ active.claimedBy === null ? '未领取样例' : '样例审核员 #' + active.claimedBy }}</dd></div><div><dt>时限</dt><dd>{{ queueSla(active).label }}</dd></div><div><dt>举报数</dt><dd>{{ active.reports }} 件</dd></div></dl>
            <section class="detail-section"><h3>01 · 当前提交版本</h3><div class="preview-card"><strong>{{ active.version }}</strong><p>{{ active.preview }}</p><p>{{ active.current }}</p></div><div class="version-pair"><div><span>线上 / 原版本</span><strong>{{ active.onlineVersion }}</strong></div><div><span>本次提交</span><strong>{{ active.version }}</strong></div></div><p class="source">{{ active.source }}</p><p class="boundary-text">真实审核必须读取并固定服务端当前版本。此处只有虚构摘要，不显示真实正文或附件。</p></section>
            <section class="detail-section"><h3>02 · 风险与历史</h3><p :class="['risk', active.riskLevel.toLowerCase()]">{{ active.risk }}</p><p class="history">{{ active.history }}</p><p class="boundary-text">私人健康资料不因进入普通审核队列而开放。</p></section>
            <section class="detail-section operation"><h3>03 · 领取与审核条件</h3>
              <p v-if="active.state === 'HANDLED'" class="lock-note">此条只展示“已处理”筛选样式。没有真实结果或审计记录，不提供再次操作。</p>
              <p v-else-if="queueTabOf(active) !== 'SUBMISSIONS'" class="lock-note">{{ typeLabels[active.type] ?? active.type }}有独立的处理动作、对象权限和结果回执。此只读样例只展示队列与核对范围，不提供通用投稿通过、驳回或领取操作。</p>
              <template v-else>
                <p v-if="active.claimedBy !== null && active.claimedBy !== sampleOperatorId" class="lock-note">样例显示由其他审核员处理中。正式页面应阻止旧状态提交，并重新查询服务端。</p>
                <p v-else-if="active.claimedBy === null" class="lock-note">尚未领取。领取前须重新验证管理员权限、对象状态和跨人锁。</p>
                <p v-else class="lock-note">样例标识为 #7 已领取；这不代表当前访问者拥有处理锁。</p>
                <button v-if="active.claimedBy === null" type="button" class="secondary full" data-testid="claim" @click="previewClaim">查看领取条件 · 不领取</button>
                <label class="note-label" for="review-note">审核意见（只保留在此页面）</label>
                <textarea id="review-note" ref="noteEl" v-model="note" rows="4" maxlength="500" placeholder="驳回请写明具体原因；此输入不会发送到平台" data-testid="note" />
                <div class="templates"><button v-for="template in REJECT_TEMPLATES" :key="template" type="button" @click="note = template">{{ template }}</button></div>
                <label class="note-label" for="disposition">驳回性质</label><select id="disposition" v-model="disposition"><option value="quality">质量退修 · 不计违规</option><option value="violation">人工确认违规 · 需独立证据</option></select>
                <p class="boundary-text">以下按钮只检查本地填写条件，不会调用审核接口、改变版本或发送通知。</p>
                <div class="action-row"><button ref="approveEl" type="button" class="primary" data-testid="approve" @click="previewAction('approve')">检查通过条件（A）</button><button type="button" class="danger" data-testid="reject" @click="previewAction('reject')">检查驳回条件（R）</button></div>
              </template>
            </section>
          </template>
        </aside>
      </div>

      <p v-if="message" class="message" role="status" data-testid="message">{{ message }}</p>
      <section class="rules"><div><h2>审核边界</h2><p>修改稿须核对当前版本；质量退修与人工确认违规分开。领取锁、状态、权限和专业复核由平台在提交前再次判断。</p></div><div><h2>键盘操作</h2><p><kbd>/</kbd> 搜索　<kbd>↑↓</kbd> 切换　<kbd>Enter</kbd> 打开　<kbd>A</kbd> 聚焦通过条件　<kbd>R</kbd> 聚焦意见　<kbd>Esc</kbd> 关闭</p><button type="button" class="text-button" @click="showShortcuts = !showShortcuts">{{ showShortcuts ? '收起说明' : '查看说明' }}</button><p v-if="showShortcuts">输入框中快捷键不触发；A 和 R 只移动焦点，不执行审核。离开页面后键盘监听会清理。</p></div></section>
    </div>

    <dialog ref="batchDialogEl" class="batch-dialog" aria-labelledby="batch-title" @close="afterBatchClose">
      <div class="dialog-body"><p class="eyebrow">BATCH · CONDITION CHECK</p><h2 id="batch-title">批量{{ batchAction === 'approve' ? '通过' : '驳回' }}条件核对</h2><p>仅预览二次确认步骤，以下样例不会提交平台或改变状态。</p>
        <div class="batch-summary"><strong>{{ selectedRows.length }} 条 {{ typeLabels[selectedRows[0]?.type ?? ''] ?? '事项' }}</strong><span>编号：{{ selectedRows.map((row) => row.code).join('、') }}</span><span>范围：同类型、待处理、零举报、未领取、无专业复核阻塞</span><span>影响：正式执行前还须逐项确认当前版本与服务端回执</span></div>
        <label v-if="batchAction === 'reject'" class="dialog-field">统一驳回意见<select v-model="batchTemplate"><option value="">选择模板</option><option v-for="template in REJECT_TEMPLATES" :key="template" :value="template">{{ template }}</option></select></label>
        <label class="ack"><input v-model="batchAcknowledged" type="checkbox" />我已核对样例数量、类型与可能影响；此处不执行处理</label>
        <div class="dialog-actions"><button type="button" class="secondary" @click="closeBatch">返回队列</button><button type="button" class="primary" :disabled="!batchAcknowledged || (batchAction === 'reject' && !batchTemplate)" @click="confirmBatchPreview">完成条件核对 · 不提交</button></div>
      </div>
    </dialog>
  </div>
</template>

<style scoped>
.bench{--ink:#2f241c;--muted:#705e50;--line:#e8dcd0;--orange:#b85111;color:var(--ink);display:grid;gap:17px;max-width:1600px;margin:auto}.page-head{display:flex;justify-content:space-between;align-items:center;gap:20px}.page-head h1{font-size:clamp(29px,3vw,40px);line-height:1.15;margin:0}.page-head p:last-child{margin:9px 0 0;color:var(--muted);font-size:14px}.eyebrow{display:block;margin:0 0 8px;color:var(--orange);font-size:11px;font-weight:800;letter-spacing:.13em}.preview-pill{flex:none;padding:8px 12px;border-radius:999px;background:#f4e7d9;color:#81410e;font-size:12px;font-weight:800}.boundary{display:flex;gap:15px;align-items:start;padding:15px 18px;border:1px solid #e8cda9;border-radius:14px;background:#fff7e9;color:#72431a;font-size:13px;line-height:1.65}.boundary strong{flex:none;color:#723508}.desktop-content{display:grid;gap:17px}.desktop-note{display:none}.stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:11px}.stats>div{display:grid;gap:9px;padding:16px 18px;border:1px solid var(--line);border-radius:15px;background:#fff}.stats span{color:var(--muted);font-size:12px}.stats strong{font-size:27px;line-height:1;color:#713509}.queue-tabs{display:flex;gap:7px;overflow:auto;padding-bottom:2px}.queue-tabs button,.state-tabs button{min-height:41px;flex:none;padding:0 15px;border:1px solid var(--line);border-radius:9px;background:#fff;color:#5e4e41;font-size:12px;font-weight:700;cursor:pointer}.queue-tabs button.active,.state-tabs button.active{border-color:var(--orange);background:var(--orange);color:#fff}.workspace{display:grid;grid-template-columns:minmax(0,1.6fr) minmax(330px,.92fr);align-items:start;gap:14px}.queue-panel,.detail-panel,.rules>div{border:1px solid var(--line);border-radius:17px;background:#fff}.queue-panel{min-width:0;overflow:hidden}.panel-head{display:flex;justify-content:space-between;align-items:end;gap:15px;padding:19px 20px 0}.panel-head h2{margin:0;font-size:20px}.panel-head>span{color:var(--muted);font-size:12px}.panel-note{margin:8px 20px 15px;color:var(--muted);font-size:12px;line-height:1.6}.state-tabs{display:flex;gap:6px;overflow:auto;margin:0 20px;padding-bottom:12px}.state-tabs button{min-height:36px;padding:0 11px;border-radius:999px}.filters{display:grid;grid-template-columns:minmax(150px,1.3fr) minmax(125px,.8fr) minmax(135px,.8fr);gap:9px;padding:13px 20px;background:#fff9f3;border-top:1px solid var(--line);border-bottom:1px solid var(--line)}.filters label,.dialog-field{display:grid;gap:5px;color:#5c4d42;font-size:11px;font-weight:700}.filters input,.filters select,.dialog-field select{width:100%;min-height:40px;padding:0 10px;border:1px solid #d9cabd;border-radius:8px;background:#fff;color:var(--ink);font-size:13px}.table-wrap{overflow:auto}table{width:100%;min-width:760px;border-collapse:collapse}th{padding:10px 11px;background:#fffbf6;color:#76675b;text-align:left;font-size:11px;font-weight:750;white-space:nowrap}td{padding:12px 11px;border-top:1px solid #f1e8df;color:#584b40;font-size:12px;vertical-align:middle}tbody tr{cursor:pointer}tbody tr:hover,tbody tr.active{background:#fff2e6}td strong,td small{display:block}td strong{color:var(--ink);font-size:12px}td small{margin-top:4px;color:#806e5e;font-size:11px}td input{width:18px;height:18px;accent-color:var(--orange)}.sla{display:inline-block;padding:5px 8px;border-radius:999px;font-size:11px;font-weight:750;white-space:nowrap}.sla.ok{background:#eaf6ed;color:#1d6b3c}.sla.breach{background:#fff0d8;color:#8d510b}.sla.escalate{background:#f5e9f5;color:#7f377c}.sla.urgent{background:#fde9e9;color:#a62d2d}.empty{padding:29px 20px;text-align:center}.empty strong{font-size:15px}.empty p{margin:7px 0 0;color:var(--muted);font-size:12px}.queue-foot{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:14px 20px;border-top:1px solid var(--line)}.queue-foot span{color:var(--muted);font-size:11px;line-height:1.5}.queue-foot>div{display:flex;gap:7px}.primary,.secondary,.danger{min-height:40px;padding:8px 13px;border:1px solid transparent;border-radius:9px;font-size:12px;font-weight:750;cursor:pointer}.primary{background:var(--orange);color:#fff}.secondary{border-color:#dac9b9;background:#fff;color:#79420f}.danger{border-color:#e9c3c3;background:#fff0f0;color:#9d2929}button:disabled{opacity:.48;cursor:not-allowed}.detail-panel{min-width:0;display:grid;gap:16px;padding:20px;outline:none}.detail-empty{display:grid;justify-items:center;align-content:center;min-height:330px;text-align:center;color:var(--muted)}.detail-empty>span{font-size:45px}.detail-empty h2{margin:12px 0 0;color:var(--ink);font-size:20px}.detail-empty p{max-width:260px;line-height:1.7;font-size:12px}.detail-top{display:flex;align-items:start;justify-content:space-between;gap:10px}.detail-top h2{margin:0;font-size:20px;line-height:1.4}.close{width:40px;height:40px;flex:none;border:0;border-radius:9px;background:#f6ede5;color:#694a34;font-size:24px;cursor:pointer}.detail-sub{margin:-8px 0 0;color:var(--muted);font-size:12px}.meta-grid{display:grid;grid-template-columns:1fr 1fr;gap:9px;margin:0}.meta-grid>div{padding:11px;border-radius:10px;background:#faf6f1}.meta-grid dt{color:var(--muted);font-size:11px}.meta-grid dd{margin:5px 0 0;font-size:12px;font-weight:750}.detail-section{display:grid;gap:10px;padding-top:17px;border-top:1px solid var(--line)}.detail-section h3{margin:0;font-size:14px}.preview-card{padding:13px;border-radius:10px;background:#fff8f0;font-size:12px;line-height:1.7}.preview-card p{margin:5px 0 0}.version-pair{display:grid;grid-template-columns:1fr 1fr;gap:8px}.version-pair>div{display:grid;gap:4px;padding:11px;border:1px solid var(--line);border-radius:10px}.version-pair span,.source,.history,.boundary-text{color:var(--muted);font-size:11px;line-height:1.6}.version-pair strong{font-size:12px}.source,.history,.boundary-text{margin:0}.risk{margin:0;padding:11px;border-radius:10px;font-size:12px;line-height:1.6}.risk.low{background:#eaf7ed;color:#206842}.risk.check{background:#fff1dd;color:#80500f}.risk.high{background:#fdeaea;color:#a12b2b}.lock-note{margin:0;padding:11px;border-radius:10px;background:#f7f1eb;color:#6e4c34;font-size:12px;line-height:1.6}.full{width:100%}.note-label{font-size:12px;font-weight:750}.operation textarea,.operation select{width:100%;min-height:40px;padding:10px;border:1px solid #d9cabd;border-radius:9px;background:#fff;color:var(--ink);font:inherit;font-size:12px}.operation textarea{resize:vertical}.templates{display:flex;flex-wrap:wrap;gap:6px}.templates button{min-height:35px;padding:5px 9px;border:1px solid var(--line);border-radius:999px;background:#fff8f2;color:#79512f;font-size:11px;cursor:pointer}.action-row{display:grid;grid-template-columns:1fr 1fr;gap:8px}.message{margin:0;padding:14px 17px;border:1px solid #d5dfe9;border-radius:11px;background:#eff6fc;color:#265078;font-size:12px;line-height:1.6}.rules{display:grid;grid-template-columns:1fr 1fr;gap:12px}.rules>div{padding:18px}.rules h2{margin:0 0 8px;font-size:14px}.rules p{margin:0;color:var(--muted);font-size:12px;line-height:1.7}.rules p+p{margin-top:10px}.text-button{min-height:36px;margin-top:5px;padding:0;border:0;background:none;color:var(--orange);font-size:12px;font-weight:750;cursor:pointer}kbd{display:inline-block;min-width:22px;padding:2px 5px;border:1px solid var(--line);border-radius:4px;background:#fff8f0;text-align:center}.batch-dialog{width:min(560px,calc(100% - 36px));max-height:calc(100vh - 40px);padding:0;border:1px solid var(--line);border-radius:18px;box-shadow:0 25px 70px #2f241c33}.batch-dialog::backdrop{background:#2f241c99}.dialog-body{display:grid;gap:14px;padding:26px}.dialog-body h2{margin:-7px 0 0;font-size:23px}.dialog-body>p:not(.eyebrow){margin:0;color:var(--muted);font-size:13px;line-height:1.6}.batch-summary{display:grid;gap:8px;padding:15px;border-radius:11px;background:#fff7ed;font-size:12px;line-height:1.55}.batch-summary strong{font-size:16px}.ack{display:flex;align-items:start;gap:9px;color:#55473c;font-size:12px;line-height:1.6}.ack input{width:18px;height:18px;flex:none;accent-color:var(--orange)}.dialog-actions{display:flex;justify-content:flex-end;gap:8px}button:focus-visible,input:focus-visible,select:focus-visible,textarea:focus-visible,tr:focus-visible,.detail-panel:focus-visible{outline:3px solid #7c380d;outline-offset:2px}@media(max-width:1400px){.workspace{grid-template-columns:minmax(0,1.35fr) minmax(310px,.9fr)}.filters{grid-template-columns:1fr 1fr}.filters label:first-child{grid-column:1/-1}.queue-foot{align-items:stretch;flex-direction:column}}@media(max-width:1100px){.desktop-note{display:block;padding:30px;border:1px solid var(--line);border-radius:16px;background:#fff}.desktop-note strong{font-size:19px}.desktop-note p{color:var(--muted);line-height:1.7}.desktop-content{display:none}.boundary{flex-direction:column}}@media(prefers-reduced-motion:reduce){*{scroll-behavior:auto!important;transition:none!important}}
.sla.unknown{background:#f1eeea;color:#65584d}
</style>
