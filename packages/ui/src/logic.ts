/**
 * 跨端组件纯逻辑（可进程内测试，不依赖 Vue 运行时）。
 * 视觉规格追溯：官网设计-UI视觉规范 §4 / 移动端设计-UI视觉规范 §4。
 */

export type ButtonVariant = 'primary' | 'secondary' | 'ghost';
export type ButtonSize = 'm' | 's';

export function buttonClass(variant: ButtonVariant = 'primary', size: ButtonSize = 'm', disabled = false): string {
  const cls = ['cp-btn', `cp-btn--${variant}`, `cp-btn--${size}`];
  if (disabled) cls.push('is-disabled');
  return cls.join(' ');
}

/** 审核状态 → 徽标色调（功能设计总纲 §2.1 统一四色 + 补充态） */
export type StatusTone = 'gray' | 'orange' | 'blue' | 'green' | 'red';

const STATUS_TONE: Record<string, StatusTone> = {
  DRAFT: 'gray',
  PENDING: 'orange',
  REVIEWING: 'blue',
  PUBLISHED: 'green',
  REJECTED: 'red',
  TAKEDOWN: 'gray',
  WITHDRAWN: 'gray',
  // 工单/提醒
  OPEN: 'orange',
  DONE: 'green',
  EXPIRED: 'red',
  CLOSED: 'gray',
};

export function statusTone(status: string): StatusTone {
  return STATUS_TONE[status] ?? 'gray';
}

/** 星级填充百分比（0–5 → 0–100） */
export function starFillPercent(average: number): number {
  const clamped = Math.min(5, Math.max(0, average));
  return Math.round((clamped / 5) * 1000) / 10; // 保留 1 位小数
}

// ---------- Toast 队列（同屏不堆叠、自动消失 —— 动效规范） ----------
export type ToastTone = 'info' | 'success' | 'error';
export interface ToastItem {
  id: number;
  message: string;
  tone: ToastTone;
}

export interface ToastStore {
  readonly items: readonly ToastItem[];
  push(message: string, tone?: ToastTone): number;
  dismiss(id: number): void;
  clear(): void;
}

export function createToastStore(maxVisible = 1): ToastStore {
  let seq = 0;
  let items: ToastItem[] = [];
  return {
    get items() {
      return items;
    },
    push(message: string, tone: ToastTone = 'info'): number {
      const id = ++seq;
      // 同屏不堆叠：超容量时顶掉最旧的
      items = [...items, { id, message, tone }].slice(-maxVisible);
      return id;
    },
    dismiss(id: number): void {
      items = items.filter((t) => t.id !== id);
    },
    clear(): void {
      items = [];
    },
  };
}

/** 空态配置：插画主题三件套（打盹=无数据 / 迷路=无结果 / 好奇=首次引导） */
export type EmptyKind = 'loading' | 'not-found' | 'first-use';

export function emptyCopy(kind: EmptyKind): { illustration: string; title: string } {
  switch (kind) {
    case 'not-found':
      return { illustration: 'lost', title: '这里空空的' };
    case 'first-use':
      return { illustration: 'curious', title: '从这里开始吧' };
    case 'loading':
    default:
      return { illustration: 'dozing', title: '加载中' };
  }
}
