package com.cutepet.adminops.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * 治理工单纯规则（T1.6）：举报 48h SLA / 版权通知-反通知 / 申诉 48h 反馈。
 * 追溯：需求-平台规则与客服 §2/§3、门禁 E 层工单流、U82/U93。
 */
public final class GovernanceRules {

    public static final int REPORT_SLA_HOURS = 48;
    public static final int APPEAL_FEEDBACK_HOURS = 48;

    private GovernanceRules() {
    }

    // ---------- 举报 ----------

    public static final String[] REPORT_TARGET_TYPES = {
            "ARTICLE", "COMMENT", "REVIEW", "USER", "ROUTE", "ACTIVITY", "POI" };

    public static boolean validReportTarget(String targetType) {
        for (String t : REPORT_TARGET_TYPES) {
            if (t.equals(targetType)) {
                return true;
            }
        }
        return false;
    }

    /** 普通举报 48h 时效：超时未解决=overdue */
    public static boolean reportOverdue(Instant createdAt, Instant resolvedAt, Instant now) {
        if (resolvedAt != null) {
            return false;
        }
        return now.isAfter(createdAt.plus(Duration.ofHours(REPORT_SLA_HOURS)));
    }

    /** 结案必须附处理说明 */
    public static boolean canResolveReport(String resolutionNote) {
        return resolutionNote != null && !resolutionNote.isBlank();
    }

    // ---------- 版权通知-删除（避风港） ----------

    /** 状态：RECEIVED → TAKEDOWN（收到合格通知立即下架）→（权利争议）DISPUTED → COUNTER_OK/COUNTER_REJECTED → CLOSED */
    public static String copyrightAfterReceived() {
        return "TAKEDOWN"; // 通知即下架（需求-平台规则与客服 §3）
    }

    public static boolean canCounterNotice(String state) {
        // 反通知仅在内容已被下架后可提交（TAKEDOWN/DISPUTED 均可推进）
        return "TAKEDOWN".equals(state) || "DISPUTED".equals(state);
    }

    public static String copyrightAfterCounter(String state) {
        if (!canCounterNotice(state)) {
            throw new IllegalStateException("当前状态不可提交反通知: " + state);
        }
        return "DISPUTED";
    }

    public static String copyrightResolveCounter(boolean counterAccepted) {
        return counterAccepted ? "COUNTER_OK" : "COUNTER_REJECTED";
    }

    /** 反通知成立 → 恢复内容并结案；不成立 → 维持下架并结案 */
    public static boolean restoreContentAfterCounter(String resolvedState) {
        return "COUNTER_OK".equals(resolvedState);
    }

    // ---------- 申诉（U82） ----------

    public static boolean canAppeal(String actionState) {
        return "ACTIVE".equals(actionState); // 仅生效中的处置可申诉
    }

    public static String appealDecision(boolean accepted) {
        return accepted ? "ACCEPTED" : "REJECTED";
    }

    public static boolean appealFeedbackOverdue(Instant createdAt, Instant resolvedAt, Instant now) {
        if (resolvedAt != null) {
            return false;
        }
        return now.isAfter(createdAt.plus(Duration.ofHours(APPEAL_FEEDBACK_HOURS)));
    }

    /** 申诉受理后处置撤销（进计数剔除，见 ModerationRules.effectiveViolationCount） */
    public static String moderationStateAfterAppealAccepted() {
        return "REVOKED";
    }

    /** 举报入口可达性（与 U93 登录回跳配合：未登录举报先登录回跳原对象） */
    public static boolean reportRequiresAuth() {
        return true;
    }
}
