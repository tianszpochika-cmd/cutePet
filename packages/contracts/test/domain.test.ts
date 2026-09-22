import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  nextOccurrence,
  addDays,
  diffDays,
  isLeapYear,
  needsWeightConfirm,
  trendMarked,
  jinToKg,
  kgToJin,
  canTransition,
  shouldSuspendForRejections,
  actionForLevel,
  escalateLevel,
} from '../src/domain.ts';

test('U55 每30天双基准：计划日基准与完成日基准各自唯一', () => {
  const rule = { kind: 'everyDays', days: 30, start: '2026-09-01' } as const;
  // 计划日基准（默认）：网格锚定 start —— 9/1 到期 → 10/1
  assert.equal(nextOccurrence(rule, '2026-09-01'), '2026-10-01');
  // 计划侧不随完成日漂移（下期唯一：计划侧仍是 10/1）
  assert.equal(nextOccurrence(rule, '2026-09-05'), '2026-10-01');
  // 完成日基准：9/5 完成 → 10/5（完成日 +30，锚点滚动）
  assert.equal(nextOccurrence(rule, '2026-09-05', 'completion'), '2026-10-05');
  // 越过当期继续推进不重复
  assert.equal(nextOccurrence(rule, '2026-10-01'), '2026-10-31');
});

test('everyDays 锚点前 after 返回起点；非法周期抛错', () => {
  const rule = { kind: 'everyDays', days: 7, start: '2026-09-10' } as const;
  assert.equal(nextOccurrence(rule, '2026-09-01'), '2026-09-10');
  assert.throws(() => nextOccurrence({ kind: 'everyDays', days: 0, start: '2026-09-10' }, '2026-09-01'));
});

test('once 到期后返回 null', () => {
  assert.equal(nextOccurrence({ kind: 'once', date: '2026-10-01' }, '2026-09-30'), '2026-10-01');
  assert.equal(nextOccurrence({ kind: 'once', date: '2026-10-01' }, '2026-10-01'), null);
});

test('U56 yearly 2/29 跨非闰年：当年落 2/28，其后恢复 2/29', () => {
  assert.equal(isLeapYear(2028), true);
  assert.equal(isLeapYear(2027), false);
  const rule = { kind: 'yearly', month: 2, day: 29, start: '2028-03-01' } as const;
  assert.equal(nextOccurrence(rule, '2026-06-01'), '2027-02-28'); // 非闰年 2 月末
  assert.equal(nextOccurrence(rule, '2027-03-01'), '2028-02-29'); // 恢复原月日
});

test('yearly 常规：年内未到给当年，已过给次年', () => {
  const rule = { kind: 'yearly', month: 6, day: 15, start: '2026-01-01' } as const;
  assert.equal(nextOccurrence(rule, '2026-03-01'), '2026-06-15');
  assert.equal(nextOccurrence(rule, '2026-06-15'), '2027-06-15'); // 当天不重复
});

test('日期工具：加减天数与差值跨月正确', () => {
  assert.equal(addDays('2026-09-30', 1), '2026-10-01');
  assert.equal(addDays('2026-01-01', -1), '2025-12-31');
  assert.equal(diffDays('2026-10-01', '2026-09-01'), 30);
});

test('体重 ≥10% 二次确认（含边界 10% 触发、9.9% 不触发）', () => {
  assert.equal(needsWeightConfirm(10, 11), true); // 恰好 10%
  assert.equal(needsWeightConfirm(10, 10.99), false); // <10%
  assert.equal(needsWeightConfirm(null, 50), false); // 无基准
  assert.equal(needsWeightConfirm(10, 9), true); // 下降同规则
});

test('趋势高亮 ≥5% 且无基准不伪造（U80）', () => {
  assert.equal(trendMarked(10, 10.5), true);
  assert.equal(trendMarked(10, 10.49), false);
  assert.equal(trendMarked(null, 12), false);
});

test('斤/kg 换算', () => {
  assert.equal(jinToKg(10), 5);
  assert.equal(kgToJin(5.5), 11);
});

test('投稿状态机：先审后发闭环 + 非法跳转拒绝（U74/U75）', () => {
  assert.equal(canTransition('DRAFT', 'PENDING'), true);
  assert.equal(canTransition('PENDING', 'REVIEWING'), true);
  assert.equal(canTransition('REVIEWING', 'PUBLISHED'), true);
  assert.equal(canTransition('REJECTED', 'PENDING'), true); // 修改重提
  assert.equal(canTransition('DRAFT', 'PUBLISHED'), false); // 草稿不可直发
  assert.equal(canTransition('PUBLISHED', 'TAKEDOWN'), true);
  assert.equal(canTransition('TAKEDOWN', 'REJECTED'), false); // 下架后不可进审核态
});

test('3 次驳回触发暂停投稿 7 天', () => {
  assert.equal(shouldSuspendForRejections(2), false);
  assert.equal(shouldSuspendForRejections(3), true);
});

test('L1–L4 处置映射 + 180 天 L1×3 升级 L2', () => {
  assert.equal(actionForLevel('L1'), 'DELETE_CONTENT');
  assert.equal(actionForLevel('L2'), 'MUTE_7D');
  assert.equal(actionForLevel('L3'), 'MUTE_30D');
  assert.equal(actionForLevel('L4'), 'PERMANENT_BAN');
  assert.equal(escalateLevel('L1', 3), 'L2');
  assert.equal(escalateLevel('L1', 2), 'L1');
  assert.equal(escalateLevel('L3', 9), 'L3'); // 已高于 L1 不降级
});
