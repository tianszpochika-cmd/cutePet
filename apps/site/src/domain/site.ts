/**
 * 官网领域逻辑（T9.1 产品矩阵 / T9.4 表单 / T9.5 品牌 / T9.6 ★分享·不可用·下载跨端一致 · U94）。
 */

// ---------- 导航与 CTA（信息架构 §8） ----------

export const SITE_NAV = [
  { id: 'home', label: '首页', to: '/' },
  { id: 'products', label: '产品与服务', to: '/products' },
  { id: 'services', label: '服务场景', to: '/services' },
  { id: 'center', label: '内容中心', to: '/center' },
  { id: 'download', label: '下载', to: '/download' },
  { id: 'about', label: '关于我们', to: '/about' },
  { id: 'contact', label: '联系', to: '/contact' },
] as const;

/** 首页四板块入口（与用户端 IA 四区一致） */
export const FOUR_SECTIONS = [
  { id: 'manage', label: '宠物管理', desc: 'L2 生命链：建档 → 记录 → 曲线 → 摘要', to: '/services' },
  { id: 'news', label: '宠物资讯', desc: '7 频道先审后发 + 互动与分享', to: '/center' },
  { id: 'explore', label: '宠物探索', desc: '附近场所 / 路线 / 本地活动与领养信息', to: '/services' },
  { id: 'goods', label: '宠物用品', desc: '纯内容导购（不交易），评测与好物清单', to: '/services' },
] as const;

/** 产品矩阵：四端固定信息（下载页与分享页共用同一源） */
export const PRODUCTS = [
  { id: 'web', label: '用户端 Web', desc: '登录/宠物/资讯/探索/用品/家庭全功能', port: 18580, cta: '打开应用', href: 'http://localhost:18580' },
  { id: 'admin', label: '运营工作台', desc: '审核、治理、看板与 32 权限点 RBAC', port: 18581, cta: '进入工作台', href: 'http://localhost:18581' },
  { id: 'site', label: '官网', desc: '品牌、服务、内容与下载（本站）', port: 18582, cta: '浏览首页', href: '/' },
  { id: 'mobile', label: '移动 H5', desc: '4-Tab 移动体验，复用同一契约', port: 18583, cta: '移动端体验', href: 'http://localhost:18583' },
] as const;

export function ctaFor(pathname: string): { label: string; to: string } {
  if (pathname.startsWith('/download')) return { label: '下载用户端', to: '/download' };
  if (pathname.startsWith('/contact')) return { label: '商务合作', to: '/contact' };
  if (pathname.startsWith('/products') || pathname.startsWith('/services')) {
    return { label: '立即体验', to: '/download' };
  }
  return { label: '开始体验', to: '/download' };
}

// ---------- T9.4 联系表单 ----------

export function contactBlockers(input: { name: string; email: string; kind: string; message: string }): string[] {
  const out: string[] = [];
  if (!input.name.trim()) out.push('NAME_REQUIRED');
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.email)) out.push('EMAIL_INVALID');
  if (!['商务合作', '媒体采访', '问题反馈', '投诉举报'].includes(input.kind)) out.push('KIND_INVALID');
  if (input.message.trim().length < 10 || input.message.trim().length > 1000) out.push('MESSAGE_INVALID');
  return out;
}

export const CONTACT_CHANNELS = [
  { id: 'business', label: '商务合作', info: 'business@example.com（占位，上线前替换）' },
  { id: 'support', label: '用户支持', info: '见应用内帮助中心（48h 反馈）' },
  { id: 'report', label: '投诉举报/版权', info: '站内举报通道 + copyright@example.com' },
] as const;

// ---------- T9.5 品牌展示（品牌名与 Logo 为占位——负责人决议项） ----------

export const BRAND_TOKENS = {
  name: 'cutePet（占位名）',
  logoNote: 'Logo 未定稿：本页与各端 favicon 使用 🐾 占位，待负责人提供品牌资产后替换。',
  colors: [
    { token: 'primary', value: '#FF7A2F', usage: '主行动 / 预警' },
    { token: 'blue', value: '#4D8DFF', usage: '用品 / 工具面板' },
    { token: 'green', value: '#2FBF71', usage: '探索地图 / 成功' },
    { token: 'purple', value: '#9B8CFF', usage: '家庭关系图谱' },
    { token: 'ink', value: '#2B2118', usage: '主文本' },
    { token: 'ink2', value: '#7A6E63', usage: '辅助文本' },
    { token: 'bg', value: '#FFF9F3', usage: '页面底色' },
  ],
  fonts: ['display: 圆润粗黑（中文 fallback: PingFang SC Bold）', 'body: 系统 UI 字体栈'],
  motion: ['入场 450ms cubic-bezier(0.22,1,0.36,1)', 'reduced-motion 仅 ≤100ms 透明度', '业务成功前不播放完成动效'],
} as const;

// ---------- T9.6 ★ 分享页（与用户端文章卡同构 · U94） ----------

export interface SharePayload {
  kind: 'article' | 'summary' | 'route' | 'event';
  idOrSlug: string;
  title: string;
  channelLabel: string;
  authorLabel: string;
  state: string; // PUBLISHED / DRAFT / REJECTED / TAKEDOWN / DELETED
}

/** 不可用内容四态文案（U94：官网展示不可用态而非空白） */
export const UNAVAILABLE_COPY: Record<string, string> = {
  DRAFT: '内容尚未发布（草稿状态），暂时不可分享。',
  REJECTED: '内容未通过审核，暂时无法查看。',
  TAKEDOWN: '内容已下架（若为处置，请在应用内查看申诉入口）。',
  DELETED: '内容已被作者删除。',
};

export function shareCardModel(payload: SharePayload): {
  visible: boolean;
  title: string;
  chip: string;
  author: string;
  unavailable?: string;
  webTarget: string;
  loginReturn: string;
} {
  const base = {
    title: payload.title,
    chip: payload.channelLabel,
    author: payload.authorLabel,
    webTarget: `/news/${payload.idOrSlug}`,
    loginReturn: `/news/${payload.idOrSlug}`,
  };
  if (payload.state === 'PUBLISHED') {
    return { visible: true, ...base };
  }
  return { visible: false, unavailable: UNAVAILABLE_COPY[payload.state] ?? UNAVAILABLE_COPY.TAKEDOWN!, ...base };
}

/** 分享登录闸门：进 Web 登录并携带回跳（与 web safeReturnPath 白名单同口径） */
export function shareLoginHref(returnPath: string): string {
  const safe = returnPath.startsWith('/') && !returnPath.startsWith('//') ? returnPath : '/';
  return `/web/login?return=${encodeURIComponent(safe)}`;
}

// ---------- T9.6 ★ 下载落地页：四端一致（单一信息源） ----------

export const DOWNLOAD_SOURCE = {
  version: '0.1.0-dev',
  channel: '设计预览版（非应用商店分发）',
  updated: '2026-09-22',
  features: ['L2 生命链核心', '7 频道资讯与创作', '探索与活动报名', '纯内容导购'],
  checksumNote: '发布前补充 SHA-256 校验值与签名信息',
  storeDistribute: false,
} as const;

export type ClientEnd = 'web' | 'admin' | 'site' | 'mobile';

/** 四端下载信息由同一源生成——结构一致仅端标识不同（跨端一致任务核心） */
export function downloadFor(end: ClientEnd): {
  end: ClientEnd;
  productLabel: string;
  version: string;
  channel: string;
  updated: string;
  features: readonly string[];
  checksumNote: string;
  openHref: string;
} {
  const product = PRODUCTS.find((p) => p.id === end)!;
  return {
    end,
    productLabel: product.label,
    version: DOWNLOAD_SOURCE.version,
    channel: DOWNLOAD_SOURCE.channel,
    updated: DOWNLOAD_SOURCE.updated,
    features: DOWNLOAD_SOURCE.features,
    checksumNote: DOWNLOAD_SOURCE.checksumNote,
    openHref: product.href,
  };
}

export function detectEnd(userAgentLike: string): ClientEnd {
  const ua = userAgentLike.toLowerCase();
  if (ua.includes('mobile') || ua.includes('android') || ua.includes('iphone')) return 'mobile';
  if (ua.includes('admin')) return 'admin';
  if (ua.includes('site')) return 'site';
  return 'web';
}

export function downloadLandingModel(userAgentLike: string): ReturnType<typeof downloadFor> {
  return downloadFor(detectEnd(userAgentLike));
}

// ---------- T9.3 内容中心 / 案例 ----------

export const CASE_CATEGORIES = ['宠物医院', '宠物店', '救助机构', '品牌方'] as const;

export interface CaseItem {
  id: number;
  category: string;
  title: string;
  summary: string;
  published: boolean;
}

export function filterCases(items: CaseItem[], category: string | null): CaseItem[] {
  const base = items.filter((c) => c.published); // 仅已发布上官网（先审后发同源）
  if (!category || category === '全部') return base;
  return base.filter((c) => c.category === category);
}

export const CENTER_NOTICE = '内容中心与用户端资讯同源（先审后发）：DRAFT/REJECTED/TAKEDOWN 内容不出现在本页（U94）。';
