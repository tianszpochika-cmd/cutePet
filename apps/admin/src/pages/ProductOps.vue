<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { api } from '@cutepet/api-client';
import { csvImportSummary, parseProductCsv, type CsvParseResult } from '../domain/operations';

interface ProductRow { id: number; name: string; brand?: string; category: string; state: string; priceMin?: number | null; priceMax?: number | null }
const products = ref<ProductRow[]>([]);
const total = ref(0);
const loading = ref(true);
const error = ref('');
const filter = ref('');
const selectedId = ref<number | null>(null);
const csvText = ref('');
const preview = ref<CsvParseResult | null>(null);
const csvError = ref('');
const visibleProducts = computed(() => products.value.filter((row) => `${row.name} ${row.brand ?? ''} ${row.category}`.toLowerCase().includes(filter.value.trim().toLowerCase())));
const selected = computed(() => products.value.find((row) => row.id === selectedId.value) ?? null);

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const result = (await api.productsList()) as { items?: ProductRow[]; total?: number };
    if (!result || !Array.isArray(result.items)) throw new Error('公开商品数据格式暂时不可用');
    products.value = result.items.filter((row) => typeof row.id === 'number' && row.state === 'ON_SHELF');
    total.value = typeof result.total === 'number' ? result.total : products.value.length;
    if (!products.value.some((row) => row.id === selectedId.value)) selectedId.value = null;
  } catch (cause) {
    products.value = [];
    total.value = 0;
    selectedId.value = null;
    error.value = cause instanceof Error ? cause.message : '公开商品暂时无法读取';
  } finally { loading.value = false; }
}
onMounted(load);
function inspectCsv() {
  csvError.value = '';
  if (!csvText.value.trim()) { preview.value = null; csvError.value = '先粘贴 CSV 内容'; return; }
  preview.value = parseProductCsv(csvText.value);
}
</script>

<template>
  <div class="ops-page">
    <div class="desktop-notice">商品运营请在电脑端查看。</div>
    <div class="workspace">
      <header class="page-head"><div><p class="eyebrow">CATALOG OPERATIONS</p><h1>商品与导入</h1><p>维护可供读者比较的商品资料，核对来源、品类和历史引用。</p></div><span class="preview-badge">只读界面预览</span></header>
      <div class="boundary"><strong>当前入口尚未连接管理员身份</strong><p>下方仅显示公开在架商品；下架、治理清除和已停用商品不在这个列表里。导入预检仅在浏览器内检查文本，不会新增或修改平台商品。</p></div>
      <div class="workgrid">
        <section class="panel"><div class="panel-head"><div><h2>公开商品库</h2><p>当前在架 · {{ total }} 件</p></div><button type="button" class="subtle" :disabled="loading" @click="load">刷新</button></div><div class="toolbar"><label class="search-label">筛选当前列表<input v-model="filter" placeholder="名称、品牌或类目" /></label><span>仅筛选本次读取结果</span></div>
          <div v-if="loading" class="state" role="status">正在读取公开商品…</div><div v-else-if="error" class="state error" role="alert"><strong>读取失败</strong><p>{{ error }}</p><button type="button" @click="load">重试</button></div><div v-else-if="!products.length" class="state"><strong>当前没有公开在架商品</strong><p>这不代表后台商品库为空。</p></div><div v-else-if="!visibleProducts.length" class="state"><strong>当前筛选没有匹配商品</strong><p>试试更短的名称或清空筛选词。</p></div>
          <div v-else class="table-scroll"><table><thead><tr><th>商品</th><th>类目</th><th>价格参考</th><th>状态</th><th>查看</th></tr></thead><tbody><tr v-for="row in visibleProducts" :key="row.id" :class="{ selected: selectedId === row.id }"><td><strong>{{ row.name }}</strong><small>#{{ row.id }} · {{ row.brand || '未标品牌' }}</small></td><td>{{ row.category }}</td><td>{{ row.priceMin != null && row.priceMax != null ? `¥${row.priceMin}–${row.priceMax}` : '—' }}</td><td><span class="status live">在架</span></td><td><button type="button" class="select" :aria-pressed="selectedId === row.id" @click="selectedId = row.id">核对影响</button></td></tr></tbody></table></div>
        </section>
        <aside class="detail"><p class="eyebrow">ITEM SCOPE</p><template v-if="selected"><h2>{{ selected.name }}</h2><p>商品 #{{ selected.id }} · {{ selected.category }} · 当前公开在架</p><div class="divider"></div><h3>上下架前需要核对</h3><ul><li>真实运营身份与对应权限点</li><li>商品来源、禁用品类和当前状态</li><li>评测、清单和收藏中的历史引用</li><li>普通下架或治理清除的不同传播范围</li><li>清理失败待办与审计结果</li></ul><p class="no-action">当前没有可核验的后台状态与回执，不提供上下架操作。</p></template><template v-else><h2>选择一个商品</h2><p>在左侧选择公开商品，查看上架、下架和治理前的核对范围。</p></template></aside>
      </div>
      <section class="panel csv-panel"><div class="panel-head"><div><h2>CSV 导入 · 本地预检</h2><p>列顺序：name,category,sourceType；支持带引号的逗号，不支持跨行单元格。</p></div><span class="permission">product.import.csv</span></div><div class="csv-layout"><div><label class="field">粘贴 CSV 文本<textarea v-model="csvText" rows="8" spellcheck="false" data-testid="csv" placeholder="name,category,sourceType&#10;逗猫球,玩具,EDITORIAL" @input="preview = null; csvError = ''" /></label><button type="button" class="primary" data-testid="csv-preview" @click="inspectCsv">运行本地预检</button><p v-if="csvError" class="error" role="alert">{{ csvError }}</p><p class="muted">预检只帮助发现明显格式、来源与禁用品类问题。正式导入仍需管理员权限及服务端逐行复检。</p></div><div class="csv-results"><template v-if="preview"><strong data-testid="csv-result">{{ csvImportSummary(preview) }}</strong><ul><li v-for="row in preview.accepted" :key="`ok-${row.line}`" class="accepted">第 {{ row.line }} 行 · {{ row.name }} · 本地规则通过</li><li v-for="row in preview.rejected" :key="`no-${row.line}`" class="rejected">第 {{ row.line }} 行 · {{ row.reason }}</li></ul></template><template v-else><strong>等待预检</strong><p>粘贴内容后可在这里查看逐行反馈；页面不会发送 CSV。</p></template></div></div></section>
    </div>
  </div>
</template>

<style scoped>
.ops-page{color:#2b2118}.workspace{max-width:1360px;margin:0 auto;display:grid;gap:20px}.desktop-notice{display:none}.page-head{display:flex;justify-content:space-between;align-items:start;gap:20px}.page-head h1{margin:0;font-size:clamp(29px,3vw,39px);letter-spacing:-.04em}.page-head p:last-child{margin:10px 0 0;color:#695d53}.eyebrow{margin:0 0 10px;color:#b85111;font-size:11px;font-weight:850;letter-spacing:.13em}.preview-badge{padding:7px 12px;border-radius:999px;background:#fff1e8;color:#8f3d0b;font-size:12px;font-weight:800;white-space:nowrap}.boundary{padding:17px 21px;border-left:4px solid #b85111;border-radius:12px;background:#fff1e8}.boundary strong{color:#8f3d0b}.boundary p{margin:5px 0 0;color:#695d53;font-size:13px;line-height:1.7}.workgrid{display:grid;grid-template-columns:minmax(0,1fr) 315px;gap:17px}.panel,.detail{min-width:0;border:1px solid #eadfd4;border-radius:17px;background:#fff;box-shadow:0 12px 30px rgba(83,55,28,.04)}.panel-head{display:flex;justify-content:space-between;align-items:start;gap:14px;padding:20px 22px;border-bottom:1px solid #eadfd4}.panel-head h2{margin:0;font-size:18px}.panel-head p{margin:5px 0 0;color:#695d53;font-size:12px}.subtle,.select{min-height:34px;padding:0 11px;border:1px solid #eadfd4;border-radius:9px;background:#fff;color:#b85111;font-size:12px;font-weight:750}.subtle:disabled{opacity:.5}.toolbar{display:flex;justify-content:space-between;align-items:end;gap:10px;padding:14px 21px;border-bottom:1px solid #eadfd4}.toolbar span{color:#897669;font-size:11px}.search-label{display:grid;gap:5px;color:#695d53;font-size:11px;font-weight:700}.search-label input{min-width:230px;min-height:36px;padding:0 11px;border:1px solid #dccfc3;border-radius:8px}.table-scroll{overflow-x:auto}table{width:100%;min-width:650px;border-collapse:collapse}th,td{padding:15px 13px;border-bottom:1px solid #f0e7df;text-align:left;font-size:12px}th{color:#695d53;background:#fffdf9}td strong{display:block;max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px}td small{display:block;margin-top:4px;color:#897669}.selected{background:#fff8f0}.status{display:inline-block;padding:4px 8px;border-radius:999px;font-size:11px;font-weight:750}.live{background:#eaf5ed;color:#22674b}.detail{padding:24px}.detail h2{margin:0;font-size:23px}.detail h3{font-size:14px}.detail p{color:#695d53;font-size:13px;line-height:1.7}.detail ul{display:grid;gap:9px;padding-left:20px;color:#5d524a;font-size:12px;line-height:1.6}.divider{height:1px;margin:20px 0;background:#eadfd4}.no-action{padding:11px;border-radius:9px;background:#f6f1ed;font-weight:700}.state{padding:27px;color:#695d53;font-size:13px}.state strong{color:#2b2118;font-size:15px}.state p{margin:7px 0 10px}.state button{border:0;background:none;color:#b85111;font-weight:750}.state.error,.error{color:#ab3131}.permission{padding:5px 8px;border-radius:6px;background:#f6effa;color:#6f4788;font-size:10px;font-weight:800}.csv-layout{display:grid;grid-template-columns:1fr 1fr;gap:22px;padding:21px}.field{display:grid;gap:8px;color:#51463c;font-size:12px;font-weight:750}.field textarea{width:100%;padding:12px;border:1px solid #dccfc3;border-radius:10px;font:12px/1.6 ui-monospace,Consolas,monospace;resize:vertical}.primary{min-height:39px;margin-top:12px;padding:0 17px;border:0;border-radius:9px;background:#b85111;color:#fff;font-size:12px;font-weight:750}.muted,.error{font-size:12px;line-height:1.7}.csv-results{min-height:190px;padding:19px;border-radius:12px;background:#faf7f3}.csv-results strong{font-size:13px}.csv-results p{color:#695d53;font-size:12px}.csv-results ul{display:grid;gap:8px;list-style:none;margin:13px 0 0;padding:0;font-size:12px}.accepted{color:#22674b}.rejected{color:#ab3131}@media(max-width:1100px){.workgrid{grid-template-columns:1fr}.csv-layout{grid-template-columns:1fr}}@media(max-width:800px){.workspace{display:none}.desktop-notice{display:block;padding:24px;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#695d53}}
</style>
