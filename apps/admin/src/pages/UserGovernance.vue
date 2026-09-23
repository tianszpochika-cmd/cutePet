<script setup lang="ts">
import { BAN_ACTION_COPY, FAMILY_DISPUTE_RULE, type BanLevel } from '../domain/governance';

const levels: BanLevel[] = ['L1', 'L2', 'L3', 'L4'];
const queues = [
  { title: '举报与版权', permission: 'report.handle', detail: '先核对举报对象、证据与处置范围；版权通知和一般举报分流处理。' },
  { title: '申诉复核', permission: 'appeal.handle', detail: '核对原处置、申诉材料与 48 小时反馈时限；成立后按回执撤销相应记录。' },
  { title: '家庭纠纷', permission: 'family.dispute', detail: FAMILY_DISPUTE_RULE },
];
</script>

<template>
  <div class="governance">
    <header class="page-head">
      <p class="eyebrow">TRUST &amp; GOVERNANCE</p>
      <h1>用户与工单治理</h1>
      <p>先确认对象、权限和证据，再决定处置与申诉结果。</p>
    </header>

    <div class="preview-note" role="status">
      <span aria-hidden="true">ⓘ</span>
      <div><strong>当前为只读界面预览</strong><p>管理身份、逐项权限和用户明细尚未接入。这里没有读取真实用户、工单或审计记录，也不会执行封禁、解封、投稿限制或申诉结案。</p></div>
    </div>

    <div class="top-grid">
      <section class="panel" aria-labelledby="user-search-title">
        <div class="panel-head"><div><p class="panel-kicker">USER LOOKUP</p><h2 id="user-search-title">用户检索</h2></div><span class="perm">user.view</span></div>
        <div class="search-row">
          <label for="governance-query">昵称或手机号</label>
          <div><input id="governance-query" type="text" placeholder="接入管理员身份后可检索" disabled /><button type="button" disabled>查询待接入</button></div>
        </div>
        <div class="unavailable">
          <strong>尚不能显示用户列表</strong>
          <p>服务端当前只返回处置摘要，缺少经 IAM 聚合的脱敏用户资料、当前限制状态与对象级权限。接入前不能选择目标或发起高危操作。</p>
        </div>
        <p class="foot">封禁与投稿资格是不同限制；解除一项不能自动解除另一项。</p>
      </section>

      <section class="panel" aria-labelledby="ban-title">
        <div class="panel-head"><div><p class="panel-kicker">MODERATION LADDER</p><h2 id="ban-title">分级处置规则</h2></div><span class="perm danger">user.ban</span></div>
        <ol class="levels">
          <li v-for="level in levels" :key="level">
            <span class="level" :class="{ severe: level === 'L4' }">{{ level }}</span>
            <div><strong>{{ BAN_ACTION_COPY[level] }}</strong><p>{{ level === 'L4' ? '永久封禁前须确认法律依据与证据留存。' : '期限、对象及影响范围须由真实工单确认。' }}</p></div>
          </li>
        </ol>
        <div class="safety"><strong>执行前的必要条件</strong><p>核验操作者和目标不是同一人、校验 user.ban、填写原因与期限、二次确认，并由服务端返回处置状态及审计编号。</p></div>
      </section>
    </div>

    <section class="rule-card" aria-labelledby="quality-rule-title">
      <span class="rule-icon" aria-hidden="true">≠</span>
      <div>
        <h2 id="quality-rule-title">质量退修与违规处罚分开计算</h2>
        <p>质量或材料问题要求补正，退修 3 次也不按违规暂停投稿。只有滚动 180 天内 3 次经人工确认的违规驳回，才按规则暂停投稿 7 天；申诉撤销的记录需剔除。</p>
      </div>
    </section>

    <div class="queue-grid">
      <section v-for="queue in queues" :key="queue.title" class="panel queue-card">
        <div class="panel-head"><h2>{{ queue.title }}</h2><span class="perm">{{ queue.permission }}</span></div>
        <p>{{ queue.detail }}</p>
        <div class="queue-status"><span aria-hidden="true">◇</span><strong>队列待接入</strong><small>没有读取真实工单，当前无法判断数量或时限。</small></div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.governance{display:grid;gap:18px;color:#2b2118;max-width:1320px;margin:auto}.page-head{margin:4px 0 0}.eyebrow,.panel-kicker{margin:0 0 6px;color:#B85111;font-size:11px;font-weight:800;letter-spacing:.15em}.page-head h1{font-size:29px;letter-spacing:-.03em;margin:0}.page-head>p:last-child{color:#6b5e52;font-size:13px;margin:8px 0 0}.preview-note{display:flex;align-items:start;gap:12px;border:1px solid #ecd8b8;border-radius:13px;background:#fff8eb;color:#654a2c;padding:15px 17px;line-height:1.6}.preview-note>span{font-size:20px}.preview-note strong{font-size:13px}.preview-note p{font-size:12px;margin:3px 0 0}.top-grid{display:grid;grid-template-columns:minmax(0,1.05fr) minmax(400px,.95fr);gap:16px}.panel{background:#fff;border:1px solid #e9ded2;border-radius:14px;box-shadow:0 8px 24px #2b211808;padding:18px}.panel-head{display:flex;justify-content:space-between;align-items:start;gap:12px;margin-bottom:17px}.panel-head h2{font-size:16px;margin:0}.perm{font:600 11px ui-monospace,SFMono-Regular,Consolas,monospace;color:#5c4d82;background:#f2eefb;border-radius:999px;padding:5px 8px;white-space:nowrap}.perm.danger{color:#96372e;background:#fff0ee}.search-row label{display:block;font-size:12px;color:#6b5e52;font-weight:700;margin-bottom:7px}.search-row>div{display:flex;gap:8px}.search-row input{flex:1;min-width:0;min-height:40px;border:1px solid #ddd0c2;border-radius:9px;padding:0 12px;font:inherit}.search-row button{min-height:40px;border:0;border-radius:9px;background:#e9e3dd;color:#73675d;padding:0 14px;font:inherit;cursor:not-allowed}.unavailable{margin-top:17px;border:1px dashed #dcc9b8;border-radius:11px;padding:20px;background:#fcfaf7}.unavailable strong{font-size:13px}.unavailable p,.foot{color:#74685e;font-size:12px;line-height:1.7}.unavailable p{margin:6px 0 0}.foot{margin:15px 0 0}.levels{list-style:none;padding:0;margin:0;display:grid;gap:8px}.levels li{display:flex;gap:12px;align-items:start;padding:9px 0;border-bottom:1px solid #f1e9e1}.level{width:38px;height:34px;display:grid;place-items:center;flex:none;background:#fff1e6;color:#9b4b17;border-radius:8px;font-weight:800;font-size:12px}.level.severe{background:#fff0ee;color:#96372e}.levels strong{font-size:13px}.levels p{font-size:11px;color:#807368;margin:4px 0 0;line-height:1.5}.safety{margin-top:16px;padding:12px 14px;border-radius:10px;background:#f8f3ed}.safety strong{font-size:12px}.safety p{font-size:11px;color:#6d6054;line-height:1.6;margin:4px 0 0}.rule-card{display:flex;align-items:start;gap:15px;padding:18px;border:1px solid #dbe7dc;border-radius:14px;background:#f5faf4}.rule-icon{width:37px;height:37px;flex:none;display:grid;place-items:center;border-radius:10px;background:#e2efe3;color:#4e7a55;font-size:22px}.rule-card h2{font-size:15px;margin:0}.rule-card p{font-size:12px;line-height:1.7;color:#5b6b5e;margin:6px 0 0}.queue-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.queue-card>p{font-size:12px;color:#6d6155;line-height:1.7;min-height:65px}.queue-status{display:flex;align-items:center;flex-wrap:wrap;gap:7px;background:#f7f3ef;border-radius:10px;padding:11px;color:#7b6d60}.queue-status strong{font-size:12px}.queue-status small{width:100%;font-size:11px}@media(max-width:1080px){.top-grid{grid-template-columns:1fr}.queue-grid{grid-template-columns:1fr}}
</style>
