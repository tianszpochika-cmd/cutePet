import { test } from 'node:test';
import assert from 'node:assert/strict';
import { E2E_CASES, type E2ECase } from './e2eCases.ts';

const LAYERS: E2ECase['layer'][] = ['L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7', 'L8', 'L9'];

test('T11.2 E2E 用例覆盖 L1–L9 每层 ≥2 条', () => {
  for (const layer of LAYERS) {
    const cases = E2E_CASES.filter((c) => c.layer === layer);
    assert.ok(cases.length >= 2, `${layer} 用例不足（当前 ${cases.length}）`);
  }
});

test('T11.2 用例结构完整：步骤/预期/编号/端', () => {
  const ends = new Set(['web', 'mobile', 'admin', 'site']);
  for (const c of E2E_CASES) {
    assert.ok(/^E-L[1-9]-\d{2}$/.test(c.id), `id 不合法: ${c.id}`);
    assert.ok(c.title.length >= 4, `${c.id} 标题过短`);
    assert.ok(c.steps.length >= 2, `${c.id} 步骤不足`);
    assert.ok(c.expected.length >= 2, `${c.id} 预期不足`);
    assert.ok(c.refs.length >= 1, `${c.id} 缺验收编号`);
    for (const r of c.refs) {
      assert.ok(/^(U\d+|#\d+|BR-\d+)$/.test(r), `${c.id} 引用格式: ${r}`);
    }
    assert.ok(ends.has(c.end), `${c.id} 端非法`);
    assert.equal(typeof c.runtimeOnly, 'boolean', `${c.id} 运行时标记缺失`);
  }
});

test('T11.2 运行隔离声明：全部用例标记 runtimeOnly（本机不执行）', () => {
  assert.ok(E2E_CASES.every((c) => c.runtimeOnly === true), 'E2E 执行归本地测试阶段（L/E 隔离）');
  assert.ok(E2E_CASES.length >= 20, `用例总量 ${E2E_CASES.length} ≥20`);
});

test('T11.2 四端均有用例（跨端链路）', () => {
  const ends = new Set(E2E_CASES.map((c) => c.end));
  assert.deepEqual([...ends].sort(), ['admin', 'mobile', 'site', 'web']);
});

test('T11.2 合规关键用例齐（U83/U90/U93/U94/#32/#53）', () => {
  const refs = new Set(E2E_CASES.flatMap((c) => c.refs));
  for (const r of ['U83', 'U90', 'U93', 'U94', '#32', '#53']) {
    assert.ok(refs.has(r), `缺关键引用 ${r}`);
  }
});
