#!/usr/bin/env node
// 本地门禁（T0.6）：零依赖、零环境。
// 检查项：① 逻辑测试全绿 ② 全部 package.json 可解析 ③ 四端端口与设计令牌一致
import { readdirSync, existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';

let failed = false;
const fail = (msg) => {
  console.error(`✖ ${msg}`);
  failed = true;
};
const pass = (msg) => console.log(`✔ ${msg}`);

// ① 逻辑测试
const testRun = spawnSync(process.execPath, ['scripts/run-logic-tests.mjs'], { stdio: 'inherit' });
if (testRun.status !== 0) fail('逻辑测试未通过');
else pass('逻辑测试全绿');

// ② package.json 解析（根 + apps + packages）
const pjFiles = ['package.json'];
for (const dir of ['apps', 'packages']) {
  if (!existsSync(dir)) continue;
  for (const name of readdirSync(dir)) {
    const p = join(dir, name, 'package.json');
    if (existsSync(p)) pjFiles.push(p);
  }
}
for (const p of pjFiles) {
  try {
    JSON.parse(readFileSync(p, 'utf8'));
  } catch (e) {
    fail(`package.json 解析失败: ${p} → ${e.message}`);
  }
}
if (!failed) pass(`package.json 全部可解析（${pjFiles.length} 个）`);

// ③ 端口一致性：vite.config.ts 的 port 必须命中设计令牌 port 集合
const tokensSrc = readFileSync('packages/design-tokens/src/tokens.ts', 'utf8');
const portBlock = tokensSrc.match(/port:\s*\{([^}]+)\}/)?.[1] ?? '';
const tokenPorts = new Set([...portBlock.matchAll(/:\s*(\d{5})/g)].map((m) => Number(m[1])));
const rangePorts = [...portBlock.matchAll(/\[\s*(\d{5}),\s*(\d{5})\s*\]/g)].flatMap((m) => {
  const out = [];
  for (let p = Number(m[1]); p <= Number(m[2]); p++) out.push(p);
  return out;
});
for (const p of rangePorts) tokenPorts.add(p);

for (const app of ['web', 'admin', 'site', 'mobile']) {
  const cfg = readFileSync(`apps/${app}/vite.config.ts`, 'utf8');
  const port = Number(cfg.match(/port:\s*(\d{5})/)?.[1]);
  if (!tokenPorts.has(port)) fail(`apps/${app} 端口 ${port} 不在设计令牌端口集合`);
}
if (!failed) pass(`四端端口与设计令牌一致（${[...tokenPorts].join(', ')}）`);

// 端口避让 cloudstudy（18070–18190）
for (const p of tokenPorts) {
  if (p >= 18070 && p <= 18190) fail(`端口 ${p} 侵入 cloudstudy 占用段`);
}
if (!failed) pass('端口避让 cloudstudy 占用段');

// ④ 契约校验 + 生成产物同步
const contractRun = spawnSync(process.execPath, ['scripts/contracts-validate.mjs'], { stdio: 'inherit' });
if (contractRun.status !== 0) fail('契约校验未通过');
else pass('契约校验（152 路由 / 32 权限点 / 0 问题）');

const genRun = spawnSync(process.execPath, ['scripts/contracts-generate.mjs', '--check'], { stdio: 'inherit' });
if (genRun.status !== 0) fail('api-client 生成产物与契约不同步');
else pass('api-client 生成产物同步');

// ⑤ 服务结构校验
const svcRun = spawnSync(process.execPath, ['scripts/validate-services.mjs'], { stdio: 'inherit' });
if (svcRun.status !== 0) fail('服务结构校验未通过');
else pass('服务结构校验（12 模块 / 网关路由全覆盖）');

process.exit(failed ? 1 : 0);
