/**
 * 活动页支撑逻辑（拆出以便 EventsPage 聚焦模板；行为与 domain/explore 同源）。
 */
import { signupBlockers as baseBlockers, SIGNUP_DENY_COPY, CONSENT_TEXT } from './explore.ts';

export { CONSENT_TEXT };

export function signupBlockers(input: {
  loggedIn: boolean;
  state: string;
  beginsAt: string;
}): 'NEED_LOGIN' | 'STATE_BLOCKED' | 'NOT_OPEN' | null {
  if (!input.loggedIn) return 'NEED_LOGIN';
  if (input.state === 'CANCELLED' || input.state === 'CHANGED') return 'STATE_BLOCKED';
  if (input.state !== 'PUBLISHED') return 'NOT_OPEN';
  const today = new Date().toISOString().slice(0, 10);
  const full = baseBlockers({
    loggedIn: input.loggedIn,
    consentChecked: true, // 同意在下一步弹层获取，此层只判窗口
    state: input.state,
    today,
    beginsAt: input.beginsAt.slice(0, 10),
    signupDeadline: null,
    quota: 0,
    activeCount: 0,
    userHasActive: false,
  });
  if (full === 'TOO_EARLY' || full === 'DEADLINE_PASSED' || full === 'STARTED_OR_OVER') return full;
  return null;
}

export function signupDenyCopyFallback(deny: string): string {
  const key = deny as keyof typeof SIGNUP_DENY_COPY;
  return SIGNUP_DENY_COPY[key] ?? '当前不可报名。';
}
