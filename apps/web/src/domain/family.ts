/**
 * 家庭与个人中心领域逻辑（T7.8 · #31/#38/#40/#45 共同状态 · BR-03/BR-04）。
 */

export type MemberRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type ShareLevel = 'MANAGE' | 'READONLY';

/** 逐动作权限（#31：不能用"可管理=全功能"；#35 摘要仅所有者） */
export type PetAction = 'VIEW' | 'EDIT_RECORD' | 'HANDLE_REMINDER' | 'SUMMARY_EXPORT' | 'SET_SHARE';

export function petActionAllowed(input: {
  actorIsPetOwner: boolean;
  role: MemberRole;
  share: ShareLevel | null;
  action: PetAction;
}): boolean {
  const { actorIsPetOwner, share, action } = input;
  if (actorIsPetOwner) return true; // 所有者全动作
  if (action === 'SUMMARY_EXPORT') return false; // #35 仅所有者可生成摘要
  if (action === 'SET_SHARE') return false; // 档位设置=所有者（U61 非所有者不能提升）
  if (action === 'VIEW') return share !== null; // 已共享才可见
  // 家庭角色不替代逐宠授权：普通成员获得 MANAGE 后也可记录和处理待办。
  // 家庭管理员若未获得这只宠物的 MANAGE，同样不能操作。
  return share === 'MANAGE';
}

/** 只读成员不得出现的入口（#31） */
export function readOnlyHiddenEntries(share: ShareLevel | null, isOwner: boolean): string[] {
  const hidden: string[] = [];
  if (isOwner) return hidden;
  if (share !== 'MANAGE') {
    hidden.push('record-edit', 'reminder-complete', 'summary-export', 'share-settings');
  } else {
    hidden.push('share-settings'); // 可管理也无档位设置权
    hidden.push('summary-export'); // 摘要仍仅所有者
  }
  return hidden;
}

/** 无权限深链 → 拒绝页（先拒绝后不渲染私有数据 —— #31） */
export function deepLinkDenied(granted: boolean): { page: 'denied'; renderData: false } | null {
  return granted ? null : { page: 'denied', renderData: false };
}

/** #38 家庭页模型：管理员 + 每只宠物所有者展示 */
export interface FamilyMemberView {
  userId: number;
  role: MemberRole;
  ownedPetNames: string[];
}

export function familyPageModel(members: FamilyMemberView[]): {
  admins: FamilyMemberView[];
  petOwners: FamilyMemberView[];
  note: string;
} {
  return {
    admins: members.filter((m) => m.role === 'OWNER' || m.role === 'ADMIN'),
    petOwners: members.filter((m) => m.ownedPetNames.length > 0),
    note: '属于家庭 ≠ 可访问其他成员未共享的个人宠物；新成员默认只读。',
  };
}

/** 新成员加入默认档位（#38） */
export const DEFAULT_NEW_MEMBER_SHARE: ShareLevel = 'READONLY';

/** #40 加入页范围声明（自有宠物仍私有、私有互动不自动开放） */
export const JOIN_SCOPE_COPY =
  '加入后：你自己的宠物仍保持私有（需你主动共享）；家庭内已共享宠物默认只读可见。你的收藏、投稿等私有互动不会自动对家人开放。';

/** #45 我的宫格（六项，含活动中心） */
export const ME_GALLERY = [
  { id: 'pets', label: '宠物', to: '/pets' },
  { id: 'family', label: '家庭', to: '/family' },
  { id: 'creation', label: '创作', to: '/submissions' },
  { id: 'favorites', label: '收藏', to: '/me/favorites' },
  { id: 'messages', label: '消息', to: '/messages' },
  { id: 'activity', label: '活动中心', to: '/me/activity-center' },
] as const;

/** 未登录统一回跳（#45） */
export function galleryGate(loggedIn: boolean, to: string): string {
  if (loggedIn) return to;
  const sep = to.includes('?') ? '&' : '?';
  return `/login?return=${encodeURIComponent(to)}${sep}resume=1`;
}

// ---------- 消息中心（T7.8 · 五类镜像后端 MessageRules） ----------

export const MESSAGE_CATEGORIES = [
  { id: 'REMINDER', label: '提醒' },
  { id: 'INTERACTION', label: '互动' },
  { id: 'REVIEW', label: '审核结果' },
  { id: 'FAMILY', label: '家庭' },
  { id: 'SYSTEM', label: '系统' },
] as const;

export function unreadBadge(totalUnread: number): string {
  if (totalUnread <= 0) return '';
  return totalUnread > 99 ? '99+' : String(totalUnread);
}

/** 收藏分组（五 Tab，与内容侧一致） */
export const FAVORITE_GROUPS = ['内容', '商品', '清单', '场所', '路线'] as const;
