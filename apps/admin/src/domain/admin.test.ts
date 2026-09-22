import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  ADMIN_NAV,
  visibleNav,
  loginBlockersAdmin,
  QUEUE_TABS,
  slaStatus,
  queueSort,
  canOperateRow,
  reviewActionBlockers,
  REJECT_TEMPLATES,
  batchEligible,
  REVIEW_SHORTCUTS,
  dashboardWidgets,
  roleSaveBlockers,
  presetRoleMatrix,
  auditQueryBlockers,
  redactValue,
  PERMISSIONS,
  can,
} from '../domain/workbench.ts';
import {
  takedownBlockers,
  pinAllowed,
  taxonomyTagValid,
  bannerScheduleValid,
  productBanned,
  parseProductCsv,
  CORRECTION_FIELDS,
  correctionResolveBlockers,
  correctionWriteBackValue,
  reviewHideBlockers,
  routeReviewBlockers,
  activityChangeOutcome,
  csvImportSummary,
} from '../domain/operations.ts';
import {
  BAN_ACTION_COPY,
  banBlockers,
  appealResolveBlockers,
  appealOverdue,
  familyDisputeBlockers,
  FAMILY_DISPUTE_RULE,
  opsTodoOverdue,
  escalateOpsTodo,
  OPS_TODO_LABELS,
  credentialTransition,
  CREDENTIAL_MATERIAL_VISIBLE_TO,
  directPublishRight,
  exportBlockers,
  EXPORT_AUDIT_NOTE,
  versionDiffRequired,
  reviewDisposition,
  canViewHealthEvidence,
} from '../domain/governance.ts';

// ---------- T8.1 ----------

test('侧边栏按权限过滤：审核员无角色/审计项', () => {
  const reviewer = ['review.article', 'comment.manage', 'report.handle', 'user.view', 'dashboard.view.all'];
  const nav = visibleNav(reviewer);
  const ids = nav.flatMap((g) => g.items.map((i) => i.id));
  assert.ok(ids.includes('review'));
  assert.ok(!ids.includes('roles'), '无 rbac.manage 不显示角色权限');
  assert.ok(!ids.includes('audit'), '无 audit.view 不显示审计');
  assert.ok(nav.every((g) => g.items.length > 0), '过滤后不出现空分组');
});

test('管理端登录闸门', () => {
  assert.deepEqual(loginBlockersAdmin({ username: 'op', password: 'secret1', granted: ['user.view'] }), []);
  assert.ok(loginBlockersAdmin({ username: '', password: 'secret1', granted: ['user.view'] }).includes('USERNAME_REQUIRED'));
  assert.ok(loginBlockersAdmin({ username: 'op', password: '123', granted: ['user.view'] }).includes('PASSWORD_TOO_SHORT'));
  assert.ok(loginBlockersAdmin({ username: 'op', password: 'secret1', granted: [] }).includes('NO_PERMISSION'));
});

// ---------- T8.2 ----------

test('SLA 三态 24/48（决议）', () => {
  assert.equal(slaStatus(3), 'OK');
  assert.equal(slaStatus(30), 'BREACH');
  assert.equal(slaStatus(60), 'ESCALATE');
});

test('队列排序：ESCALATE→BREACH→OK，同级按举报数', () => {
  const rows = [
    { id: 1, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 2, reports: 9, claimedBy: null },
    { id: 2, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 30, reports: 0, claimedBy: null },
    { id: 3, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 60, reports: 0, claimedBy: null },
    { id: 4, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 2, reports: 5, claimedBy: null },
  ];
  const sorted = queueSort(rows);
  // SLA 优先（3 ESCL→2 BREACH），同为 OK 按举报数降序：1(9票) > 4(5票)
  assert.deepEqual(sorted.map((r) => r.id), [3, 2, 1, 4]);
});

test('处理中锁：非领取人不可操作（U15 镜像）', () => {
  const row = { id: 1, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 1, reports: 0, claimedBy: 7 };
  assert.equal(canOperateRow(row, 7).allowed, true);
  const other = canOperateRow(row, 8);
  assert.equal(other.allowed, false);
  assert.ok(other.reason.includes('处理中锁'));
});

test('驳回必填意见 / 通过附言上限', () => {
  assert.ok(reviewActionBlockers('reject', ' ').includes('REJECT_NOTE_REQUIRED'));
  assert.deepEqual(reviewActionBlockers('approve', '通过'), []);
  assert.ok(reviewActionBlockers('approve', 'x'.repeat(501)).includes('NOTE_TOO_LONG'));
  assert.equal(REJECT_TEMPLATES.length, 3);
});

test('批量仅同类型且无锁定行', () => {
  const base = { id: 1, type: 'a', title: 't', submitter: 's', submittedAt: '', waitedHours: 1, reports: 0, claimedBy: null };
  assert.equal(batchEligible([base, { ...base, id: 2 }], true), true);
  assert.equal(batchEligible([base], false), false, '跨类型不可批量');
  assert.equal(batchEligible([{ ...base, claimedBy: 9 }], true), false, '锁定行不可批量');
  assert.equal(batchEligible([], true), false);
});

test('审核快捷键映射（交互设计 §5）', () => {
  assert.equal(REVIEW_SHORTCUTS.Enter, 'open');
  assert.equal(REVIEW_SHORTCUTS.a, 'approve');
  assert.equal(REVIEW_SHORTCUTS.r, 'reject');
  assert.equal(REVIEW_SHORTCUTS.Escape, 'close');
});

// ---------- T8.5 ----------

test('看板小部件按域权限', () => {
  const w = dashboardWidgets(['poi.create.edit']);
  assert.deepEqual(w, ['探索域']);
  const admin = dashboardWidgets(['dashboard.view.all', 'user.ban']);
  assert.ok(admin.includes('内容域') && admin.includes('留存域'));
});

test('角色保存校验：32 白名单/内置与管理员不可改', () => {
  const ok = { roleName: 'my-role', permissions: ['user.view', 'review.article'], builtin: false };
  assert.deepEqual(roleSaveBlockers(ok), []);
  assert.ok(roleSaveBlockers({ ...ok, permissions: ['fake.perm'] })[0]!.includes('INVALID_PERMISSIONS'));
  assert.ok(roleSaveBlockers({ ...ok, permissions: [] }).includes('PERMISSIONS_EMPTY'));
  assert.ok(roleSaveBlockers({ ...ok, roleName: 'administrator' }).includes('ADMIN_ROLE_IMMUTABLE'));
  assert.ok(roleSaveBlockers({ ...ok, builtin: true }).includes('BUILTIN_ROLE_IMMUTABLE'));
  assert.equal(Object.keys(presetRoleMatrix()).length, 6);
  assert.equal(PERMISSIONS.length, 32);
  assert.ok(presetRoleMatrix().supervisor!.includes('user.ban'), 'supervisor 持有封禁权（角色矩阵）');
  assert.ok(!presetRoleMatrix().supervisor!.includes('rbac.manage'), 'supervisor 无高危双点');
});

test('审计区间 ≤90 天 + 脱敏镜像', () => {
  assert.deepEqual(auditQueryBlockers('2026-07-01', '2026-09-20'), []);
  assert.deepEqual(auditQueryBlockers('2026-01-01', '2026-09-20'), ['RANGE_TOO_WIDE']);
  assert.deepEqual(auditQueryBlockers('2026-09-20', '2026-07-01'), ['RANGE_INVALID']);
  const masked = redactValue('联系 13812345678 或 a@b.com');
  assert.ok(masked.includes('138****5678'));
  assert.ok(masked.includes('a***@b.com'));
  assert.equal(redactValue('13812345678').includes('12345678'), false);
});

// ---------- T8.3 ----------

test('下架必附原因；置顶仅已发布；标签与排期校验', () => {
  assert.ok(takedownBlockers(' ').includes('REASON_REQUIRED'));
  assert.deepEqual(takedownBlockers('违规内容'), []);
  assert.equal(pinAllowed('PUBLISHED'), true);
  assert.equal(pinAllowed('DRAFT'), false);
  assert.equal(taxonomyTagValid('幼猫'), true);
  assert.equal(taxonomyTagValid(' '), false);
  assert.equal(bannerScheduleValid('2026-10-01', '2026-09-01'), false);
  assert.equal(bannerScheduleValid('', ''), true, '空=常驻');
});

test('CSV 三通道同规则（U77）：禁品类与来源逐行拒绝', () => {
  const csv = [
    'name,category,sourceType',
    '幼猫无谷粮,主粮,EDITORIAL',
    '肾脏处方粮,主粮,EDITORIAL',
    '犬用处方药,医疗保健,CSV_IMPORT',
    '宠物感冒药,医疗保健,CSV_IMPORT',
    ',主粮,EDITORIAL',
    '逗球,玩具,WEIRD',
  ].join('\n');
  const result = parseProductCsv(csv);
  assert.equal(result.accepted.length, 1, '仅一行合法');
  assert.equal(result.rejected.length, 5, '禁品类×2 + 来源非法 + 字段缺失 + 感冒药');
  assert.ok(result.rejected.some((r) => r.reason.includes('禁止品类')));
  assert.ok(result.rejected.some((r) => r.reason.includes('来源')));
  assert.ok(result.rejected.some((r) => r.reason.includes('字段缺失')));
  assert.equal(productBanned('主粮', '幼猫粮'), false);
  assert.ok(csvImportSummary(result).includes('通过 1'));
});

test('纠错：字段白名单/采纳回写/双向必填', () => {
  assert.equal(CORRECTION_FIELDS.length, 5);
  assert.ok(correctionResolveBlockers(false, ' ').includes('REJECT_NOTE_REQUIRED'));
  assert.ok(correctionResolveBlockers(true, ' ').includes('ACCEPT_VALUE_REQUIRED'));
  assert.deepEqual(correctionWriteBackValue('address', ' 东城 8 号 '), { field: 'address', value: '东城 8 号' });
});

test('评价抽审与路线审核边界', () => {
  assert.ok(reviewHideBlockers('VISIBLE', true).includes('NOT_HIDDEN'));
  assert.ok(reviewHideBlockers('HIDDEN', false).includes('ALREADY_HIDDEN'));
  assert.deepEqual(reviewHideBlockers('VISIBLE', false), []);
  assert.ok(routeReviewBlockers(true, '', 'PUBLISHED').includes('ALREADY_PUBLISHED'));
  assert.ok(routeReviewBlockers(false, ' ', 'PENDING').includes('REJECT_NOTE_REQUIRED'));
  assert.deepEqual(routeReviewBlockers(false, '不合规', 'PENDING'), []);
  const out = activityChangeOutcome(true);
  assert.equal(out.nextState, 'PUBLISHED');
  assert.ok(out.notify.includes('APPROVED'));
  assert.ok(activityChangeOutcome(false).notify.includes('KEEP_OLD'), '驳回保留旧信息');
});

// ---------- T8.4 / T8.6 ----------

test('封禁：L1–L4 文案 + 不能封自己 + 高危双保险', () => {
  assert.equal(BAN_ACTION_COPY.L4.includes('永久封禁'), true);
  const base = { actorId: 1, targetId: 2, level: 'L2' as const, reason: '广告', doubleConfirmed: true };
  assert.deepEqual(banBlockers(base), []);
  assert.ok(banBlockers({ ...base, targetId: 1 }).includes('CANNOT_BAN_SELF'));
  assert.ok(banBlockers({ ...base, reason: ' ' }).includes('REASON_REQUIRED'));
  assert.ok(banBlockers({ ...base, doubleConfirmed: false }).includes('DOUBLE_CONFIRM_REQUIRED'));
});

test('申诉：必填结论 + 48h 超时', () => {
  assert.ok(appealResolveBlockers(true, ' ').includes('RESOLUTION_REQUIRED'));
  assert.deepEqual(appealResolveBlockers(true, '撤销处置'), []);
  assert.equal(
    appealOverdue('2026-09-20T00:00:00Z', null, '2026-09-23T00:00:00Z'),
    true,
  );
  assert.equal(appealOverdue('2026-09-20T00:00:00Z', '2026-09-21T00:00:00Z', '2026-09-23T00:00:00Z'), false);
});

test('家庭纠纷：结论必填 + 不代替转移原则', () => {
  assert.ok(familyDisputeBlockers(' ').includes('RESOLUTION_REQUIRED'));
  assert.deepEqual(familyDisputeBlockers('已通知所有者处理'), []);
  assert.ok(FAMILY_DISPUTE_RULE.includes('不因接到投诉代替所有者'));
});

test('T8.6 运行异常待办：逾期升级且不显示"已运行"', () => {
  const todo = {
    kind: 'CLEANUP_FAILED' as const,
    refId: 'signup-9',
    owner: 'op1',
    createdAtIso: '2026-09-21T00:00:00Z',
    resolved: false,
  };
  assert.equal(opsTodoOverdue(todo, '2026-09-22T00:00:00Z'), false, '24h 内');
  assert.equal(opsTodoOverdue(todo, '2026-09-23T01:00:00Z'), true);
  const esc = escalateOpsTodo(todo, '2026-09-23T01:00:00Z');
  assert.equal(esc.escalated, true);
  assert.ok(esc.visible.includes('升级主管'));
  assert.ok(esc.visible.includes('失败原因'), '不得仅展示已运行');
  assert.equal(Object.keys(OPS_TODO_LABELS).length, 4);
  assert.equal(opsTodoOverdue({ ...todo, resolved: true }, '2026-09-30T00:00:00Z'), false);
});

test('T8.6 认证 5 态流转 + 材料不公开 + 直发按当前认证（U89）', () => {
  assert.equal(credentialTransition('APPLY', 'approve'), 'APPROVED');
  assert.equal(credentialTransition('APPLY', 'reject'), 'REJECTED');
  assert.equal(credentialTransition('APPROVED', 'revoke'), 'REVOKED');
  assert.equal(credentialTransition('APPROVED', 'expire'), 'EXPIRED');
  assert.equal(credentialTransition('REJECTED', 'approve'), null, '非法流转拒绝');
  assert.ok(CREDENTIAL_MATERIAL_VISIBLE_TO.includes('不公开'));
  assert.equal(directPublishRight('APPROVED'), true);
  assert.equal(directPublishRight('EXPIRED'), false);
  assert.equal(directPublishRight('REVOKED'), false);
});

test('T8.6 名单导出双条件 + 保留期 + 同意凭证（U72/U86）', () => {
  const base = { hasExportPermission: true, isOrgOwner: true, consentRecordsPresent: true, pastRetention: false };
  assert.deepEqual(exportBlockers(base), []);
  assert.ok(exportBlockers({ ...base, hasExportPermission: false }).includes('NEED_ACTIVITY_EXPORT'));
  assert.ok(exportBlockers({ ...base, isOrgOwner: false }).includes('NEED_ORG_OWNER'));
  assert.ok(exportBlockers({ ...base, pastRetention: true }).includes('RETENTION_EXPIRED'));
  assert.ok(EXPORT_AUDIT_NOTE.includes('有效期'));
});

test('T8.6 版本差异必看 + 质量退修与违规分离 + 健康证据工单绑定（U76/闭环§3）', () => {
  assert.equal(versionDiffRequired('ARTICLE'), true);
  assert.equal(versionDiffRequired('BANNER'), false);
  const quality = reviewDisposition({ kind: 'quality', rejectCount: 3 });
  assert.equal(quality.countsAsViolation, false, '质量不计违规');
  assert.equal(quality.suspended, true, '3 次暂停');
  const violation = reviewDisposition({ kind: 'violation', rejectCount: 1 });
  assert.equal(violation.countsAsViolation, true);

  const denied = canViewHealthEvidence({ boundTicketId: null, purpose: '', actorIsHandler: false });
  assert.equal(denied.allowed, false);
  assert.ok(denied.reason.includes('不开放健康档案'));
  const allowed = canViewHealthEvidence({ boundTicketId: 't-9', purpose: '核实过敏史投诉', actorIsHandler: true });
  assert.equal(allowed.allowed, true);
  assert.equal(allowed.requiresAudit, true, '查看必须留痕');
  const noPurpose = canViewHealthEvidence({ boundTicketId: 't-9', purpose: ' ', actorIsHandler: true });
  assert.equal(noPurpose.allowed, false, '必须说明用途');
});
