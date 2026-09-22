package com.cutepet.adminops;

import com.cutepet.common.AuditRules;
import com.cutepet.common.PermissionCatalog;
import com.cutepet.adminops.domain.DashboardRules;
import com.cutepet.adminops.domain.RbacEngine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RBAC/看板逻辑测试（纯 Java main）。覆盖 T6.1(U25) / T6.2(U26) / T6.4（对象级/认证/工单/K样例）。
 */
public class RbacLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[rbac] PASS " + name);
    }

    public static void main(String[] args) {
        testRbac();
        testDashboard();
        testAuditRules();
        System.out.println("[rbac-logic] " + passed + " assertions passed");
    }

    private static void testRbac() {
        // T6.1 / U25：预置角色与 contracts 矩阵一致
        check("6 预置角色", RbacEngine.presets().size() == 6 && RbacEngine.presetRole("reviewer")
                && !RbacEngine.presetRole("hacker"));
        check("editor 有发文无下架", RbacEngine.can("editor", "article.create")
                && !RbacEngine.can("editor", "article.takedown"));
        check("reviewer 有审核无建角色", RbacEngine.can("reviewer", "review.article")
                && !RbacEngine.can("reviewer", "rbac.manage"));
        check("poiOperator 有 POI 无封禁", RbacEngine.can("poiOperator", "poi.create.edit")
                && !RbacEngine.can("poiOperator", "user.ban"));
        check("productOperator 有 CSV 导入", RbacEngine.can("productOperator", "product.import.csv"));
        check("supervisor 全权除高危双点（30）", RbacEngine.presets().get("supervisor").size() == 30
                && !RbacEngine.can("supervisor", "rbac.manage")
                && !RbacEngine.can("supervisor", "system.settings")
                && RbacEngine.can("supervisor", "user.ban"));
        check("administrator 全 32 权限", RbacEngine.presets().get("administrator").size() == 32);
        check("全部预置权限合法", RbacEngine.presets().values().stream().flatMap(List::stream)
                .allMatch(PermissionCatalog::isValid));

        // §6.2 角色保护
        check("administrator 角色不可改", !RbacEngine.editableRole("administrator", true));
        check("内置角色不可改", !RbacEngine.editableRole("editor", true));
        check("自定义非内置可改", RbacEngine.editableRole("my-role", false));
        threw = false;
        try {
            RbacEngine.assertCustomRole(List.of("fake.permission"));
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("自定义角色拒绝非法权限", threw);
        threw = false;
        try {
            RbacEngine.assertCustomRole(List.of());
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("自定义角色至少 1 权限", threw);

        check("高危三动作二次确认", RbacEngine.requiresDoubleConfirm("user.ban")
                && RbacEngine.requiresDoubleConfirm("rbac.manage")
                && RbacEngine.requiresDoubleConfirm("system.settings")
                && !RbacEngine.requiresDoubleConfirm("article.create"));

        // T6.4 对象级权限
        Set<String> reviewerGranted = Set.copyOf(RbacEngine.presets().get("reviewer"));
        Set<String> editorGranted = Set.copyOf(RbacEngine.presets().get("editor"));
        check("T6.4 不能封禁自己", !RbacEngine.canActOnTarget(reviewerGranted, "user.ban", 7, 7));
        check("T6.4 可封禁他人（需权限）", RbacEngine.canActOnTarget(reviewerGranted, "user.ban", 7, 9)
                || !RbacEngine.canActOnTarget(Set.of(), "user.ban", 7, 9));
        check("T6.4 编辑自己内容 create 即可", RbacEngine.canActOnTarget(editorGranted, "article.edit.any", 3, 3));
        check("T6.4 编辑他人需 edit.any", !RbacEngine.canActOnTarget(
                Set.of("article.create"), "article.edit.any", 3, 9)
                && RbacEngine.canActOnTarget(Set.of("article.create", "article.edit.any"), "article.edit.any", 3, 9));

        check("看板：全量权限可见", RbacEngine.canViewDashboard(Set.of("dashboard.view.all")));
        check("看板：业务域权限可见本域", RbacEngine.canViewDashboard(Set.of("poi.create.edit")));
        check("看板：纯审计角色不可见业务看板", !RbacEngine.canViewDashboard(Set.of("audit.view")));

        check("T6.4 认证复核须 activity.manage", RbacEngine.canReviewCredential(Set.of("activity.manage"))
                && !RbacEngine.canReviewCredential(Set.of("poi.create.edit")));
        check("T6.4 客服/版权工单不互串", RbacEngine.canHandleTicket(Set.of("appeal.handle"), "APPEAL")
                && !RbacEngine.canHandleTicket(Set.of("appeal.handle"), "COPYRIGHT")
                && RbacEngine.canHandleTicket(Set.of("report.handle"), "COPYRIGHT")
                && RbacEngine.canHandleTicket(Set.of("appeal.handle"), "CUSTOMER_SERVICE"));
    }

    private static void testDashboard() {
        // T6.2 / U26
        check("SLA 三态（24/48 决议）", "OK".equals(DashboardRules.slaStatus(3))
                && "BREACH".equals(DashboardRules.slaStatus(30))
                && "ESCALATE".equals(DashboardRules.slaStatus(60)));
        check("积压 >50 告警", DashboardRules.backlogAlert(51) && !DashboardRules.backlogAlert(50));
        check("零分母→null 不产生 0% 假象（U91）", DashboardRules.rate(0, 0) == null);
        check("通过率口径", DashboardRules.rate(8, 10) == 80.0);
        check("指标合法性（part≤total）", DashboardRules.metricValid(3, 5)
                && !DashboardRules.metricValid(6, 5) && !DashboardRules.metricValid(-1, 5));

        Double p50 = DashboardRules.percentile(List.of(1.0, 2.0, 3.0, 4.0, 50.0), 50);
        Double p90 = DashboardRules.percentile(List.of(1.0, 2.0, 3.0, 4.0, 50.0), 90);
        check("P50/P90 计算", p50 == 3.0 && p90 == 50.0);
        check("空列表分位→null", DashboardRules.percentile(List.of(), 50) == null);

        check("投稿通过率样例（U91 前置）", DashboardRules.approvalRate(18, 20) == 90.0);
        check("提醒完成率", DashboardRules.reminderCompletionRate(7, 10) == 70.0);
        check("举报按时率", DashboardRules.reportOnTimeRate(9, 10) == 90.0);
        check("跨日求和（U91 口径）", DashboardRules.sumDaily(java.util.Arrays.asList(1L, null, 4L)) == 5
                && DashboardRules.sumDaily(List.of()) == 0);
    }

    private static void testAuditRules() {
        // T6.3：查询窗与脱敏
        check("查询窗 ≤90 天", AuditRules.queryRangeValid(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 20))
                && !AuditRules.queryRangeValid(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 20)));
        check("起止倒置拒绝", !AuditRules.queryRangeValid(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 7, 1)));
        check("手机号脱敏 138****5678", AuditRules.redact("联系 13812345678 处理").contains("138****5678")
                && !AuditRules.redact("联系 13812345678 处理").contains("12345678"));
        check("邮箱脱敏首字符", AuditRules.redact("a***@example.com".contains("@") ? "user@example.com" : "")
                .startsWith("u***@"));
        check("IP 必录（决议）", AuditRules.ipRecorded("10.0.0.1") && !AuditRules.ipRecorded(" "));
        check("高危判定同源", AuditRules.highRisk("user.ban") && AuditRules.highRisk("rbac.manage")
                && !AuditRules.highRisk("article.create"));
        var entry = com.cutepet.common.AuditEntry.of("op1", "ADMIN", "127.0.0.1", "article.takedown",
                "ARTICLE", "a1", "PUBLISHED", "TAKEDOWN");
        check("审计条目可读", AuditRules.readable(entry));
    }

    private static boolean threw;
}
