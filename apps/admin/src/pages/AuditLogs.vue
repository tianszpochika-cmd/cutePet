<script setup lang="ts">
import { ref } from 'vue';
import { auditQueryBlockers } from '../domain/workbench';

const from = ref('');
const to = ref('');
const action = ref('');
const operator = ref('');
const error = ref('');
const message = ref('');

function checkDraft() {
  error.value = '';
  message.value = '';
  if (!from.value || !to.value) {
    error.value = '请填写完整的起止日期。';
    return;
  }
  const blockers = auditQueryBlockers(from.value, to.value);
  if (blockers.includes('RANGE_INVALID')) error.value = '结束日期不能早于开始日期。';
  else if (blockers.includes('RANGE_TOO_WIDE')) error.value = '单次查询范围不能超过 90 天。';
  else message.value = '筛选条件格式已检查；当前为只读预览，尚未向审计服务发送查询。';
}
</script>

<template>
  <div class="audit">
    <header class="page-head">
      <p class="eyebrow">AUDIT TRAIL · READ ONLY</p>
      <h1>操作审计</h1>
      <p>按时间与动作定位操作链，核对操作者、对象及脱敏前后值。</p>
    </header>

    <div class="preview-note" role="status">
      <span aria-hidden="true">ⓘ</span>
      <div><strong>当前没有连接管理员审计数据</strong><p>只读界面预览不具备真实 audit.view 授权。下方仅可检查筛选条件格式；不会发起查询、读取日志或导出记录。</p></div>
    </div>

    <section class="panel" aria-labelledby="filter-title">
      <div class="panel-head"><div><p class="panel-kicker">FILTER DRAFT</p><h2 id="filter-title">筛选条件</h2></div><span>时间跨度 ≤ 90 天</span></div>
      <div class="filters">
        <label>开始日期<input v-model="from" type="date" data-testid="from" /></label>
        <label>结束日期<input v-model="to" type="date" data-testid="to" /></label>
        <label>动作类型<input v-model.trim="action" type="text" placeholder="如 user.ban" /></label>
        <label>操作者<input v-model.trim="operator" type="text" placeholder="操作账号" /></label>
        <button type="button" class="check" data-testid="query" @click="checkDraft">检查条件</button>
      </div>
      <p class="filter-hint">动作与操作者是将来的查询字段。当前输入只保留在本页，不参与任何服务端筛选。</p>
      <p v-if="error" class="feedback error" role="alert" data-testid="error">{{ error }}</p>
      <p v-if="message" class="feedback info" role="status">{{ message }}</p>
    </section>

    <section class="panel records" aria-labelledby="records-title">
      <div class="panel-head"><div><p class="panel-kicker">AUDIT RECORDS</p><h2 id="records-title">日志记录</h2></div><span class="readonly">只读</span></div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>时间</th><th>操作者</th><th>IP</th><th>动作</th><th>对象</th><th>前值 → 后值</th></tr></thead>
          <tbody><tr><td colspan="6" class="unavailable"><strong>日志查询待接入</strong><p>尚未读取任何记录，无法判断当前条件是否有结果。管理员身份、audit.view 权限和查询回执接通后才会展示真实日志。</p></td></tr></tbody>
        </table>
      </div>
    </section>

    <div class="principles">
      <div><strong>写入即脱敏</strong><span>手机号、邮箱等敏感值在写入与读出时均需处理。</span></div>
      <div><strong>高危动作留痕</strong><span>封禁、权限变更和系统设置需关联操作者、IP 与对象。</span></div>
      <div><strong>审计只读</strong><span>本页不提供更改、删除或未授权导出入口。</span></div>
    </div>
  </div>
</template>

<style scoped>
.audit{max-width:1320px;margin:auto;display:grid;gap:17px;color:#2b2118}.page-head{margin:4px 0 0}.eyebrow,.panel-kicker{font-size:11px;font-weight:800;letter-spacing:.15em;color:#B85111;margin:0 0 6px}.page-head h1{font-size:29px;letter-spacing:-.03em;margin:0}.page-head>p:last-child{font-size:13px;color:#6b5e52;margin:8px 0 0}.preview-note{display:flex;gap:12px;align-items:start;border:1px solid #ecd8b8;background:#fff8eb;color:#654a2c;border-radius:13px;padding:15px 17px;line-height:1.6}.preview-note>span{font-size:20px}.preview-note strong{font-size:13px}.preview-note p{font-size:12px;margin:3px 0 0}.panel{background:#fff;border:1px solid #e9ded2;border-radius:14px;box-shadow:0 8px 24px #2b211808;overflow:hidden}.panel-head{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:16px 18px;border-bottom:1px solid #eee5dc}.panel-head h2{font-size:16px;margin:0}.panel-head>span,.readonly{font-size:11px;color:#75685b}.filters{display:flex;align-items:end;gap:10px;padding:18px;flex-wrap:wrap}.filters label{display:grid;gap:6px;color:#625548;font-weight:700;font-size:12px;min-width:138px;flex:1}.filters input{min-height:40px;box-sizing:border-box;width:100%;border:1px solid #dacbbc;border-radius:9px;padding:0 10px;color:#2b2118;font:inherit;font-weight:400}.check{min-height:40px;border:0;border-radius:9px;background:#B85111;color:#fff;padding:0 16px;font:inherit;font-weight:700;cursor:pointer}.filter-hint{margin:0;padding:0 18px 16px;color:#807164;font-size:11px;line-height:1.6}.feedback{margin:0 18px 16px;padding:10px 12px;border-radius:9px;font-size:12px}.feedback.error{background:#fff0ee;color:#9b342e}.feedback.info{background:#eff6ef;color:#356941}.table-wrap{overflow-x:auto}table{width:100%;min-width:820px;border-collapse:collapse}th{text-align:left;background:#fff9f3;color:#6b5d51;font-size:11px;padding:12px 14px}td{border-top:1px solid #f0e8df;padding:28px 14px}.unavailable{text-align:center;color:#65594e}.unavailable strong{font-size:14px}.unavailable p{font-size:12px;line-height:1.7;max-width:560px;margin:6px auto 0}.principles{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}.principles div{background:#fff;border:1px solid #e9ded2;border-radius:11px;padding:13px 15px;display:grid;gap:5px}.principles strong{font-size:12px}.principles span{font-size:11px;color:#75685c;line-height:1.6}@media(max-width:1000px){.principles{grid-template-columns:1fr}}
</style>
