/**
 * 宠物生命链领域逻辑（T7.2 · 需求-宠物管理 B1–B6 · 后端格式镜像）。
 * recurrenceRule 字符串与 pet-service ReminderService.parseRecurrence 严格一致。
 */
import { needsWeightConfirm, trendMarked } from '../../../../packages/contracts/src/domain.ts';

// ---------- 建档向导（4 步，L1 激活链） ----------

export const WIZARD_STEPS = ['物种', '基础信息', '照片', '体重基线'] as const;

export const SPECIES = ['犬', '猫', '异宠'] as const;

export interface WizardInfo {
  species?: string;
  name?: string;
  breed?: string;
  weightKg?: number;
}

/** 每步校验：返回 null=通过，否则阻断码 */
export function validateWizardStep(stepIndex: number, info: WizardInfo): string | null {
  switch (stepIndex) {
    case 0:
      return info.species && (SPECIES as readonly string[]).includes(info.species)
        ? null
        : 'SPECIES_REQUIRED';
    case 1: {
      const name = (info.name ?? '').trim();
      if (name.length < 1 || name.length > 20) return 'NAME_INVALID';
      if (!info.breed?.trim()) return 'BREED_REQUIRED';
      return null;
    }
    case 2:
      return null; // 照片可跳过
    case 3: {
      const w = info.weightKg;
      if (w === undefined || Number.isNaN(w) || w < 0.1 || w > 200) return 'WEIGHT_INVALID';
      return null;
    }
    default:
      return 'STEP_OUT_OF_RANGE';
  }
}

export function wizardComplete(info: WizardInfo): boolean {
  for (let i = 0; i < WIZARD_STEPS.length; i++) {
    if (validateWizardStep(i, info) !== null) return false;
  }
  return true;
}

// ---------- 记录表单（7 类字段定义 · 需求 B4） ----------

export interface FieldDef {
  key: string;
  label: string;
  required: boolean;
  type: 'date' | 'text' | 'textarea' | 'number' | 'dateRange' | 'photo';
}

export const RECORD_KINDS = ['疫苗', '驱虫', '体检', '就医', '用药', '过敏', '体重'] as const;

export function recordFields(kind: string): FieldDef[] {
  switch (kind) {
    case '体重':
      return [
        { key: 'weightKg', label: '体重(kg)', required: true, type: 'number' },
        { key: 'measuredAt', label: '测量时间', required: true, type: 'date' },
        { key: 'note', label: '备注', required: false, type: 'textarea' },
      ];
    case '疫苗':
      return [
        { key: 'kindName', label: '疫苗名称', required: true, type: 'text' },
        { key: 'eventDate', label: '接种日期', required: true, type: 'date' },
        { key: 'org', label: '接种机构', required: false, type: 'text' },
        { key: 'nextReminderDate', label: '下次提醒(自动排期)', required: false, type: 'date' },
        { key: 'photo', label: '照片', required: false, type: 'photo' },
      ];
    case '驱虫':
      return [
        { key: 'kindName', label: '体内/体外', required: true, type: 'text' },
        { key: 'eventDate', label: '日期', required: true, type: 'date' },
        { key: 'nextReminderDate', label: '下次提醒(自动排期)', required: false, type: 'date' },
      ];
    case '体检':
      return [
        { key: 'eventDate', label: '体检日期', required: true, type: 'date' },
        { key: 'org', label: '机构', required: false, type: 'text' },
        { key: 'note', label: '结果摘要', required: true, type: 'textarea' },
      ];
    case '就医':
      return [
        { key: 'eventDate', label: '就诊日期', required: true, type: 'date' },
        { key: 'note', label: '原因', required: true, type: 'textarea' },
        { key: 'org', label: '医院', required: false, type: 'text' },
        { key: 'doctor', label: '医生', required: false, type: 'text' },
      ];
    case '用药':
      return [
        { key: 'kindName', label: '药名', required: true, type: 'text' },
        { key: 'validFrom', label: '开始日期', required: true, type: 'date' },
        { key: 'validTo', label: '结束日期', required: false, type: 'date' },
        { key: 'note', label: '医嘱', required: false, type: 'textarea' },
      ];
    case '过敏':
      return [
        { key: 'kindName', label: '过敏原', required: true, type: 'text' },
        { key: 'note', label: '反应', required: true, type: 'textarea' },
      ];
    default:
      return [];
  }
}

export function validateRecord(kind: string, values: Record<string, unknown>): string | null {
  for (const field of recordFields(kind)) {
    const v = values[field.key];
    if (field.required && (v === undefined || v === null || String(v).trim() === '')) {
      return `REQUIRED:${field.key}`;
    }
  }
  const from = values.validFrom as string | undefined;
  const to = values.validTo as string | undefined;
  if (from && to && to < from) return 'MED_COURSE_INVALID'; // 起止关系（BR-07）
  const eventDate = values.eventDate as string | undefined;
  if (eventDate) {
    const today = new Date().toISOString().slice(0, 10);
    if (eventDate > today) return 'EVENT_FUTURE_NOT_ALLOWED'; // U67：未来已发生事件拒绝
  }
  return null;
}

// ---------- 提醒周期规则（后端解析格式镜像） ----------

export type Recurrence =
  | { kind: 'once'; date: string }
  | { kind: 'everyDays'; days: number; start: string }
  | { kind: 'yearly'; month: number; day: number; start: string };

/** → once:"2026-10-01" | everyDays:"30|2026-09-01" | yearly:"2|29|2028-03-01" */
export function buildRecurrenceRule(rule: Recurrence): string {
  switch (rule.kind) {
    case 'once':
      return rule.date;
    case 'everyDays':
      if (!Number.isInteger(rule.days) || rule.days < 1) throw new Error('周期天数必须为正整数');
      return `${rule.days}|${rule.start}`;
    case 'yearly':
      if (rule.month < 1 || rule.month > 12 || rule.day < 1 || rule.day > 31) {
        throw new Error('年周期月日不合法');
      }
      return `${rule.month}|${rule.day}|${rule.start}`;
  }
}

export function parseRecurrenceRule(kind: string, rule: string): Recurrence {
  const parts = rule.split('|');
  switch (kind) {
    case 'once':
      return { kind: 'once', date: parts[0]! };
    case 'everyDays':
      return { kind: 'everyDays', days: Number(parts[0]), start: parts[1]! };
    case 'yearly':
      return { kind: 'yearly', month: Number(parts[0]), day: Number(parts[1]), start: parts[2]! };
    default:
      throw new Error(`周期类型不合法: ${kind}`);
  }
}

// ---------- 体重/摘要展示 ----------

export function weightSheetConfirm(prevKg: number | null, nextKg: number): boolean {
  return needsWeightConfirm(prevKg, nextKg);
}

export function trendHighlight(baseKg: number | null, currentKg: number): boolean {
  return trendMarked(baseKg, currentKg);
}

export const SUMMARY_MODULES = ['档案资料', '健康记录', '提醒计划'] as const;

/** 模块勾选变化 → 预览模型（U79：取消/缺项可见） */
export function summaryPreview(
  selected: string[],
  availableWithRecords: string[],
): { included: string[]; omitted: { module: string; reason: string }[] } {
  const included = selected.filter((m) => availableWithRecords.includes(m));
  const omitted: { module: string; reason: string }[] = [];
  for (const m of availableWithRecords) {
    if (!selected.includes(m)) omitted.push({ module: m, reason: '用户取消该模块' });
  }
  for (const m of selected) {
    if (!availableWithRecords.includes(m)) omitted.push({ module: m, reason: '无数据（缺项可见）' });
  }
  return { included, omitted };
}

// ---------- 归档/删除交互（U66） ----------

export function deleteConfirmCopy(state: string): { title: string; detail: string } | null {
  if (state === 'DELETED') {
    return { title: '恢复这只宠物？', detail: '软删 30 天内可恢复，超期需联系客服。' };
  }
  if (state === 'ARCHIVED') {
    return { title: '归档后仍可恢复', detail: '归档宠物不出现在默认列表，历史记录保留。' };
  }
  return { title: '删除这只宠物？', detail: '30 天内可恢复；关联记录与提醒将一并进入待清理。' };
}
