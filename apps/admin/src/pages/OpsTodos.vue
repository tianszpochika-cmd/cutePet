<script setup lang="ts">
import { OPS_TODO_LABELS, type OpsTodoKind } from '../domain/governance';

const kinds: OpsTodoKind[] = ['FAILED_NOTIFICATION', 'CLEANUP_FAILED', 'PRO_REVIEW_PENDING', 'ACCOUNT_CLEANUP'];
const details: Record<OpsTodoKind, { owner: string; check: string; next: string }> = {
  FAILED_NOTIFICATION: {
    owner: '通知域负责人',
    check: '检查原消息、渠道失败原因和业务状态，避免重复通知。',
    next: '按原任务重试并等待投递回执；再次失败时交由人工接手。',
  },
  CLEANUP_FAILED: {
    owner: '数据清理负责人',
    check: '核对保留期限、受影响记录和失败原因，不在页面展示报名人等敏感字段。',
    next: '补清理后核对范围与结果，再关闭关联工单。',
  },
  PRO_REVIEW_PENDING: {
    owner: '专业复核负责人',
    check: '只查看绑定工单和明确用途下的最小证据范围。',
    next: '完成专业意见与审核回执，不以普通运营身份直接浏览私有健康记录。',
  },
  ACCOUNT_CLEANUP: {
    owner: '账号治理负责人',
    check: '核对注销、家庭与宠物依赖以及仍在进行的活动事项。',
    next: '按依赖处理顺序执行清理，并保留失败恢复入口。',
  },
};
</script>

<template>
  <div class="ops-todos">
    <header class="page-head">
      <p class="eyebrow">OPERATIONS FOLLOW-UP</p>
      <h1>运行异常待办</h1>
      <p>失败任务需要有人接手、重试并核对回执，不能只显示“任务已运行”。</p>
    </header>

    <div class="preview-note" role="status">
      <span aria-hidden="true">ⓘ</span>
      <div><strong>待办汇总尚未接入</strong><p>当前为只读界面预览，未读取队列、负责人、创建时间、失败原因或处理状态。因此无法判断是否有待办、是否逾期，也不会标记完成或发出升级通知。</p></div>
    </div>

    <div class="summary" aria-label="待办指标状态">
      <div><span>待处理</span><strong>—</strong><small>未读取</small></div>
      <div><span>超过 24 小时</span><strong>—</strong><small>未读取</small></div>
      <div><span>主管升级</span><strong>—</strong><small>未读取</small></div>
    </div>

    <section class="panel" aria-labelledby="types-title">
      <div class="panel-head"><div><p class="panel-kicker">HANDOFF TYPES</p><h2 id="types-title">需接入的异常类型</h2></div><span class="readonly">4 类 · 只读说明</span></div>
      <ul class="type-list">
        <li v-for="kind in kinds" :key="kind" class="type-card">
          <span class="type-icon" aria-hidden="true">{{ kind === 'FAILED_NOTIFICATION' ? '↗' : kind === 'CLEANUP_FAILED' ? '⌁' : kind === 'PRO_REVIEW_PENDING' ? '✚' : '◎' }}</span>
          <div class="type-body">
            <div class="type-title"><h3>{{ OPS_TODO_LABELS[kind] }}</h3><span>待联通</span></div>
            <p><strong>责任范围</strong>{{ details[kind].owner }}</p>
            <p><strong>先核对</strong>{{ details[kind].check }}</p>
            <p><strong>下一步</strong>{{ details[kind].next }}</p>
          </div>
        </li>
      </ul>
    </section>

    <section class="rule-card">
      <strong>24 小时升级口径</strong>
      <p>只有服务端返回真实创建时间、当前状态和负责人后，才能计算逾期。达到阈值只是升级条件；是否受理、是否通知主管及最终处理结果必须分别有回执。</p>
    </section>
  </div>
</template>

<style scoped>
.ops-todos{max-width:1320px;margin:auto;display:grid;gap:17px;color:#2b2118}.page-head{margin:4px 0 0}.eyebrow,.panel-kicker{font-size:11px;font-weight:800;letter-spacing:.15em;color:#B85111;margin:0 0 6px}.page-head h1{font-size:29px;letter-spacing:-.03em;margin:0}.page-head>p:last-child{font-size:13px;color:#6b5e52;margin:8px 0 0}.preview-note{display:flex;gap:12px;align-items:start;border:1px solid #ecd8b8;background:#fff8eb;color:#654a2c;border-radius:13px;padding:15px 17px;line-height:1.6}.preview-note>span{font-size:20px}.preview-note strong{font-size:13px}.preview-note p{font-size:12px;margin:3px 0 0}.summary{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}.summary div{background:#fff;border:1px solid #e9ded2;border-radius:12px;padding:15px 18px;display:grid;gap:2px}.summary span{font-size:12px;color:#74665a}.summary strong{font-size:29px;color:#86786b;line-height:1.2}.summary small{font-size:11px;color:#9a8c7e}.panel{background:#fff;border:1px solid #e9ded2;border-radius:14px;box-shadow:0 8px 24px #2b211808;overflow:hidden}.panel-head{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:16px 18px;border-bottom:1px solid #eee5dc}.panel-head h2{font-size:16px;margin:0}.readonly{font-size:11px;color:#786b5f}.type-list{list-style:none;padding:16px 18px;margin:0;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.type-card{display:flex;gap:15px;align-items:start;border:1px solid #e9ded2;border-radius:12px;padding:16px;background:#fffdfb}.type-icon{width:38px;height:38px;flex:none;display:grid;place-items:center;border-radius:10px;background:#fff0e2;color:#B85111;font-size:20px}.type-body{flex:1;min-width:0}.type-title{display:flex;justify-content:space-between;align-items:start;gap:10px}.type-title h3{font-size:14px;margin:0 0 10px}.type-title span{font-size:10px;color:#6e5b86;background:#f2eef8;border-radius:999px;padding:4px 8px;white-space:nowrap}.type-body p{font-size:11px;color:#70645a;line-height:1.6;margin:5px 0}.type-body p strong{color:#4e433a;margin-right:8px}.rule-card{padding:15px 18px;border:1px solid #dce8dc;background:#f5faf4;border-radius:12px}.rule-card strong{font-size:13px;color:#376945}.rule-card p{font-size:12px;line-height:1.7;color:#5b6a5d;margin:5px 0 0}@media(max-width:1050px){.type-list{grid-template-columns:1fr}}
</style>
