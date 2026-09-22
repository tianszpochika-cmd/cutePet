package com.cutepet.adminops;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.adminops.domain.GovernanceRules;
import com.cutepet.adminops.domain.ModerationRules;
import com.cutepet.adminops.domain.ModerationRules.Level;
import com.cutepet.adminops.domain.TrackingRules;

import java.time.Instant;
import java.util.Map;

/**
 * admin-ops 领域逻辑测试（纯 Java main）。
 * 覆盖：T1.6（U6/U82/U86/U93 相关规则、48h SLA、版权避风港流）、T1.8（埋点隐私）。
 */
public class AdminOpsLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[admin-ops] PASS " + name);
    }

    public static void main(String[] args) {
        testPermissionCatalog();
        testModeration();
        testReports();
        testCopyright();
        testAppeal();
        testTracking();
        System.out.println("[admin-ops-logic] " + passed + " assertions passed");
    }

    private static void testPermissionCatalog() {
        check("权限目录 32 且无重复", PermissionCatalog.ALL.size() == 32
                && PermissionCatalog.ALL.stream().distinct().count() == 32);
        check("合法权限校验", PermissionCatalog.isValid("user.ban") && !PermissionCatalog.isValid("hacker.do"));
        check("请求头解析", PermissionCatalog.parseHeader("user.view, user.ban ,").size() == 2);
        check("空头解析为空集", PermissionCatalog.parseHeader(null).isEmpty());
        check("invalidOf 输出非法项", PermissionCatalog.invalidOf(java.util.List.of("a.b", "user.ban")).size() == 1);
    }

    private static void testModeration() {
        // L1–L4 映射（与 TS contracts 一致）
        check("L1→删内容", ModerationRules.actionForLevel(Level.L1) == ModerationRules.Action.DELETE_CONTENT);
        check("L2→禁言7天", ModerationRules.actionForLevel(Level.L2) == ModerationRules.Action.MUTE_7D);
        check("L3→禁言30天", ModerationRules.actionForLevel(Level.L3) == ModerationRules.Action.MUTE_30D);
        check("L4→永久封禁", ModerationRules.actionForLevel(Level.L4) == ModerationRules.Action.PERMANENT_BAN);
        check("180天 L1×3 升级 L2", ModerationRules.escalate(Level.L1, 3) == Level.L2);
        check("L1×2 不升级", ModerationRules.escalate(Level.L1, 2) == Level.L1);
        check("已高于 L1 不降级", ModerationRules.escalate(Level.L3, 9) == Level.L3);

        // U76 质量 vs 违规分离
        check("质量 3 次仅暂停投稿 7 天", ModerationRules.suspendForQuality(3) && !ModerationRules.suspendForQuality(2));
        check("质量不计违规窗口", ModerationRules.effectiveViolationCount(0, 0) == 0);
        check("违规确认 3 次触发处罚", ModerationRules.penaltyTriggers(ModerationRules.effectiveViolationCount(3, 0)));
        check("申诉撤销剔除且不重复计", ModerationRules.effectiveViolationCount(3, 1) == 2
                && !ModerationRules.penaltyTriggers(ModerationRules.effectiveViolationCount(3, 1)));

        // U82 封禁可达性
        check("封禁后申诉/工单/登录可达",
                ModerationRules.reachableWhenBanned("/tickets") && ModerationRules.reachableWhenBanned("/auth/sms/login")
                        && ModerationRules.reachableWhenBanned("/me/tickets"));
        check("封禁后普通业务拒绝", !ModerationRules.normalBusinessWhenBanned()
                && !ModerationRules.reachableWhenBanned("/pets"));
        check("处置状态机 ACTIVE→EXPIRED/REVOKED",
                ModerationRules.moderationTransition("ACTIVE", "EXPIRED")
                        && ModerationRules.moderationTransition("ACTIVE", "REVOKED")
                        && !ModerationRules.moderationTransition("REVOKED", "ACTIVE"));
    }

    private static void testReports() {
        check("举报对象类型枚举 7 类", GovernanceRules.REPORT_TARGET_TYPES.length == 7);
        check("合法/非法对象", GovernanceRules.validReportTarget("ARTICLE") && !GovernanceRules.validReportTarget("ANY"));
        Instant created = Instant.parse("2026-09-20T00:00:00Z");
        check("48h 内未超时", !GovernanceRules.reportOverdue(created, null, created.plus(Duration47())));
        check("48h 后超时", GovernanceRules.reportOverdue(created, null, created.plus(Duration49())));
        check("已结案不再计超时", !GovernanceRules.reportOverdue(created, created.plus(Duration72()), created.plus(Duration96())));
        check("结案必填说明", !GovernanceRules.canResolveReport(" ") && GovernanceRules.canResolveReport("已删评"));
        check("举报需登录", GovernanceRules.reportRequiresAuth());
    }

    private static java.time.Duration Duration47() {
        return java.time.Duration.ofHours(47);
    }

    private static java.time.Duration Duration49() {
        return java.time.Duration.ofHours(49);
    }

    private static java.time.Duration Duration72() {
        return java.time.Duration.ofHours(72);
    }

    private static java.time.Duration Duration96() {
        return java.time.Duration.ofHours(96);
    }

    private static void testCopyright() {
        check("收到合格通知即下架", "TAKEDOWN".equals(GovernanceRules.copyrightAfterReceived()));
        check("下架后可反通知", GovernanceRules.canCounterNotice("TAKEDOWN"));
        check("反通知进入争议态", "DISPUTED".equals(GovernanceRules.copyrightAfterCounter("TAKEDOWN")));
        boolean threw = false;
        try {
            GovernanceRules.copyrightAfterCounter("CLOSED");
        } catch (IllegalStateException ex) {
            threw = true;
        }
        check("已结案不可反通知", threw);
        check("反通知成立→COUNTER_OK 并恢复内容",
                "COUNTER_OK".equals(GovernanceRules.copyrightResolveCounter(true))
                        && GovernanceRules.restoreContentAfterCounter("COUNTER_OK"));
        check("反通知不成立→维持下架",
                "COUNTER_REJECTED".equals(GovernanceRules.copyrightResolveCounter(false))
                        && !GovernanceRules.restoreContentAfterCounter("COUNTER_REJECTED"));
    }

    private static void testAppeal() {
        check("仅生效处置可申诉", GovernanceRules.canAppeal("ACTIVE") && !GovernanceRules.canAppeal("EXPIRED"));
        check("申诉两结论", "ACCEPTED".equals(GovernanceRules.appealDecision(true))
                && "REJECTED".equals(GovernanceRules.appealDecision(false)));
        Instant created = Instant.parse("2026-09-21T00:00:00Z");
        check("申诉 48h 反馈超时判定", GovernanceRules.appealFeedbackOverdue(created, null, created.plus(java.time.Duration.ofHours(49))))
                ;
        check("申诉受理→处置撤销", "REVOKED".equals(GovernanceRules.moderationStateAfterAppealAccepted()));
    }

    private static void testTracking() {
        check("事件名规范", TrackingRules.validEventName("article_view") && !TrackingRules.validEventName("Page View"));
        check("白名单内", TrackingRules.allowedEvent("record_created") && !TrackingRules.allowedEvent("hack.event"));
        check("白名单命名全部合规", TrackingRules.schemaConsistent());
        long now = System.currentTimeMillis();
        check("合法事件通过", TrackingRules.validate("page_view", now, "s1", "d1", Map.of("path", "/home")).isEmpty());
        Map<String, String> pii = TrackingRules.validate("page_view", now, "s1", "d1", Map.of("phone", "13812345678"));
        check("PII 手机号拒绝（隐私清单#6）", "PII_NOT_ALLOWED".equals(pii.get("props.phone")));
        Map<String, String> email = TrackingRules.validate("click", now, "s1", "d1", Map.of("mail", "a@b.com"));
        check("PII 邮箱拒绝", email.containsKey("props.mail"));
        check("缺会话拒绝", TrackingRules.validate("page_view", now, "", "d1", null).containsKey("sessionId"));
        check("时钟偏差超 7 天拒绝", TrackingRules.validate("page_view", now - 8L * 24 * 3600 * 1000, "s", "d", null)
                .containsKey("timestamp"));
        Map<String, Object> many = new HashMapFiller().fill();
        check("属性超 20 拒绝", TrackingRules.validate("click", now, "s", "d", many).containsKey("props"));
    }

    private static class HashMapFiller {
        Map<String, Object> fill() {
            Map<String, Object> m = new java.util.HashMap<>();
            for (int i = 0; i < 21; i++) {
                m.put("k" + i, i);
            }
            return m;
        }
    }
}
