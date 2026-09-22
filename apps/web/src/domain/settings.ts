/**
 * 设置与合规领域逻辑（T7.9 · #53 注销两维依赖 / X15 受限账号 / 规则五页 / 受限游客 #31）。
 */

/** 规则五页（合规 B8） */
export const RULE_DOCS = [
  { slug: 'terms', label: '用户协议' },
  { slug: 'privacy', label: '隐私政策' },
  { slug: 'community', label: '社区规范' },
  { slug: 'children', label: '儿童个人信息处理规则' },
  { slug: 'report', label: '投诉举报与版权通道' },
] as const;

/** 注销依赖（#53：所有者 + 家庭管理员两维度 + 活动与报名，且给实际入口） */
export interface DeleteDependencies {
  ownedSharedPets: number;
  isFamilyAdmin: boolean;
  activeSignups: number;
  publishedActivities: number;
}

export interface DeleteBlocker {
  code: string;
  reason: string;
  actionLabel: string;
  actionRoute: string;
}

export function deleteBlockers(dep: DeleteDependencies): DeleteBlocker[] {
  const blockers: DeleteBlocker[] = [];
  if (dep.ownedSharedPets > 0) {
    blockers.push({
      code: 'OWNED_SHARED_PETS',
      reason: `你有 ${dep.ownedSharedPets} 只共享中的宠物（所有者维度）`,
      actionLabel: '转移所有权 / 解除共享',
      actionRoute: '/family',
    });
  }
  if (dep.isFamilyAdmin) {
    blockers.push({
      code: 'FAMILY_ADMIN',
      reason: '你是家庭管理员（管理员维度）',
      actionLabel: '转交管理员 / 解散家庭',
      actionRoute: '/family',
    });
  }
  if (dep.publishedActivities > 0) {
    blockers.push({
      code: 'PUBLISHED_ACTIVITIES',
      reason: `你发布了 ${dep.publishedActivities} 个进行中活动`,
      actionLabel: '结束或取消活动',
      actionRoute: '/me/activity-center',
    });
  }
  if (dep.activeSignups > 0) {
    blockers.push({
      code: 'ACTIVE_SIGNUPS',
      reason: `你有 ${dep.activeSignups} 个有效报名`,
      actionLabel: '取消报名',
      actionRoute: '/me/activity-center',
    });
  }
  return blockers;
}

export const COOLDOWN_DAYS = 15;

/** 冷静期展示：剩余天数 + 是否可撤销 */
export function cooldownState(requestedAtIso: string, nowIso: string): {
  daysLeft: number;
  cancellable: boolean;
} {
  const requested = new Date(requestedAtIso).getTime();
  const now = new Date(nowIso).getTime();
  const expiry = requested + COOLDOWN_DAYS * 86_400_000;
  const daysLeft = Math.max(0, Math.ceil((expiry - now) / 86_400_000));
  return { daysLeft, cancellable: now < expiry };
}

/** #53 冷静期内登录 → 明确撤销确认页（不静默撤销） */
export const COOLDOWN_LOGIN_COPY = {
  title: '你的账号正在注销冷静期',
  body: `冷静期 ${COOLDOWN_DAYS} 天内登录会进入撤销确认；只有点击「确认撤销注销」才会恢复账号，单纯登录不自动撤销。`,
  confirmLabel: '确认撤销注销',
  keepLabel: '继续注销',
};

// ---------- X15 受限账号（封禁页） ----------

export const BLOCKED_ALLOWED_ACTIONS = ['申诉', '状态查询', '查看规则', '提交信息请求'] as const;

/** #56：各处罚独立计时，期满仅恢复对应能力 */
export function restoredCapabilities(input: {
  banExpired: boolean;
  muteExpired: boolean;
  postRightSuspended: boolean;
}): string[] {
  const restored: string[] = [];
  if (input.banExpired) restored.push('登录与普通浏览');
  if (input.muteExpired) restored.push('评论与投稿互动');
  if (input.postRightSuspended === false || input.postRightSuspended === true) {
    // 投稿资格独立：未被暂停或已到期才恢复
  }
  return restored;
}

export function blockedCopy(): { title: string; note: string } {
  return {
    title: '账号当前受限',
    note: '你可以在此页完成申诉与状态查询，无需恢复账号即可查看结果；不会跳转普通个人中心。',
  };
}

// ---------- 受限游客（#31：未同意隐私 → 不加载可选分析/定位） ----------

export function guestCapabilities(privacyAgreed: boolean): { analytics: boolean; geo: boolean } {
  return { analytics: privacyAgreed, geo: privacyAgreed };
}

// ---------- 导出/帮助 ----------

export const EXPORT_STEPS = ['验证码身份核验', '生成 JSON（资料+档案+健康记录）', '导出行为留痕'] as const;

export const HELP_SECTIONS = [
  { id: 'account', label: '账号与安全' },
  { id: 'pet', label: '宠物档案' },
  { id: 'content', label: '内容与投稿' },
  { id: 'explore', label: '探索与活动' },
  { id: 'privacy', label: '隐私与合规' },
] as const;

export interface NotificationSettings {
  reminderPush: boolean;
  interactionPush: boolean;
 免打扰: boolean;
}

export function notificationWarnings(s: NotificationSettings): string[] {
  const warnings: string[] = [];
  if (!s.reminderPush) warnings.push('关闭提醒推送后，将收不到疫苗/驱虫等重要提醒（仅站内通知）。');
  if (s.免打扰) warnings.push('免打扰时段内，提醒将顺延至时段结束后送达。');
  return warnings;
}
