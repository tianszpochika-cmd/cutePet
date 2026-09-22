import { test } from 'node:test';
import assert from 'node:assert/strict';
import { tokens } from '../src/tokens.ts';

const HEX = /^#[0-9A-Fa-f]{6}$/;

test('所有颜色为合法 6 位 HEX', () => {
  const flat = [
    tokens.color.primary,
    tokens.color.primaryHover,
    tokens.color.primarySoft,
    tokens.color.ink,
    tokens.color.ink2,
    tokens.color.line,
    tokens.color.bg,
    tokens.color.surface,
    ...Object.values(tokens.color.module),
    ...Object.values(tokens.color.semantic),
  ];
  for (const c of flat) assert.match(c, HEX, `非法颜色: ${c}`);
});

test('五个模块色互不相同', () => {
  const list = Object.values(tokens.color.module);
  assert.equal(new Set(list).size, list.length, '模块色存在重复');
});

test('间距阶梯严格递增且为 4 的倍数', () => {
  const s = tokens.spacing;
  for (let i = 0; i < s.length; i++) {
    assert.equal(s[i]! % 4, 0, `间距 ${s[i]} 不是 4 的倍数`);
    if (i > 0) assert.ok(s[i]! > s[i - 1]!, `间距未递增于 index ${i}`);
  }
});

test('圆角阶梯（除 full）递增', () => {
  const order = [tokens.radius.s, tokens.radius.input, tokens.radius.card, tokens.radius.cardL, tokens.radius.panel, tokens.radius.panelL];
  for (let i = 1; i < order.length; i++) assert.ok(order[i]! > order[i - 1]!, `圆角未递增于 index ${i}`);
  assert.ok(tokens.radius.full > tokens.radius.panelL);
});

test('双基准字阶键一致且桌面 ≥ 移动', () => {
  const d = tokens.typeScale.desktop;
  const m = tokens.typeScale.mobile;
  assert.deepEqual(Object.keys(d), Object.keys(m), '两套字阶键不一致');
  for (const key of Object.keys(d) as Array<keyof typeof d>) {
    assert.ok(d[key] >= m[key], `字阶 ${key} 桌面(${d[key]}) < 移动(${m[key]})`);
  }
});

test('端口唯一且避让 cloudstudy 占用段 18070–18190', () => {
  const ports = [tokens.port.gateway, tokens.port.web, tokens.port.admin, tokens.port.site, tokens.port.mobile, ...tokens.port.services];
  assert.equal(new Set(ports).size, ports.length, '端口冲突');
  for (const p of ports) {
    assert.ok(p < 18070 || p > 18190, `端口 ${p} 落入 cloudstudy 占用段`);
  }
  assert.ok(tokens.port.services[0]! < tokens.port.services[1]!);
});
