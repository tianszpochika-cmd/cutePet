package com.cutepet.content;

import com.cutepet.content.domain.FeedRules;
import com.cutepet.content.domain.InteractionRules;
import com.cutepet.content.domain.SearchRules;
import com.cutepet.content.domain.SubmissionRules;
import com.cutepet.content.domain.SubmissionRules.State;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * content 领域逻辑测试（纯 Java main）。
 * 覆盖：T3.1(U13) / T3.2(U14/U15/U74–U76/U94) / T3.4(U16) / T3.5(U17) / T3.6(U73/U74/U75/U88/U94)。
 */
public class ContentLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[content] PASS " + name);
    }

    public static void main(String[] args) {
        testFeed();
        testStateMachine();
        testVersionsAndWithdraw();
        testQualityAndPublish();
        testInteractions();
        testSearch();
        System.out.println("[content-logic] " + passed + " assertions passed");
    }

    private static void testFeed() {
        // T3.1 / U13
        check("7 频道白名单", FeedRules.CHANNELS.size() == 7 && FeedRules.validChannel("疾病健康")
                && !FeedRules.validChannel("时政"));
        check("频道 slug 映射", "health".equals(FeedRules.channelSlug("疾病健康"))
                && "news".equals(FeedRules.channelSlug("行业快讯")));
        check("热度窗 7 天内计分", FeedRules.hotness(1, 1, 1, 3) == 1 + 2 + 3);
        check("超窗不计热度（-1 回退时间序）", FeedRules.hotness(99, 99, 99, 8) == -1);
        // 排序：置顶优先 → 热度 → 时间
        check("置顶压过热度", FeedRules.compareFeed(true, 0, 1, false, 999, 2) < 0);
        check("同置顶比热度", FeedRules.compareFeed(true, 10, 1, true, 5, 9) < 0);
        check("热度同比时间（新在前）", FeedRules.compareFeed(false, 5, 2, false, 5, 1) < 0);
        check("置顶仅限已发布", FeedRules.pinAllowed(true) && !FeedRules.pinAllowed(false));
        int[] page = FeedRules.clampPage(0, 999);
        check("分页收敛", page[0] == 1 && page[1] == FeedRules.PAGE_SIZE_DEFAULT);
    }

    private static void testStateMachine() {
        // T3.2 / U14 先审后发闭环
        check("草稿只能提交/撤回", SubmissionRules.canTransition(State.DRAFT, State.PENDING)
                && !SubmissionRules.canTransition(State.DRAFT, State.PUBLISHED));
        check("待审→领取→通过", SubmissionRules.canTransition(State.PENDING, State.REVIEWING)
                && SubmissionRules.canTransition(State.REVIEWING, State.PUBLISHED));
        check("驳回→修改重提", SubmissionRules.canTransition(State.REJECTED, State.PENDING));
        check("下架后不可进审核态", !SubmissionRules.canTransition(State.TAKEDOWN, State.REJECTED));
        boolean threw = false;
        try {
            SubmissionRules.assertTransition(State.DRAFT, State.PUBLISHED);
        } catch (IllegalStateException ex) {
            threw = true;
        }
        check("非法流转抛错", threw);

        // U15 领取锁
        check("仅待审可领取", SubmissionRules.claimable(State.PENDING) && !SubmissionRules.claimable(State.REVIEWING));
        check("处理中锁：同人可释放、他人不可", SubmissionRules.releaseable(State.REVIEWING, true)
                && !SubmissionRules.releaseable(State.REVIEWING, false));
    }

    private static void testVersionsAndWithdraw() {
        // U74 线上/修改版
        check("待审版本冻结不可原位改", SubmissionRules.versionImmutableWhileReviewing("PENDING")
                && SubmissionRules.versionImmutableWhileReviewing("REVIEWING")
                && !SubmissionRules.versionImmutableWhileReviewing("DRAFT"));
        check("驳回后旧线上版仍可见（U74）", SubmissionRules.liveVisibleAfterReject("PUBLISHED"));
        check("通过替换：旧版→SUPERSEDED", "SUPERSEDED".equals(SubmissionRules.stateAfterPublishSwap()));

        // U75 撤下
        check("作者撤下终止未决审核", SubmissionRules.terminatesPendingOnWithdraw());
        check("作者不能绕过治理恢复", !SubmissionRules.authorCanRestoreFromTakedown());
        check("运营恢复需治理放行标志", SubmissionRules.operatorCanRestore(true)
                && !SubmissionRules.operatorCanRestore(false));

        // U88 治理传播
        Set<String> propagation = SubmissionRules.governancePropagationTargets();
        check("治理传播覆盖收藏/清单/绑定/阻断重提",
                propagation.contains("FAVORITES_MARK_UNAVAILABLE") && propagation.contains("LIST_ITEMS_HIDE")
                        && propagation.contains("RELATED_BINDINGS_KEEP_TEXT") && propagation.contains("BLOCK_RESUBMIT"));
        check("治理下架不可再提审（U88）", !SubmissionRules.canResubmitFromTakedown());
    }

    private static void testQualityAndPublish() {
        // U76 质量退修
        check("质量 3 次→暂停投稿 7 天", SubmissionRules.suspendForQuality(3)
                && !SubmissionRules.suspendForQuality(2) && SubmissionRules.qualitySuspendDays() == 7);
        check("通过后质量计数清零", SubmissionRules.rejectCountAfterApprove() == 0);
        check("审核留痕必备", SubmissionRules.auditTrailRequired());

        // U94 复核与直发
        check("UGC 永不直发", !SubmissionRules.publishAllowed("犬", "UGC", 9L, true));
        check("无直发权限不放行（编辑不继承）", !SubmissionRules.publishAllowed("犬", "PGC", null, false));
        check("健康科普缺复核不发布", !SubmissionRules.publishAllowed("疾病健康", "PGC", null, true));
        check("健康科普有复核人可发布", SubmissionRules.publishAllowed("疾病健康", "PGC", 9L, true));
        check("普通频道 PGC+权限直发", SubmissionRules.publishAllowed("犬", "PGC", null, true));

        // U73 类型入口
        check("文章/评测/清单三类型", SubmissionRules.validKind("ARTICLE") && SubmissionRules.validKind("REVIEW")
                && SubmissionRules.validKind("LIST") && !SubmissionRules.validKind("VIDEO"));
        check("进正确队列（评测独立队列组）", "queue.lists".equals(SubmissionRules.queueFor("LIST"))
                && "queue.reviews".equals(SubmissionRules.queueFor("REVIEW"))
                && "queue.articles".equals(SubmissionRules.queueFor("ARTICLE")));
    }

    private static void testInteractions() {
        // T3.4 / U16
        check("点赞幂等语义", InteractionRules.likeChanged(false, true) && !InteractionRules.likeChanged(true, true));
        check("收藏五 Tab", InteractionRules.FAVORITE_TABS.size() == 5
                && InteractionRules.validFavoriteTab("场所") && !InteractionRules.validFavoriteTab("宠物"));
        check("评论长度 1–500", InteractionRules.validComment("沙发")
                && !InteractionRules.validComment(" ")
                && !InteractionRules.validComment("x".repeat(501)));

        // 一层回复归根
        check("回复根评论=根", InteractionRules.resolveParentId(10L, null) == 10L);
        check("回复回复归位到根（一层）", InteractionRules.resolveParentId(11L, 10L) == 10L);
        check("顶层= null", InteractionRules.resolveParentId(null, null) == null);

        // 机审
        check("正常内容 PASS", InteractionRules.machineReview("写得真好") == InteractionRules.MachineState.PASS);
        check("导流词 SUSPECT 仅自见", InteractionRules.machineReview("加微信详聊") == InteractionRules.MachineState.SUSPECT
                && !InteractionRules.visibleToOthers(InteractionRules.MachineState.SUSPECT)
                && InteractionRules.visibleToAuthor(InteractionRules.MachineState.SUSPECT));
        check("违禁词 REJECT 双不可见", InteractionRules.machineReview("违禁药渠道") == InteractionRules.MachineState.REJECT
                && !InteractionRules.visibleToOthers(InteractionRules.MachineState.REJECT)
                && !InteractionRules.visibleToAuthor(InteractionRules.MachineState.REJECT));
        check("REJECT 优先于 SUSPECT", InteractionRules.machineReview("加微信买违禁药") == InteractionRules.MachineState.REJECT);
        check("REJECT 词表内容不放行", InteractionRules.machineReview("代购疫苗") == InteractionRules.MachineState.REJECT);

        check("关注幂等", InteractionRules.followChanged(false, true) && !InteractionRules.followChanged(true, true));
        check("删评：本人或管理权", InteractionRules.canDeleteComment(1, 1, false)
                && InteractionRules.canDeleteComment(2, 1, true)
                && !InteractionRules.canDeleteComment(2, 1, false));
        check("封禁作者内容不进流（U16）", !InteractionRules.feedVisible(true) && InteractionRules.feedVisible(false));
        check("互动目标枚举", InteractionRules.interactionTargets().contains("ARTICLE"));
    }

    private static void testSearch() {
        // T3.5 / U17
        check("查询长度校验", SearchRules.validQuery("猫粮") && !SearchRules.validQuery(" ")
                && !SearchRules.validQuery("x".repeat(51)));
        check("标题命中 30 > 标签 20 > 正文 10",
                SearchRules.score("幼猫粮怎么选", List.of(), "", "猫粮") == 30);
        check("标签命中计分", SearchRules.score("A", List.of("猫粮", "营养"), "", "猫粮") == 20);
        check("正文命中计分", SearchRules.score("A", List.of(), "这里谈猫粮选择", "猫粮") == 10);
        check("无命中 0 分", SearchRules.score("A", List.of(), "B", "狗") == 0);

        List<SearchRules.Hit> ranked = SearchRules.rank(List.of(
                new SearchRules.Hit("弱", 10),
                new SearchRules.Hit("强", 30),
                new SearchRules.Hit("零", 0)));
        check("降序且过滤 0 分", ranked.size() == 2 && "强".equals(ranked.get(0).title()));
        check("空结果判定", SearchRules.isEmpty(List.of()) && !SearchRules.isEmpty(ranked));
        check("空结果兜底推荐入参", SearchRules.fallbackWhenEmpty(List.of("推荐A"), 3).contains("推荐A"));

        List<SearchRules.Segment> segs = SearchRules.highlight("幼猫粮怎么选", "猫粮");
        check("高亮切分含命中段", segs.size() == 3 && segs.get(1).hit() && "猫粮".equals(segs.get(1).text()));
        List<SearchRules.Segment> multi = SearchRules.highlight("幼猫粮与成猫粮", "猫粮");
        check("多处命中全部标记", multi.size() == 4 && multi.get(1).hit() && multi.get(3).hit());
        check("未命中整体非高亮", SearchRules.highlight("全文", "缺失").get(0).hit() == false);
    }
}
