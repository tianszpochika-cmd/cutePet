/**
 * 资讯端领域逻辑（T7.4 · 需求-资讯 C1–C6 · 机审/评论镜像后端 InteractionRules）。
 */

export const CHANNELS = ['推荐', '犬', '猫', '异宠', '营养', '疾病健康', '训练行为', '行业快讯'] as const;

export const COMMENT_MAX = 500;

export interface ArticleCard {
  id: number;
  slug: string;
  title: string;
  channel: string;
  authorId: number;
  likes: number;
  comments: number;
  top?: boolean;
}

/** 频道 chip → 请求参数（推荐=不传 channel） */
export function channelParam(channel: string): string | undefined {
  return channel === '推荐' ? undefined : channel;
}

/** 疾病/营养频道文末固定免责声明（合规 B6） */
export function disclaimerFor(channel: string): string | null {
  if (channel === '疾病健康' || channel === '营养') {
    return '内容仅供科普参考，不构成兽医诊疗建议，用药与诊疗请遵医嘱。';
  }
  return null;
}

/** 行业快讯边界提示（合规 B6：严禁时政） */
export function channelBoundaryNote(channel: string): string | null {
  return channel === '行业快讯' ? '仅宠物行业动态，不涉时政与社会新闻。' : null;
}

/** 评论可见性（镜像后端：SUSPECT 仅自见、REJECT 双不可见） */
export function commentVisibility(machineState: string, viewerIsAuthor: boolean): 'VISIBLE' | 'ONLY_SELF' | 'HIDDEN' {
  if (machineState === 'REJECT') return 'HIDDEN';
  if (machineState === 'SUSPECT') return viewerIsAuthor ? 'ONLY_SELF' : 'HIDDEN';
  return 'VISIBLE';
}

export function validateComment(content: string): string | null {
  const trimmed = content.trim();
  if (trimmed.length === 0) return 'COMMENT_EMPTY';
  if (trimmed.length > COMMENT_MAX) return 'COMMENT_TOO_LONG';
  return null;
}

/** 审核结果文案（消息中心/投稿状态共用） */
export function reviewStateCopy(state: string): { label: string; tone: 'gray' | 'orange' | 'blue' | 'green' | 'red' } {
  switch (state) {
    case 'DRAFT':
      return { label: '草稿', tone: 'gray' };
    case 'PENDING':
      return { label: '待审核', tone: 'orange' };
    case 'REVIEWING':
      return { label: '审核中', tone: 'blue' };
    case 'PUBLISHED':
      return { label: '已发布', tone: 'green' };
    case 'REJECTED':
      return { label: '已驳回', tone: 'red' };
    case 'TAKEDOWN':
      return { label: '已下架', tone: 'gray' };
    case 'WITHDRAWN':
      return { label: '已撤回', tone: 'gray' };
    default:
      return { label: state, tone: 'gray' };
  }
}

/** 分享卡片深链（站内回跳，登录后 resume） */
export function sharePath(kind: 'article' | 'summary' | 'route' | 'event', idOrSlug: string): string {
  switch (kind) {
    case 'article':
      return `/news/${idOrSlug}`;
    case 'summary':
      return `/pets/${idOrSlug}`;
    case 'route':
      return `/explore/routes?id=${idOrSlug}`;
    case 'event':
      return `/events?id=${idOrSlug}`;
  }
}

/** 收藏五分组（IA：内容/商品/清单/场所/路线） */
export const FAVORITE_TABS = ['内容', '商品', '清单', '场所', '路线'] as const;

/** 搜索高亮切分（与后端 SearchRules.highlight 同语义） */
export function highlight(text: string, query: string): { text: string; hit: boolean }[] {
  const q = query.trim();
  if (!q || !text.includes(q)) return [{ text, hit: false }];
  const out: { text: string; hit: boolean }[] = [];
  let idx = 0;
  let found: number;
  while ((found = text.indexOf(q, idx)) >= 0) {
    if (found > idx) out.push({ text: text.slice(idx, found), hit: false });
    out.push({ text: text.slice(found, found + q.length), hit: true });
    idx = found + q.length;
  }
  if (idx < text.length) out.push({ text: text.slice(idx), hit: false });
  return out;
}

/** 空搜索结果 → 兜底推荐（U17 文案） */
export function emptySearchCopy(query: string): string {
  return `未找到「${query}」相关内容，试试下面的推荐？`;
}
