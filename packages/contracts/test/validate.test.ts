import { test } from 'node:test';
import assert from 'node:assert/strict';
import { isValidNickname, isValidPetName, clampPage, isValidWeightKg, isValidDate } from '../src/validate.ts';

test('昵称规则', () => {
  assert.equal(isValidNickname('旺财妈'), true);
  assert.equal(isValidNickname('tom_2026'), true);
  assert.equal(isValidNickname('a'), false); // 过短
  assert.equal(isValidNickname('x'.repeat(17)), false); // 过长
  assert.equal(isValidNickname('含 空格'), false);
});

test('宠物名规则', () => {
  assert.equal(isValidPetName('  豆豆 '), true); // trim 后合法
  assert.equal(isValidPetName(''), false);
  assert.equal(isValidPetName('x'.repeat(21)), false);
});

test('分页收敛', () => {
  assert.deepEqual(clampPage(0), { page: 1, size: 20 });
  assert.deepEqual(clampPage(3, 50), { page: 3, size: 50 });
  assert.deepEqual(clampPage(2, 999), { page: 2, size: 20 }); // size 超上限回默认
  assert.deepEqual(clampPage(Number.NaN), { page: 1, size: 20 });
});

test('体重硬边界 0.1–200kg', () => {
  assert.equal(isValidWeightKg(0.1), true);
  assert.equal(isValidWeightKg(200), true);
  assert.equal(isValidWeightKg(0.05), false);
  assert.equal(isValidWeightKg(201), false);
  assert.equal(isValidWeightKg(Number.NaN), false);
});

test('日期格式与真实日历', () => {
  assert.equal(isValidDate('2026-02-28'), true);
  assert.equal(isValidDate('2027-02-29'), false); // 非闰年
  assert.equal(isValidDate('2026-13-01'), false);
  assert.equal(isValidDate('20260101'), false);
});
