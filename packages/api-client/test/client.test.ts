import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { buildUrl, isApiError, ApiClientError, setAccessToken, getAccessToken } from '../src/request.ts';
import { ROUTES } from '../../contracts/src/routes.ts';
import { buildClientSource } from '../../contracts/src/generate.ts';

test('buildUrl：路径参数替换 + 编码 + query 序列化', () => {
  assert.equal(buildUrl('/pets/:id', { params: { id: 'p1' } }), '/pets/p1');
  assert.equal(buildUrl('/pets/:id', { params: { id: '带 空格' } }), '/pets/%E5%B8%A6%20%E7%A9%BA%E6%A0%BC');
  assert.equal(buildUrl('/pois'), '/pois');
  assert.equal(buildUrl('/pois', { query: { lat: 39.9, lng: 116.4, q: '宠物 医院', empty: undefined } }), '/pois?lat=39.9&lng=116.4&q=%E5%AE%A0%E7%89%A9%20%E5%8C%BB%E9%99%A2');
});

test('buildUrl：缺失路径参数抛 ApiClientError(400)', () => {
  assert.throws(
    () => buildUrl('/pets/:id', { params: {} }),
    (e: unknown) => e instanceof ApiClientError && e.status === 400 && e.code === 'MISSING_PARAM',
  );
});

test('isApiError 结构判定', () => {
  assert.ok(isApiError({ code: 'X', message: 'y' }));
  assert.ok(!isApiError({ code: 'X' }));
  assert.ok(!isApiError(null));
});

test('token 存取', () => {
  setAccessToken('t-1');
  assert.equal(getAccessToken(), 't-1');
  setAccessToken(null);
  assert.equal(getAccessToken(), null);
});

test('生成产物与契约同步（每个路由都有 api.<id>）', () => {
  const generated = readFileSync(new URL('../src/generated.ts', import.meta.url), 'utf8');
  for (const r of ROUTES) {
    assert.ok(generated.includes(`  ${r.id}: (args: CallArgs`), `generated 缺少 ${r.id}`);
  }
  // 确定性：再构建一次与磁盘一致
  assert.equal(generated, buildClientSource(ROUTES));
});
