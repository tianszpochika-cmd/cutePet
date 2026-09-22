import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  POI_TYPES,
  RADII,
  buildPoiQuery,
  distanceLabel,
  starsPercent,
  reviewBlockers,
  suspectReviewHint,
  duplicateDayBlocked,
  signupBlockers,
  SIGNUP_DENY_COPY,
  CONSENT_TEXT,
  routePointsValid,
  ADOPTION_CARD_FLAGS,
  adoptionVisible,
} from '../domain/explore.ts';

test('类型 7 与半径 5/10/20（决议）', () => {
  assert.equal(POI_TYPES.length, 7);
  assert.deepEqual([...RADII], [5, 10, 20]);
});

test('筛选→查询：非法半径回落 5', () => {
  assert.deepEqual(buildPoiQuery({ radiusKm: 10, city: '北京', type: '宠物医院' }), {
    radius: 10,
    city: '北京',
    type: '宠物医院',
  });
  assert.equal(buildPoiQuery({ radiusKm: 7 }).radius, 5);
  assert.equal(buildPoiQuery({}).radius, 5);
});

test('距离标签米/公里', () => {
  assert.equal(distanceLabel(0.85), '850m');
  assert.equal(distanceLabel(4.25), '4.3km');
  assert.equal(distanceLabel(0), '0m');
});

test('星级百分比与暂无评分（U81）', () => {
  assert.equal(starsPercent(4.5), 90);
  assert.equal(starsPercent(null), null);
  assert.equal(starsPercent(6), 100);
});

test('写评价闸门 1–5 与 500 字', () => {
  const ok = { friendly: 4, env: 5, service: 3, content: '环境不错' };
  assert.deepEqual(reviewBlockers(ok), []);
  assert.ok(reviewBlockers({ ...ok, friendly: 0 }).includes('SCORE_INVALID:friendly'));
  assert.ok(reviewBlockers({ ...ok, content: '' }).includes('CONTENT_EMPTY'));
  assert.ok(reviewBlockers({ ...ok, content: 'x'.repeat(501) }).includes('CONTENT_TOO_LONG'));
});

test('全 5 短文预告人工复审（防刷决议）', () => {
  assert.ok(suspectReviewHint({ friendly: 5, env: 5, service: 5, content: '很好' }));
  assert.equal(suspectReviewHint({ friendly: 5, env: 5, service: 5, content: '环境很好，服务很棒，狗子玩得开心' }), null);
  assert.equal(suspectReviewHint({ friendly: 4, env: 4, service: 4, content: '好' }), null);
});

test('同场所当日已评禁再发（1 天 1 条）', () => {
  assert.equal(duplicateDayBlocked(true), true);
  assert.equal(duplicateDayBlocked(false), false);
});

test('报名闸门（U68/U69：90天窗/截止/开始/满员/重复/同意）', () => {
  const base = {
    loggedIn: true,
    consentChecked: true,
    state: 'PUBLISHED',
    today: '2026-09-22',
    beginsAt: '2026-12-01',
    signupDeadline: '2026-11-20' as string | null,
    quota: 10,
    activeCount: 3,
    userHasActive: false,
  };
  assert.equal(signupBlockers(base), null);
  assert.equal(signupBlockers({ ...base, loggedIn: false }), 'NEED_LOGIN');
  // 开始 2026-12-01 → 90 天前 = 2026-09-02；今天 09-22 在窗内。过早用更远活动：
  assert.equal(signupBlockers({ ...base, beginsAt: '2027-06-01' }), 'TOO_EARLY');
  assert.equal(signupBlockers({ ...base, today: '2026-11-21' }), 'DEADLINE_PASSED');
  // 已开始用例：无截止日（拒因优先级=截止先于开始，与后端 ActivityRules 一致）
  assert.equal(signupBlockers({ ...base, signupDeadline: null, today: '2026-12-01' }), 'STARTED_OR_OVER');
  assert.equal(signupBlockers({ ...base, quota: 3 }), 'FULL');
  assert.equal(signupBlockers({ ...base, userHasActive: true }), 'DUPLICATE');
  assert.equal(signupBlockers({ ...base, consentChecked: false }), 'CONSENT_REQUIRED');
  assert.equal(signupBlockers({ ...base, state: 'CHANGED' }), 'STATE_BLOCKED');
  assert.equal(signupBlockers({ ...base, state: 'PENDING' }), 'NOT_OPEN');
});

test('全部拒因有中文文案 + 同意告知文案', () => {
  const denys = Object.keys(SIGNUP_DENY_COPY) as (keyof typeof SIGNUP_DENY_COPY)[];
  assert.equal(denys.length, 8);
  for (const d of denys) assert.ok(SIGNUP_DENY_COPY[d].length > 4, d);
  assert.ok(CONSENT_TEXT.includes('电话') && CONSENT_TEXT.includes('90 天'));
});

test('路线 ≥2 点且格式合法（U21）', () => {
  assert.equal(routePointsValid('116.40,39.90;116.50,39.90'), true);
  assert.equal(routePointsValid('116.40,39.90'), false);
  assert.equal(routePointsValid('bad;format'), false);
});

test('领养卡无报名/交易按钮 + 过期隐藏（U90）', () => {
  assert.equal(ADOPTION_CARD_FLAGS.hasSignupButton, false);
  assert.equal(ADOPTION_CARD_FLAGS.hasTradeButton, false);
  assert.equal(adoptionVisible('2026-10-01', '2026-09-22'), true);
  assert.equal(adoptionVisible('2026-09-01', '2026-09-22'), false);
  assert.equal(adoptionVisible(null, '2026-09-22'), true);
});
