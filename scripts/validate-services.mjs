#!/usr/bin/env node
// 服务结构校验（T0.3 配套，零环境）：
// ① BOM 模块目录/pom 齐全 ② 每个域服务有 Application 主类 + application.yml
// ③ 端口在 18480–18488 且唯一 ④ 域服务有 Flyway V1 且 schema 命名 cutepet_*
// ⑤ 网关路由覆盖全部域服务端口
import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';

let failed = 0;
const fail = (m) => {
  console.error(`✖ ${m}`);
  failed++;
};

const LIBS = new Set(['common-core', 'common-web', 'service-template']);
const NO_DB = new Set(['common-core', 'service-template', 'api-gateway']); // 无 Flyway 要求

const bom = readFileSync('services/pom.xml', 'utf8');
const modules = [...bom.matchAll(/<module>([^<]+)<\/module>/g)].map((m) => m[1]);
if (modules.length !== 12) fail(`BOM 模块数异常: ${modules.length}（应为 12）`);

function findFiles(dir, suffix, out = []) {
  if (!existsSync(dir)) return out;
  for (const e of readdirSync(dir)) {
    const full = join(dir, e);
    if (statSync(full).isDirectory()) findFiles(full, suffix, out);
    else if (e.endsWith(suffix)) out.push(full);
  }
  return out;
}

const ports = new Map();
const domainServices = [];

for (const mod of modules) {
  const dir = join('services', mod);
  if (!existsSync(join(dir, 'pom.xml'))) {
    fail(`模块 ${mod} 缺少 pom.xml`);
    continue;
  }
  if (LIBS.has(mod)) continue;
  domainServices.push(mod);

  if (mod !== 'api-gateway' && findFiles(join(dir, 'src', 'main', 'java'), 'Application.java').length === 0) {
    fail(`模块 ${mod} 缺少 Application 主类`);
  }

  const ymlPath = join(dir, 'src', 'main', 'resources', 'application.yml');
  if (!existsSync(ymlPath)) {
    fail(`模块 ${mod} 缺少 application.yml`);
    continue;
  }
  const yml = readFileSync(ymlPath, 'utf8');
  const port = Number(yml.match(/server:\s*\n\s*port:\s*(\d+)/)?.[1]);
  const expectedLo = mod === 'api-gateway' ? 18470 : 18480;
  const expectedHi = mod === 'api-gateway' ? 18470 : 18488;
  if (!(port >= expectedLo && port <= expectedHi)) fail(`模块 ${mod} 端口非法: ${port}`);
  if (ports.has(port)) fail(`端口重复 ${port}: ${ports.get(port)} 与 ${mod}`);
  ports.set(port, mod);

  if (NO_DB.has(mod)) continue;
  const schema = yml.match(/schemas:\s*(\S+)/)?.[1];
  if (!schema || !schema.startsWith('cutepet_')) fail(`模块 ${mod} flyway schema 非法: ${schema}`);
  if (!existsSync(join(dir, 'src', 'main', 'resources', 'db', 'migration', 'V1__init.sql'))) {
    fail(`模块 ${mod} 缺少 Flyway V1 迁移`);
  }
}

// 网关覆盖全部域服务（8 个 18480–18488 的端口都必须被某条路由指向）
const gw = readFileSync('services/api-gateway/src/main/resources/application.yml', 'utf8');
const routed = new Set([...gw.matchAll(/uri:\s*http:\/\/127\.0\.0\.1:(\d+)/g)].map((m) => Number(m[1])));
const dbServices = domainServices.filter((m) => !NO_DB.has(m));
for (const mod of dbServices) {
  const port = ports.get([...ports].find(([, m]) => m === mod)?.[0] ?? -1);
  void port;
}
for (const [port, mod] of ports) {
  if (mod === 'api-gateway') continue;
  if (!routed.has(port)) fail(`网关路由未覆盖 ${mod}(${port})`);
}

if (failed === 0) {
  console.log(
    `✔ 服务结构校验通过：${modules.length} 模块（${domainServices.length} 个可运行）、${ports.size - 1} 个域服务端口、网关路由全覆盖`,
  );
}
process.exit(failed === 0 ? 0 : 1);
