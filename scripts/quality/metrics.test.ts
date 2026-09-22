import { test } from 'node:test';
import assert from 'node:assert/strict';
import { rate, sumDays, sumDedup, receiptRate, percentile, slaOf, kSamples } from './metricsCalc.ts';

test('T11.6 U91-1 零分母 → null 不产生 0% 假象', () => {
  assert.equal(rate(0, 0), null);
  assert.equal(receiptRate(0, 0), null);
  assert.equal(rate(1, 0), null);
});

test('T11.6 U91-1 脏数据越界抛错（part>total / 负数）', () => {
  assert.throws(() => rate(11, 10), RangeError);
  assert.throws(() => rate(-1, 10), RangeError);
});

test('T11.6 U91-2 跨日按自然日求和，null 忽略', () => {
  const points = [
    { day: '2026-09-20', value: 3 },
    { day: '2026-09-21', value: 5 },
    { day: '2026-09-22', value: 0 },
  ];
  assert.equal(sumDays(points), 8);
  assert.equal(sumDays([]), 0);
  assert.equal(sumDays([{ day: '2026-09-22', value: 0 }]), 0);
});

test('T11.6 U91-3 重复上报同 key 只计一次', () => {
  const dup = [
    { day: '2026-09-22', value: 1, dedupKey: 'evt-1' },
    { day: '2026-09-22', value: 1, dedupKey: 'evt-1' },
    { day: '2026-09-23', value: 1, dedupKey: 'evt-2' },
  ];
  assert.equal(sumDedup(dup), 2, '重复 evt-1 去重');
  assert.equal(sumDedup([{ day: '2026-09-22', value: 4 }]), 4, '无 key 原样计');
});

test('T11.6 U91-4 无回执：未发送零回执为 null、超发截断', () => {
  assert.equal(receiptRate(10, 0), 0.0, '已发送但零回执 → 0（与未发送区分）');
  assert.equal(receiptRate(10, 12), 100.0, '回执超发送截断为 100%');
  assert.equal(receiptRate(8, 6), 75.0);
});

test('T11.6 分位数 ceil 法与 SLA 24/48', () => {
  assert.equal(percentile([1, 2, 3, 4, 50], 50), 3);
  assert.equal(percentile([1, 2, 3, 4, 50], 90), 50);
  assert.equal(percentile([], 50), null);
  assert.equal(slaOf(3), 'OK');
  assert.equal(slaOf(30), 'BREACH');
  assert.equal(slaOf(60), 'ESCALATE');
});

test('T11.6 K 样例复算 = 公布值（原始→口径→公布三段一致）', () => {
  const samples = kSamples();
  assert.ok(samples.length >= 7);
  for (const s of samples) {
    assert.equal(s.compute(), s.published, `${s.id} 复算不一致`);
  }
});
