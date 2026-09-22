/** 契约级输入校验（纯函数，双端共用；错误文案由 UI 层本地化）。 */

/** 昵称：2–16 位，中英文数字下划线中划线（BR-09 账号信息审核的前置基础校验） */
const NICKNAME_RE = /^[\w一-龥-]{2,16}$/;

export function isValidNickname(value: string): boolean {
  return NICKNAME_RE.test(value);
}

/** 宠物昵称：去空白后 1–20 字符 */
export function isValidPetName(value: string): boolean {
  const t = value.trim();
  return t.length >= 1 && t.length <= 20;
}

/** 分页收敛：page ≥1，size 1–50 默认 20 */
export function clampPage(page: number, size = 20): { page: number; size: number } {
  const p = Number.isFinite(page) && page >= 1 ? Math.floor(page) : 1;
  const s = Number.isFinite(size) && size >= 1 && size <= 50 ? Math.floor(size) : 20;
  return { page: p, size: s };
}

/** 体重输入合法性：0.1–200kg 区间（超出需二次确认由 needsWeightConfirm 承担，此处为硬边界） */
export function isValidWeightKg(value: number): boolean {
  return Number.isFinite(value) && value >= 0.1 && value <= 200;
}

/** 日期格式 YYYY-MM-DD 且为真实日历日 */
export function isValidDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const epoch = Date.parse(`${value}T00:00:00Z`);
  if (Number.isNaN(epoch)) return false;
  const d = new Date(epoch);
  return (
    d.getUTCFullYear() === Number(value.slice(0, 4)) &&
    d.getUTCMonth() + 1 === Number(value.slice(5, 7)) &&
    d.getUTCDate() === Number(value.slice(8, 10))
  );
}
