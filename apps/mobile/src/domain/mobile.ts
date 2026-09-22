/**
 * 移动骨架与导航领域逻辑（T10.1 4-Tab/栈/Sheet/手势/安全区 · L1–L5 层模型 · 决议 4-Tab）。
 */

// ---------- 4-Tab（决议：用品并入首页，不做独立 Tab） ----------

export const TABS = [
  { id: 'home', label: '首页', icon: '🏠', path: '/' },
  { id: 'pet', label: '宠物', icon: '🐾', path: '/pets' },
  { id: 'explore', label: '探索', icon: '🧭', path: '/explore' },
  { id: 'me', label: '我的', icon: '👤', path: '/me' },
] as const;

export type TabId = (typeof TABS)[number]['id'];

/** Tab 切换保留各自导航栈；同 Tab 内 push/pop（栈规则） */
export function stackAction(currentTab: TabId, targetTab: TabId, pushPath?: string): 'SWITCH_KEEP_STACK' | 'PUSH' | 'POP_TO_ROOT' {
  if (currentTab !== targetTab) return 'SWITCH_KEEP_STACK';
  if (pushPath) return 'PUSH';
  return 'POP_TO_ROOT'; // 再点当前 Tab → 回根
}

// ---------- L1–L5 层模型（从登录到所有功能） ----------

export const LAYERS = [
  { id: 'L1', name: '登录闸门', anchors: ['协议双勾', '年龄分支', '监护人'] },
  { id: 'L2', name: '生命链核心', anchors: ['建档', '记录', '曲线', '提醒', '摘要'] },
  { id: 'L3', name: '首页面板', anchors: ['待办条', '宠物卡', '一键记录', '用品分区', '资讯分区'] },
  { id: 'L4', name: '四大分区', anchors: ['资讯创作', '探索活动', '用品', '家庭'] },
  { id: 'L5', name: '我的与设置', anchors: ['宫格', '消息', '通知三通道', '注销', '帮助'] },
] as const;

export function layerCoverage(): { layer: string; anchors: number }[] {
  return LAYERS.map((l) => ({ layer: l.id, anchors: l.anchors.length }));
}

// ---------- 手势与 Sheet（T10.1） ----------

export const PULL_REFRESH_THRESHOLD = 60; // px
export const SHEET_DISMISS_THRESHOLD = 120; // px
export const SHEET_MAX_HEIGHT_RATIO = 0.78; // 视口
export const TAP_TARGET_MIN = 44; // px（无障碍最小点按）

/** 下拉刷新触发判定 */
export function pullRefreshTriggered(dy: number): boolean {
  return dy >= PULL_REFRESH_THRESHOLD;
}

/** Sheet 关闭：位移过阈或快速下滑（速度阈 0.5px/ms） */
export function sheetDismiss(dy: number, velocity: number): boolean {
  return dy >= SHEET_DISMISS_THRESHOLD || (dy > 0 && velocity > 0.5);
}

/** 安全区：底部 Tab 栏高度 = 内容高 + 安全区内边距 */
export function safeAreaInsets(env: { bottom: number; top: number }): { tabBottom: number; headerTop: number } {
  return { tabBottom: Math.max(env.bottom, 12), headerTop: Math.max(env.top, 12) };
}

export function sheetViewportHeight(viewportH: number): number {
  return Math.round(viewportH * SHEET_MAX_HEIGHT_RATIO);
}

/** 栈返回：无历史时回当前 Tab 根 */
export function backTarget(stackDepth: number, tabRoot: string): string {
  return stackDepth > 1 ? '__BACK__' : tabRoot;
}

// ---------- L1 登录闸门（移动侧 · 与 Web auth 同口径） ----------

export function loginBlockersMobile(input: { phone: string; code: string; agreedTerms: boolean; agreedPrivacy: boolean }): string[] {
  const out: string[] = [];
  if (!/^1[3-9]\d{9}$/.test(input.phone)) out.push('PHONE_INVALID');
  if (input.code.length !== 6) out.push('CODE_INVALID');
  if (!input.agreedTerms || !input.agreedPrivacy) out.push('AGREEMENTS_REQUIRED');
  return out;
}
