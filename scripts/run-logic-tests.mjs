#!/usr/bin/env node
// 纯逻辑测试运行器（跨平台，零依赖）。
// 收集 packages/apps/scripts 下全部 *.test.ts/.mjs/.js，交给 node --test 进程内执行。
// 约束：不启动任何服务、不连数据库、不触碰环境（开发计划 §11 本轮执行模式）。
import { readdirSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';

const ROOTS = ['packages', 'apps', 'scripts'];
const files = [];

function walk(dir) {
  if (!existsSync(dir)) return;
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name === 'dist') continue;
    const full = join(dir, entry.name);
    if (entry.isDirectory()) walk(full);
    else if (/\.test\.(ts|mjs|js)$/.test(entry.name)) files.push(full);
  }
}

for (const root of ROOTS) walk(join(process.cwd(), root));

if (files.length === 0) {
  console.log('[logic-test] 未发现 *.test.ts，跳过。');
  process.exit(0);
}

console.log(`[logic-test] 运行 ${files.length} 个测试文件（进程内，零环境）`);
const result = spawnSync(process.execPath, ['--test', ...files], { stdio: 'inherit' });
process.exit(result.status ?? 1);
