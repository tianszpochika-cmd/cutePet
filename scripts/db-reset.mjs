#!/usr/bin/env node
// 数据库重置与 seed 脚本（T0.5）。
// 约束（开发计划 §11）：本机不连库 —— 默认 dry-run 仅打印执行计划与数据规模；
// 真实执行在开发机：pnpm db:reset --apply --dsn <cutepet DSN>（需 MySQL 与 Flyway 就绪）。
import { buildSeedData } from './seed-data.mjs';

const apply = process.argv.includes('--apply');
const seed = buildSeedData();

if (!apply) {
  console.log('[db:reset] dry-run（默认，不触碰任何数据库）');
  console.log('[db:reset] 将执行：Flyway repair → 重建 cutepet_* schema → 应用 database/migrations → 写入 seed');
  console.log('[db:reset] seed 规模：', JSON.stringify(seed.counts));
  console.log('[db:reset] 如需真实执行（仅限开发机）：pnpm db:reset --apply --dsn <mysql dsn>');
  process.exit(0);
}

console.error('[db:reset] --apply 需要在具备 MySQL 的开发机上执行（本机为设计机，禁止连库）。');
console.error('[db:reset] 将来此处调用 database/migrations 的 Flyway 执行器；seed 记录数：', JSON.stringify(seed.counts));
process.exit(2);
