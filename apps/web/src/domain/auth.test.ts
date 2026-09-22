import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  ageBand,
  loginBlockers,
  guardianStepNeeded,
  safeReturnPath,
  gateReturnTarget,
  resendCountdownLeft,
  wechatBlockers,
} from '../domain/auth.ts';

test('年龄分支：满14=ADULT，差一天=MINOR（方案甲/U83）', () => {
  assert.equal(ageBand('2012-09-22', '2026-09-22'), 'ADULT');
  assert.equal(ageBand('2012-09-23', '2026-09-22'), 'MINOR');
  assert.throws(() => ageBand('2030-01-01', '2026-09-22'));
});

test('登录闸门：手机号/验证码/协议双勾选', () => {
  const ok = { phone: '13812345678', code: '123456', agreedTerms: true, agreedPrivacy: true };
  assert.deepEqual(loginBlockers(ok), []);
  assert.ok(loginBlockers({ ...ok, phone: '123' }).includes('PHONE_INVALID'));
  assert.ok(loginBlockers({ ...ok, agreedPrivacy: false }).includes('AGREEMENTS_REQUIRED'));
  assert.ok(loginBlockers({ ...ok, code: '123' }).includes('CODE_INVALID'));
});

test('监护人步骤按年龄触发', () => {
  assert.equal(guardianStepNeeded('2015-01-01', '2026-09-22'), true);
  assert.equal(guardianStepNeeded('2000-01-01', '2026-09-22'), false);
  assert.equal(guardianStepNeeded(null, '2026-09-22'), false);
});

test('U93 回跳白名单：站内绝对路径放行、外部与伪协议拒绝', () => {
  assert.equal(safeReturnPath('/articles/abc?resume=like'), '/articles/abc?resume=like');
  assert.equal(safeReturnPath('https://evil.com'), '/');
  assert.equal(safeReturnPath('//evil.com'), '/');
  assert.equal(safeReturnPath('/\\evil.com'), '/');
  assert.equal(safeReturnPath('javascript:alert(1)'), '/');
  assert.equal(safeReturnPath('/javascript:alert(1)'), '/');
  assert.equal(safeReturnPath(''), '/');
  assert.equal(safeReturnPath(null, '/home'), '/home');
});

test('闸门回跳带 resume 参数且目标仍过白名单', () => {
  const target = gateReturnTarget('/pets/p1', 'record:weight');
  assert.ok(target.startsWith('/pets/p1?resume='));
  assert.equal(gateReturnTarget('https://evil.com', 'x'), '/?resume=x');
});

test('验证码重发 60s 倒计时', () => {
  assert.equal(resendCountdownLeft(0), 60);
  assert.equal(resendCountdownLeft(59.9), 1);
  assert.equal(resendCountdownLeft(61), 0);
});

test('微信登录不可绕过手机/协议/监护人（U83）', () => {
  const base = {
    phoneBound: true,
    code: '123456',
    agreedTerms: true,
    agreedPrivacy: true,
    ageBandValue: 'ADULT' as const,
    guardianConsented: false,
  };
  assert.deepEqual(wechatBlockers(base), []);
  assert.ok(wechatBlockers({ ...base, phoneBound: false }).includes('NEED_PHONE_BIND'));
  assert.ok(
    wechatBlockers({ ...base, ageBandValue: 'MINOR', guardianConsented: false }).includes('NEED_GUARDIAN'),
  );
  assert.ok(wechatBlockers({ ...base, agreedTerms: false }).includes('AGREEMENTS_REQUIRED'));
});
