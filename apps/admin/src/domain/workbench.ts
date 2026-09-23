/**
 * 管理端工作台领域逻辑（T8.1 登录权限指令 / T8.2 审核工作台 / T8.5 看板与角色）。
 * 权限单源：packages/contracts/permissions.ts（32 权限点 + 6 预置角色）。
 */
import { PERMISSIONS, ROLES, can, type Permission } from '../../../../packages/contracts/src/permissions.ts';

// ---------- T8.1 登录与导航 ----------

/** 侧边栏结构（信息架构 §7）+ 所需权限（无权限项隐藏 —— 指令式渲染） */
export const ADMIN_NAV: { section: string; items: { id: string; label: string; to: string; perm: Permission }[] }[] = [
  {
    section: '总览',
    items: [
      { id: 'dashboard', label: '数据看板', to: '/dashboard', perm: 'dashboard.view.all' },
      { id: 'todos', label: '运行异常待办', to: '/todos', perm: 'dashboard.view.all' },
    ],
  },
  {
    section: '内容运营',
    items: [
      { id: 'review', label: '投稿审核', to: '/review', perm: 'review.article' },
      { id: 'content', label: '文章与分类', to: '/content', perm: 'article.edit.any' },
      { id: 'banners', label: '推荐位', to: '/content', perm: 'banner.manage' },
    ],
  },
  {
    section: '商品导购',
    items: [
      { id: 'products', label: '商品与导入', to: '/products', perm: 'product.create.edit' },
      { id: 'lists', label: '清单运营', to: '/products', perm: 'list.manage' },
    ],
  },
  {
    section: '探索运营',
    items: [
      { id: 'pois', label: 'POI 与纠错', to: '/explore', perm: 'poi.create.edit' },
      { id: 'routes', label: '路线/活动审核', to: '/explore', perm: 'route.approve' },
      { id: 'credential', label: '机构认证', to: '/credentials', perm: 'activity.manage' },
    ],
  },
  {
    section: '用户与治理',
    items: [
      { id: 'users', label: '用户与工单', to: '/governance', perm: 'user.view' },
      { id: 'appeals', label: '申诉队列', to: '/governance', perm: 'appeal.handle' },
    ],
  },
  {
    section: '系统',
    items: [
      { id: 'audit', label: '审计日志', to: '/audit', perm: 'audit.view' },
      { id: 'roles', label: '角色权限', to: '/roles', perm: 'rbac.manage' },
    ],
  },
];

export function visibleNav(granted: string[]): typeof ADMIN_NAV {
  return ADMIN_NAV.map((g) => ({ ...g, items: g.items.filter((i) => granted.includes(i.perm)) })).filter(
    (g) => g.items.length > 0,
  );
}

export function loginBlockersAdmin(input: { username: string; password: string; granted: string[] }): string[] {
  const out: string[] = [];
  if (!input.username.trim()) out.push('USERNAME_REQUIRED');
  if (input.password.length < 6) out.push('PASSWORD_TOO_SHORT');
  if (input.granted.length === 0) out.push('NO_PERMISSION'); // 无任何权限点不可入工作台
  return out;
}

// ---------- T8.2 审核工作台 ----------

export type QueueTab = 'SUBMISSIONS' | 'COMMENTS' | 'REVIEWS' | 'REPORTS' | 'APPEALS';
export type QueueStateFilter = 'ALL' | 'UNCLAIMED' | 'MINE' | 'OTHER' | 'HANDLED';
export type QueueSlaFilter = 'ALL' | 'URGENT' | 'ESCALATE' | 'BREACH' | 'OK' | 'UNKNOWN';

export const QUEUE_TABS: { id: QueueTab; label: string; perm: Permission }[] = [
  { id: 'SUBMISSIONS', label: '投稿（文章/评测/清单）', perm: 'review.article' },
  { id: 'COMMENTS', label: '评论疑似', perm: 'comment.manage' },
  { id: 'REVIEWS', label: '评价抽审', perm: 'ugv.review.hide' },
  { id: 'REPORTS', label: '举报', perm: 'report.handle' },
  { id: 'APPEALS', label: '申诉', perm: 'appeal.handle' },
];

/** 等待时长 → SLA 三态（24/48 决议，与后端 DashboardRules.slaStatus 同口径） */
export function slaStatus(waitHours: number): 'OK' | 'BREACH' | 'ESCALATE' {
  if (waitHours >= 48) return 'ESCALATE';
  if (waitHours >= 24) return 'BREACH';
  return 'OK';
}

/** 队列行模型（含处理中锁） */
export interface QueueRow {
  id: number;
  type: string;
  title: string;
  submitter: string;
  submittedAt: string;
  waitedHours: number;
  reports: number;
  claimedBy: number | null;
  /** 扩展字段均为可选，兼容已有的队列纯逻辑调用者。 */
  queue?: QueueTab;
  state?: 'PENDING' | 'HANDLED';
  code?: string;
  version?: string;
  batchable?: boolean;
  requiresProfessionalReview?: boolean;
}

/** 不把评测投稿与场所评价抽审混在一个权限队列里。 */
export function queueTabOf(row: QueueRow): QueueTab {
  if (row.queue) return row.queue;
  if (row.type === 'COMMENT') return 'COMMENTS';
  if (row.type === 'POI_REVIEW') return 'REVIEWS';
  if (row.type === 'REPORT' || row.type === 'COPYRIGHT') return 'REPORTS';
  if (row.type === 'APPEAL') return 'APPEALS';
  return 'SUBMISSIONS';
}

/** 版权合格通知立即处理；普通举报和申诉使用独立的 48 小时反馈时限。 */
export function queueSla(row: QueueRow): { tier: Exclude<QueueSlaFilter, 'ALL'>; label: string } {
  if (row.type === 'COPYRIGHT') return { tier: 'URGENT', label: '立即核查' };
  if (row.type === 'REPORT' || row.type === 'APPEAL') {
    return row.waitedHours >= 48
      ? { tier: 'ESCALATE', label: '反馈超 48 小时' }
      : { tier: 'OK', label: '48 小时反馈窗内' };
  }
  if (row.type === 'COMMENT' || row.type === 'POI_REVIEW') {
    return { tier: 'UNKNOWN', label: '时限待核对' };
  }
  const tier = slaStatus(row.waitedHours);
  return {
    tier,
    label: tier === 'ESCALATE' ? '超 48 小时 · 升级' : tier === 'BREACH' ? '超 24 小时' : '目标时限内',
  };
}

export function queueStateOf(row: QueueRow, sampleOperatorId: number): Exclude<QueueStateFilter, 'ALL'> {
  if (row.state === 'HANDLED') return 'HANDLED';
  if (row.claimedBy === null) return 'UNCLAIMED';
  return row.claimedBy === sampleOperatorId ? 'MINE' : 'OTHER';
}

export function queueMatches(row: QueueRow, filters: {
  tab: QueueTab;
  state: QueueStateFilter;
  type: string;
  sla: QueueSlaFilter;
  query: string;
  sampleOperatorId: number;
}): boolean {
  if (queueTabOf(row) !== filters.tab) return false;
  if (filters.state !== 'ALL' && queueStateOf(row, filters.sampleOperatorId) !== filters.state) return false;
  if (filters.type !== 'ALL' && row.type !== filters.type) return false;
  if (filters.sla !== 'ALL' && queueSla(row).tier !== filters.sla) return false;
  const term = filters.query.trim().toLocaleLowerCase();
  return !term || [row.title, row.code ?? String(row.id)].some((value) => value.toLocaleLowerCase().includes(term));
}

export function queueSort(rows: QueueRow[]): QueueRow[] {
  return [...rows].sort((a, b) => {
    const sla = rank(queueSla(b).tier) - rank(queueSla(a).tier);
    if (sla !== 0) return sla; // URGENT → ESCALATE → BREACH → OK
    const reports = b.reports - a.reports;
    return reports !== 0 ? reports : b.waitedHours - a.waitedHours;
  });
  function rank(s: string): number {
    return s === 'URGENT' ? 4 : s === 'ESCALATE' ? 3 : s === 'BREACH' ? 2 : s === 'OK' ? 1 : 0;
  }
}

/** 领取与操作权限（处理中锁：非领取人不可操作 U15 镜像） */
export function canOperateRow(row: QueueRow, operatorId: number): { allowed: boolean; reason: string } {
  if (row.claimedBy !== null && row.claimedBy !== operatorId) {
    return { allowed: false, reason: `处理中锁：已由 #${row.claimedBy} 领取` };
  }
  return { allowed: true, reason: '' };
}

/** 驳回必填意见；通过可附言（模板快捷输入项） */
export function reviewActionBlockers(action: 'approve' | 'reject', note: string): string[] {
  const out: string[] = [];
  if (action === 'reject' && !note.trim()) out.push('REJECT_NOTE_REQUIRED');
  if (note.length > 500) out.push('NOTE_TOO_LONG');
  return out;
}

export const REJECT_TEMPLATES = [
  '标题/封面需调整后重提',
  '正文存在未经证实的医疗结论，请补充来源',
  '内容与频道不符，请修改分类',
] as const;

/** 批量操作仅用于同类型、待处理、低风险且未认领的对象；最终还须服务端复核。 */
export function batchEligible(rows: QueueRow[], sameTabType: boolean): boolean {
  return rows.length > 0 && sameTabType && rows.every((row) =>
    queueTabOf(row) === queueTabOf(rows[0]!) &&
    row.type === rows[0]!.type &&
    row.state !== 'HANDLED' &&
    row.claimedBy === null &&
    row.reports === 0 &&
    row.batchable !== false &&
    row.requiresProfessionalReview !== true,
  );
}

/** 键位语义；只读预览页中的 A/R 仅聚焦条件，不执行审核。 */
export const REVIEW_SHORTCUTS: Record<string, string> = {
  Enter: 'open',
  a: 'approve',
  r: 'reject',
  Escape: 'close',
};

// ---------- T8.5 看板 / 角色 / 审计 ----------

export function dashboardWidgets(granted: string[]): string[] {
  const widgets: string[] = [];
  if (granted.includes('dashboard.view.all') || granted.includes('review.article')) widgets.push('内容域');
  if (granted.includes('product.create.edit')) widgets.push('导购域');
  if (granted.includes('poi.create.edit')) widgets.push('探索域');
  if (granted.includes('report.handle')) widgets.push('治理域');
  if (granted.includes('user.ban')) widgets.push('留存域');
  return widgets; // 各运营看自己权限的域（§6.1 注）
}

/** 角色保存校验：32 白名单 + 内置不可改（administrator 永不可编辑） */
export function roleSaveBlockers(input: {
  roleName: string;
  permissions: string[];
  builtin: boolean;
}): string[] {
  const out: string[] = [];
  if (!/^[a-zA-Z][a-zA-Z0-9_-]{2,31}$/.test(input.roleName)) out.push('ROLE_NAME_INVALID');
  if (input.permissions.length === 0) out.push('PERMISSIONS_EMPTY');
  const invalid = input.permissions.filter((p) => !(PERMISSIONS as readonly string[]).includes(p));
  if (invalid.length > 0) out.push(`INVALID_PERMISSIONS:${invalid.join(',')}`);
  if (input.roleName === 'administrator') out.push('ADMIN_ROLE_IMMUTABLE');
  if (input.builtin) out.push('BUILTIN_ROLE_IMMUTABLE');
  return out;
}

export function presetRoleMatrix(): Record<string, string[]> {
  return Object.fromEntries(Object.entries(ROLES).map(([k, v]) => [k, [...v]]));
}

/** 审计查询：区间 ≤90 天 + 值脱敏（与后端 AuditRules 同口径镜像） */
export function auditQueryBlockers(from: string, to: string): string[] {
  if (!from || !to) return [];
  if (from > to) return ['RANGE_INVALID'];
  const days = (Date.parse(to) - Date.parse(from)) / 86_400_000;
  return days > 90 ? ['RANGE_TOO_WIDE'] : [];
}

export function redactValue(value: string): string {
  return value
    .replace(/(?<!\d)(1[3-9]\d)\d{4}(\d{4})(?!\d)/g, '$1****$2')
    .replace(/([A-Za-z0-9._%+-])[A-Za-z0-9._%+-]*@([A-Za-z0-9.-]+\.[A-Za-z]{2,})/g, '$1***@$2');
}

export { PERMISSIONS, ROLES, can };
