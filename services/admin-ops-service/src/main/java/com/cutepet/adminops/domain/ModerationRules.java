package com.cutepet.adminops.domain;

/**
 * 违规处置纯规则（T1.6 · 需求-管理端 §2.8 · BR-06/BR-09 · U76/U82）。
 */
public final class ModerationRules {

    public enum Level { L1, L2, L3, L4 }

    public enum Action { DELETE_CONTENT, MUTE_7D, MUTE_30D, PERMANENT_BAN }

    public static final int L1_WINDOW_DAYS = 180;
    public static final int L1_ESCALATE_COUNT = 3;
    public static final int QUALITY_REJECT_SUSPEND_COUNT = 3;
    public static final int QUALITY_SUSPEND_DAYS = 7;

    private ModerationRules() {
    }

    public static Action actionForLevel(Level level) {
        return switch (level) {
            case L1 -> Action.DELETE_CONTENT;
            case L2 -> Action.MUTE_7D;
            case L3 -> Action.MUTE_30D;
            case L4 -> Action.PERMANENT_BAN;
        };
    }

    /** 180 天窗口内 L1×3 → 升级 L2（升级不重复计数） */
    public static Level escalate(Level level, int l1CountInWindow) {
        if (level == Level.L1 && l1CountInWindow >= L1_ESCALATE_COUNT) {
            return Level.L2;
        }
        return level;
    }

    /** U76 质量退修与违规分离：质量驳回仅暂停投稿资格，不计入违规窗口 */
    public static boolean suspendForQuality(int rejectCount) {
        return rejectCount >= QUALITY_REJECT_SUSPEND_COUNT;
    }

    public static int qualitySuspendDays() {
        return QUALITY_SUSPEND_DAYS;
    }

    /** 违规计数仅统计 CONFIRMED；申诉撤销(REVOKED)剔除且不重复计 */
    public static int effectiveViolationCount(int confirmed, int revokedOnAppeal) {
        return Math.max(0, confirmed - revokedOnAppeal);
    }

    public static boolean penaltyTriggers(int effectiveViolationCount) {
        return effectiveViolationCount >= L1_ESCALATE_COUNT;
    }

    /** U82：封禁后申诉通道与登录页可达，普通业务拒绝 */
    public static boolean reachableWhenBanned(String path) {
        if (path == null) {
            return false;
        }
        return path.equals("/tickets") || path.equals("/me/tickets")
                || path.startsWith("/appeals") || path.startsWith("/auth/")
                || path.equals("/reports");
    }

    public static boolean normalBusinessWhenBanned() {
        return false; // 普通业务仍拒绝（U82）
    }

    /** 处置状态机：ACTIVE → EXPIRED（期限届满） / REVOKED（申诉撤销） */
    public static boolean moderationTransition(String from, String to) {
        return switch (from) {
            case "ACTIVE" -> "EXPIRED".equals(to) || "REVOKED".equals(to);
            default -> false;
        };
    }
}
