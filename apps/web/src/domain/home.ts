/**
 * 首页聚合逻辑（T7.3 · 信息架构 §2 首页区块 · L2 生命链枢纽）。
 */

export interface HomeContext {
  loggedIn: boolean;
  petCount: number;
  hasUnfinishedWizard: boolean;
  pendingReminders: number;
}

export type HomeSectionId =
  | 'loginGuide'
  | 'reminderBar'
  | 'myPets'
  | 'quickRecord'
  | 'goods'
  | 'nearbyExplore'
  | 'feed';

export interface SectionVisibility {
  id: HomeSectionId;
  visible: boolean;
  reason: string;
}

/**
 * 首页区块可见性（自上而下顺序即渲染顺序）：
 * - 未登录：宠物区 → 登录引导卡（L1 闸门）；资讯/用品/探索游客可读
 * - 提醒条：仅有待办时置顶出现（L2）
 * - 「＋记录」仅登录且有宠物时展示
 */
export function homeSections(ctx: HomeContext): SectionVisibility[] {
  return [
    {
      id: 'loginGuide',
      visible: !ctx.loggedIn,
      reason: ctx.loggedIn ? '已登录隐藏' : '未登录展示建档引导（L1 闸门）',
    },
    {
      id: 'reminderBar',
      visible: ctx.loggedIn && ctx.pendingReminders > 0,
      reason: ctx.pendingReminders > 0 ? `待办 ${ctx.pendingReminders} 条置顶` : '无待办隐藏',
    },
    {
      id: 'myPets',
      visible: ctx.loggedIn && ctx.petCount > 0,
      reason: ctx.petCount > 0 ? '宠物卡' : ctx.loggedIn ? '空态→建档引导' : '未登录隐藏',
    },
    {
      id: 'quickRecord',
      visible: ctx.loggedIn && ctx.petCount > 0,
      reason: 'L2 生命链：首页一步到记录',
    },
    { id: 'goods', visible: true, reason: '游客可读（用品并入首页·4-Tab 决议）' },
    { id: 'nearbyExplore', visible: true, reason: '游客可读' },
    { id: 'feed', visible: true, reason: '游客可读' },
  ];
}

export function renderOrder(ctx: HomeContext): HomeSectionId[] {
  return homeSections(ctx)
    .filter((s) => s.visible)
    .map((s) => s.id);
}

/** 待办提醒条文案（排序取最近一条） */
export function reminderBarModel(reminders: { petName: string; type: string; due: string }[]): {
  text: string;
  count: number;
} | null {
  if (reminders.length === 0) return null;
  const sorted = [...reminders].sort((a, b) => a.due.localeCompare(b.due));
  const first = sorted[0]!;
  return {
    text: `${first.petName} · ${first.type} · ${first.due}`,
    count: sorted.length,
  };
}

/** 未完成建档草稿 → 列表「继续完善」提示 */
export function unfinishedWizardHint(ctx: HomeContext): string | null {
  return ctx.loggedIn && ctx.hasUnfinishedWizard ? '继续完善宠物档案' : null;
}
