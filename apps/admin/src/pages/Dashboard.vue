<script setup lang="ts">
const priority = [
  { title: '审核与举报', description: '核对对象版本、处理锁、原因及处置影响。', to: '/review', tag: '审核' },
  { title: '运行异常待办', description: '失败通知、清理任务和专业复核的处理入口。', to: '/todos', tag: '异常' },
  { title: '用户治理', description: '先看工单范围，再检查独立的限制和申诉。', to: '/governance', tag: '治理' },
];
const domains = [
  { title: '内容运营', description: '文章、分类与推荐位', to: '/content' },
  { title: '商品导购', description: '商品、清单与本地 CSV 预检', to: '/products' },
  { title: '探索运营', description: '场所、纠错、路线与活动', to: '/explore' },
  { title: '机构资质', description: '申请材料与核验边界', to: '/credentials' },
  { title: '审计日志', description: '操作与结果追溯', to: '/audit' },
  { title: '角色权限', description: '权限点与预置角色说明', to: '/roles' },
];
const indicators = [
  { label: '待审核对象', note: '需服务端聚合、权限裁剪与当前状态' },
  { label: '超时与升级', note: '需对象时间窗、24/48 小时规则与回执' },
  { label: '失败通知', note: '需送达结果与人工接手任务' },
  { label: '活跃与完成率', note: '需分子、分母、观察窗和数据成熟状态' },
];
</script>

<template>
  <div class="dashboard">
    <header class="page-head"><div><p class="eyebrow">WORKSPACE OVERVIEW</p><h1>先看需要判断的事</h1><p>按任务进入对应工作区。当前为只读界面预览，卡片不代表存在待办或获得处理权限。</p></div><span class="edition">预览版本 · 2026.09</span></header>

    <section aria-labelledby="priority-title">
      <div class="section-head"><h2 id="priority-title">重点工作入口</h2><span>对象与状态以正式平台返回为准</span></div>
      <div class="priority-grid"><router-link v-for="(item, index) in priority" :key="item.to" :to="item.to" class="priority-card"><div class="card-top"><span class="index">0{{ index + 1 }}</span><span class="tag">{{ item.tag }}</span></div><h3>{{ item.title }}</h3><p>{{ item.description }}</p><span class="card-link">查看界面 <span aria-hidden="true">↗</span></span></router-link></div>
    </section>

    <section class="data-section" aria-labelledby="data-title"><div class="section-head"><h2 id="data-title">指标接入状态</h2><span>暂无可核验的管理端聚合数据</span></div><div class="indicator-grid"><article v-for="item in indicators" :key="item.label" class="indicator"><span class="indicator-label">{{ item.label }}</span><strong>—</strong><small>{{ item.note }}</small></article></div><p class="data-note">正式指标需同时给出权限范围、分子分母、观察窗、更新时间与失败状态。零分母显示“—”，不得把示例数值当作运营结果。</p></section>

    <section aria-labelledby="domains-title"><div class="section-head"><h2 id="domains-title">业务工作区</h2><span>目录入口，不表示管理权限</span></div><div class="domain-grid"><router-link v-for="item in domains" :key="item.to" :to="item.to" class="domain-card"><div><h3>{{ item.title }}</h3><p>{{ item.description }}</p></div><span aria-hidden="true">→</span></router-link></div></section>
  </div>
</template>

<style scoped>
.dashboard { display: grid; gap: 40px; max-width: 1380px; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
.eyebrow { margin: 0 0 9px; color: var(--primary); font-size: 11px; font-weight: 800; letter-spacing: .15em; }
h1 { margin: 0 0 10px; font-size: clamp(28px, 2.7vw, 39px); }
.page-head p:last-child { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.7; }
.edition { color: var(--muted); font-size: 12px; white-space: nowrap; }
.section-head { display: flex; justify-content: space-between; gap: 16px; align-items: baseline; margin-bottom: 15px; }
.section-head h2 { margin: 0; font-size: 18px; }
.section-head span { color: var(--muted); font-size: 12px; }
.priority-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.priority-card { display: flex; flex-direction: column; min-height: 200px; padding: 22px; border: 1px solid var(--line); border-radius: 18px; background: #fff; color: var(--ink); text-decoration: none; transition: border-color .15s, transform .15s; }
.priority-card:hover { border-color: #d09d74; transform: translateY(-2px); }
.card-top { display: flex; justify-content: space-between; align-items: center; }
.index { color: #d2a482; font-size: 13px; font-weight: 800; }
.tag { padding: 4px 9px; border-radius: 7px; background: #fff1e4; color: #8e420e; font-size: 11px; font-weight: 700; }
.priority-card h3 { margin: 26px 0 7px; font-size: 20px; }
.priority-card p { margin: 0; color: var(--muted); font-size: 12px; line-height: 1.7; }
.card-link { margin-top: auto; padding-top: 17px; color: var(--primary); font-size: 12px; font-weight: 800; }
.indicator-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.indicator { min-height: 150px; display: grid; align-content: start; gap: 6px; padding: 18px; border: 1px solid var(--line); border-radius: 15px; background: #fff; }
.indicator-label { color: var(--muted); font-size: 12px; }
.indicator strong { font-size: 32px; font-weight: 500; }
.indicator small { color: var(--muted); font-size: 11px; line-height: 1.5; }
.data-note { margin: 11px 0 0; color: var(--muted); font-size: 11px; line-height: 1.7; }
.domain-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.domain-card { min-height: 94px; display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 17px 19px; border: 1px solid var(--line); border-radius: 14px; background: #fff; color: var(--ink); text-decoration: none; }
.domain-card:hover { border-color: #d09d74; }
.domain-card h3 { margin: 0 0 4px; font-size: 14px; }
.domain-card p { margin: 0; color: var(--muted); font-size: 11px; }
.domain-card > span { color: var(--primary); font-size: 18px; }
@media (max-width: 1140px) { .priority-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .indicator-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .domain-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (prefers-reduced-motion: reduce) { .priority-card { transition: none; } .priority-card:hover { transform: none; } }
</style>
