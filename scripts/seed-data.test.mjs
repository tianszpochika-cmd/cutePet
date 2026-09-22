import { test } from 'node:test';
import assert from 'node:assert/strict';
import { buildSeedData, CHANNELS } from './seed-data.mjs';

const seed = buildSeedData();

test('seed 规模满足 §0.1-2 口径', () => {
  const c = seed.counts;
  assert.ok(c.accounts >= 2);
  assert.ok(c.pets >= 3);
  assert.equal(c.articles, 30);
  assert.equal(c.pois, 200);
  assert.equal(c.products, 50);
  assert.ok(c.reports >= 1 && c.submissions >= 1);
  assert.equal(c.families, 1);
});

test('账号手机号合法（契约校验口径）', () => {
  const re = /^1[3-9]\d{9}$/;
  for (const a of seed.accounts) assert.match(a.phone, re);
});

test('文章覆盖 7 个频道且状态分布含待审/驳回/草稿', () => {
  const channels = new Set(seed.articles.map((a) => a.channel));
  assert.equal(channels.size, CHANNELS.length);
  const states = new Set(seed.articles.map((a) => a.status));
  for (const s of ['PUBLISHED', 'PENDING', 'REJECTED', 'DRAFT']) assert.ok(states.has(s), `缺少状态 ${s}`);
});

test('商品不含禁止品类（兽药/处方）', () => {
  const banned = ['兽药', '处方'];
  for (const p of seed.products) {
    for (const b of banned) assert.ok(!p.category.includes(b) && !p.name.includes(b));
  }
});

test('POI 坐标在城市邻域内且五城覆盖', () => {
  const cities = new Set(seed.pois.map((p) => p.city));
  assert.equal(cities.size, 5);
  for (const p of seed.pois) {
    assert.ok(p.lng > 70 && p.lng < 140, `lng 越界 ${p.lng}`);
    assert.ok(p.lat > 15 && p.lat < 55, `lat 越界 ${p.lat}`);
  }
});

test('家庭结构：1 创建者 + 1 成员 + 共享宠物档位', () => {
  const f = seed.family;
  assert.equal(f.members.length, 2);
  assert.equal(f.ownerId, 'u1');
  assert.equal(f.members.find((m) => m.userId === 'u1')?.level, 'OWNER');
  assert.equal(f.sharedPets[0]?.petId, 'p1');
});
