/**
 * 创作中心领域逻辑（T7.5 · 功能设计-资讯 C7 · 自动存草稿/状态时间线/U76 暂停提示）。
 */

export const SUBMISSION_KINDS = [
  { kind: 'ARTICLE', label: '写文章' },
  { kind: 'REVIEW', label: '写评测' },
  { kind: 'LIST', label: '写清单' },
] as const;

export const AUTOSAVE_INTERVAL_MS = 30_000; // 决议：30s 自动存

/** 状态时间线步骤（先审后发闭环） */
export const STATUS_STEPS = ['草稿', '待审核', '审核中', '结果'] as const;

/** 当前状态 → 已完成步数（0 基）与当前步 */
export function statusStepIndex(state: string): { done: number; current: number } {
  switch (state) {
    case 'DRAFT':
    case 'WITHDRAWN':
      return { done: 0, current: 0 };
    case 'PENDING':
      return { done: 1, current: 1 };
    case 'REVIEWING':
      return { done: 2, current: 2 };
    case 'PUBLISHED':
    case 'REJECTED':
    case 'TAKEDOWN':
      return { done: 4, current: 3 };
    default:
      return { done: 0, current: 0 };
  }
}

/** 编辑器提交闸门 */
export function submitBlockers(input: { kind: string; title: string; body: string; channel: string }): string[] {
  const blockers: string[] = [];
  const validKinds = (SUBMISSION_KINDS as readonly { kind: string }[]).map((k) => k.kind);
  if (!validKinds.includes(input.kind)) blockers.push('KIND_INVALID'); // U73 三类型
  if (input.title.trim().length < 1 || input.title.trim().length > 120) blockers.push('TITLE_INVALID');
  if (input.body.trim().length < 10) blockers.push('BODY_TOO_SHORT');
  if (!input.channel.trim()) blockers.push('CHANNEL_REQUIRED');
  return blockers;
}

/** 30s 自动保存节流判定 */
export function shouldAutosave(dirty: boolean, lastSavedAt: number | null, now: number): boolean {
  if (!dirty) return false;
  if (lastSavedAt === null) return true;
  return now - lastSavedAt >= AUTOSAVE_INTERVAL_MS;
}

/** 离开编辑器的未保存拦截 */
export function unsavedGuard(dirty: boolean): { block: boolean; message: string } {
  return dirty
    ? { block: true, message: '有未保存的修改，确定离开吗？（草稿会保留在草稿箱）' }
    : { block: false, message: '' };
}

/** 驳回 → 质量退修提示（U76：3 次暂停 7 天，质量问题不进违规） */
export function rejectCopy(rejectCount: number): { message: string; suspended: boolean } {
  if (rejectCount >= 3) {
    return {
      message: `累计 ${rejectCount} 次质量退修，投稿资格暂停 7 天（质量问题不计违规），可联系编辑沟通。`,
      suspended: true,
    };
  }
  return {
    message: `第 ${rejectCount} 次修改意见，请按编辑意见调整后重提（累计 3 次将暂停 7 天）。`,
    suspended: false,
  };
}

/** 撤回可用性（镜像 SubmissionRules：TAKEDOWN 治理下架不可自行恢复） */
export function withdrawCopy(state: string): { allowed: boolean; note: string } {
  switch (state) {
    case 'PENDING':
    case 'REVIEWING':
      return { allowed: true, note: '撤回后未决审核将终止。' };
    case 'PUBLISHED':
      return { allowed: true, note: '撤下后文章即时不可见，可重新提审。' };
    case 'TAKEDOWN':
      return { allowed: false, note: '治理下架不可自行恢复（U75），如有异议请走申诉。' };
    case 'REJECTED':
      return { allowed: true, note: '可修改后重新提交。' };
    default:
      return { allowed: false, note: '当前状态无需撤回。' };
  }
}

/** 创作数据卡片模型 */
export function statCards(stats: { views: number; likes: number; comments: number; favorites: number }[]): {
  label: string;
  value: number;
}[] {
  const sum = (pick: (s: { views: number; likes: number; comments: number; favorites: number }) => number) =>
    stats.reduce((acc, s) => acc + pick(s), 0);
  return [
    { label: '浏览', value: sum((s) => s.views) },
    { label: '点赞', value: sum((s) => s.likes) },
    { label: '评论', value: sum((s) => s.comments) },
    { label: '收藏', value: sum((s) => s.favorites) },
  ];
}
