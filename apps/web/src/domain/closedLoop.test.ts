import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  todoConfirmSuccess,
  keepInputsOnFailure,
  PLAN_DETAIL_RULES,
  resumeNeedsFutureConfirm,
  visibleShares,
  shareChangeNotifyTargets,
  OWNER_VERIFY_COPY,
  TRANSFER_TTL_HOURS,
  transferStateV7,
  OWNERSHIP_TRANSFER_IMPACTS,
  transferAcceptEffects,
  transferFailedKeepsOwner,
  adminTransferInvalidatedByExit,
  ADMIN_TRANSFER_SEPARATION,
  recycleRemainingDays,
  restoreAllowed,
  RESTORE_GUIDANCE,
  syncRowCopy,
  LOGOUT_SYNC_CHOICES,
  activityPublishBlockers,
  publishTargetState,
  SIGNUP_DETAIL_FIELDS,
  cancelSignupConfirmCopy,
  onlyOwnActivities,
  maskName,
  SIGNUP_VIEW_AUDIT,
  cancelActivityNotifyProgress,
  LIST_EDITOR_STEPS,
  listEditorBlockers,
  versionHeader,
  VERSION_DIFF_REQUIRED,
  WITHDRAW_THEN_EDIT,
  versionOutcome,
  guardianControlFlow,
  GUARDIAN_REVOKE_EFFECTS,
  SUMMARY_STATIC_SNAPSHOT_NOTE,
} from '../domain/closedLoop.ts';

test('X01 成功显示处理人/记录/下一次；家人已完成不重复写入（U57）', () => {
  const model = {
    petName: '旺财',
    existingRecord: '健康记录·疫苗',
    occurredDate: '2026-09-22',
    nextDuePreview: '2027-03-21',
    handler: '妈妈',
  };
  const ok = todoConfirmSuccess(model, 'COMPLETED');
  assert.equal(ok.headline, '已完成');
  assert.ok(ok.lines.some((l) => l.includes('妈妈')));
  assert.ok(ok.lines.some((l) => l.includes('2027-03-21')));
  const dup = todoConfirmSuccess(model, 'ALREADY_COMPLETED');
  assert.ok(dup.headline.includes('家人完成'));
  assert.ok(dup.lines.some((l) => l.includes('未重复写入')));
  assert.equal(keepInputsOnFailure(), true, '失败保留输入');
});

test('X02 编辑范围/暂停跳过区分/恢复须未来时间', () => {
  assert.ok(PLAN_DETAIL_RULES.editScope.includes('未完成'));
  assert.ok(PLAN_DETAIL_RULES.pauseVsSkip.includes('跳过'));
  const need = resumeNeedsFutureConfirm('2026-10-01', '2026-09-22');
  assert.equal(need.required, true);
  const past = resumeNeedsFutureConfirm('2026-09-01', '2026-09-22');
  assert.equal(past.required, false);
  assert.ok(past.copy.includes('必须晚于今天'));
});

test('X03 成员只见自己权限、所有者核验、变更通知受影响成员', () => {
  const views = [
    { userId: 1, petId: 10, share: 'MANAGE' as const },
    { userId: 2, petId: 10, share: 'READONLY' as const },
    { userId: 3, petId: 11, share: 'MANAGE' as const },
  ];
  assert.equal(visibleShares(views, 2, false).length, 1, '非所有者只见自己');
  assert.equal(visibleShares(views, 1, true).length, 3, '所有者见全部');
  assert.ok(OWNER_VERIFY_COPY.includes('核验'));

  const after = [
    { userId: 1, petId: 10, share: 'MANAGE' as const },
    { userId: 2, petId: 10, share: 'MANAGE' as const }, // 提权
    { userId: 3, petId: 11, share: null }, // 移除
  ];
  const notified = shareChangeNotifyTargets(views, after);
  assert.ok(notified.includes(2), '提权成员收到通知');
  assert.ok(notified.includes(3), '被移除成员收到通知');
  assert.ok(!notified.includes(1), '未变化成员不通知');
});

test('X04 转移 24h/影响清单/接受重置只读/失败不变更（U62）', () => {
  assert.equal(TRANSFER_TTL_HOURS, 24);
  const t0 = '2026-09-22T00:00:00Z';
  assert.equal(transferStateV7(t0, '2026-09-22T12:00:00Z', false), 'PENDING');
  assert.equal(transferStateV7(t0, '2026-09-23T01:00:00Z', false), 'EXPIRED');
  assert.equal(transferStateV7(t0, '2026-09-22T12:00:00Z', true), 'REVOKED');
  assert.ok(OWNERSHIP_TRANSFER_IMPACTS.length >= 4);
  assert.ok(OWNERSHIP_TRANSFER_IMPACTS.some((i) => i.includes('24')));
  const eff = transferAcceptEffects();
  assert.equal(eff.sharesResetTo, 'READONLY');
  assert.equal(eff.ownerChanged, true);
  assert.equal(transferFailedKeepsOwner(), true);
});

test('X05 管理员转交独立；成员退出使待接受申请失效', () => {
  assert.ok(ADMIN_TRANSFER_SEPARATION.includes('独立'));
  assert.equal(adminTransferInvalidatedByExit(true, true), true, '退出+待接受=失效');
  assert.equal(adminTransferInvalidatedByExit(false, true), false, '未退出仍有效');
  assert.equal(adminTransferInvalidatedByExit(true, false), false, '无在途申请');
});

test('X06 回收站剩余天数/仅所有者恢复/恢复后引导不自动执行（U66）', () => {
  assert.equal(recycleRemainingDays('2026-09-22T00:00:00Z', '2026-09-23T00:00:00Z'), 29);
  assert.equal(recycleRemainingDays('2026-08-01T00:00:00Z', '2026-09-22T00:00:00Z'), 0, '超窗归 0');
  assert.equal(restoreAllowed(true), true);
  assert.equal(restoreAllowed(false), false, '仅所有者');
  assert.equal(RESTORE_GUIDANCE.length, 2);
  assert.ok(RESTORE_GUIDANCE[0].includes('手动确认'), '不自动重新共享');
  assert.ok(RESTORE_GUIDANCE[1].includes('手动确认'), '不自动启用提醒');
});

test('X07 同步四态文案 + 登出前双选', () => {
  assert.ok(syncRowCopy('PENDING').includes('待同步'));
  assert.ok(syncRowCopy('FAILED').includes('保留'));
  assert.ok(syncRowCopy('LOST_RIGHT').includes('脱敏'), '失权保留脱敏提示');
  assert.equal(syncRowCopy('SUCCESS'), '已同步');
  assert.equal(LOGOUT_SYNC_CHOICES.length, 2);
  assert.ok(LOGOUT_SYNC_CHOICES[0].includes('同步'));
  assert.ok(LOGOUT_SYNC_CHOICES[1].includes('清除'));
});

test('X09 发布校验与提交目标态（机构直发/普通待审/领养必审 U89）', () => {
  const base = {
    type: '活动' as const,
    title: '秋日遛宠会',
    beginsAt: '2026-12-01 00:00',
    endsAt: '2026-12-02 00:00',
    deadline: '2026-11-20',
    quota: 20,
    orgState: 'APPROVED' as const,
  };
  assert.deepEqual(activityPublishBlockers(base), []);
  assert.ok(activityPublishBlockers({ ...base, title: ' ' }).includes('TITLE_REQUIRED'));
  assert.ok(activityPublishBlockers({ ...base, endsAt: '2026-11-01' }).includes('ENDS_BEFORE_BEGINS'));
  assert.ok(activityPublishBlockers({ ...base, deadline: '2026-12-05' }).includes('DEADLINE_AFTER_START'));
  assert.ok(activityPublishBlockers({ ...base, quota: -1 }).includes('QUOTA_INVALID'));
  assert.equal(publishTargetState({ type: '活动', orgState: 'APPROVED' }), 'PUBLISHED', '机构直发');
  assert.equal(publishTargetState({ type: '活动', orgState: 'NONE' }), 'PENDING', '普通待审');
  assert.equal(publishTargetState({ type: '领养', orgState: 'APPROVED' }), 'PENDING', '领养必审');
});

test('X10 报名详情字段与取消后果说明', () => {
  assert.equal(SIGNUP_DETAIL_FIELDS.length, 5);
  assert.ok(SIGNUP_DETAIL_FIELDS.includes('变更历史' as never));
  assert.ok(cancelSignupConfirmCopy('秋日遛宠会').includes('名额将释放'));
});

test('X11 仅本人活动/实名脱敏/查看留痕/无普通导出/取消进度（U72/U86 关联）', () => {
  const list = [{ orgUserId: 1 }, { orgUserId: 2 }];
  assert.equal(onlyOwnActivities(list, 1).length, 1);
  assert.equal(maskName('张三丰'), '张*丰');
  assert.equal(maskName('李四'), '李*');
  assert.equal(maskName(''), '***');
  assert.ok(SIGNUP_VIEW_AUDIT.includes('留痕'));
  assert.ok(SIGNUP_VIEW_AUDIT.includes('无导出按钮'));
  assert.ok(cancelActivityNotifyProgress(2).includes('接手队列'));
  assert.ok(cancelActivityNotifyProgress(0).includes('全部发送'));
});

test('X13 清单编辑六步 + 来益声明 + 上下架再校验', () => {
  assert.equal(LIST_EDITOR_STEPS.length, 6);
  const ok = { title: '幼猫清单', itemCount: 3, interestDeclared: true, itemsChangedAvailability: false, revalidated: false };
  assert.deepEqual(listEditorBlockers(ok), []);
  assert.ok(listEditorBlockers({ ...ok, title: ' ' }).includes('TITLE_REQUIRED'));
  assert.ok(listEditorBlockers({ ...ok, itemCount: 0 }).includes('ITEM_REQUIRED'));
  assert.ok(listEditorBlockers({ ...ok, interestDeclared: false }).includes('INTEREST_DECLARATION_REQUIRED'));
  assert.ok(
    listEditorBlockers({ ...ok, itemsChangedAvailability: true, revalidated: false }).includes('REVALIDATE_REQUIRED'),
    '上下架变化提交前再校验',
  );
});

test('X14 版本头/撤回再编辑/通过替换驳回留旧（U74）', () => {
  const head = versionHeader(1);
  assert.equal(head.live, '线上 v1');
  assert.equal(head.draft, '修改稿 v2');
  assert.equal(VERSION_DIFF_REQUIRED, true, '对比预览必看');
  assert.ok(WITHDRAW_THEN_EDIT.includes('撤回待审'));
  assert.ok(versionOutcome('approved').includes('替换上线'));
  assert.ok(versionOutcome('rejected').includes('旧版保留'));
});

test('X16 监护人：无关联不泄露存在性、匹配可查可撤（U84）', () => {
  const unknown = guardianControlFlow({ guardianPhoneMatches: true, minorLinked: false });
  assert.equal(unknown.ok, false);
  assert.equal(unknown.leak, false);
  assert.ok(unknown.display.includes('未查询到关联账号'));
  const mismatch = guardianControlFlow({ guardianPhoneMatches: false, minorLinked: true });
  assert.equal(mismatch.ok, false);
  const ok = guardianControlFlow({ guardianPhoneMatches: true, minorLinked: true });
  assert.equal(ok.ok, true);
  assert.ok(ok.display.includes('脱敏'));
  assert.equal(GUARDIAN_REVOKE_EFFECTS.length, 3);
  assert.ok(GUARDIAN_REVOKE_EFFECTS[2].includes('不暴露'));
});

test('#35 摘要静态快照说明齐（所有者/缺项/无私有实时链接）', () => {
  assert.ok(SUMMARY_STATIC_SNAPSHOT_NOTE.includes('静态快照'));
  assert.ok(SUMMARY_STATIC_SNAPSHOT_NOTE.includes('仅所有者'));
  assert.ok(SUMMARY_STATIC_SNAPSHOT_NOTE.includes('实时私有链接'));
});
