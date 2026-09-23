<script setup lang="ts">
import { CREDENTIAL_MATERIAL_VISIBLE_TO, directPublishRight, type CredentialState } from '../domain/governance';

const states: { value: CredentialState; label: string; description: string }[] = [
  { value: 'APPLY', label: '待核验', description: '需先确认申请人、材料与指定审核人。' },
  { value: 'APPROVED', label: '已认证', description: '有效期内的普通活动仍须经过安全检查。' },
  { value: 'REJECTED', label: '驳回待补', description: '向申请人说明具体补充内容。' },
  { value: 'EXPIRED', label: '已到期', description: '重新核验后才能恢复相应资格。' },
  { value: 'REVOKED', label: '已撤销', description: '保留撤销依据与变更历史。' },
];
</script>

<template>
  <div class="credential-page">
    <div class="desktop-notice">资质核验请在电脑端查看。</div>
    <div class="workspace">
      <header class="page-head"><div><p class="eyebrow">CREDENTIAL REVIEW</p><h1>机构资质核验</h1><p>证明材料有明确的查看范围，核验结论必须能追溯到申请对象和审核人。</p></div><span class="preview-badge">只读界面预览</span></header>
      <div class="boundary"><strong>认证申请记录暂不可读取</strong><p>当前没有真实管理员会话、申请列表或安全查看材料的入口。这里展示流程与状态含义，不代表“没有待审核申请”，也不会授予或撤销机构资格。</p></div>
      <div class="workgrid">
        <section class="panel queue"><div class="panel-head"><div><h2>申请队列</h2><p>待核验、驳回待补、已认证与到期记录应分开筛选。</p></div><span class="pending">待接入</span></div><div class="empty"><span class="empty-icon" aria-hidden="true">◌</span><h3>暂时无法展示申请对象</h3><p>接入真实身份和申请列表后，才会展示机构名、申请时间、有效期与当前处理人。证明文件默认不在列表中显示。</p></div></section>
        <aside class="principles"><p class="eyebrow">ACCESS RULES</p><h2>材料与权限边界</h2><div class="rule"><span>01</span><div><strong>限定查看者</strong><p>{{ CREDENTIAL_MATERIAL_VISIBLE_TO }}。打开材料前需说明用途，并记录查看行为。</p></div></div><div class="rule"><span>02</span><div><strong>双重核验</strong><p>审核人先核对材料和机构身份；直发资格的授予或撤销还需主管复核。</p></div></div><div class="rule"><span>03</span><div><strong>结果与例外</strong><p>认证只影响有效期内的普通活动。领养信息始终进入人工审核。</p></div></div></aside>
      </div>
      <section class="panel process"><div class="panel-head"><div><h2>处理流程 · 规则预览</h2><p>下列步骤只说明需要什么；没有真实申请对象时不会产生处理结果。</p></div></div><ol><li><span>01</span><div><h3>核对申请</h3><p>读取申请人、机构资料、当前状态及同一对象的历史结论。</p></div></li><li><span>02</span><div><h3>限定范围查看材料</h3><p>核对指定审核人、用途和可见期限，查看失败不能作通过决定。</p></div></li><li><span>03</span><div><h3>记录决定与理由</h3><p>驳回和撤销必须附理由；通过需确认一年有效期及主管复核。</p></div></li><li><span>04</span><div><h3>核对平台回执</h3><p>分别查看新状态、资格变化、通知结果及审计记录；通知入队不代表送达。</p></div></li></ol></section>
      <section class="panel"><div class="panel-head"><div><h2>认证状态说明</h2><p>仅为规则说明，不是平台当前申请数量。</p></div></div><div class="table-scroll"><table><thead><tr><th>状态</th><th>对机构的含义</th><th>普通活动直发资格</th></tr></thead><tbody><tr v-for="item in states" :key="item.value"><td><span :class="['status', item.value.toLowerCase()]">{{ item.label }}</span></td><td>{{ item.description }}</td><td>{{ directPublishRight(item.value) ? '可能生效，仍需核对有效期与安全检查' : '不生效' }}</td></tr></tbody></table></div></section>
    </div>
  </div>
</template>

<style scoped>
.credential-page{color:#2b2118}.workspace{max-width:1360px;margin:0 auto;display:grid;gap:20px}.desktop-notice{display:none}.page-head{display:flex;justify-content:space-between;align-items:start;gap:20px}.page-head h1{margin:0;font-size:clamp(29px,3vw,39px);letter-spacing:-.04em}.page-head p:last-child{margin:10px 0 0;color:#695d53}.eyebrow{margin:0 0 10px;color:#b85111;font-size:11px;font-weight:850;letter-spacing:.13em}.preview-badge{padding:7px 12px;border-radius:999px;background:#fff1e8;color:#8f3d0b;font-size:12px;font-weight:800;white-space:nowrap}.boundary{padding:17px 21px;border-left:4px solid #b85111;border-radius:12px;background:#fff1e8}.boundary strong{color:#8f3d0b}.boundary p{margin:5px 0 0;color:#695d53;font-size:13px;line-height:1.7}.workgrid{display:grid;grid-template-columns:minmax(0,1fr) 330px;gap:17px}.panel,.principles{min-width:0;border:1px solid #eadfd4;border-radius:17px;background:#fff;box-shadow:0 12px 30px rgba(83,55,28,.04)}.panel-head{display:flex;justify-content:space-between;align-items:start;gap:14px;padding:20px 22px;border-bottom:1px solid #eadfd4}.panel-head h2{margin:0;font-size:18px}.panel-head p{margin:5px 0 0;color:#695d53;font-size:12px}.pending{padding:5px 9px;border-radius:999px;background:#f6effa;color:#6f4788;font-size:11px;font-weight:800}.empty{display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:260px;padding:25px;text-align:center}.empty-icon{display:grid;place-items:center;width:53px;height:53px;border-radius:17px;background:#fff1e8;color:#b85111;font-size:34px}.empty h3{margin:16px 0 7px;font-size:18px}.empty p{max-width:480px;margin:0;color:#695d53;font-size:13px;line-height:1.7}.principles{padding:24px;background:#fffdf9}.principles h2{margin:0 0 17px;font-size:23px}.rule{display:flex;gap:13px;padding:14px 0;border-top:1px solid #eadfd4}.rule>span{color:#b85111;font-size:11px;font-weight:850}.rule strong{font-size:13px}.rule p{margin:5px 0 0;color:#695d53;font-size:12px;line-height:1.7}.process ol{display:grid;grid-template-columns:repeat(4,1fr);gap:13px;list-style:none;margin:0;padding:21px}.process li{display:flex;gap:11px;padding:18px;border-radius:12px;background:#faf7f3}.process li>span{color:#b85111;font-size:12px;font-weight:850}.process h3{margin:0;font-size:14px}.process p{margin:8px 0 0;color:#695d53;font-size:12px;line-height:1.7}.table-scroll{overflow-x:auto}table{width:100%;min-width:650px;border-collapse:collapse}th,td{padding:15px 20px;border-bottom:1px solid #f0e7df;text-align:left;font-size:12px}th{color:#695d53;background:#fffdf9}.status{display:inline-block;padding:4px 9px;border-radius:999px;font-size:11px;font-weight:750}.status.apply{background:#fff1e8;color:#8f3d0b}.status.approved{background:#eaf5ed;color:#22674b}.status.rejected,.status.revoked,.status.expired{background:#fff0ed;color:#ab3131}.footnote{margin:0;padding:15px 21px;color:#695d53;font-size:11px;line-height:1.6}@media(max-width:1150px){.workgrid{grid-template-columns:1fr}.process ol{grid-template-columns:repeat(2,1fr)}}@media(max-width:800px){.workspace{display:none}.desktop-notice{display:block;padding:24px;border:1px solid #eadfd4;border-radius:12px;background:#fff;color:#695d53}}
</style>
