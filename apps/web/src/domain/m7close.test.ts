import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  CATEGORIES,
  SORT_OPTIONS,
  bannedProductGuard,
  refStateCopy,
  editorTestBadge,
  PRICE_DISCLAIMER,
  sourceLabel,
  favoriteToast,
} from '../domain/goods.ts';
import {
  petActionAllowed,
  readOnlyHiddenEntries,
  deepLinkDenied,
  familyPageModel,
  DEFAULT_NEW_MEMBER_SHARE,
  JOIN_SCOPE_COPY,
  ME_GALLERY,
  galleryGate,
  MESSAGE_CATEGORIES,
  unreadBadge,
  FAVORITE_GROUPS,
} from '../domain/family.ts';
import {
  RULE_DOCS,
  deleteBlockers,
  cooldownState,
  COOLDOWN_DAYS,
  COOLDOWN_LOGIN_COPY,
  BLOCKED_ALLOWED_ACTIONS,
  restoredCapabilities,
  blockedCopy,
  guestCapabilities,
  EXPORT_STEPS,
  HELP_SECTIONS,
  notificationWarnings,
} from '../domain/settings.ts';
import {
  pageState,
  staggerDelay,
  motionPlan,
  successMotionGate,
  LOOP_BUDGET,
  loopWithinBudget,
  skeletonRows,
  errorCopy,
} from '../domain/motion.ts';

// ---------- T7.7 用品 ----------

test('用品 7 类 + 排序选项', () => {
  assert.equal(CATEGORIES.length, 7);
  assert.equal(SORT_OPTIONS.length, 2);
});

test('禁止品类展示层兜底（U23/U77 与后端同表）', () => {
  assert.ok(bannedProductGuard('主粮', '肾脏处方粮'));
  assert.ok(bannedProductGuard('医疗保健', '宠物感冒药'));
  assert.equal(bannedProductGuard('主粮', '幼猫无谷粮'), null);
});

test('U88 引用展示三态文案', () => {
  assert.equal(refStateCopy('ON_SHELF').tone, 'normal');
  assert.ok(refStateCopy('OFF_SHELF').text.includes('已下架'));
  assert.equal(refStateCopy('OFF_SHELF').tone, 'offShelf');
  assert.equal(refStateCopy('CLEARED').tone, 'removed');
});

test('U77 编辑部测试徽标仅编辑+员工', () => {
  assert.equal(editorTestBadge(true, true), true);
  assert.equal(editorTestBadge(false, true), false);
  assert.equal(editorTestBadge(true, false), false);
});

test('价格免责与来源标注（方案 A/T5.3）', () => {
  assert.ok(PRICE_DISCLAIMER.includes('不提供购买服务'));
  assert.equal(sourceLabel('EDITORIAL'), '资料整理');
  assert.equal(sourceLabel('UNKNOWN'), '未标注来源');
  assert.equal(favoriteToast(true), '已加入我的商品收藏');
});

// ---------- T7.8 家庭与个人中心 ----------

test('#31 逐动作权限：摘要仅所有者、档位仅所有者、只读仅可见', () => {
  const owner = { actorIsPetOwner: true, role: 'MEMBER' as const, share: null };
  assert.equal(petActionAllowed({ ...owner, action: 'SUMMARY_EXPORT' }), true);
  // 所有者但动作维度在非所有者视角
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'ADMIN', share: 'MANAGE', action: 'SUMMARY_EXPORT' }),
    false,
    '#35 摘要仅所有者',
  );
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'ADMIN', share: 'MANAGE', action: 'SET_SHARE' }),
    false,
    'U61 非所有者不能提升档位',
  );
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'MEMBER', share: 'READONLY', action: 'VIEW' }),
    true,
  );
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'MEMBER', share: 'READONLY', action: 'EDIT_RECORD' }),
    false,
  );
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'ADMIN', share: 'MANAGE', action: 'EDIT_RECORD' }),
    true,
    '可管理档可编辑（管理员角色不越权到无共享宠物）',
  );
  assert.equal(
    petActionAllowed({ actorIsPetOwner: false, role: 'ADMIN', share: null, action: 'VIEW' }),
    false,
    '未共享不可见（属于家庭≠访问个人未共享宠物 #38）',
  );
});

test('#31 只读隐藏入口清单', () => {
  const hidden = readOnlyHiddenEntries('READONLY', false);
  assert.ok(hidden.includes('record-edit'));
  assert.ok(hidden.includes('summary-export'));
  assert.ok(readOnlyHiddenEntries('MANAGE', false).includes('summary-export'));
  assert.deepEqual(readOnlyHiddenEntries(null, true), [], '所有者无隐藏');
});

test('#31 无权限深链 → 拒绝页且不渲染数据', () => {
  assert.equal(deepLinkDenied(true), null);
  const denied = deepLinkDenied(false)!;
  assert.equal(denied.page, 'denied');
  assert.equal(denied.renderData, false);
});

test('#38 家庭页模型：管理员与宠物所有者分列 + 私有边界说明', () => {
  const model = familyPageModel([
    { userId: 1, role: 'OWNER', ownedPetNames: ['旺财'] },
    { userId: 2, role: 'MEMBER', ownedPetNames: ['豆豆'] },
    { userId: 3, role: 'MEMBER', ownedPetNames: [] },
  ]);
  assert.equal(model.admins.length, 1);
  assert.equal(model.petOwners.length, 2);
  assert.ok(model.note.includes('默认只读'));
  assert.equal(DEFAULT_NEW_MEMBER_SHARE, 'READONLY');
});

test('#40 加入范围声明覆盖三条边界', () => {
  assert.ok(JOIN_SCOPE_COPY.includes('自有') || JOIN_SCOPE_COPY.includes('自己的宠物'));
  assert.ok(JOIN_SCOPE_COPY.includes('只读'));
  assert.ok(JOIN_SCOPE_COPY.includes('不会自动'));
});

test('#45 宫格六项含活动中心 + 未登录统一回跳', () => {
  assert.equal(ME_GALLERY.length, 6);
  assert.ok(ME_GALLERY.some((g) => g.id === 'activity' && g.to === '/me/activity-center'));
  assert.equal(galleryGate(true, '/pets'), '/pets');
  const gate = galleryGate(false, '/pets');
  assert.ok(gate.startsWith('/login?return='));
  assert.ok(decodeURIComponent(gate).includes('/pets'));
});

test('消息五类 + 未读角标 + 收藏五组', () => {
  assert.equal(MESSAGE_CATEGORIES.length, 5);
  assert.equal(unreadBadge(0), '');
  assert.equal(unreadBadge(5), '5');
  assert.equal(unreadBadge(120), '99+');
  assert.equal(FAVORITE_GROUPS.length, 5);
});

// ---------- T7.9 设置与合规 ----------

test('规则五页齐（合规 B8）', () => {
  assert.equal(RULE_DOCS.length, 5);
  const slugs = RULE_DOCS.map((d) => d.slug);
  for (const s of ['terms', 'privacy', 'community', 'children', 'report']) {
    assert.ok(slugs.includes(s as never), s);
  }
});

test('#53 注销两维依赖 + 活动报名 + 实际入口', () => {
  const blockers = deleteBlockers({
    ownedSharedPets: 2,
    isFamilyAdmin: true,
    activeSignups: 1,
    publishedActivities: 1,
  });
  assert.equal(blockers.length, 4);
  assert.ok(blockers.every((b) => b.actionRoute.startsWith('/')));
  assert.ok(blockers.some((b) => b.code === 'OWNED_SHARED_PETS'));
  assert.ok(blockers.some((b) => b.code === 'FAMILY_ADMIN'));
  assert.ok(blockers.some((b) => b.code === 'ACTIVE_SIGNUPS'));
  assert.equal(
    deleteBlockers({ ownedSharedPets: 0, isFamilyAdmin: false, activeSignups: 0, publishedActivities: 0 }).length,
    0,
    '无依赖即可注销',
  );
});

test('冷静期 15 天与可撤销窗口', () => {
  assert.equal(COOLDOWN_DAYS, 15);
  const state = cooldownState('2026-09-22T00:00:00Z', '2026-09-24T00:00:00Z');
  assert.equal(state.daysLeft, 13);
  assert.equal(state.cancellable, true);
  const expired = cooldownState('2026-09-01T00:00:00Z', '2026-09-25T00:00:00Z');
  assert.equal(expired.cancellable, false);
  assert.ok(COOLDOWN_LOGIN_COPY.body.includes('不自动撤销'), '#53 登录=明确撤销确认页');
});

test('X15 受限账号：仅四类动作 + 不转普通个人中心', () => {
  assert.equal(BLOCKED_ALLOWED_ACTIONS.length, 4);
  assert.ok(BLOCKED_ALLOWED_ACTIONS.includes('申诉'));
  const copy = blockedCopy();
  assert.ok(copy.note.includes('不会跳转普通个人中心'));
  const restored = restoredCapabilities({
    banExpired: true,
    muteExpired: false,
    postRightSuspended: true,
  });
  assert.deepEqual(restored, ['登录与普通浏览'], '#56 仅恢复已到期的处罚能力');
  const all = restoredCapabilities({ banExpired: true, muteExpired: true, postRightSuspended: false });
  assert.equal(all.length, 2);
});

test('#31 受限游客：未同意隐私不加载分析/定位', () => {
  assert.deepEqual(guestCapabilities(false), { analytics: false, geo: false });
  assert.deepEqual(guestCapabilities(true), { analytics: true, geo: true });
});

test('导出三步与帮助五区 + 通知警告', () => {
  assert.equal(EXPORT_STEPS.length, 3);
  assert.ok(EXPORT_STEPS[0].includes('核验'));
  assert.equal(HELP_SECTIONS.length, 5);
  const warnings = notificationWarnings({ reminderPush: false, interactionPush: true, 免打扰: false });
  assert.ok(warnings[0]!.includes('仅站内通知'), '#34 未就绪推送明确写仅站内');
  assert.ok(
    notificationWarnings({ reminderPush: true, interactionPush: true, 免打扰: true })[0]!.includes('顺延'),
  );
});

// ---------- T7.10 动效与三态 ----------

test('页面四态解析（三态+ready）', () => {
  assert.equal(pageState({ loading: true, error: null, empty: false }), 'loading');
  assert.equal(pageState({ loading: false, error: 'x', empty: false }), 'error');
  assert.equal(pageState({ loading: false, error: null, empty: true }), 'empty');
  assert.equal(pageState({ loading: false, error: null, empty: false }), 'ready');
});

test('级联延迟 80ms×min(i,8)', () => {
  assert.equal(staggerDelay(0), 0);
  assert.equal(staggerDelay(3), 240);
  assert.equal(staggerDelay(99), 640, '封顶 8 项');
});

test('reduced-motion 降级：关位移、≤100ms、不循环', () => {
  const plan = motionPlan(true, 'float');
  assert.equal(plan.transform, false);
  assert.equal(plan.durationMs, 100);
  assert.equal(plan.loop, false);
  const enter = motionPlan(false, 'enter');
  assert.equal(enter.transform, true);
  assert.equal(motionPlan(false, 'float').loop, true);
});

test('闭环§4：业务成功前不得播放完成照护动效', () => {
  assert.equal(successMotionGate(true, true), true);
  assert.equal(successMotionGate(true, false), false, '未成功不播');
  assert.equal(successMotionGate(false, true), false, '未提供也不播');
});

test('循环动画预算 Web≤3 / 移动≤1', () => {
  assert.equal(LOOP_BUDGET.web, 3);
  assert.equal(LOOP_BUDGET.mobile, 1);
  assert.equal(loopWithinBudget('web', 3), true);
  assert.equal(loopWithinBudget('web', 4), false);
  assert.equal(loopWithinBudget('mobile', 2), false);
});

test('骨架行数与错误文案', () => {
  assert.equal(skeletonRows('feed'), 12);
  assert.equal(skeletonRows('detail'), 4);
  assert.equal(errorCopy(null), '');
  assert.ok(errorCopy('boom').includes('可重试'));
});
