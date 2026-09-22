/**
 * 契约级领域规则（纯函数，双端共享的单一来源）。
 * 追溯：BR-02（提醒/计划周期）、BR-07（健康记录时间）、需求 B3（体重）、
 *       功能设计-资讯（投稿状态机）、需求-管理端 §2.8（L1–L4 处置）。
 * 约定：日期一律使用 `YYYY-MM-DD` 日历字符串做纯日历运算（调度层负责 Asia/Shanghai 解释）。
 */

// ---------- 日期工具 ----------
const DAY_MS = 86_400_000;

export function isLeapYear(year: number): boolean {
  return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}

/** 'YYYY-MM-DD' → UTC 零点 epoch（纯日历数学，不涉及时区） */
export function parseDate(date: string): number {
  const [y, m, d] = date.split('-').map(Number) as [number, number, number];
  return Date.UTC(y, m - 1, d);
}

export function formatDate(epoch: number): string {
  const d = new Date(epoch);
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())}`;
}

export function addDays(date: string, days: number): string {
  return formatDate(parseDate(date) + days * DAY_MS);
}

/** a - b 的日历天数差（a 早于 b 为负） */
export function diffDays(a: string, b: string): number {
  return Math.round((parseDate(a) - parseDate(b)) / DAY_MS);
}

// ---------- 提醒/计划周期（BR-02 · U55/U56/U57） ----------
export type Recurrence =
  | { kind: 'once'; date: string }
  | { kind: 'everyDays'; days: number; start: string }
  | { kind: 'yearly'; month: number; day: number; start: string };

/** 触发基准：plan=计划日网格锚定 start；completion=完成日滚动锚定（U55 双基准，各自唯一） */
export type RecurrenceBasis = 'plan' | 'completion';

/**
 * 计算 after（不含）之后的下一次触发日；无后续返回 null。
 * - everyDays：plan 基准以 start 为锚点按 N 天推进（计划侧不随完成漂移）；
 *   completion 基准以上一次完成日（传入的 after）为新锚点 +N 天（U55 完成日基准）。
 * - yearly 2/29：非闰年落 2/28（仅当年），其余年份保留原月日（U56）。
 * - 同一天不重复触发；跳过需上层记录原因（U56「跳过有原因」）。
 */
export function nextOccurrence(rule: Recurrence, after: string, basis: RecurrenceBasis = 'plan'): string | null {
  if (rule.kind === 'once') {
    return rule.date > after ? rule.date : null;
  }
  if (rule.kind === 'everyDays') {
    if (!Number.isInteger(rule.days) || rule.days < 1) {
      throw new Error('周期天数必须为正整数');
    }
    if (after < rule.start) return rule.start;
    if (basis === 'completion') {
      return addDays(after, rule.days);
    }
    const elapsed = diffDays(after, rule.start);
    const k = Math.floor(elapsed / rule.days) + 1;
    const next = addDays(rule.start, k * rule.days);
    return next > after ? next : addDays(next, rule.days);
  }
  // yearly
  const month = rule.month;
  const day = rule.day;
  const [afterY] = after.split('-').map(Number) as [number];
  const md = (y: number): string => {
    if (month === 2 && day === 29 && !isLeapYear(y)) return `${y}-02-28`;
    return `${y}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  };
  const candidate = md(afterY);
  return candidate > after ? candidate : md(afterY + 1);
}

// ---------- 体重规则（需求 B3 · U80） ----------
export const WEIGHT_CONFIRM_RATIO = 0.1; // 单次 ≥10% 二次确认
export const TREND_RATIO = 0.05; // 较基准 ≥5% 高亮

export function needsWeightConfirm(prevKg: number | null, nextKg: number): boolean {
  if (prevKg === null || prevKg <= 0) return false;
  return Math.abs(nextKg - prevKg) / prevKg >= WEIGHT_CONFIRM_RATIO;
}

export function trendMarked(baseKg: number | null, currentKg: number): boolean {
  if (baseKg === null || baseKg <= 0) return false; // 不伪造趋势（U80：无基准不高亮）
  return Math.abs(currentKg - baseKg) / baseKg >= TREND_RATIO;
}

export function jinToKg(jin: number): number {
  return Math.round(jin * 0.5 * 1000) / 1000;
}
export function kgToJin(kg: number): number {
  return Math.round(kg * 2 * 1000) / 1000;
}

// ---------- 投稿状态机（功能设计-资讯 C7 · U74/U75） ----------
export type ReviewState = 'DRAFT' | 'PENDING' | 'REVIEWING' | 'PUBLISHED' | 'REJECTED' | 'TAKEDOWN' | 'WITHDRAWN';

const TRANSITIONS: Record<ReviewState, ReviewState[]> = {
  DRAFT: ['PENDING', 'WITHDRAWN'],
  PENDING: ['REVIEWING', 'WITHDRAWN'],
  REVIEWING: ['PUBLISHED', 'REJECTED', 'PENDING'], // PENDING = 处理中释放回队列
  REJECTED: ['PENDING'], // 修改重提
  PUBLISHED: ['TAKEDOWN', 'WITHDRAWN'], // 运营下架 / 作者撤下
  TAKEDOWN: ['PUBLISHED'], // 运营恢复（治理清除不可恢复，上层置 isLegalClear 标记）
  WITHDRAWN: ['PENDING'],
};

export function canTransition(from: ReviewState, to: ReviewState): boolean {
  return TRANSITIONS[from].includes(to);
}

export const REJECT_SUSPEND_THRESHOLD = 3; // 累计 3 次驳回不过
export const REJECT_SUSPEND_DAYS = 7;
/** 驳回计数 → 是否暂停投稿资格 7 天（质量退修 vs 违规计数分离：BR-06/U76） */
export function shouldSuspendForRejections(rejectCount: number): boolean {
  return rejectCount >= REJECT_SUSPEND_THRESHOLD;
}

// ---------- 违规处置分级（需求-管理端 §2.8 · BR-09） ----------
export type ViolationLevel = 'L1' | 'L2' | 'L3' | 'L4';
export type ModerationAction = 'DELETE_CONTENT' | 'MUTE_7D' | 'MUTE_30D' | 'PERMANENT_BAN';

export function actionForLevel(level: ViolationLevel): ModerationAction {
  switch (level) {
    case 'L1':
      return 'DELETE_CONTENT';
    case 'L2':
      return 'MUTE_7D';
    case 'L3':
      return 'MUTE_30D';
    case 'L4':
      return 'PERMANENT_BAN';
  }
}

export const L1_ESCALATE_COUNT = 3; // 180 天内 L1×3 → L2
/** 累计升级：返回升级后的等级 */
export function escalateLevel(level: ViolationLevel, l1CountInWindow: number): ViolationLevel {
  if (level === 'L1' && l1CountInWindow >= L1_ESCALATE_COUNT) return 'L2';
  return level;
}
