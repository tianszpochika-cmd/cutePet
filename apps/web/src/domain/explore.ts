/**
 * 探索端领域逻辑（T7.6 · 需求-探索 D1–D7 · 后端 ActivityRules/PoiRules 镜像的 UI 层）。
 */

export const POI_TYPES = ['宠物店', '宠物医院', '宠物公园', '宠物友好餐厅', '寄养', '美容', '训练'] as const;

export const RADII = [5, 10, 20] as const;

export interface PoiFilters {
  city?: string;
  type?: string;
  radiusKm?: number;
  lat?: number;
  lng?: number;
}

/** 筛选 → 查询参数（非法半径回落 5km 决议默认） */
export function buildPoiQuery(filters: PoiFilters): Record<string, string | number> {
  const radius = (RADII as readonly number[]).includes(filters.radiusKm ?? -1)
    ? filters.radiusKm!
    : 5;
  const query: Record<string, string | number> = { radius };
  if (filters.city) query.city = filters.city;
  if (filters.type) query.type = filters.type;
  if (typeof filters.lat === 'number') query.lat = filters.lat;
  if (typeof filters.lng === 'number') query.lng = filters.lng;
  return query;
}

/** 距离展示：<1km 用米 */
export function distanceLabel(km: number): string {
  if (km <= 0) return '0m';
  if (km < 1) return `${Math.round(km * 1000)}m`;
  return `${km.toFixed(1)}km`;
}

/** 均分 → 星填充百分比；null=暂无评分（U81） */
export function starsPercent(avg: number | null): number | null {
  if (avg === null) return null;
  return Math.round(Math.min(5, Math.max(0, avg)) * 20);
}

// ---------- 写评价（T7.6 · 防刷决议镜像） ----------

export function reviewBlockers(input: {
  friendly: number;
  env: number;
  service: number;
  content: string;
}): string[] {
  const blockers: string[] = [];
  for (const [key, v] of [
    ['friendly', input.friendly],
    ['env', input.env],
    ['service', input.service],
  ] as const) {
    if (v < 1 || v > 5) blockers.push(`SCORE_INVALID:${key}`);
  }
  if (input.content.trim().length === 0) blockers.push('CONTENT_EMPTY');
  if (input.content.trim().length > 500) blockers.push('CONTENT_TOO_LONG');
  return blockers;
}

/** 全 5 分且正文 <10 字 → 预告进入人工复审（防刷决议） */
export function suspectReviewHint(input: { friendly: number; env: number; service: number; content: string }): string | null {
  const allFive = input.friendly === 5 && input.env === 5 && input.service === 5;
  if (allFive && input.content.trim().length < 10) {
    return '内容较短的全五星评价将进入人工复审后展示。';
  }
  return null;
}

/** 同场所当日已评（1 天 1 条决议）→ 禁用提交 */
export function duplicateDayBlocked(alreadyReviewedToday: boolean): boolean {
  return alreadyReviewedToday;
}

// ---------- 活动报名（T7.6 · ActivityRules.denyReason 的 UI 文案映射 · U68/U69） ----------

export type SignupDeny =
  | 'TOO_EARLY'
  | 'NOT_OPEN'
  | 'DEADLINE_PASSED'
  | 'STARTED_OR_OVER'
  | 'FULL'
  | 'DUPLICATE'
  | 'CONSENT_REQUIRED'
  | 'STATE_BLOCKED';

export const SIGNUP_DENY_COPY: Record<SignupDeny, string> = {
  TOO_EARLY: '报名将于活动结束前 90 天开放。',
  NOT_OPEN: '报名尚未开放。',
  DEADLINE_PASSED: '报名已截止。',
  STARTED_OR_OVER: '活动已开始或结束，无法报名。',
  FULL: '名额已满。',
  DUPLICATE: '你已报名本活动（同账号仅一条有效），可在消息中查看凭证。',
  CONSENT_REQUIRED: '请先阅读并勾选报名信息告知同意。',
  STATE_BLOCKED: '活动信息核实中或已取消，暂停报名。',
};

/** 报名前置校验（日期/名额/重复/同意 —— 与后端 denyReason 同因） */
export function signupBlockers(input: {
  loggedIn: boolean;
  consentChecked: boolean;
  state: string;
  today: string;
  beginsAt: string;
  signupDeadline: string | null;
  quota: number;
  activeCount: number;
  userHasActive: boolean;
}): SignupDeny | 'NEED_LOGIN' | null {
  if (!input.loggedIn) return 'NEED_LOGIN';
  if (input.state === 'CANCELLED' || input.state === 'CHANGED' || input.state !== 'PUBLISHED') {
    if (input.state === 'CANCELLED' || input.state === 'CHANGED') return 'STATE_BLOCKED';
    if (input.state !== 'PUBLISHED') return 'NOT_OPEN';
  }
  const openFrom = shiftDays(input.beginsAt, -90);
  if (input.today < openFrom) return 'TOO_EARLY'; // U69：不早于结束前 90 天
  if (input.signupDeadline && input.today > input.signupDeadline) return 'DEADLINE_PASSED';
  if (input.today >= input.beginsAt) return 'STARTED_OR_OVER';
  if (input.quota > 0 && input.activeCount >= input.quota) return 'FULL'; // U68
  if (input.userHasActive) return 'DUPLICATE'; // U68 同账号一条
  if (!input.consentChecked) return 'CONSENT_REQUIRED';
  return null;
}

function shiftDays(date: string, days: number): string {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
}

/** 告知同意卡必显（合规：提交前单独同意） */
export const CONSENT_TEXT = '同意将姓名与电话提供给活动发布方，仅用于活动通知（保存 ≤90 天）。';

// ---------- 路线/领养（U21/U90 镜像） ----------

export function routePointsValid(pointsStr: string): boolean {
  const points = pointsStr
    .split(';')
    .map((s) => s.trim())
    .filter(Boolean);
  if (points.length < 2) return false;
  return points.every((p) => /^-?\d+(\.\d+)?,-?\d+(\.\d+)?$/.test(p));
}

/** 领养卡：无报名/交易按钮（U90） */
export const ADOPTION_CARD_FLAGS = { hasSignupButton: false, hasTradeButton: false } as const;

/** 领养过期（30 天不确认停止公开）→ 卡片隐藏 */
export function adoptionVisible(expireOn: string | null, today: string): boolean {
  if (!expireOn) return true;
  return today <= expireOn;
}
