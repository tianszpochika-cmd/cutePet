/**
 * 通知三通道领域逻辑（T10.4 · Web Notification + 站内 + 厂商 stub）。
 */

export type Channel = 'WEB_NOTIFICATION' | 'IN_APP' | 'VENDOR_PUSH';

export const CHANNEL_META: Record<Channel, { label: string; support: 'native' | 'always' | 'stub'; note: string }> = {
  WEB_NOTIFICATION: {
    label: '浏览器通知',
    support: 'native',
    note: '需 HTTPS/localhost + 用户授权（Notification.requestPermission）',
  },
  IN_APP: {
    label: '站内消息',
    support: 'always',
    note: '消息中心五类（提醒/互动/审核结果/家庭/系统），永不关闭',
  },
  VENDOR_PUSH: {
    label: '厂商推送（APNs/厂商通道）',
    support: 'stub',
    note: 'H5 无原生厂商通道：预留 adapter 接口，App 壳阶段接入（占位 stub，不谎报可用）',
  },
};

/** 浏览器 Notification 权限状态机 */
export type PermissionState = 'default' | 'granted' | 'denied';

export function nextPermissionState(current: PermissionState, event: 'request' | 'userGrant' | 'userDeny' | 'reset'): PermissionState | 'STAY' {
  switch (event) {
    case 'request':
      return current === 'granted' ? 'STAY' : current === 'denied' ? 'STAY' : 'default'; // 请求只弹一次由浏览器管
    case 'userGrant':
      return 'granted';
    case 'userDeny':
      return 'denied';
    case 'reset':
      return 'default';
    default:
      return 'STAY';
  }
}

/** 开启浏览器通知的前置闸门：隐私已同意（#31 可选能力） */
export function webNotifyBlockers(input: {
  privacyAgreed: boolean;
  permission: PermissionState;
  secureContext: boolean;
}): string[] {
  const out: string[] = [];
  if (!input.secureContext) out.push('NEED_HTTPS');
  if (!input.privacyAgreed) out.push('PRIVACY_NOT_AGREED');
  if (input.permission === 'denied') out.push('PERMISSION_DENIED'); // 引导去浏览器设置
  return out;
}

/** 设置页三通道模型：站内不可关；其余按授权态展示 */
export function channelPanel(channel: Channel, permission: PermissionState, privacyAgreed: boolean): {
  enabled: boolean;
  toggleable: boolean;
  hint: string;
} {
  if (channel === 'IN_APP') {
    return { enabled: true, toggleable: false, hint: '站内消息始终开启（核心通知）' };
  }
  if (channel === 'VENDOR_PUSH') {
    return { enabled: false, toggleable: false, hint: CHANNEL_META.VENDOR_PUSH.note };
  }
  const blockers = webNotifyBlockers({ privacyAgreed, permission, secureContext: true });
  return {
    enabled: permission === 'granted',
    toggleable: blockers.length === 0,
    hint: blockers.length > 0 ? blockers.join(' / ') : '点击授权浏览器通知',
  };
}

/** 站内消息未读角标（与 Web 同口径 99+） */
export function unreadBadgeMobile(total: number): string {
  if (total <= 0) return '';
  return total > 99 ? '99+' : String(total);
}

export const MESSAGE_CATEGORIES_MOBILE = ['提醒', '互动', '审核结果', '家庭', '系统'] as const;
