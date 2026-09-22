#!/usr/bin/env node
// Java 纯逻辑测试运行器（T0.3 配套，零环境）。
// 用系统已安装的 javac/java 编译并执行 services 下的 *LogicTest.java（纯 Java，无 Spring/JUnit 依赖）。
// 输出目录仅在仓库内 .tmp-java/；不写入 ~/.m2、不启动任何服务。
import { readdirSync, existsSync, statSync } from 'node:fs';
import { join, dirname, relative, sep } from 'node:path';
import { spawnSync } from 'node:child_process';

const SERVICES = 'services';
const COMMON_MAIN = join(SERVICES, 'common-core', 'src', 'main', 'java');

function collect(dir, predicate, out = []) {
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) collect(full, predicate, out);
    else if (predicate(full)) out.push(full);
  }
  return out;
}

const tests = collect(SERVICES, (f) => f.endsWith('LogicTest.java'));
if (tests.length === 0) {
  console.log('[java-logic-test] 未发现 *LogicTest.java，跳过。');
  process.exit(0);
}

const outDir = join('.tmp-java', 'classes');
let failed = 0;

for (const testFile of tests) {
  // 归属服务目录 = services/<name>
  const rel = relative(process.cwd(), testFile).split(sep);
  const serviceName = rel[1];
  const serviceMain = join(SERVICES, serviceName, 'src', 'main', 'java');

  const sources = [
    ...(existsSync(COMMON_MAIN) ? collect(COMMON_MAIN, (f) => f.endsWith('.java')) : []),
    ...(existsSync(serviceMain) ? collect(serviceMain, (f) => f.endsWith('.java')) : []),
    testFile,
  ];

  // 类名（含包）
  const pkgDir = testFile.slice(0, testFile.lastIndexOf(sep));
  const root = join(SERVICES, serviceName, 'src', 'test', 'java');
  const fqcnFile = relative(root, join(pkgDir, ''));
  const className = testFile.slice(testFile.lastIndexOf(sep) + 1, -5);
  const pkg = fqcnFile.split(sep).filter(Boolean).join('.');

  const compile = spawnSync('javac', ['-encoding', 'UTF-8', '-d', outDir, ...sources], { stdio: 'inherit' });
  if (compile.status !== 0) {
    console.error(`✖ 编译失败: ${testFile}`);
    failed++;
    continue;
  }
  const run = spawnSync('java', ['-Dfile.encoding=UTF-8', '-cp', outDir, `${pkg}.${className}`], {
    stdio: 'inherit',
  });
  if (run.status !== 0) {
    console.error(`✖ 运行失败: ${pkg}.${className}`);
    failed++;
  } else {
    console.log(`✔ ${serviceName} / ${className}`);
  }
}

console.log(failed === 0 ? `[java-logic-test] 全部通过（${tests.length} 个）` : `[java-logic-test] ${failed} 个失败`);
process.exit(failed === 0 ? 0 : 1);
