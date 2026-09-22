package com.cutepet.content.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 投稿/版本纯规则（T3.2 + T3.6 ★ · 功能设计-资讯 C7 · BR-06 · U14/U15/U73–U76/U88/U94）。
 */
public final class SubmissionRules {

    /** 投稿类型（U73：文章/评测/清单 均有入口、进正确队列） */
    public static final List<String> SUBMISSION_KINDS = List.of("ARTICLE", "REVIEW", "LIST");

    /** 需人工复核的健康科普频道（U94：缺复核不发布） */
    public static final List<String> REVIEW_REQUIRED_CHANNELS = List.of("疾病健康", "营养");

    public static final int REJECT_SUSPEND_COUNT = 3;
    public static final int REJECT_SUSPEND_DAYS = 7;

    public enum State { DRAFT, PENDING, REVIEWING, PUBLISHED, REJECTED, TAKEDOWN, WITHDRAWN }

    private SubmissionRules() {
    }

    // ---------- 状态机（与 contracts TS 镜像） ----------

    public static boolean canTransition(State from, State to) {
        return switch (from) {
            case DRAFT -> to == State.PENDING || to == State.WITHDRAWN;
            case PENDING -> to == State.REVIEWING || to == State.WITHDRAWN;
            case REVIEWING -> to == State.PUBLISHED || to == State.REJECTED || to == State.PENDING;
            case REJECTED -> to == State.PENDING;        // 修改重提
            case PUBLISHED -> to == State.TAKEDOWN || to == State.WITHDRAWN; // 运营下架/作者撤下
            case TAKEDOWN -> to == State.PUBLISHED;      // 运营恢复（治理清除不可恢复见下）
            case WITHDRAWN -> to == State.PENDING;
        };
    }

    public static void assertTransition(State from, State to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("投稿状态不可流转: " + from + " → " + to);
        }
    }

    // ---------- 领取/审核锁（U15） ----------

    public static boolean claimable(State current) {
        return current == State.PENDING;
    }

    public static boolean releaseable(State current, boolean sameReviewer) {
        return current == State.REVIEWING && sameReviewer; // 处理中锁：他人不可抢
    }

    // ---------- U74 线上/修改版 ----------

    /** 待审版本固定：PENDING/REVIEWING 期间不可原位修改 */
    public static boolean versionImmutableWhileReviewing(String versionState) {
        return "PENDING".equals(versionState) || "REVIEWING".equals(versionState);
    }

    /** 通过后原子替换：旧线上版→SUPERSEDED，待审版→PUBLISHED；驳回时旧线上版保持可见 */
    public static boolean liveVisibleAfterReject(String liveState) {
        return "PUBLISHED".equals(liveState);
    }

    public static String stateAfterPublishSwap() {
        return "SUPERSEDED";
    }

    // ---------- U75 撤下与治理 ----------

    /** 作者撤下：即时不可见，且未决审核终止 */
    public static boolean terminatesPendingOnWithdraw() {
        return true;
    }

    /** 不能绕过治理恢复（治理清除后作者无权恢复；运营恢复需 isLegalClear） */
    public static boolean authorCanRestoreFromTakedown() {
        return false;
    }

    public static boolean operatorCanRestore(boolean governanceCleared) {
        return governanceCleared;
    }

    // ---------- U76 质量退修 vs 违规 ----------

    /** 质量驳回计数达 3 → 暂停投稿资格 7 天（质量问题不进违规窗口） */
    public static boolean suspendForQuality(int rejectCount) {
        return rejectCount >= REJECT_SUSPEND_COUNT;
    }

    public static int qualitySuspendDays() {
        return REJECT_SUSPEND_DAYS;
    }

    /** 通过后重置质量计数 */
    public static int rejectCountAfterApprove() {
        return 0;
    }

    // ---------- U94 复核与直发 ----------

    /** U94：健康科普频道缺复核不发布；UGC 永不直发 */
    public static boolean publishAllowed(String channelName, String authorKind, Long reviewerId, boolean hasDirectPermission) {
        if ("UGC".equals(authorKind)) {
            return false; // 先审后发（唯一通道）
        }
        if (!hasDirectPermission) {
            return false; // U94：编辑不继承直发权
        }
        if (REVIEW_REQUIRED_CHANNELS.contains(channelName)) {
            return reviewerId != null; // 需复核人留痕
        }
        return true;
    }

    /** 审核留痕必备：通过/驳回必须记录 reviewer + 时间 + 动作 */
    public static boolean auditTrailRequired() {
        return true;
    }

    // ---------- U88 治理传播 ----------

    /**
     * 治理下架传播：收藏处标不可用、清单项隐藏、文内商品绑定降级纯文本、不可再提审。
     * 返回受影响面标识集合。
     */
    public static Set<String> governancePropagationTargets() {
        Set<String> targets = new HashSet<>();
        targets.add("FAVORITES_MARK_UNAVAILABLE");
        targets.add("LIST_ITEMS_HIDE");
        targets.add("RELATED_BINDINGS_KEEP_TEXT");
        targets.add("BLOCK_RESUBMIT");
        return targets;
    }

    public static boolean canResubmitFromTakedown() {
        return false; // BLOCK_RESUBMIT（U88/U75）
    }

    // ---------- U73 类型入口 ----------

    public static boolean validKind(String kind) {
        return SUBMISSION_KINDS.contains(kind);
    }

    public static String queueFor(String kind) {
        return switch (kind) {
            case "ARTICLE" -> "queue.articles";
            case "REVIEW" -> "queue.reviews";   // 评测独立队列组（工作台分组展示）
            case "LIST" -> "queue.lists";
            default -> throw new IllegalArgumentException("未知投稿类型: " + kind);
        };
    }
}
