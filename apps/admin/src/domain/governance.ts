/**
 * 管理端治理领域逻辑（T8.4 用户治理与工单 / T8.6 ★版本差异·认证·清理失败·受限工单）。
 */

// ---------- T8.4 封禁与申诉 ----------

export type BanLevel = 'L1' | 'L2' | 'L3' | 'L4';

export const BAN_ACTION_COPY: Record<BanLevel, string> = {
  L1: '删内容 + 提醒',
  L2: '禁言 7 天',
  L3: '禁言 30 天',
  L4: '永久封禁（证据留存，依法上报）',
};

/** 高危双保险：封禁需二次确认 + 不能封禁自己（对象级权限镜像） */
export function banBlockers(input: {
  actorId: number;
  targetId: number;
  level: BanLevel;
  reason: string;
  doubleConfirmed: boolean;
}): string[] {
  const out: string[] = [];
  if (input.actorId === input.targetId) out.push('CANNOT_BAN_SELF');
  if (!input.reason.trim()) out.push('REASON_REQUIRED');
  if (!input.doubleConfirmed) out.push('DOUBLE_CONFIRM_REQUIRED'); // §6.2 高危双保险
  if (input.level === 'L4' && !input.reason.trim()) out.push('L4_REASON_REQUIRED');
  return out;
}

export function appealResolveBlockers(accepted: boolean, resolution: string): string[] {
  if (!resolution.trim()) return ['RESOLUTION_REQUIRED'];
  if (accepted === undefined) return [];
  return [];
}

export const APPEAL_SLA_HOURS = 48;

export function appealOverdue(createdAtIso: string, resolvedAtIso: string | null, nowIso: string): boolean {
  if (resolvedAtIso) return false;
  return new Date(nowIso).getTime() - new Date(createdAtIso).getTime() > APPEAL_SLA_HOURS * 3600_000;
}

export function familyDisputeBlockers(resolution: string): string[] {
  return resolution.trim() ? [] : ['RESOLUTION_REQUIRED']; // 闭环§3：处理依据与通知必须留痕
}

export const FAMILY_DISPUTE_RULE = '不因接到投诉代替所有者转移宠物——处理仅限授权/证据范围并留痕通知。';

// ---------- T8.6 ★ 运行异常待办（闭环 §3：不得仅展示“已运行”） ----------

export type OpsTodoKind =
  | 'FAILED_NOTIFICATION'
  | 'CLEANUP_FAILED'
  | 'PRO_REVIEW_PENDING'
  | 'ACCOUNT_CLEANUP';

export interface OpsTodo {
  kind: OpsTodoKind;
  refId: string;
  owner: string;
  createdAtIso: string;
  resolved: boolean;
}

export function opsTodoOverdue(todo: OpsTodo, nowIso: string, overdueHours = 24): boolean {
  if (todo.resolved) return false;
  return new Date(nowIso).getTime() - new Date(todo.createdAtIso).getTime() > overdueHours * 3600_000;
}

/** 主管升级：逾期 → 标记升级并通知主管（不得只显示“任务已运行”） */
export function escalateOpsTodo(todo: OpsTodo, nowIso: string): { escalated: boolean; visible: string } {
  const overdue = opsTodoOverdue(todo, nowIso);
  return {
    escalated: overdue,
    visible: overdue ? `${todo.kind} 逾期，已升级主管处理（含失败原因与重试入口）` : '',
  };
}

export const OPS_TODO_LABELS: Record<OpsTodoKind, string> = {
  FAILED_NOTIFICATION: '失败通知（可接手重发）',
  CLEANUP_FAILED: '数据清理失败（保留期已过）',
  PRO_REVIEW_PENDING: '未完成专业复核',
  ACCOUNT_CLEANUP: '账号清理待办',
};

// ---------- T8.6 机构认证审核 ----------

export type CredentialState = 'APPLY' | 'APPROVED' | 'REJECTED' | 'EXPIRED' | 'REVOKED';

export function credentialTransition(from: CredentialState, action: 'approve' | 'reject' | 'revoke' | 'expire'): CredentialState | null {
  switch (action) {
    case 'approve':
      return from === 'APPLY' ? 'APPROVED' : null;
    case 'reject':
      return from === 'APPLY' ? 'REJECTED' : null;
    case 'revoke':
      return from === 'APPROVED' ? 'REVOKED' : null;
    case 'expire':
      return from === 'APPROVED' ? 'EXPIRED' : null;
    default:
      return null;
  }
}

export const CREDENTIAL_MATERIAL_VISIBLE_TO = '仅本人与审核人（材料不公开——U89）';

export function directPublishRight(state: CredentialState): boolean {
  return state === 'APPROVED'; // 直发资格按当前认证（U89）
}

// ---------- T8.6 名单导出（U72/U86） ----------

export function exportBlockers(input: {
  hasExportPermission: boolean;
  isOrgOwner: boolean;
  consentRecordsPresent: boolean;
  pastRetention: boolean;
}): string[] {
  const out: string[] = [];
  if (!input.hasExportPermission) out.push('NEED_ACTIVITY_EXPORT'); // 双条件之一
  if (!input.isOrgOwner) out.push('NEED_ORG_OWNER'); // 双条件之二
  if (input.pastRetention) out.push('RETENTION_EXPIRED'); // 90 天后字段已清理（U86）
  if (!input.consentRecordsPresent) out.push('CONSENT_RECORD_MISSING');
  return out;
}

export const EXPORT_AUDIT_NOTE = '导出需记录：操作者/时间/范围/条数；下载链接带有效期（U72/U86）。';

// ---------- T8.6 版本差异（审核侧，闭环 §3 内容审核） ----------

export function versionDiffRequired(submissionKind: string): boolean {
  return submissionKind === 'ARTICLE' || submissionKind === 'REVIEW' || submissionKind === 'LIST';
}

/** 审核仅对当前提交版本生效；明确区分质量退修 vs 违规 */
export function reviewDisposition(input: {
  kind: 'quality' | 'violation';
  rejectCount: number;
}): { label: string; countsAsViolation: boolean; suspended: boolean } {
  if (input.kind === 'quality') {
    return {
      label: `质量退修第 ${input.rejectCount} 次（不计违规）`,
      countsAsViolation: false,
      suspended: input.rejectCount >= 3,
    };
  }
  return { label: '确认违规', countsAsViolation: true, suspended: false };
}

// ---------- T8.6 健康私有证据最小化（闭环 §3） ----------

export function canViewHealthEvidence(input: {
  boundTicketId: string | null;
  purpose: string;
  actorIsHandler: boolean;
}): { allowed: boolean; requiresAudit: boolean; reason: string } {
  const allowed = input.actorIsHandler && !!input.boundTicketId && input.purpose.trim().length > 0;
  return {
    allowed,
    requiresAudit: allowed, // 工单绑定最小范围 + 说明用途 + 留痕
    reason: allowed
      ? '仅展示该工单绑定的最小证据范围，查看已留痕'
      : '普通运营页不开放健康档案浏览（闭环 §3）',
  };
}
