import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  SUBMISSION_KINDS,
  AUTOSAVE_INTERVAL_MS,
  statusStepIndex,
  submitBlockers,
  shouldAutosave,
  unsavedGuard,
  rejectCopy,
  withdrawCopy,
  statCards,
} from '../domain/submission.ts';

test('投稿三类型（U73：文章/评测/清单）', () => {
  assert.equal(SUBMISSION_KINDS.length, 3);
  assert.deepEqual(SUBMISSION_KINDS.map((k) => k.kind), ['ARTICLE', 'REVIEW', 'LIST']);
});

test('状态时间线步进映射', () => {
  assert.deepEqual(statusStepIndex('DRAFT'), { done: 0, current: 0 });
  assert.deepEqual(statusStepIndex('PENDING'), { done: 1, current: 1 });
  assert.deepEqual(statusStepIndex('REVIEWING'), { done: 2, current: 2 });
  assert.deepEqual(statusStepIndex('PUBLISHED'), { done: 4, current: 3 });
  assert.deepEqual(statusStepIndex('REJECTED'), { done: 4, current: 3 });
});

test('提交闸门：类型/标题/正文≥10/频道', () => {
  const ok = { kind: 'ARTICLE', title: '幼猫喂养指南', body: '这是一篇不少于十个字的正文内容', channel: '猫' };
  assert.deepEqual(submitBlockers(ok), []);
  assert.ok(submitBlockers({ ...ok, kind: 'VIDEO' }).includes('KIND_INVALID'));
  assert.ok(submitBlockers({ ...ok, title: ' ' }).includes('TITLE_INVALID'));
  assert.ok(submitBlockers({ ...ok, body: '短' }).includes('BODY_TOO_SHORT'));
  assert.ok(submitBlockers({ ...ok, channel: '' }).includes('CHANNEL_REQUIRED'));
});

test('30s 自动保存节流（决议）', () => {
  const now = 1_000_000;
  assert.equal(AUTOSAVE_INTERVAL_MS, 30_000);
  assert.equal(shouldAutosave(true, null, now), true); // 从未保存→立即
  assert.equal(shouldAutosave(true, now - 29_000, now), false);
  assert.equal(shouldAutosave(true, now - 30_000, now), true);
  assert.equal(shouldAutosave(false, now - 60_000, now), false); // 无改动不存
});

test('离开拦截', () => {
  assert.equal(unsavedGuard(true).block, true);
  assert.ok(unsavedGuard(true).message.includes('未保存'));
  assert.equal(unsavedGuard(false).block, false);
});

test('驳回→质量退修提示（BR-06：质量退修不暂停投稿）', () => {
  assert.equal(rejectCopy(1).suspended, false);
  assert.equal(rejectCopy(3).suspended, false);
  assert.ok(rejectCopy(3).message.includes('不暂停投稿'));
  assert.ok(rejectCopy(3).message.includes('人工确认的违规'));
});

test('撤回可用性（U75：治理下架不可自行恢复）', () => {
  assert.equal(withdrawCopy('PENDING').allowed, true);
  assert.equal(withdrawCopy('PUBLISHED').allowed, true);
  const takedown = withdrawCopy('TAKEDOWN');
  assert.equal(takedown.allowed, false);
  assert.ok(takedown.note.includes('不可自行恢复'));
});

test('创作数据汇总卡片', () => {
  const cards = statCards([
    { views: 10, likes: 1, comments: 2, favorites: 3 },
    { views: 5, likes: 0, comments: 1, favorites: 0 },
  ]);
  assert.deepEqual(
    cards.map((c) => c.value),
    [15, 1, 3, 3],
  );
});
