import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { CORE_AREAS, TEST_SUITE_GLOBS } from './coverageMap.ts';

const ROOT = process.cwd();

test('T11.1 核心规则域全部有测试文件落地（4状态机+权限+防刷+波动+排期）', () => {
  assert.equal(CORE_AREAS.length, 8, '四状态机 + 权限 + 防刷 + 波动 + 排期 = 8 域');
  for (const area of CORE_AREAS) {
    for (const file of area.testFiles) {
      assert.ok(existsSync(join(ROOT, file)), `${area.id} 缺测试文件: ${file}`);
    }
  }
});

test('T11.1 断言标记存在（静态覆盖核对口径）', () => {
  for (const area of CORE_AREAS) {
    let hit = 0;
    for (const file of area.testFiles) {
      const content = readFileSync(join(ROOT, file), 'utf8');
      for (const marker of area.markers) {
        if (content.includes(marker)) hit += 1;
      }
    }
    assert.ok(
      hit >= area.minMarkers,
      `${area.id}（${area.label}）断言标记不足：命中 ${hit} < 要求 ${area.minMarkers}`,
    );
  }
});

test('T11.1 覆盖口径声明：测试套件清单有效（非空且含四端与服务层）', () => {
  assert.ok(TEST_SUITE_GLOBS.some((g) => g.startsWith('packages')));
  assert.ok(TEST_SUITE_GLOBS.some((g) => g.startsWith('apps')));
  assert.ok(TEST_SUITE_GLOBS.some((g) => g.includes('LogicTest')));
  assert.ok(CORE_AREAS.every((a) => a.requiredBy.length > 0), '每个域标注任务来源');
});
