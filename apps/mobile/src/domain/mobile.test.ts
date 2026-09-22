import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  TABS,
  stackAction,
  LAYERS,
  layerCoverage,
  PULL_REFRESH_THRESHOLD,
  SHEET_DISMISS_THRESHOLD,
  PULL_REFRESH_THRESHOLD as PR,
  pullRefreshTriggered,
  sheetDismiss,
  safeAreaInsets,
  sheetViewportHeight,
  backTarget,
  loginBlockersMobile,
} from '../domain/mobile.ts';
import {
  CHANNEL_META,
  nextPermissionState,
  webNotifyBlockers,
  channelPanel,
  unreadBadgeMobile,
  MESSAGE_CATEGORIES_MOBILE,
} from '../domain/notify.ts';
import {
  FOLLOW_FACTOR,
  followDy,
  sheetTransform,
  recordSuccessMotion,
  LOOP_BUDGET,
  syncRowCopy,
  LOGOUT_SYNC_CHOICES,
  OFFLINE_COPY,
  conflictOptions,
  applyConflictChoice,
  offlineActionsAllowed,
  pendingBadge,
  convertWeight,
  weightLabel,
} from '../domain/mobileMotion.ts';

// ---------- T10.1 骨架 ----------

test('4-Tab（决议：用品并入首页）+ 栈规则', () => {
  assert.equal(TABS.length, 4);
  assert.deepEqual(TABS.map((t) => t.id), ['home', 'pet', 'explore', 'me']);
  assert.equal(stackAction('home', 'pet'), 'SWITCH_KEEP_STACK', '切 Tab 保留各自栈');
  assert.equal(stackAction('home', 'home', '/pets/1'), 'PUSH', '同 Tab 入栈');
  assert.equal(stackAction('home', 'home'), 'POP_TO_ROOT', '再点当前 Tab 回根');
});

test('L1–L5 层覆盖（从登录到所有功能）', () => {
  assert.equal(LAYERS.length, 5);
  const cov = layerCoverage();
  assert.equal(cov.length, 5);
  assert.ok(cov.every((c) => c.anchors >= 3), '每层锚点齐全');
  assert.ok(LAYERS[0]!.anchors.includes('协议双勾' as never));
  assert.ok(LAYERS[2]!.anchors.includes('一键记录' as never));
});

test('手势阈值与安全区（T10.1）', () => {
  assert.equal(PULL_REFRESH_THRESHOLD, 60);
  assert.equal(SHEET_DISMISS_THRESHOLD, 120);
  assert.equal(pullRefreshTriggered(61), true);
  assert.equal(pullRefreshTriggered(30), false);
  assert.equal(sheetDismiss(130, 0), true, '位移过阈');
  assert.equal(sheetDismiss(50, 0.8), true, '快速下滑');
  assert.equal(sheetDismiss(50, 0.1), false, '回弹');
  const insets = safeAreaInsets({ bottom: 34, top: 47 });
  assert.equal(insets.tabBottom, 34, '取设备安全区');
  assert.equal(safeAreaInsets({ bottom: 0, top: 0 }).tabBottom, 12, '无安全区保底');
  assert.equal(sheetViewportHeight(800), 624, 'Sheet 78% 视口');
  assert.equal(backTarget(3, '/pets'), '__BACK__');
  assert.equal(backTarget(1, '/pets'), '/pets', '无历史回 Tab 根');
});

test('移动登录闸门与 Web 同口径', () => {
  const ok = { phone: '13812345678', code: '123456', agreedTerms: true, agreedPrivacy: true };
  assert.deepEqual(loginBlockersMobile(ok), []);
  assert.ok(loginBlockersMobile({ ...ok, agreedTerms: false }).includes('AGREEMENTS_REQUIRED'));
  assert.equal(PR, 60);
});

// ---------- T10.4 通知三通道 ----------

test('三通道元数据（厂商=stub 不谎报）', () => {
  assert.equal(Object.keys(CHANNEL_META).length, 3);
  assert.equal(CHANNEL_META.VENDOR_PUSH.support, 'stub');
  assert.ok(CHANNEL_META.VENDOR_PUSH.note.includes('占位'));
  assert.equal(CHANNEL_META.IN_APP.support, 'always');
});

test('权限状态机 + 隐私前置闸门', () => {
  assert.equal(nextPermissionState('default', 'userGrant'), 'granted');
  assert.equal(nextPermissionState('granted', 'request'), 'STAY', '已授权不再弹');
  assert.equal(nextPermissionState('denied', 'request'), 'STAY', '已拒绝不重复弹');
  assert.equal(nextPermissionState('denied', 'reset'), 'default');
  assert.deepEqual(webNotifyBlockers({ privacyAgreed: true, permission: 'default', secureContext: true }), []);
  assert.ok(webNotifyBlockers({ privacyAgreed: false, permission: 'default', secureContext: true }).includes('PRIVACY_NOT_AGREED'));
  assert.ok(webNotifyBlockers({ privacyAgreed: true, permission: 'denied', secureContext: true }).includes('PERMISSION_DENIED'));
  assert.ok(webNotifyBlockers({ privacyAgreed: true, permission: 'default', secureContext: false }).includes('NEED_HTTPS'));
});

test('设置页三通道面板：站内不可关/厂商占位/浏览器按授权态', () => {
  const inApp = channelPanel('IN_APP', 'default', false);
  assert.equal(inApp.enabled, true);
  assert.equal(inApp.toggleable, false, '站内不可关');
  const vendor = channelPanel('VENDOR_PUSH', 'default', true);
  assert.equal(vendor.enabled, false);
  assert.equal(vendor.toggleable, false);
  const web = channelPanel('WEB_NOTIFICATION', 'default', true);
  assert.equal(web.toggleable, true);
  const denied = channelPanel('WEB_NOTIFICATION', 'denied', true);
  assert.ok(denied.hint.includes('PERMISSION_DENIED'));
  assert.equal(unreadBadgeMobile(120), '99+');
  assert.equal(unreadBadgeMobile(0), '');
  assert.equal(MESSAGE_CATEGORIES_MOBILE.length, 5);
});

// ---------- T10.5 动效 ----------

test('跟手阻尼与 Sheet 变换', () => {
  assert.equal(FOLLOW_FACTOR, 0.5);
  assert.equal(followDy(200, 300), 100, '半阻尼');
  assert.equal(followDy(800, 300), 300, '封顶');
  assert.equal(followDy(-10, 300), 0, '上推不跟手');
  const open = sheetTransform(80, false);
  assert.equal(open.translateY, 80);
  assert.equal(open.durationMs, 320);
  const closed = sheetTransform(0, true);
  assert.equal(closed.opacity, 0);
  assert.ok(closed.translateY > 500);
});

test('记成功动效复用闭环§4 门控 + 循环预算移动=1', () => {
  assert.equal(recordSuccessMotion(true, true), true);
  assert.equal(recordSuccessMotion(true, false), false, '业务未成功不播');
  assert.equal(recordSuccessMotion(false, true), false);
  assert.equal(LOOP_BUDGET.mobile, 1, '移动端循环预算 ≤1（动效规范 §7）');
  assert.equal(LOOP_BUDGET.web, 3);
});

// ---------- T10.6 离线反馈（复用 T7.11 syncRowCopy） ----------

test('离线五态文案：未同步≠已完成（#32）', () => {
  assert.equal(Object.keys(OFFLINE_COPY).length, 5);
  assert.ok(OFFLINE_COPY.OFFLINE_PENDING.recordState.startsWith('未同步'));
  assert.ok(!OFFLINE_COPY.OFFLINE_PENDING.recordState.includes('已完成'), '离线绝不显示已完成');
  assert.ok(OFFLINE_COPY.CONFLICT.banner.includes('不会自动覆盖'));
  assert.ok(syncRowCopy('PENDING').includes('待同步'), '复用 Web closedLoop 同一实现');
  assert.equal(LOGOUT_SYNC_CHOICES.length, 2, '复用登出双选');
});

test('冲突双选永不自动 + 离线权限矩阵', () => {
  const opts = conflictOptions('CONFLICT');
  assert.equal(opts.length, 2);
  assert.equal(opts[0]!.choice, 'DISCARD_LOCAL');
  assert.equal(opts[1]!.danger, true, '覆盖为危险操作');
  assert.deepEqual(conflictOptions('ONLINE'), []);
  assert.ok(applyConflictChoice('CONFIRM_OVERWRITE').notice.includes('绝不自动覆盖'));
  const allowed = offlineActionsAllowed('OFFLINE_PENDING');
  assert.equal(allowed.record, true, '离线可记');
  assert.equal(allowed.submit, false, '投稿报名需在线');
  assert.equal(pendingBadge(3, 'OFFLINE_PENDING'), '3');
  assert.equal(pendingBadge(0, 'ONLINE'), '');
  assert.equal(pendingBadge(0, 'CONFLICT'), '…', '冲突态有指示');
});

test('体重 kg/斤双单位（决议，存储统一 kg）', () => {
  assert.equal(convertWeight(10, 'jin'), 20);
  assert.equal(convertWeight(20, 'kg'), 20);
  assert.equal(convertWeight(12.5, 'jin'), 25);
  assert.equal(weightLabel(12.5, 'kg'), '12.5kg');
  assert.equal(weightLabel(12.5, 'jin'), '25斤');
});

// ---------- T10.6 X 复用（同一 closedLoop 实现跨端一致） ----------

test('复用 T7.11：X 系列规则在移动端同源可用', async () => {
  const cl = await import('../../../web/src/domain/closedLoop.ts');
  assert.equal(cl.TRANSFER_TTL_HOURS, 24, 'X04 24h 同源');
  assert.equal(cl.transferFailedKeepsOwner(), true);
  assert.ok(cl.OWNERSHIP_TRANSFER_IMPACTS.length >= 4);
  assert.equal(cl.restoreAllowed(false), false, 'X06 仅所有者恢复同源');
  const fam = await import('../../../web/src/domain/family.ts');
  assert.equal(fam.DEFAULT_NEW_MEMBER_SHARE, 'READONLY', '#38 新成员默认只读同源');
  assert.equal(fam.galleryGate(false, '/pets').startsWith('/login'), true, '#45 未登录回跳同源');
  const motion = await import('../../../web/src/domain/motion.ts');
  assert.equal(motion.pageState({ loading: true, error: null, empty: false }), 'loading', '三态同源');
  assert.equal(motion.successMotionGate(true, false), false, '动效门控同源');
});
