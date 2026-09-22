package com.cutepet.explore;

import com.cutepet.explore.domain.ActivityRules;
import com.cutepet.explore.domain.ActivityRules.OrgState;
import com.cutepet.explore.domain.ActivityRules.SignupDeny;
import com.cutepet.explore.domain.ActivityRules.State;
import com.cutepet.explore.domain.CorrectionRules;
import com.cutepet.explore.domain.PoiRules;
import com.cutepet.explore.domain.ReviewRules;
import com.cutepet.explore.domain.ReviewRules.SpamVerdict;
import com.cutepet.explore.domain.RouteRules;

import java.time.LocalDate;
import java.util.List;

/**
 * explore 领域逻辑测试（纯 Java main）。
 * 覆盖：T4.1(U18) / T4.2(U19/U81) / T4.3(U20) / T4.4(U21) / T4.5(U22) / T4.6(U68–U72/U86/U89/U90)。
 */
public class ExploreLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[explore] PASS " + name);
    }

    public static void main(String[] args) {
        testPoi();
        testReviews();
        testCorrections();
        testRoutes();
        testActivities();
        testOrgAndAdoption();
        System.out.println("[explore-logic] " + passed + " assertions passed");
    }

    private static void testPoi() {
        // T4.1 / U18
        check("7 类型白名单", PoiRules.TYPES.size() == 7 && PoiRules.validType("宠物医院")
                && !PoiRules.validType("赌场"));
        check("半径 5/10/20（决议）", PoiRules.RADII_KM.equals(List.of(5, 10, 20))
                && PoiRules.validRadius(10) && !PoiRules.validRadius(7));
        double bjSh = PoiRules.distanceKm(116.40, 39.90, 121.47, 31.23);
        check("京沪直线距离约1067km±30", bjSh > 1037 && bjSh < 1097);
        check("同城距离≈0", PoiRules.distanceKm(116.40, 39.90, 116.40, 39.90) < 0.001);
        check("半径过滤", PoiRules.withinRadius(4.2, 5) && !PoiRules.withinRadius(5.5, 5));
        check("展示均值：无评价→null（暂无评分 U81）", PoiRules.displayAvg(0, 0) == null);
        check("展示均值：有评价返回均值", PoiRules.displayAvg(3, 4.6) == 4.6);
        check("排名均值：无评价按3.5基准（决议）", PoiRules.rankingAvg(0, 0) == 3.5);
        double composite = PoiRules.compositeScore(1.0, 5, 0, 0); // 距离1km/半径5km → 距离归一 1-1/5=0.8；无评价评分 3.5/5=0.7
        check("综合分=0.6×0.8 + 0.4×0.7", Math.abs(composite - (0.6 * 0.8 + 0.4 * 0.7)) < 1e-9);
        double far = PoiRules.compositeScore(5.0, 5, 0, 0);
        check("越近分越高", PoiRules.compare(composite, far) < 0);
        double highScore = PoiRules.compositeScore(1.0, 5, 5, 5.0);
        check("评分高分更高（同距离）", highScore > composite);
    }

    private static void testReviews() {
        // T4.2 / U19
        check("三维 1–5 校验", ReviewRules.threeScoresValid(1, 3, 5)
                && !ReviewRules.threeScoresValid(0, 3, 5) && !ReviewRules.threeScoresValid(3, 6, 5));
        check("三均均值", ReviewRules.avg(5, 4, 3) == 4.0);

        check("防刷：同人同日第二条拒绝（决议1天1条 U19）",
                ReviewRules.spamCheck(1, 0, new int[] { 4, 4, 4 }, "不错") == SpamVerdict.DUPLICATE_DAY);
        check("防刷：同设备>5 拒绝（决议）",
                ReviewRules.spamCheck(0, 5, new int[] { 4, 4, 4 }, "不错") == SpamVerdict.DEVICE_LIMIT);
        check("防刷：全5短文→复审（决议）",
                ReviewRules.spamCheck(0, 0, new int[] { 5, 5, 5 }, "很好") == SpamVerdict.SUSPECT_REVIEW);
        check("正常评价放行", ReviewRules.spamCheck(0, 0, new int[] { 5, 5, 5 }, "环境很好，宠物玩得开心") == SpamVerdict.OK);
        check("全5长文放行", ReviewRules.spamCheck(0, 0, new int[] { 5, 5, 5 }, "这次带狗子来，环境非常棒服务也好") == SpamVerdict.OK);

        // U81 新号先审后计
        check("新号<7天→PENDING 先审后计（U81）",
                ReviewRules.stateFor(3, SpamVerdict.OK) == ReviewRules.State.PENDING);
        check("老号正常→VISIBLE", ReviewRules.stateFor(30, SpamVerdict.OK) == ReviewRules.State.VISIBLE);
        check("疑似→PENDING", ReviewRules.stateFor(30, SpamVerdict.SUSPECT_REVIEW) == ReviewRules.State.PENDING);

        check("仅 VISIBLE 计入均值（U81）",
                ReviewRules.countsInAvg(ReviewRules.State.VISIBLE)
                        && !ReviewRules.countsInAvg(ReviewRules.State.HIDDEN)
                        && !ReviewRules.countsInAvg(ReviewRules.State.PENDING));
        check("PENDING 仅本人可见", ReviewRules.visibleToAuthor(ReviewRules.State.PENDING)
                && !ReviewRules.visibleToOthers(ReviewRules.State.PENDING));
        check("HIDDEN 双不可见", !ReviewRules.visibleToAuthor(ReviewRules.State.HIDDEN)
                && !ReviewRules.visibleToOthers(ReviewRules.State.HIDDEN));

        Double recomputed = ReviewRules.recomputeAvg(
                List.of(ReviewRules.State.VISIBLE, ReviewRules.State.HIDDEN, ReviewRules.State.PENDING),
                List.of(5.0, 1.0, 2.0));
        check("恢复/隐藏后重算仅计有效（U81）", recomputed != null && recomputed == 5.0);
        check("全部无效→null 暂无评分（U81）",
                ReviewRules.recomputeAvg(List.of(ReviewRules.State.HIDDEN), List.of(1.0)) == null);

        check("图片重复检测（决议）", ReviewRules.imageDuplicate(List.of("a", "a"))
                && !ReviewRules.imageDuplicate(List.of("a", "b"))
                && !ReviewRules.imageDuplicate(List.of()));
        check("day_key 唯一键构造", "1#2#2026-09-22".equals(
                ReviewRules.dayKey(1, 2, LocalDate.of(2026, 9, 22))));
    }

    private static void testCorrections() {
        // T4.3 / U20
        check("5 类纠错字段", CorrectionRules.FIELDS.size() == 5
                && CorrectionRules.validField("phone") && !CorrectionRules.validField("owner"));
        check("工单仅 OPEN→ACCEPTED/REJECTED",
                CorrectionRules.transition(CorrectionRules.State.OPEN, CorrectionRules.State.ACCEPTED)
                        && !CorrectionRules.transition(CorrectionRules.State.ACCEPTED, CorrectionRules.State.REJECTED));
        boolean threw = false;
        try {
            CorrectionRules.assertTransition(CorrectionRules.State.REJECTED, CorrectionRules.State.ACCEPTED);
        } catch (IllegalStateException ex) {
            threw = true;
        }
        check("非法流转抛错", threw);
        check("同类合并取首个 OPEN（U20 合并）",
                CorrectionRules.mergeTarget(List.of(7L, 8L, 9L)).orElse(-1L) == 7L
                        && CorrectionRules.mergeTarget(List.of()).isEmpty());
        check("采纳回写 proposed", "东城区8号".equals(CorrectionRules.valueToWriteBack(" 东城区8号 ")));
        check("驳回必附理由", !CorrectionRules.canReject(" ") && CorrectionRules.canReject("信息不实"));
        check("双向通知标识", CorrectionRules.notificationFor(CorrectionRules.State.ACCEPTED).contains("ACCEPTED")
                && CorrectionRules.notificationFor(CorrectionRules.State.REJECTED).contains("REJECTED"));
    }

    private static void testRoutes() {
        // T4.4 / U21
        boolean threw = false;
        double d = RouteRules.totalDistanceKm("116.40,39.90;116.50,39.90");
        check("点串距离≈10km 量级", d > 8 && d < 12);
        threw = false;
        try {
            RouteRules.totalDistanceKm("116.40,39.90");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("至少 2 点", threw);
        threw = false;
        try {
            RouteRules.parse("bad-format");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("格式非法拒绝", threw);
        check("先审后发：PENDING→PUBLISHED/REJECTED",
                RouteRules.transition(RouteRules.State.PENDING, RouteRules.State.PUBLISHED)
                        && RouteRules.transition(RouteRules.State.PENDING, RouteRules.State.REJECTED)
                        && !RouteRules.transition(RouteRules.State.PUBLISHED, RouteRules.State.PENDING));
        check("驳回可重提", RouteRules.transition(RouteRules.State.REJECTED, RouteRules.State.PENDING));
        check("仅发布可见", RouteRules.feedVisible(RouteRules.State.PUBLISHED)
                && !RouteRules.feedVisible(RouteRules.State.PENDING));
        check("点赞收藏幂等", RouteRules.toggleChanged(false, true) && !RouteRules.toggleChanged(true, true));
    }

    private static void testActivities() {
        LocalDate now = LocalDate.of(2026, 9, 22);
        LocalDate begins = LocalDate.of(2026, 12, 1);   // 活动开始
        LocalDate deadline = LocalDate.of(2026, 11, 20);

        // U69 开放窗口：不早于结束前 90 天（begins-90 = 9/2 → now 9/22 已开放）
        check("U69 过早报名拒绝", ActivityRules.denyReason(State.PUBLISHED, LocalDate.of(2027, 3, 1),
                null, false, 0, 0, false, true, now) == SignupDeny.TOO_EARLY);
        check("U69 窗口内允许（无其他阻碍）", ActivityRules.denyReason(State.PUBLISHED, begins,
                deadline, false, 10, 3, false, true, now) == null);
        check("U69 截止后拒绝", ActivityRules.denyReason(State.PUBLISHED, begins,
                LocalDate.of(2026, 9, 1), false, 0, 0, false, true, now) == SignupDeny.DEADLINE_PASSED);
        check("U69 已开始拒绝", ActivityRules.denyReason(State.PUBLISHED, LocalDate.of(2026, 9, 1),
                null, false, 0, 0, false, true, now) == SignupDeny.STARTED_OR_OVER);

        // U68 名额与重复
        check("U68 满员拒绝", ActivityRules.denyReason(State.PUBLISHED, begins,
                deadline, false, 5, 5, false, true, now) == SignupDeny.FULL);
        check("U68 最后名额未满允许", ActivityRules.denyReason(State.PUBLISHED, begins,
                deadline, false, 5, 4, false, true, now) == null);
        check("U68 同账号仅一条有效", ActivityRules.denyReason(State.PUBLISHED, begins,
                deadline, false, 10, 2, true, true, now) == SignupDeny.DUPLICATE);
        check("并发超名额判定 DENIED", ActivityRules.decide(5, 6) == ActivityRules.SignupOutcome.DENIED
                && ActivityRules.decide(5, 5) == ActivityRules.SignupOutcome.OK);
        check("同意凭证必填", ActivityRules.denyReason(State.PUBLISHED, begins,
                deadline, false, 10, 0, false, false, now) == SignupDeny.CONSENT_REQUIRED);
        check("同意凭证校验", ActivityRules.consentValid("v1", "2026-09-22T10:00")
                && !ActivityRules.consentValid("", "t") && !ActivityRules.consentValid("v", ""));

        // U70 修改暂停
        check("U70 修改核实中暂停新报名", ActivityRules.denyReason(State.CHANGED, begins,
                deadline, true, 10, 1, false, true, now) == SignupDeny.STATE_BLOCKED
                && ActivityRules.pauseSignupsOnEdit(State.CHANGED));
        check("U70 通过回发布/驳回回退", "PUBLISHED".equals(ActivityRules.changeReviewResult(true))
                && "REVERTED".equals(ActivityRules.changeReviewResult(false)));

        // U71 取消
        check("U71 取消后普通编辑被阻断", ActivityRules.normalEditBlocked(State.CANCELLED)
                && !ActivityRules.normalEditBlocked(State.PUBLISHED));
        check("U71 取消态不可再流转（不可恢复）", !ActivityRules.transition(State.CANCELLED, State.PUBLISHED));
        check("U71 取消→有效报名转活动取消", "ACTIVITY_CANCELLED".equals(ActivityRules.signupStateAfterActivityCancelled())
                && ActivityRules.failedNotifyGoesToRecoveryQueue());
        check("取消后重报名=新历史行允许", ActivityRules.allowReSignupAfterCancel(false)
                && !ActivityRules.allowReSignupAfterCancel(true));

        // U72 名单权限
        check("U72 组织者本人可查", ActivityRules.canViewSignups(7, 7, false));
        check("U72 他人不可查", !ActivityRules.canViewSignups(8, 7, false));
        check("U72 导出需组织者+权限双满足", ActivityRules.canExportSignups(7, 7, true)
                && !ActivityRules.canExportSignups(7, 7, false)
                && !ActivityRules.canExportSignups(7, 8, true));

        // 状态机
        check("活动状态机：发布/取消链", ActivityRules.transition(State.PENDING, State.PUBLISHED)
                && ActivityRules.transition(State.PUBLISHED, State.CHANGED)
                && ActivityRules.transition(State.PUBLISHED, State.ENDED)
                && !ActivityRules.transition(State.PUBLISHED, State.PENDING));

        // U86 保存期限
        check("U86 结束 90 天后到期清理", ActivityRules.cleanupDue(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 10, 1))
                && !ActivityRules.cleanupDue(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1)));
        List<String> actions = ActivityRules.cleanupActions();
        check("U86 清理四动作（脱敏/失效/通知删副本/留痕）", actions.size() == 4
                && actions.contains("PLATFORM_FIELDS_CLEARED") && actions.contains("DOWNLOAD_TOKEN_INVALIDATED")
                && actions.contains("NOTIFY_ORG_DELETE_COPY") && actions.contains("AUDIT_TRAIL_KEPT"));
    }

    private static void testOrgAndAdoption() {
        LocalDate now = LocalDate.of(2026, 9, 22);
        // U89 机构认证
        OrgState s = ActivityRules.orgTransition(OrgState.NONE, "apply", null, now);
        check("U89 申请", s == OrgState.APPLY);
        check("U89 通过", ActivityRules.orgTransition(s, "approve", now.plusYears(1), now) == OrgState.APPROVED);
        OrgState approved = ActivityRules.orgTransition(s, "approve", now.plusYears(1), now);
        check("U89 过期检查", ActivityRules.orgTransition(approved, "expire-check", now.minusDays(1), now) == OrgState.EXPIRED);
        check("U89 撤销", ActivityRules.orgTransition(approved, "revoke", null, now) == OrgState.REVOKED);
        check("U89 直发资格=当前认证有效", ActivityRules.orgDirectPublish(OrgState.APPROVED)
                && !ActivityRules.orgDirectPublish(OrgState.EXPIRED)
                && !ActivityRules.orgDirectPublish(OrgState.REVOKED));
        check("U89 领养始终人工审", ActivityRules.adoptionAlwaysManualReview());
        check("U89 认证材料不公开", !ActivityRules.credentialMaterialPublic());

        // U90 领养
        check("U90 领养无报名按钮", !ActivityRules.adoptionHasSignupButton());
        check("U90 领养无交易按钮", !ActivityRules.adoptionHasTradeButton());
        check("U90 未过期公开", ActivityRules.adoptionPublicVisible(LocalDate.of(2026, 10, 1), now, "PUBLISHED"));
        check("U90 过 30 天停止公开", !ActivityRules.adoptionPublicVisible(LocalDate.of(2026, 9, 1), now, "PUBLISHED"));
        check("U90 未发布不公开", !ActivityRules.adoptionPublicVisible(LocalDate.of(2026, 10, 1), now, "PENDING"));
        check("U90 售卖举报可追溯", ActivityRules.sellReportTraced("SELLING_SUSPECTED")
                && !ActivityRules.sellReportTraced("OTHER"));
    }
}
