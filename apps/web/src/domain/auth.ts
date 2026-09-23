/** Web 认证流的纯规则及当前标签页内的短暂步骤状态。 */

export type AuthStep = 'phone' | 'age' | 'sms' | 'guardian';
export const AUTH_STEPS: AuthStep[] = ['phone', 'age', 'sms', 'guardian'];

export interface AuthFlow {
  mode: 'preview' | 'live';
  phone: string;
  returnPath: string;
  resumeAction: string;
  agreedTerms: boolean;
  agreedPrivacy: boolean;
  birthDate?: string;
}

/** 手机号与生日不进入 URL 或持久存储；刷新页面后重新开始。 */
let pendingFlow: AuthFlow | null = null;

export function startAuthFlow(input: AuthFlow): void {
  const returnPath = safeReturnPath(input.returnPath);
  pendingFlow = {
    ...input,
    returnPath: /^\/login(?:\/|[?#]|$)/i.test(returnPath) ? '/' : returnPath,
    resumeAction: input.resumeAction.slice(0, 80),
  };
}

export function getAuthFlow(): AuthFlow | null {
  return pendingFlow ? { ...pendingFlow } : null;
}

export function setAuthBirthDate(birthDate: string): void {
  if (!pendingFlow) throw new Error('请从登录入口重新开始');
  ageBand(birthDate, beijingToday());
  pendingFlow.birthDate = birthDate;
}

export function clearAuthFlow(): void {
  pendingFlow = null;
}

/**
 * 正式协议版本和认证通道由发布环境显式启用；默认不能接受占位条款。
 * 这只是前端门禁，发布前仍须由后端验证协议版本和短信通道。
 */
export function activeAuthConfig(): { termsVersion: string; privacyVersion: string } | null {
  const env = (import.meta as unknown as { env?: Record<string, string | undefined> }).env;
  if (env?.VITE_WEB_AUTH_READY !== 'true') return null;
  const termsVersion = env.VITE_WEB_TERMS_VERSION?.trim();
  const privacyVersion = env.VITE_WEB_PRIVACY_VERSION?.trim();
  if (!termsVersion || !privacyVersion) return null;
  return { termsVersion, privacyVersion };
}

/** 按北京时间取日期，避免跨时区凌晨误判年龄。 */
export function beijingToday(now = new Date()): string {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(now);
  const value = (type: string) => parts.find((part) => part.type === type)?.value ?? '';
  return [value('year'), value('month'), value('day')].join('-');
}

/** 严格的实际周岁分支（方案甲）。 */
export function ageBand(birthDate: string, today: string): 'ADULT' | 'MINOR' {
  const isoDate = /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/;
  if (!isoDate.test(birthDate) || !isoDate.test(today)) throw new Error('出生日期不合法');
  const b = new Date(birthDate + 'T00:00:00Z');
  const t = new Date(today + 'T00:00:00Z');
  if (
    Number.isNaN(b.getTime()) ||
    Number.isNaN(t.getTime()) ||
    b.toISOString().slice(0, 10) !== birthDate ||
    t.toISOString().slice(0, 10) !== today ||
    b > t
  ) {
    throw new Error('出生日期不合法');
  }
  let age = t.getUTCFullYear() - b.getUTCFullYear();
  const beforeBirthday =
    t.getUTCMonth() < b.getUTCMonth() ||
    (t.getUTCMonth() === b.getUTCMonth() && t.getUTCDate() < b.getUTCDate());
  if (beforeBirthday) age -= 1;
  return age >= 14 ? 'ADULT' : 'MINOR';
}

/** 输入格式与双协议选择；验证码真假只能由服务端判断。 */
export function loginBlockers(input: {
  phone: string;
  code?: string;
  agreedTerms: boolean;
  agreedPrivacy: boolean;
}): string[] {
  const blockers: string[] = [];
  if (!/^1[3-9][0-9]{9}$/.test(input.phone)) blockers.push('PHONE_INVALID');
  if (input.code !== undefined && !/^[0-9]{6}$/.test(input.code)) blockers.push('CODE_INVALID');
  if (!input.agreedTerms || !input.agreedPrivacy) blockers.push('AGREEMENTS_REQUIRED');
  return blockers;
}

export function guardianStepNeeded(birth: string | null | undefined, today: string): boolean {
  if (!birth) return false;
  return ageBand(birth, today) === 'MINOR';
}

/** 仅允许本站绝对路径，拒绝外站、协议相对路径、反斜杠和控制字符。 */
export function safeReturnPath(candidate: string | null | undefined, fallback = '/'): string {
  if (!candidate) return fallback;
  const value = candidate.trim();
  if (!value.startsWith('/') || value.startsWith('//')) return fallback;
  if (value.includes(String.fromCharCode(92)) || /%5c/i.test(value)) return fallback;
  if (Array.from(value).some((char) => char.charCodeAt(0) < 32 || char.charCodeAt(0) === 127)) return fallback;
  if (value.slice(1).split('/')[0]?.includes(':')) return fallback;
  return value;
}

/** 回原页面的动作位只做定位；发布、报名和加入仍须用户再次确认。 */
export function gateReturnTarget(currentPath: string, action: string): string {
  const base = safeReturnPath(currentPath);
  if (!action) return base;
  const hashAt = base.indexOf('#');
  const path = hashAt < 0 ? base : base.slice(0, hashAt);
  const hash = hashAt < 0 ? '' : base.slice(hashAt);
  const sep = path.includes('?') ? '&' : '?';
  return path + sep + 'resume=' + encodeURIComponent(action.slice(0, 80)) + hash;
}

export const RESEND_CD_SECONDS = 60;
export function resendCountdownLeft(elapsedSeconds: number): number {
  return Math.max(0, RESEND_CD_SECONDS - Math.max(0, Math.floor(elapsedSeconds)));
}

/** 微信接入也必须经过手机、协议和未成年人监护的服务端核验。 */
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
  if (!/^[0-9]{6}$/.test(input.code)) blockers.push('NEED_SMS');
  if (!input.agreedTerms || !input.agreedPrivacy) blockers.push('AGREEMENTS_REQUIRED');
  if (input.ageBandValue === 'MINOR' && !input.guardianConsented) blockers.push('NEED_GUARDIAN');
  return blockers;
}
