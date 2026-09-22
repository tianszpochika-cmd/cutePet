import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  buttonClass,
  statusTone,
  starFillPercent,
  createToastStore,
  emptyCopy,
} from '../src/logic.ts';

test('按钮类名：变体/尺寸/禁用组合', () => {
  assert.equal(buttonClass(), 'cp-btn cp-btn--primary cp-btn--m');
  assert.equal(buttonClass('secondary', 's', true), 'cp-btn cp-btn--secondary cp-btn--s is-disabled');
});

test('状态徽标色调映射（四色 + 补充态，默认灰）', () => {
  assert.equal(statusTone('PENDING'), 'orange');
  assert.equal(statusTone('REVIEWING'), 'blue');
  assert.equal(statusTone('PUBLISHED'), 'green');
  assert.equal(statusTone('REJECTED'), 'red');
  assert.equal(statusTone('DRAFT'), 'gray');
  assert.equal(statusTone('EXPIRED'), 'red');
  assert.equal(statusTone('UNKNOWN'), 'gray');
});

test('星级填充百分比且夹紧 0–5', () => {
  assert.equal(starFillPercent(4.5), 90);
  assert.equal(starFillPercent(0), 0);
  assert.equal(starFillPercent(6), 100);
  assert.equal(starFillPercent(-1), 0);
});

test('Toast 队列：同屏不堆叠、按 id 关闭、清空', () => {
  const store = createToastStore(1);
  const a = store.push('第一条');
  assert.equal(store.items.length, 1);
  const b = store.push('第二条顶掉第一条', 'success');
  assert.equal(store.items.length, 1);
  assert.equal(store.items[0]!.id, b);
  store.dismiss(b);
  assert.equal(store.items.length, 0);
  store.push('x');
  store.clear();
  assert.equal(store.items.length, 0);
  assert.ok(a < b);
});

test('空态三件套文案', () => {
  assert.equal(emptyCopy('not-found').illustration, 'lost');
  assert.equal(emptyCopy('first-use').title, '从这里开始吧');
  assert.equal(emptyCopy('loading').illustration, 'dozing');
});
