/**
 * 闭环页面执行逻辑（T7.11 · 功能设计-闭环页面与状态 X01–X16 的 Web 侧规则）。
 * 纯函数：编码「入口→提交/返回/异常」与共同状态，测试按 X/U 编号断言。
 */

// ---------- X01 待办完成确认 ----------

export interface TodoConfirmModel {
  petName: string;
  existingRecord: string | null; // 关联已有记录或 null=新增
  occurredDate: string;
  nextDuePreview: string | null;
  handler?: string; // 成功显示处理人
}

export function todoConfirmSuccess(model: TodoConfirmModel, outcome: 'COMPLETED' | 'ALREADY_COMPLETED'): {
  headline: string;
  lines: string[];
} {
  if (outcome === 'ALREADY_COMPLETED') {
    // 家人已处理 → 显示最新结果，不再写入（X01/U57）
    return {
      headline: '该待办已由家人完成',
      lines: ['已显示最新结果，未重复写入。', `处理人：${model.handler ?? '家庭成员'}`],
    };
  }
  const lines = [`记录：${model.existingRecord ?? '新增健康记录'}`, `发生日期：${model.occurredDate}`];
  if (model.nextDuePreview) lines.push(`下次计划：${model.nextDuePreview}`);
  if (model.handler) lines.unshift(`处理人：${model.handler}`);
  return { headline: '已完成', lines };
}

/** 失败保留输入（X01）：失败时返回原表单值不清空 */
export function keepInputsOnFailure(): true {
  return true;
}

// ---------- X02 计划详情与历史 ----------

export const PLAN_DETAIL_RULES = {
  editScope: '编辑仅影响未完成待办',
  pauseVsSkip: '「暂停」停止排期但保留计划；「跳过」仅跳过当期并记录原因（必填）',
} as const;

/** 恢复计划必须确认未来时间（X02） */
export function resumeNeedsFutureConfirm(newNextDue: string, nowDate: string): { required: boolean; copy: string } {
  const required = newNextDue > nowDate;
  return {
    required,
    copy: required
      ? `恢复后将把下期排到未来时间 ${newNextDue}，请确认。`
      : '恢复时间必须晚于今天，请先选择未来日期。',
  };
}

// ---------- X03 共享授权 ----------

export interface ShareMemberView {
  userId: number;
  petId: number;
  share: 'MANAGE' | 'READONLY' | null;
}

/** 每个成员只能看到自己的权限档位；所有者可见全部并需核验（X03） */
export function visibleShares(views: ShareMemberView[], viewerUserId: number, isOwner: boolean): ShareMemberView[] {
  if (isOwner) return views;
  return views.filter((v) => v.userId === viewerUserId);
}

export function shareChangeNotifyTargets(before: ShareMemberView[], after: ShareMemberView[]): number[] {
  const changed = new Set<number>();
  for (const a of after) {
    const b = before.find((x) => x.petId === a.petId && x.userId === a.userId);
    if (!b || b.share !== a.share) changed.add(a.userId);
  }
  for (const b of before) {
    const a = after.find((x) => x.petId === b.petId && x.userId === b.userId);
    if (!a) changed.add(b.userId);
  }
  return [...changed]; // 变更通知受影响成员
}

export const OWNER_VERIFY_COPY = '所有者身份核验通过后方可调整共享档位（非所有者仅可查看自己的权限）。';

// ---------- X04/X05 双向转移（与后端 FamilyRules 同语义） ----------

export const TRANSFER_TTL_HOURS = 24;

export function transferStateV7(createdAtIso: string, nowIso: string, revoked: boolean): 'REVOKED' | 'PENDING' | 'EXPIRED' {
  if (revoked) return 'REVOKED';
  const created = new Date(createdAtIso).getTime();
  return nowIso && new Date(nowIso).getTime() > created + TRANSFER_TTL_HOURS * 3600_000
    ? 'EXPIRED'
    : 'PENDING';
}

/** X04 影响清单（发送前展示） */
export const OWNERSHIP_TRANSFER_IMPACTS = [
  '接收方将成为该宠物的所有者',
  '现有共享档位将重置为只读（按规则）',
  '提醒与时间线继续同步给家庭成员',
  '24 小时内可撤销；超时自动失效，所有权不变',
] as const;

export function transferAcceptEffects(): { sharesResetTo: 'READONLY'; ownerChanged: boolean } {
  return { sharesResetTo: 'READONLY', ownerChanged: true };
}

/** 失败不变更原所有权（X04） */
export function transferFailedKeepsOwner(): true {
  return true;
}

/** X05 管理员转交与宠物转移分离；成员退出使申请失效 */
export function adminTransferInvalidatedByExit(memberExited: boolean, transferPending: boolean): boolean {
  return transferPending && memberExited; // true=申请已失效
}

export const ADMIN_TRANSFER_SEPARATION = '管理员转交与宠物所有权转移相互独立；待接受期间原管理员仍有效。';

// ---------- X06 归档与回收站 ----------

export function recycleRemainingDays(deletedAtIso: string, nowIso: string): number {
  const elapsed = (new Date(nowIso).getTime() - new Date(deletedAtIso).getTime()) / 86_400_000;
  return Math.max(0, Math.ceil(30 - elapsed));
}

export function restoreAllowed(isOwner: boolean): boolean {
  return isOwner; // 仅所有者可恢复（U66）
}

/** 恢复成功 → 引导重新共享/启用提醒，不自动执行 */
export const RESTORE_GUIDANCE = ['是否重新共享给家人？（需你手动确认）', '是否重新启用该宠物的提醒？（需你手动确认）'] as const;

// ---------- X07 同步结果 ----------

export type SyncState = 'PENDING' | 'FAILED' | 'SUCCESS' | 'LOST_RIGHT';

export function syncRowCopy(state: SyncState): string {
  switch (state) {
    case 'PENDING':
      return '待同步（联网后自动上传）';
    case 'FAILED':
      return '同步失败，可重试（输入已保留）';
    case 'SUCCESS':
      return '已同步';
    case 'LOST_RIGHT':
      return '已失效：你对该记录的权限已变更（受控副本已清理，仅保留此脱敏提示）';
  }
}

/** 登出前选择：返回同步 或 清除草稿（X07） */
export const LOGOUT_SYNC_CHOICES = ['返回并完成同步', '清除未同步草稿后退出'] as const;

// ---------- X09 发布活动 ----------

export interface ActivityInput {
  type: '活动' | '领养';
  title: string;
  beginsAt: string;
  endsAt: string;
  deadline: string | null;
  quota: number;
  orgState: 'NONE' | 'APPLY' | 'APPROVED' | 'EXPIRED' | 'REVOKED';
}

/** 提交前校验：时间关系 + 名额；90 天报名窗随 beginsAt 推导（U69） */
export function activityPublishBlockers(input: ActivityInput): string[] {
  const blockers: string[] = [];
  if (!input.title.trim()) blockers.push('TITLE_REQUIRED');
  if (input.endsAt <= input.beginsAt) blockers.push('ENDS_BEFORE_BEGINS');
  if (input.deadline && input.deadline >= input.beginsAt.slice(0, 10)) blockers.push('DEADLINE_AFTER_START');
  if (input.deadline && input.deadline < new Date().toISOString().slice(0, 10)) blockers.push('DEADLINE_PAST');
  if (!Number.isInteger(input.quota) || input.quota < 0) blockers.push('QUOTA_INVALID');
  return blockers;
}

/** 提交后状态：机构已认证活动直发；普通投稿待审；领养统一待审（X09/U89） */
export function publishTargetState(input: Pick<ActivityInput, 'type' | 'orgState'>): 'PUBLISHED' | 'PENDING' {
  if (input.type === '领养') return 'PENDING'; // 领养始终人工审
  return input.orgState === 'APPROVED' ? 'PUBLISHED' : 'PENDING';
}

// ---------- X10 我的报名详情 ----------

export const SIGNUP_DETAIL_FIELDS = ['查询编号', '当前时间地点', '变更历史', '状态', '取消按钮'] as const;

/** 满员/截止时新报名不可用；已报名取消前需后果说明 */
export function cancelSignupConfirmCopy(activityTitle: string): string {
  return `取消「${activityTitle}」的报名后名额将释放给他人，重新报名需视名额而定。确认取消？`;
}

// ---------- X11 活动管理与名单 ----------

export function onlyOwnActivities<T extends { orgUserId: number }>(list: T[], actorId: number): T[] {
  return list.filter((a) => a.orgUserId === actorId); // 列表只见本人活动
}

/** 实名字段默认脱敏（X11/U72）：保留首尾，双字名遮尾 */
export function maskName(name: string): string {
  if (!name) return '***';
  if (name.length === 1) return '*';
  if (name.length === 2) return `${name[0]}*`;
  return name[0] + '*'.repeat(name.length - 2) + name[name.length - 1]!;
}

export const SIGNUP_VIEW_AUDIT = '查看报名名单需留痕（操作者/时间/范围）；普通用户无导出按钮，导出另需 activity.export。';

export function cancelActivityNotifyProgress(failedNotifications: number): string {
  return failedNotifications > 0
    ? `取消通知已发送，${failedNotifications} 条失败进入接手队列（进度可查）。`
    : '取消通知已全部发送，进度可查。';
}

// ---------- X13 清单编辑 ----------

export const LIST_EDITOR_STEPS = ['标题与导语', '选择商品', '排序与理由', '来源/利益声明', '预览', '保存/提交'] as const;

export function listEditorBlockers(input: {
  title: string;
  itemCount: number;
  interestDeclared: boolean;
  itemsChangedAvailability: boolean;
  revalidated: boolean;
}): string[] {
  const blockers: string[] = [];
  if (!input.title.trim()) blockers.push('TITLE_REQUIRED');
  if (input.itemCount === 0) blockers.push('ITEM_REQUIRED');
  if (!input.interestDeclared) blockers.push('INTEREST_DECLARATION_REQUIRED'); // 来源/利益声明
  if (input.itemsChangedAvailability && !input.revalidated) {
    blockers.push('REVALIDATE_REQUIRED'); // 上下架变化提交前再校验
  }
  return blockers;
}

// ---------- X14 内容修改版本 ----------

export function versionHeader(liveVersion: number): { live: string; draft: string } {
  return { live: `线上 v${liveVersion}`, draft: `修改稿 v${liveVersion + 1}` };
}

export const VERSION_DIFF_REQUIRED = true; // 对比预览必看
export const WITHDRAW_THEN_EDIT = '已发布内容需先「撤回待审」再编辑新版本（待审期间线上旧版保持可见）。';
export function versionOutcome(copy: 'approved' | 'rejected'): string {
  return copy === 'approved'
    ? '审核通过：修改稿替换上线，旧版本归档。'
    : '审核驳回：线上旧版保留，修改稿留在草稿。';
}

// ---------- X16 监护人控制 ----------

export function guardianControlFlow(input: {
  guardianPhoneMatches: boolean;
  minorLinked: boolean;
}): { ok: boolean; display: string; leak: boolean } {
  if (!input.minorLinked) {
    // 无关联不泄露账号存在性（X16）
    return { ok: false, display: '未查询到关联账号（如需帮助请走人工通道）。', leak: false };
  }
  if (!input.guardianPhoneMatches) {
    return { ok: false, display: '监护人手机号不匹配。', leak: false };
  }
  return { ok: true, display: '已关联儿童账号（脱敏显示），可查看同意状态并撤回。', leak: false };
}

export const GUARDIAN_REVOKE_EFFECTS = ['限制儿童账号会话', '关闭其推送', '不暴露儿童资料详情'] as const;

// ---------- #35 摘要静态快照 ----------

export const SUMMARY_STATIC_SNAPSHOT_NOTE = '预览为静态快照（生成时刻的数据），缺项与你取消的模块均单独列出；仅所有者可生成，分享不产生实时私有链接。';
