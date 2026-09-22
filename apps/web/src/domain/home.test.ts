import { test } from 'node:test';
import assert from 'node:assert/strict';
import { homeSections, renderOrder, reminderBarModel, unfinishedWizardHint } from '../domain/home.ts';

test('未登录：登录引导置顶，宠物区与记录隐藏，游客区可见', () => {
  const order = renderOrder({ loggedIn: false, petCount: 0, hasUnfinishedWizard: false, pendingReminders: 0 });
  assert.equal(order[0], 'loginGuide');
  assert.ok(!order.includes('myPets'));
  assert.ok(!order.includes('quickRecord'));
  assert.ok(order.includes('feed') && order.includes('goods') && order.includes('nearbyExplore'));
});

test('有待办：提醒条置顶于宠物区之前（L2）', () => {
  const order = renderOrder({
    loggedIn: true,
    petCount: 2,
    hasUnfinishedWizard: false,
    pendingReminders: 3,
  });
  assert.equal(order[0], 'reminderBar');
  assert.equal(order[1], 'myPets');
  assert.ok(order.includes('quickRecord'));
  assert.ok(!order.includes('loginGuide'));
});

test('无待办：提醒条隐藏', () => {
  const sections = homeSections({
    loggedIn: true,
    petCount: 1,
    hasUnfinishedWizard: false,
    pendingReminders: 0,
  });
  assert.equal(sections.find((s) => s.id === 'reminderBar')!.visible, false);
});

test('登录无宠物：展示空态理由而非记录入口', () => {
  const sections = homeSections({
    loggedIn: true,
    petCount: 0,
    hasUnfinishedWizard: false,
    pendingReminders: 0,
  });
  const pets = sections.find((s) => s.id === 'myPets')!;
  assert.equal(pets.visible, false);
  assert.ok(pets.reason.includes('建档引导'));
});

test('提醒条模型取最近一条并带计数', () => {
  const model = reminderBarModel([
    { petName: '旺财', type: '疫苗', due: '2026-10-01' },
    { petName: '咪咪', type: '驱虫', due: '2026-09-25' },
  ]);
  assert.ok(model!.text.includes('咪咪'));
  assert.equal(model!.count, 2);
  assert.equal(reminderBarModel([]), null);
});

test('未完成建档草稿提示', () => {
  assert.ok(
    unfinishedWizardHint({ loggedIn: true, petCount: 0, hasUnfinishedWizard: true, pendingReminders: 0 }),
  );
  assert.equal(
    unfinishedWizardHint({ loggedIn: true, petCount: 1, hasUnfinishedWizard: false, pendingReminders: 0 }),
    null,
  );
});
