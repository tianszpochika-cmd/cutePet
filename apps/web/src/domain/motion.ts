/**
 * 动效与三态领域逻辑（T7.10 · 动效规范/闭环文档 §4：业务成功前不得播放完成照护动效）。
 */

/** 页面三态（+ready）解析 —— 空/载/错覆盖全站 */
export function pageState(input: {
  loading: boolean;
  error: string | null;
  empty: boolean;
}): 'loading' | 'error' | 'empty' | 'ready' {
  if (input.loading) return 'loading';
  if (input.error) return 'error';
  if (input.empty) return 'empty';
  return 'ready';
}

/** 级联延迟：base 80ms，同组最多 8 项（动效规范 §2.2） */
export function staggerDelay(index: number, base = 80, cap = 8): number {
  if (index < 0) return 0;
  return Math.min(index, cap) * base;
}

export interface MotionPlan {
  transform: boolean;
  durationMs: number;
  loop: boolean;
}

/** reduced-motion 降级：仅保留 ≤100ms 的 opacity，关位移/缩放/循环 */
export function motionPlan(reduced: boolean, kind: 'enter' | 'hover' | 'float' | 'success'): MotionPlan {
  if (reduced) {
    return { transform: false, durationMs: 100, loop: false };
  }
  switch (kind) {
    case 'enter':
      return { transform: true, durationMs: 450, loop: false };
    case 'hover':
      return { transform: true, durationMs: 150, loop: false };
    case 'float':
      return { transform: true, durationMs: 3000, loop: true };
    case 'success':
      return { transform: true, durationMs: 400, loop: false };
  }
}

/** 闭环 §4 硬规则：业务成功前不得播放"完成照护"动效 */
export function successMotionGate(provided: boolean, businessSucceeded: boolean): boolean {
  return provided && businessSucceeded;
}

/** 循环动画预算（动效规范 §7：Web ≤3 / 移动 ≤1） */
export const LOOP_BUDGET = { web: 3, mobile: 1 } as const;

export function loopWithinBudget(platform: 'web' | 'mobile', currentLoops: number): boolean {
  return currentLoops <= LOOP_BUDGET[platform];
}

/** 骨架屏行数建议 */
export function skeletonRows(section: 'home' | 'feed' | 'list' | 'detail'): number {
  switch (section) {
    case 'home':
      return 6;
    case 'feed':
      return 12;
    case 'list':
      return 8;
    case 'detail':
      return 4;
  }
}

/** 错误态文案（统一） */
export function errorCopy(error: string | null): string {
  if (!error) return '';
  return `加载失败：${error}（可重试；dev 环境需对应服务在运行）`;
}
