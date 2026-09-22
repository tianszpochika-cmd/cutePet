#!/usr/bin/env node
// 契约校验（T0.4）：checkSpec + 权限覆盖完整性。零环境。
import { ROUTES } from '../packages/contracts/src/routes.ts';
import { PERMISSIONS } from '../packages/contracts/src/permissions.ts';
import { checkSpec, uncoveredPermissions } from '../packages/contracts/src/spec.ts';

const issues = checkSpec(ROUTES);
const uncovered = uncoveredPermissions(ROUTES, PERMISSIONS);

if (issues.length || uncovered.length) {
  for (const i of issues) console.error(`✖ [${i.routeId ?? '-'}] ${i.message}`);
  for (const p of uncovered) console.error(`✖ 权限点未被任何路由引用: ${p}`);
  process.exit(1);
}
console.log(`✔ 契约校验通过：${ROUTES.length} 条路由、${PERMISSIONS.length} 个权限点、0 问题`);
