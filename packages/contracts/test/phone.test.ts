import { test } from 'node:test';
import assert from 'node:assert/strict';
import { isValidCnPhone, maskPhone, verifyDevSmsCode } from '../src/phone.ts';

test('手机号校验：合法号段通过，非法输入拒绝', () => {
  assert.equal(isValidCnPhone('13812345678'), true);
  assert.equal(isValidCnPhone('19912345678'), true);
  assert.equal(isValidCnPhone('12345678901'), false); // 非 1 开头号段
  assert.equal(isValidCnPhone('12312345678'), false); // 2 段非法
  assert.equal(isValidCnPhone('1381234567'), false); // 10 位
  assert.equal(isValidCnPhone(''), false);
});

test('脱敏：合法号 3+4 位遮蔽，非法输入返回 ***', () => {
  assert.equal(maskPhone('13812345678'), '138****5678');
  assert.equal(maskPhone('abc'), '***');
});

test('dev 验证码仅接受固定码', () => {
  assert.equal(verifyDevSmsCode('123456'), true);
  assert.equal(verifyDevSmsCode('000000'), false);
});
