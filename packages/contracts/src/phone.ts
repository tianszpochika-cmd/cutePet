/**
 * 契约纯逻辑（T0.4 起逐步扩展为全量 schema）。
 * 手机号规则对齐需求 A1：中国大陆号段、后台实名前提。
 */
const CN_PHONE = /^1[3-9]\d{9}$/;

export function isValidCnPhone(value: string): boolean {
  return CN_PHONE.test(value);
}

/** 脱敏展示：138****5678（对齐探索电话脱敏决议） */
export function maskPhone(value: string): string {
  if (!isValidCnPhone(value)) return '***';
  return `${value.slice(0, 3)}****${value.slice(7)}`;
}

/** dev 模式固定验证码校验（真服务属 E02，本地仅逻辑） */
export const DEV_SMS_CODE = '123456';

export function verifyDevSmsCode(input: string): boolean {
  return input === DEV_SMS_CODE;
}
