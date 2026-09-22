/**
 * 全端设计令牌 —— 单一事实源。
 * 来源：docs/02-设计文档/视觉设计.md（全端设计系统总纲 §2）。
 * 冲突时以本文为准；官网/移动分册为执行细则。
 */
export const tokens = {
  color: {
    primary: '#FF7A2F',
    primaryHover: '#F26A1B',
    primarySoft: '#FFF1E8',
    ink: '#2B2118',
    ink2: '#7A6E63',
    line: '#F0E6DC',
    bg: '#FFF9F3',
    surface: '#FFFFFF',
    module: {
      manager: '#FF7A2F',
      news: '#FF5C7A',
      explore: '#2FBF71',
      supplies: '#4D8DFF',
      family: '#9B8CFF',
    },
    semantic: {
      success: '#22C55E',
      warning: '#F59E0B',
      error: '#EF4444',
      info: '#4D8DFF',
    },
  },
  /** 4 基数间距阶梯 */
  spacing: [4, 8, 12, 16, 24, 32, 48, 64, 96, 128],
  radius: { s: 8, input: 12, card: 16, cardL: 20, panel: 24, panelL: 28, full: 999 },
  shadow: {
    sh1: '0 2px 8px rgba(43,33,24,.06)',
    sh2: '0 12px 32px rgba(43,33,24,.10)',
    sh3: '0 24px 64px rgba(43,33,24,.14)',
  },
  /** 双基准字阶（px，桌面/移动） */
  typeScale: {
    desktop: { display: 60, h1: 44, h2: 32, h3: 22, bodyL: 18, body: 16, small: 14, micro: 13 },
    mobile: { display: 30, h1: 24, h2: 20, h3: 17, bodyL: 17, body: 15, small: 13, micro: 11 },
  },
  breakpoint: { md: 768, lg: 1200 },
  /** 固定本地端口（strictPort；避让 cloudstudy 18070–18190） */
  port: { gateway: 18470, services: [18480, 18488], web: 18580, admin: 18581, site: 18582, mobile: 18583 },
} as const;

export type Tokens = typeof tokens;
