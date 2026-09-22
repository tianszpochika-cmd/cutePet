/**
 * 移动动效与离线反馈领域逻辑（T10.5 跟手/Sheet/记成功/reduced-motion · T10.6 离线与冲突）。
 * 复用 Web 侧：successMotionGate / syncRowCopy（closedLoop）、LOOP_BUDGET（motion）。
 */
import { syncRowCopy, LOGOUT_SYNC_CHOICES } from '../../../web/src/domain/closedLoop.ts';
import { successMotionGate, LOOP_BUDGET } from '../../../web/src/domain/motion.ts';

// ---------- T10.5 跟手与 Sheet 物理 ----------

export const FOLLOW_FACTOR = 0.5; // 下拉阻尼
export const SHEET_SNAP_MS = 320;

/** 跟手下拉：阻尼位移（不超过最大高度） */
export function followDy(rawDy: number, maxDy: number): number {
  if (rawDy <= 0) return 0;
  return Math.min(Math.round(rawDy * FOLLOW_FACTOR), maxDy);
}

export function sheetTransform(dy: number, dismissed: boolean): { translateY: number; durationMs: number; opacity: number } {
  if (dismissed) {
    return { translateY: 1000, durationMs: SHEET_SNAP_MS, opacity: 0 };
  }
  return { translateY: Math.max(0, dy), durationMs: SHEET_SNAP_MS, opacity: 1 };
}

// ---------- 记录成功动效（业务成功后才播放——复用闭环 §4 门控） ----------

export function recordSuccessMotion(provided: boolean, businessSucceeded: boolean): boolean {
  return successMotionGate(provided, businessSucceeded);
}

export { LOOP_BUDGET, syncRowCopy, LOGOUT_SYNC_CHOICES };

// ---------- T10.6 离线反馈（#32：离线显示未同步而非已完成；冲突不自动覆盖） ----------

export type OfflineStatus = 'ONLINE' | 'OFFLINE_PENDING' | 'SYNCING' | 'CONFLICT' | 'SYNC_FAILED';

export const OFFLINE_COPY: Record<OfflineStatus, { banner: string; recordState: string; tone: 'ok' | 'warn' | 'error' }> = {
  ONLINE: { banner: '', recordState: '已同步', tone: 'ok' },
  OFFLINE_PENDING: {
    banner: '当前离线 —— 记录保存在本地，联网后自动同步',
    recordState: '未同步（待联网上传，非完成态）',
    tone: 'warn',
  },
  SYNCING: { banner: '同步中…', recordState: '同步中', tone: 'warn' },
  CONFLICT: {
    banner: '检测到同步冲突：请在两份数据中选择，系统不会自动覆盖',
    recordState: '冲突待处理',
    tone: 'error',
  },
  SYNC_FAILED: { banner: '同步失败（输入已保留），可重试', recordState: '同步失败', tone: 'error' },
};

/** 冲突页双选：放弃本地 / 再次确认覆盖 —— 永不自动 */
export type ConflictChoice = 'DISCARD_LOCAL' | 'CONFIRM_OVERWRITE';

export function conflictOptions(status: OfflineStatus): { choice: ConflictChoice; label: string; danger: boolean }[] {
  if (status !== 'CONFLICT') return [];
  return [
    { choice: 'DISCARD_LOCAL', label: '放弃本次本地修改（保留服务器版本）', danger: false },
    { choice: 'CONFIRM_OVERWRITE', label: '再次确认：用本地覆盖服务器', danger: true },
  ];
}

export function applyConflictChoice(choice: ConflictChoice): { applied: boolean; notice: string } {
  return choice === 'DISCARD_LOCAL'
    ? { applied: true, notice: '已放弃本地修改' }
    : { applied: true, notice: '已按你的确认覆盖（未经确认绝不自动覆盖——#32）' };
}

/** 离线可继续操作：记录/待办完成进入待同步队列 */
export function offlineActionsAllowed(status: OfflineStatus): { record: boolean; completeTodo: boolean; submit: boolean } {
  return {
    record: status === 'ONLINE' || status === 'OFFLINE_PENDING',
    completeTodo: status === 'ONLINE' || status === 'OFFLINE_PENDING',
    submit: status === 'ONLINE', // 投稿/报名等需在线（防误提交）
  };
}

/** 待同步角标（我的 Tab 未同步数） */
export function pendingBadge(pendingCount: number, status: OfflineStatus): string {
  if (status === 'ONLINE' && pendingCount === 0) return '';
  if (pendingCount === 0) return '…';
  return pendingCount > 99 ? '99+' : String(pendingCount);
}

// ---------- 体重单位（决议：kg / 斤 双单位，存储统一 kg） ----------

export function convertWeight(kg: number, unit: 'kg' | 'jin'): number {
  const value = unit === 'kg' ? kg : kg * 2; // 1kg = 2 斤
  return Math.round(value * 10) / 10;
}

export function weightLabel(kg: number, unit: 'kg' | 'jin'): string {
  return unit === 'kg' ? `${convertWeight(kg, 'kg')}kg` : `${convertWeight(kg, 'jin')}斤`;
}
