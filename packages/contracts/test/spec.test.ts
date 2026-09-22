import { test } from 'node:test';
import assert from 'node:assert/strict';
import { ROUTES } from '../src/routes.ts';
import { PERMISSIONS } from '../src/permissions.ts';
import { checkSpec, uncoveredPermissions } from '../src/spec.ts';

test('真实路由契约 0 问题', () => {
  const issues = checkSpec(ROUTES);
  assert.deepEqual(issues, [], JSON.stringify(issues, null, 2));
});

test('32 权限点全部被路由引用（覆盖完整性）', () => {
  const uncovered = uncoveredPermissions(ROUTES, PERMISSIONS);
  assert.deepEqual(uncovered, [], `未被引用的权限点: ${uncovered.join(', ')}`);
});

test('规模与结构：admin 路由均带权限、公开路由可匿名', () => {
  const admin = ROUTES.filter((r) => r.auth === 'admin');
  assert.ok(admin.length >= 40, `admin 路由过少: ${admin.length}`);
  assert.ok(admin.every((r) => !!r.permission));
  assert.ok(ROUTES.filter((r) => r.auth === 'public').some((r) => r.id === 'articlesFeed'));
});

test('checkSpec 能捕获各类违规', () => {
  const dup = checkSpec([
    { id: 'a1', method: 'GET', path: '/x', auth: 'public', summary: 's' },
    { id: 'a1', method: 'GET', path: '/x', auth: 'public', summary: 's' },
  ]);
  assert.ok(dup.some((i) => i.message.includes('id 重复')));
  assert.ok(dup.some((i) => i.message.includes('路由重复')));

  const noPerm = checkSpec([{ id: 'a2', method: 'GET', path: '/admin/x', auth: 'admin', summary: 's' }]);
  assert.ok(noPerm.some((i) => i.message.includes('缺少 permission')));

  const badPerm = checkSpec([
    { id: 'a3', method: 'GET', path: '/admin/y', auth: 'admin', permission: 'not.a.perm', summary: 's' } as never,
  ]);
  assert.ok(badPerm.some((i) => i.message.includes('不在 32 权限点内')));

  const leak = checkSpec([{ id: 'a4', method: 'GET', path: '/z', auth: 'user', permission: 'user.ban', summary: 's' } as never]);
  assert.ok(leak.some((i) => i.message.includes('不应携带 permission')));

  const badPath = checkSpec([{ id: 'a5', method: 'GET', path: '/x/:', auth: 'public', summary: 's' }]);
  assert.ok(badPath.some((i) => i.message.includes('路径参数非法') || i.message.includes('path 非法')));
});
