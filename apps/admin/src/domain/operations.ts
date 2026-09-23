/**
 * 管理端运营领域逻辑（T8.3 内容/商品/POI/纠错/路线活动运营页）。
 */

// ---------- 文章与分类 ----------

export function takedownBlockers(reason: string): string[] {
  return reason.trim() ? [] : ['REASON_REQUIRED']; // 下架必附原因
}

export function pinAllowed(articleState: string): boolean {
  return articleState === 'PUBLISHED';
}

export function taxonomyTagValid(name: string): boolean {
  const t = name.trim();
  return t.length >= 1 && t.length <= 24;
}

export function bannerScheduleValid(startsAt: string, endsAt: string): boolean {
  if (!startsAt && !endsAt) return true; // 两端都空才是常驻
  if (!startsAt || !endsAt) return false;
  return endsAt > startsAt;
}

// ---------- 商品与 CSV（镜像后端 ProductRules 三通道同规则 U77） ----------

const BANNED = ['兽药', '处方'];

export function productBanned(category: string, name: string): boolean {
  for (const kw of BANNED) {
    if (category.includes(kw) || name.includes(kw)) return true;
  }
  return category === '医疗保健' && name.includes('药');
}

export interface CsvParseResult {
  accepted: { line: number; name: string; category: string; source: string }[];
  rejected: { line: number; reason: string }[];
}

/** 仅用于本地预检；服务端仍须对原文件逐行复检。 */
function csvCells(line: string): string[] | null {
  const cells: string[] = [];
  let current = '';
  let quoted = false;
  let afterQuote = false;
  for (let i = 0; i < line.length; i += 1) {
    const char = line[i];
    if (quoted) {
      if (char === '"' && line[i + 1] === '"') { current += '"'; i += 1; }
      else if (char === '"') { quoted = false; afterQuote = true; }
      else current += char;
    } else if (char === ',') {
      cells.push(current.trim()); current = ''; afterQuote = false;
    } else if (char === '"' && !current.trim() && !afterQuote) {
      quoted = true;
    } else if (afterQuote && char !== ' ' && char !== '\t') {
      return null;
    } else if (char === '"') {
      return null;
    } else if (!afterQuote) {
      current += char;
    }
  }
  if (quoted) return null;
  cells.push(current.trim());
  return cells;
}

/** CSV 行格式：name,category,sourceType；本地预检不代表平台受理。 */
export function parseProductCsv(content: string): CsvParseResult {
  const accepted: CsvParseResult['accepted'] = [];
  const rejected: CsvParseResult['rejected'] = [];
  const lines = content.replace(/^\uFEFF/, '').replace(/\r\n?/g, '\n').split('\n');
  let firstData = true;
  lines.forEach((line, i) => {
    if (!line.trim()) return;
    const cells = csvCells(line);
    if (firstData && cells?.length === 3 && cells[0]?.toLowerCase() === 'name' &&
        cells[1]?.toLowerCase() === 'category' && cells[2]?.toLowerCase() === 'sourcetype') {
      firstData = false;
      return;
    }
    firstData = false;
    if (!cells || cells.length !== 3) {
      rejected.push({ line: i + 1, reason: '字段缺失或列数不符' });
      return;
    }
    const [name = '', category = '', source = ''] = cells;
    if (!name || !category) {
      rejected.push({ line: i + 1, reason: '字段缺失' });
      return;
    }
    if (!['EDITORIAL', 'BRAND_INFO', 'USER_SUBMIT', 'CSV_IMPORT'].includes(source)) {
      rejected.push({ line: i + 1, reason: '来源类型不合法' });
      return;
    }
    if (productBanned(category, name)) {
      rejected.push({ line: i + 1, reason: '禁止品类（兽药/处方）' });
      return;
    }
    accepted.push({ line: i + 1, name, category, source });
  });
  return { accepted, rejected };
}

// ---------- 纠错工单（镜像后端 CorrectionRules） ----------

export const CORRECTION_FIELDS = ['address', 'phone', 'open_hours', 'closed', 'attrs'] as const;

export function correctionResolveBlockers(accept: boolean, note: string): string[] {
  const out: string[] = [];
  if (accept && !note.trim()) out.push('ACCEPT_VALUE_REQUIRED'); // 采纳需有效回写值
  if (!accept && !note.trim()) out.push('REJECT_NOTE_REQUIRED');
  return out;
}

export function correctionWriteBackValue(field: string, proposed: string): { field: string; value: string } {
  return { field, value: proposed.trim() };
}

// ---------- 评价抽审 / 路线 / 活动 ----------

export function reviewHideBlockers(state: string, restore: boolean): string[] {
  if (restore && state !== 'HIDDEN') return ['NOT_HIDDEN'];
  if (!restore && state === 'HIDDEN') return ['ALREADY_HIDDEN'];
  return [];
}

export function routeReviewBlockers(approve: boolean, note: string, routeState: string): string[] {
  const out: string[] = [];
  if (routeState === 'PUBLISHED') out.push('ALREADY_PUBLISHED');
  if (!approve && !note.trim()) out.push('REJECT_NOTE_REQUIRED');
  return out;
}

/** 活动重大变更通过 → 跟踪通知结果；驳回 → 保留旧信息（闭环 §3） */
export function activityChangeOutcome(approved: boolean): { nextState: string; notify: string } {
  return approved
    ? { nextState: 'PUBLISHED', notify: 'CHANGE_APPROVED_NOTIFY_SIGNUPS' }
    : { nextState: 'PUBLISHED', notify: 'CHANGE_REJECTED_KEEP_OLD' };
}

export function csvImportSummary(result: { accepted: unknown[]; rejected: { line: number; reason: string }[] }): string {
  return `本地预检：通过 ${result.accepted.length} 条，拒绝 ${result.rejected.length} 条；未导入平台${
    result.rejected.length ? `（首条：第 ${result.rejected[0]!.line} 行 ${result.rejected[0]!.reason}）` : ''
  }`;
}
