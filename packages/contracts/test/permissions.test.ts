import { test } from 'node:test';
import assert from 'node:assert/strict';
import { PERMISSIONS, ROLES, can, canViewDashboard, isPermission } from '../src/permissions.ts';

test('32 权限点且无重复', () => {
  assert.equal(PERMISSIONS.length, 32);
  assert.equal(new Set(PERMISSIONS).size, 32);
});

test('预置 6 角色存在且权限均合法', () => {
  assert.deepEqual(
    Object.keys(ROLES).sort(),
    ['administrator', 'editor', 'poiOperator', 'productOperator', 'reviewer', 'supervisor'].sort(),
  );
  for (const [role, perms] of Object.entries(ROLES)) {
    for (const p of perms) assert.ok(isPermission(p), `${role} 含非法权限 ${p}`);
    assert.equal(new Set(perms).size, perms.length, `${role} 权限重复`);
  }
});

test('角色矩阵抽查（对齐功能设计-管理端 §6.1）', () => {
  assert.ok(can(ROLES.reviewer!, 'review.article'));
  assert.ok(!can(ROLES.reviewer!, 'rbac.manage'));
  assert.ok(can(ROLES.editor!, 'article.create'));
  assert.ok(!can(ROLES.editor!, 'article.takedown')); // 下架归审核员
  assert.ok(can(ROLES.poiOperator!, 'poi.create.edit'));
  assert.ok(!can(ROLES.poiOperator!, 'user.ban'));
  assert.ok(can(ROLES.productOperator!, 'product.import.csv'));
  assert.ok(!can(ROLES.supervisor!, 'system.settings')); // 高危仅管理员
  assert.ok(can(ROLES.supervisor!, 'user.ban'));
  assert.equal(ROLES.administrator!.length, 32);
});

test('高危双保险：#31/#32 仅 administrator', () => {
  for (const [role, perms] of Object.entries(ROLES)) {
    if (role === 'administrator') continue;
    assert.ok(!perms.includes('rbac.manage'), `${role} 不得持有 rbac.manage`);
    assert.ok(!perms.includes('system.settings'), `${role} 不得持有 system.settings`);
  }
});

test('看板访问：全量权限 或 任一业务域权限', () => {
  assert.ok(canViewDashboard(['dashboard.view.all']));
  assert.ok(canViewDashboard(['poi.create.edit'])); // 本域看板
  assert.ok(!canViewDashboard(['audit.view'])); // 纯审计角色不看业务看板？审计属治理，规则按 §6.1 无业务域则 false
  assert.ok(!canViewDashboard([]));
});
