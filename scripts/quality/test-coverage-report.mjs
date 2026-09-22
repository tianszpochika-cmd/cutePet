#!/usr/bin/env node
// T11.1 单测覆盖报告（纯静态核对，零环境）：node scripts/quality/test-coverage-report.mjs
import { readdirSync, existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';

const ROOT = process.cwd();
const AREAS_PATH = join(ROOT, 'scripts/quality/coverageMap.ts');

// 直接内联最小映射（避免 TS 运行时依赖）：与 coverageMap.ts 保持一致
const AREAS = [
  ['状态机①账号/会话', ['services/iam-service/src/test/java/com/cutepet/iam/IamLogicTest.java']],
  ['状态机②内容投稿', ['packages/contracts/test/domain.test.ts', 'apps/web/src/domain/submission.test.ts']],
  ['状态机③宠物与提醒', ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'apps/web/src/domain/pet.test.ts']],
  ['状态机④商品', ['services/catalog-service/src/test/java/com/cutepet/catalog/CatalogLogicTest.java']],
  ['权限点 32+6角色', ['services/admin-ops-service/src/test/java/com/cutepet/adminops/RbacLogicTest.java', 'apps/admin/src/domain/admin.test.ts', 'packages/contracts/test/permissions.test.ts']],
  ['防刷', ['services/explore-service/src/test/java/com/cutepet/explore/ExploreLogicTest.java', 'apps/web/src/domain/explore.test.ts']],
  ['体重波动', ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'apps/web/src/domain/pet.test.ts']],
  ['排期规则', ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'packages/contracts/test/domain.test.ts']],
];

const ROOTS = ['packages', 'apps', 'scripts'];
const suites = [];
function walk(dir) {
  if (!existsSync(dir)) return;
  for (const e of readdirSync(dir, { withFileTypes: true })) {
    if (e.name === 'node_modules' || e.name === 'dist') continue;
    const full = join(dir, e.name);
    if (e.isDirectory()) walk(full);
    else if (/\.test\.(ts|mjs|js)$/.test(e.name)) suites.push(full.slice(ROOT.length + 1));
  }
}
for (const r of ROOTS) walk(join(ROOT, r));

// services Java 套件
function walkJava(dir, out) {
  if (!existsSync(dir)) return;
  for (const e of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, e.name);
    if (e.isDirectory()) walkJava(full, out);
    else if (/LogicTest\.java$/.test(e.name)) out.push(full.slice(ROOT.length + 1));
  }
}
walkJava(join(ROOT, 'services'), suites);

let pass = 0;
let fail = 0;
const lines = [];
lines.push('=== T11.1 单测覆盖报告（静态核对口径 · 零环境） ===');
lines.push(`测试文件总数：${suites.length}`);
for (const [label, files] of AREAS) {
  const ok = files.every((f) => existsSync(join(ROOT, f)));
  const count = files.filter((f) => existsSync(join(ROOT, f))).length;
  if (ok) pass += 1;
  else fail += 1;
  lines.push(`${ok ? '✔' : '✖'} ${label} —— ${count}/${files.length} 文件`);
}
// 断言规模粗算
let assertions = 0;
for (const f of suites) {
  try {
    const content = readFileSync(join(ROOT, f), 'utf8');
    assertions += (content.match(/assert\.|check\(|测试断言/g) || []).length;
  } catch {}
}
lines.push(`断言规模（静态计数）：约 ${assertions} 处`);
lines.push(`规则域覆盖：${pass}/${AREAS.length} 通过${fail ? `，${fail} 失败` : ''}`);
lines.push('说明：无插桩覆盖率工具（本机零安装约束）；本报告以「域→文件→断言存在」为口径，完整插桩覆盖率随本地阶段补测。');
console.log(lines.join('\n'));
process.exit(fail > 0 ? 1 : 0);
