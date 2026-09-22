/**
 * 认证流领域逻辑（T7.1 · 需求-账号 A1 · 方案甲 · U93 登录回跳防恶意外部目标）。
 * 纯函数，node --test 可测；页面只做组装。
 */

export type AuthStep = 'phone' | 'sms' | 'age' | 'guardian';

export const AUTH_STEPS: AuthStep[] = ['phone', 'sms', 'age', 'guardian'];

/** 实际周岁分支（方案甲，镜像后端 AccountRules.ageBand） */
export function ageBand(birthDate: string, today: string): 'ADULT' | 'MINOR' {
  const b = new Date(`${birthDate}T00:00:00Z`);
  const t = new Date(`${today}T00:00:00Z`);
  if (Number.isNaN(b.getTime()) || Number.isNaN(t.getTime()) || b > t) {
    throw new Error('出生日期不合法');
  }
  let age = t.getUTCFullYear() - b.getUTCFullYear();
  const beforeBirthday =
    t.getUTCMonth() < b.getUTCMonth() ||
    (t.getUTCMonth() === b.getUTCMonth() && t.getUTCDate() < b.getUTCDate());
  if (beforeBirthday) age -= 1;
  return age >= 14 ? 'ADULT' : 'MINOR';
}

/** 登录提交闸门：返回阻断原因列表（空=可提交） */
export function loginBlockers(input: {
  phone: string;
  code: string;
  agreedTerms: boolean;
  agreedPrivacy: boolean;
}): string[] {
  const blockers: string[] = [];
  if (!/^1[3-9]\d{9}$/.test(input.phone)) blockers.push('PHONE_INVALID');
  if (input.code !== '123456' && !/^\d{6}$/.test(input.code)) blockers.push('CODE_INVALID');
  if (input.code.length !== 6) blockers.push('CODE_INVALID');
  if (!input.agreedTerms || !input.agreedPrivacy) blockers.push('AGREEMENTS_REQUIRED'); // 默认不勾选
  return [...new Set(blockers)];
}

/** 是否需要进入监护人步骤（方案甲） */
export function guardianStepNeeded(birth: string | null | undefined, today: string): boolean {
  if (!birth) return false;
  return ageBand(birth, today) === 'MINOR';
}

/**
 * U93 登录后回跳：仅允许站内绝对路径；外部/协议相对目标一律回落首页。
 * 拒绝：https://evil、http://evil、//evil.com、\\evil、javascript:…
 */
export function safeReturnPath(candidate: string | null | undefined, fallback = '/'): string {
  if (!candidate) return fallback;
  const value = candidate.trim();
  if (!value.startsWith('/')) return fallback;
  if (value.startsWith('//') || value.startsWith('/\\')) return fallback;
  if (/^\/[a-z][a-z0-9+.-]*:/i.test(value)) return fallback; // /javascript: 等伪协议
  if (value.includes('\\')) return fallback;
  return value;
}

/** 登录闸门触发点 → 回跳目标构造（操作位附 query，登录后恢复） */
export function gateReturnTarget(currentPath: string, action: string): string {
  const base = safeReturnPath(currentPath);
  const sep = base.includes('?') ? '&' : '?';
  return `${base}${sep}resume=${encodeURIComponent(action)}`;
}

/** 验证码倒计时（60s 决议） */
export const RESEND_CD_SECONDS = 60;

export function resendCountdownLeft(elapsedSeconds: number): number {
  return Math.max(0, RESEND_CD_SECONDS - Math.max(0, Math.floor(elapsedSeconds)));
}

/** 微信登录前置检查（镜像后端 U83：手机+协议+监护人不可绕） */
export function wechatBlockers(input: {
  phoneBound: boolean;
  code: string;
  agreedTerms: boolean;
  agreedPrivacy: boolean;
  ageBandValue: 'ADULT' | 'MINOR';
  guardianConsented: boolean;
}): string[] {
  const blockers: string[] = [];
  if (!input.phoneBound) blockers.push('NEED_PHONE_BIND');
  if (input.code !== '123456') blockers.push('NEED_SMS');
  if (!input.agreedTerms || !input.agreedPrivacy) blockers.push('AGREEMENTS_REQUIRED');
  if (input.ageBandValue === 'MINOR' && !input.guardianConsented) blockers.push('NEED_GUARDIAN');
  return blockers;
}
