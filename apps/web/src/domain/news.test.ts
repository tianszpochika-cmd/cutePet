import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  CHANNELS,
  channelParam,
  disclaimerFor,
  channelBoundaryNote,
  commentVisibility,
  validateComment,
  reviewStateCopy,
  sharePath,
  FAVORITE_TABS,
  highlight,
  emptySearchCopy,
} from '../domain/news.ts';

test('频道 8 项（推荐+7 频道）与参数映射', () => {
  assert.equal(CHANNELS.length, 8);
  assert.equal(channelParam('推荐'), undefined);
  assert.equal(channelParam('疾病健康'), '疾病健康');
});

test('疾病/营养频道免责声明、快讯边界提示（合规 B6）', () => {
  assert.ok(disclaimerFor('疾病健康')!.includes('不构成兽医诊疗建议'));
  assert.ok(disclaimerFor('营养')!.includes('遵医嘱'));
  assert.equal(disclaimerFor('犬'), null);
  assert.ok(channelBoundaryNote('行业快讯')!.includes('不涉时政'));
  assert.equal(channelBoundaryNote('猫'), null);
});

test('评论可见性：SUSPECT 仅自见、REJECT 双不可见（镜像 U16）', () => {
  assert.equal(commentVisibility('PASS', false), 'VISIBLE');
  assert.equal(commentVisibility('SUSPECT', true), 'ONLY_SELF');
  assert.equal(commentVisibility('SUSPECT', false), 'HIDDEN');
  assert.equal(commentVisibility('REJECT', true), 'HIDDEN');
});

test('评论 1–500 校验', () => {
  assert.equal(validateComment('沙发'), null);
  assert.equal(validateComment('   '), 'COMMENT_EMPTY');
  assert.equal(validateComment('x'.repeat(501)), 'COMMENT_TOO_LONG');
});

test('审核状态文案与色调', () => {
  assert.deepEqual(reviewStateCopy('PENDING'), { label: '待审核', tone: 'orange' });
  assert.equal(reviewStateCopy('REJECTED').tone, 'red');
  assert.equal(reviewStateCopy('PUBLISHED').tone, 'green');
  assert.equal(reviewStateCopy('TAKEDOWN').label, '已下架');
});

test('分享深链四类', () => {
  assert.equal(sharePath('article', 'abc'), '/news/abc');
  assert.equal(sharePath('summary', '12'), '/pets/12');
  assert.ok(sharePath('route', '9').includes('id=9'));
  assert.equal(FAVORITE_TABS.length, 5);
});

test('搜索高亮切分（单/多命中）', () => {
  const single = highlight('幼猫粮怎么选', '猫粮');
  assert.equal(single.length, 3);
  assert.equal(single[1]!.hit, true);
  const multi = highlight('幼猫粮与成猫粮', '猫粮');
  assert.equal(multi.length, 4);
  assert.ok(multi[1]!.hit && multi[3]!.hit);
  assert.equal(highlight('全文', '缺失')[0]!.hit, false);
});

test('空搜索兜底文案（U17）', () => {
  assert.ok(emptySearchCopy('狗粮').includes('狗粮'));
});
