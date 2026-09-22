/**
 * K 指标复算纯函数（T11.6 · U91 四场景：零分母/跨日/重复/无回执）。
 * 本机可逻辑测；K01–K19 全量复算随本地阶段接入真实数据后执行。
 */

export interface DailyPoint {
  day: string; // YYYY-MM-DD
  value: number;
  dedupKey?: string; // 重复上报去重
}

/** 比率：零分母 → null（不产生 0% 假象，U91-1） */
export function rate(part: number, total: number): number | null {
  if (total <= 0) return null;
  if (part < 0 || part > total) return throwInvalid();
  return Math.round((part * 1000) / total) / 10;
}

function throwInvalid(): never {
  throw new RangeError('metric invalid: part/total 越界（U91 脏数据防护）');
}

/** 跨日聚合：按自然日求和，null 忽略（U91-2） */
export function sumDays(points: DailyPoint[]): number {
  return points.reduce((acc, p) => acc + (p.value ?? 0), 0);
}

/** 跨日去重：同 dedupKey 只计一次（U91-3 重复上报） */
export function sumDedup(points: DailyPoint[]): number {
  const seen = new Set<string>();
  let sum = 0;
  for (const p of points) {
    if (p.dedupKey) {
      if (seen.has(p.dedupKey)) continue;
      seen.add(p.dedupKey);
    }
    sum += p.value ?? 0;
  }
  return sum;
}

/** 无回执：发送数与回执数分别统计，回执率零回执 → null（U91-4） */
export function receiptRate(sent: number, acked: number): number | null {
  if (sent <= 0) return null;
  return rate(Math.min(acked, sent), sent);
}

/** 分位数（近似插值：ceil 法） */
export function percentile(values: number[], p: number): number | null {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const idx = Math.min(sorted.length - 1, Math.max(0, Math.ceil((p / 100) * sorted.length) - 1));
  return sorted[idx]!;
}

/** SLA（24/48 决议同口径） */
export function slaOf(waitHours: number): 'OK' | 'BREACH' | 'ESCALATE' {
  if (waitHours >= 48) return 'ESCALATE';
  if (waitHours >= 24) return 'BREACH';
  return 'OK';
}

/** K 样例复算表（T11.6：以原始计数重算并与公布值比对） */
export interface KSample {
  id: string;
  compute: () => number | null;
  published: number | null;
}

export function kSamples(): KSample[] {
  return [
    { id: 'K-通过率样例', compute: () => rate(18, 20), published: 90.0 },
    { id: 'K-提醒完成率', compute: () => rate(7, 10), published: 70.0 },
    { id: 'K-举报按时率', compute: () => rate(9, 10), published: 90.0 },
    { id: 'K-零分母', compute: () => rate(0, 0), published: null },
    { id: 'K-回执率(未发送)', compute: () => receiptRate(0, 0), published: null },
    { id: 'K-队列P50', compute: () => percentile([1, 2, 3, 4, 50], 50), published: 3 },
    { id: 'K-队列P90', compute: () => percentile([1, 2, 3, 4, 50], 90), published: 50 },
  ];
}
