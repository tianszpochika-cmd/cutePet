#!/usr/bin/env node
// 契约生成（T0.4）：routes.ts → api-client/src/generated.ts；--check 校验同步。零环境。
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { ROUTES } from '../packages/contracts/src/routes.ts';
import { buildClientSource } from '../packages/contracts/src/generate.ts';

const TARGET = 'packages/api-client/src/generated.ts';
const source = buildClientSource(ROUTES);

if (process.argv.includes('--check')) {
  if (!existsSync(TARGET)) {
    console.error(`✖ ${TARGET} 不存在，请运行 pnpm contracts:generate`);
    process.exit(1);
  }
  const onDisk = readFileSync(TARGET, 'utf8');
  if (onDisk !== source) {
    console.error(`✖ ${TARGET} 与契约不同步，请运行 pnpm contracts:generate`);
    process.exit(1);
  }
  console.log(`✔ 生成产物与契约同步（${ROUTES.length} 个 api 方法）`);
  process.exit(0);
}

writeFileSync(TARGET, source, 'utf8');
console.log(`✔ 已生成 ${TARGET}（${ROUTES.length} 个 api 方法）`);
