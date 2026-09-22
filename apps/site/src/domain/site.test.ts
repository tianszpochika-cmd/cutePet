import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  SITE_NAV,
  FOUR_SECTIONS,
  PRODUCTS,
  ctaFor,
  contactBlockers,
  CONTACT_CHANNELS,
  BRAND_TOKENS,
  shareCardModel,
  shareLoginHref,
  UNAVAILABLE_COPY,
  DOWNLOAD_SOURCE,
  downloadFor,
  detectEnd,
  downloadLandingModel,
  CASE_CATEGORIES,
  filterCases,
  CENTER_NOTICE,
} from '../domain/site.ts';

test('T9.1 导航 7 项与首页四板块/产品矩阵四端', () => {
  assert.equal(SITE_NAV.length, 7);
  assert.equal(FOUR_SECTIONS.length, 4);
  const ids = FOUR_SECTIONS.map((s) => s.id);
  for (const id of ['manage', 'news', 'explore', 'goods']) assert.ok(ids.includes(id as never), id);
  assert.equal(PRODUCTS.length, 4);
  const ports = PRODUCTS.map((p) => p.port);
  assert.deepEqual([...ports].sort((a, b) => a - b), [18580, 18581, 18582, 18583], '官网 18582 端口固定');
});

test('CTA 随路径变化（T9.1）', () => {
  assert.equal(ctaFor('/').label, '开始体验');
  assert.equal(ctaFor('/download').label, '下载用户端');
  assert.equal(ctaFor('/contact').label, '商务合作');
  assert.ok(ctaFor('/services').label.includes('体验'));
});

test('T9.4 联系表单校验与三通道', () => {
  const ok = { name: '张三', email: 'a@b.com', kind: '商务合作', message: '希望接入你们的方案，详谈。' };
  assert.deepEqual(contactBlockers(ok), []);
  assert.ok(contactBlockers({ ...ok, name: ' ' }).includes('NAME_REQUIRED'));
  assert.ok(contactBlockers({ ...ok, email: 'bad' }).includes('EMAIL_INVALID'));
  assert.ok(contactBlockers({ ...ok, kind: '闲聊' }).includes('KIND_INVALID'));
  assert.ok(contactBlockers({ ...ok, message: '短' }).includes('MESSAGE_INVALID'));
  assert.equal(CONTACT_CHANNELS.length, 3);
  assert.ok(CONTACT_CHANNELS.some((c) => c.id === 'report'));
});

test('T9.5 品牌令牌：色/字/动效 + 占位声明', () => {
  assert.ok(BRAND_TOKENS.name.includes('占位'));
  assert.ok(BRAND_TOKENS.logoNote.includes('占位'));
  assert.equal(BRAND_TOKENS.colors.length, 7);
  assert.equal(BRAND_TOKENS.fonts.length, 2);
  assert.ok(BRAND_TOKENS.motion.some((m) => m.includes('reduced-motion')));
  assert.ok(BRAND_TOKENS.motion.some((m) => m.includes('业务成功前')));
});

test('T9.6 分享卡与用户端同构 + 四态不可用（U94）', () => {
  const published = shareCardModel({
    kind: 'article',
    idOrSlug: 'puppy-food',
    title: '幼猫换粮指南',
    channelLabel: '猫',
    authorLabel: '编辑部',
    state: 'PUBLISHED',
  });
  assert.equal(published.visible, true);
  assert.deepEqual(
    ['title', 'chip', 'author', 'webTarget', 'loginReturn'].every((k) => k in published),
    true,
    '卡片字段与文章卡同构（U94）',
  );
  assert.equal(published.webTarget, '/news/puppy-food');

  for (const state of ['DRAFT', 'REJECTED', 'TAKEDOWN', 'DELETED'] as const) {
    const card = shareCardModel({
      kind: 'article',
      idOrSlug: 'x',
      title: 't',
      channelLabel: 'c',
      authorLabel: 'a',
      state,
    });
    assert.equal(card.visible, false, state);
    assert.ok(card.unavailable && card.unavailable.length > 4, state);
  }
  assert.ok(UNAVAILABLE_COPY.TAKEDOWN!.includes('下架'));
  assert.ok(UNAVAILABLE_COPY.TAKEDOWN!.includes('申诉'), '下架态给申诉指引（U82 官网可达）');
});

test('T9.6 分享登录闸门白名单（与 web 同口径）', () => {
  assert.equal(shareLoginHref('/news/abc'), '/web/login?return=%2Fnews%2Fabc');
  assert.equal(shareLoginHref('//evil.com'), '/web/login?return=%2F', '协议相对拒绝');
  assert.equal(shareLoginHref('https://evil.com'), '/web/login?return=%2F', '外部拒绝');
});

test('T9.6 下载信息单一源：四端结构一致（跨端一致核心）', () => {
  const ends = ['web', 'admin', 'site', 'mobile'] as const;
  const models = ends.map((e) => downloadFor(e));
  const versions = new Set(models.map((m) => m.version));
  const features = new Set(models.map((m) => m.features.join('|')));
  const checksums = new Set(models.map((m) => m.checksumNote));
  assert.equal(versions.size, 1, '版本全端一致');
  assert.equal(features.size, 1, '特性清单全端一致');
  assert.equal(checksums.size, 1, '校验说明全端一致');
  assert.deepEqual(new Set(models.map((m) => m.end)), new Set(ends), '仅端标识不同');
  assert.equal(DOWNLOAD_SOURCE.storeDistribute, false, '非商店分发（合规决策记录在案）');
  assert.ok(DOWNLOAD_SOURCE.channel.includes('非应用商店'));
});

test('UA 识别端 → 落地页（T9.6）', () => {
  assert.equal(detectEnd('Mozilla/5.0 (iPhone; CPU iPhone OS 17)'), 'mobile');
  assert.equal(detectEnd('Android 14 / Chrome'), 'mobile');
  assert.equal(detectEnd('admin-preview/1.0'), 'admin');
  assert.equal(detectEnd('site-bot/2.0'), 'site');
  assert.equal(detectEnd('Mozilla/5.0 (Windows NT 10.0)'), 'web');
  const landing = downloadLandingModel('Android 14');
  assert.equal(landing.end, 'mobile');
  assert.ok(landing.productLabel.length > 0);
  assert.ok(landing.version === DOWNLOAD_SOURCE.version, '落地页仍用同一源');
});

test('T9.3 案例过滤仅已发布 + 内容中心同源声明（U94）', () => {
  const items = [
    { id: 1, category: '宠物医院', title: '案例A', summary: '', published: true },
    { id: 2, category: '宠物店', title: '待审', summary: '', published: false },
    { id: 3, category: '宠物医院', title: '案例B', summary: '', published: true },
  ];
  assert.equal(filterCases(items, '全部').length, 2, '未发布不上官网');
  assert.equal(filterCases(items, '宠物医院').length, 2);
  assert.equal(filterCases(items, '救助机构').length, 0);
  assert.equal(CASE_CATEGORIES.length, 4);
  assert.ok(CENTER_NOTICE.includes('先审后发'));
  assert.ok(CENTER_NOTICE.includes('REJECTED'));
});
