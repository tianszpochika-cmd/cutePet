package com.cutepet.explore.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * 活动/报名/领养/机构认证纯规则（T4.5 + T4.6 ★ · BR-05 · U68–U72/U86/U89/U90）。
 */
public final class ActivityRules {

    /** 报名不早于活动结束前 90 天开放（U69） */
    public static final int SIGNUP_OPEN_BEFORE_END_DAYS = 90;
    /** 报名信息保存期限 ≤90 天（决议/U86） */
    public static final int SIGNUP_RETENTION_DAYS = 90;

    public enum State { PENDING, PUBLISHED, CHANGED, ENDED, CANCELLED }

    public enum SignupDeny { TOO_EARLY, NOT_OPEN, DEADLINE_PASSED, STARTED_OR_OVER, FULL, DUPLICATE, CONSENT_REQUIRED, STATE_BLOCKED }

    public enum SignupOutcome { OK, DENIED }

    public enum OrgState { NONE, APPLY, APPROVED, REJECTED, EXPIRED, REVOKED }

    private ActivityRules() {
    }

    // ---------- 活动状态 ----------

    public static boolean transition(State from, State to) {
        return switch (from) {
            case PENDING -> to == State.PUBLISHED || to == State.CANCELLED;
            case PUBLISHED -> to == State.CHANGED || to == State.CANCELLED || to == State.ENDED;
            case CHANGED -> to == State.PUBLISHED || to == State.CANCELLED; // 修改审核通过回发布
            case ENDED -> to == State.CANCELLED;
            case CANCELLED -> false; // U71：普通编辑不可恢复
        };
    }

    /** U70：有报名后修改地点/时间 → CHANGED 暂停新报名，旧信息标注核实中 */
    public static boolean pauseSignupsOnEdit(State state) {
        return state == State.CHANGED;
    }

    public static boolean normalEditBlocked(State state) {
        return state == State.CANCELLED; // U71：不能普通保存恢复
    }

    // ---------- 报名窗口与并发（U68/U69） ----------

    public static SignupDeny denyReason(State state, LocalDate beginsAt, LocalDate signupDeadline,
                                        boolean hasSignupsOnEdit, int quota, int activeCount,
                                        boolean userHasActive, boolean consentProvided,
                                        LocalDate now) {
        if (state == State.CANCELLED) {
            return SignupDeny.STATE_BLOCKED;
        }
        if (state != State.PUBLISHED && state != State.CHANGED) {
            return SignupDeny.NOT_OPEN;
        }
        if (state == State.CHANGED) {
            return SignupDeny.STATE_BLOCKED; // U70 修改核实中暂停新报名
        }
        if (now.isBefore(beginsAt.minusDays(SIGNUP_OPEN_BEFORE_END_DAYS))) {
            return SignupDeny.TOO_EARLY; // U69 不早于结束前 90 天
        }
        if (signupDeadline != null && !now.isBefore(signupDeadline.plusDays(1))) {
            return SignupDeny.DEADLINE_PASSED;
        }
        if (!now.isBefore(beginsAt)) {
            return SignupDeny.STARTED_OR_OVER;
        }
        if (quota > 0 && activeCount >= quota) {
            return SignupDeny.FULL; // U68 名额
        }
        if (userHasActive) {
            return SignupDeny.DUPLICATE; // U68 同账号仅一条有效
        }
        if (!consentProvided) {
            return SignupDeny.CONSENT_REQUIRED; // 告知同意凭证
        }
        return null; // 允许
    }

    /** U68 抢最后名额：quota>0 且 activeCount<quota → 允许（并发下服务层再校验） */
    public static SignupOutcome decide(int quota, int activeCountAfterInsertGuard) {
        if (quota > 0 && activeCountAfterInsertGuard > quota) {
            return SignupOutcome.DENIED;
        }
        return SignupOutcome.OK;
    }

    public static boolean consentValid(String consentVersion, String consentAt) {
        return consentVersion != null && !consentVersion.isBlank()
                && consentAt != null && !consentAt.isBlank();
    }

    /** 取消报名 → 该行 CANCELLED（名额释放）；重报名 = 新历史行（U68） */
    public static boolean allowReSignupAfterCancel(boolean hasActiveRow) {
        return !hasActiveRow;
    }

    // ---------- 修改审核（U70） ----------

    public static String changeReviewResult(boolean approved) {
        return approved ? "PUBLISHED" : "REVERTED";
    }

    // ---------- 取消与通知（U71） ----------

    /** 取消活动 → 有效报名统一转 ACTIVITY_CANCELLED；通知失败进接手队列 */
    public static String signupStateAfterActivityCancelled() {
        return "ACTIVITY_CANCELLED";
    }

    public static boolean failedNotifyGoesToRecoveryQueue() {
        return true; // 失败可接手
    }

    // ---------- 名单权限（U72） ----------

    public static boolean canViewSignups(long actorId, long orgUserId, boolean hasExportPermission) {
        return actorId == orgUserId; // 本人（组织者）可查
    }

    public static boolean canExportSignups(long orgUserId, long actorId, boolean hasExportPermission) {
        // 内部导出另需权限 + 范围 + 审计（hasExportPermission = activity.export）
        return hasExportPermission && actorId == orgUserId;
    }

    // ---------- 保存期限清理（U86 · 决议 ≤90 天） ----------

    public static boolean cleanupDue(LocalDate endsAt, LocalDate now) {
        return endsAt != null && now.isAfter(endsAt.plusDays(SIGNUP_RETENTION_DAYS));
    }

    /** 清理动作：平台字段脱敏清除 + 下载失效 + 通知组织者删除副本（留痕） */
    public static List<String> cleanupActions() {
        return List.of("PLATFORM_FIELDS_CLEARED", "DOWNLOAD_TOKEN_INVALIDATED",
                "NOTIFY_ORG_DELETE_COPY", "AUDIT_TRAIL_KEPT");
    }

    // ---------- 机构认证（U89） ----------

    public static OrgState orgTransition(OrgState from, String action, LocalDate expiresAt, LocalDate now) {
        return switch (action) {
            case "apply" -> from == OrgState.NONE || from == OrgState.REJECTED ? OrgState.APPLY : from;
            case "approve" -> from == OrgState.APPLY ? OrgState.APPROVED : from;
            case "reject" -> from == OrgState.APPLY ? OrgState.REJECTED : from;
            case "revoke" -> from == OrgState.APPROVED ? OrgState.REVOKED : from;
            case "expire-check" -> {
                if (from == OrgState.APPROVED && expiresAt != null && now.isAfter(expiresAt)) {
                    yield OrgState.EXPIRED;
                }
                yield from;
            }
            default -> from;
        };
    }

    /** 直发资格按当前认证（过期/撤销即失格 —— U89） */
    public static boolean orgDirectPublish(OrgState state) {
        return state == OrgState.APPROVED;
    }

    public static boolean adoptionAlwaysManualReview() {
        return true; // U89 领养始终人工审
    }

    public static boolean credentialMaterialPublic() {
        return false; // U89 材料不公开
    }

    // ---------- 领养（U90） ----------

    public static boolean adoptionHasSignupButton() {
        return false; // 无报名/交易按钮
    }

    public static boolean adoptionHasTradeButton() {
        return false;
    }

    public static boolean adoptionPublicVisible(LocalDate expireOn, LocalDate now, String state) {
        if (!"PUBLISHED".equals(state)) {
            return false;
        }
        return expireOn == null || !now.isAfter(expireOn); // 过 30 天不确认 → 停止公开
    }

    public static boolean sellReportTraced(String reasonCode) {
        return "SELLING_SUSPECTED".equals(reasonCode); // 违规售卖举报 → 处置可追溯
    }
}
