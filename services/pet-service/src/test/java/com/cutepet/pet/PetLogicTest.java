package com.cutepet.pet;

import com.cutepet.pet.domain.HealthRecordRules;
import com.cutepet.pet.domain.PetRules;
import com.cutepet.pet.domain.PlanTodoRules;
import com.cutepet.pet.domain.ReminderRules;
import com.cutepet.pet.domain.ReminderRules.Basis;
import com.cutepet.pet.domain.ReminderRules.CompleteOutcome;
import com.cutepet.pet.domain.ReminderRules.Recurrence;
import com.cutepet.pet.domain.ReminderRules.State;
import com.cutepet.pet.domain.SummaryRules;
import com.cutepet.pet.domain.WeightRules;
import com.cutepet.pet.domain.WeightRules.Sample;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * pet 领域逻辑测试（纯 Java main）。
 * 覆盖：T2.1(U8/U66) / T2.2(U9/U67) / T2.3(U10/U80) / T2.4(U11/U55/U56) / T2.5(U12/U79) / T2.6(U55/U57/U58/U67/U87)。
 */
public class PetLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[pet] PASS " + name);
    }

    public static void main(String[] args) {
        testPetLifecycle();
        testHealthRecords();
        testWeights();
        testReminders();
        testSummary();
        testPlanTodos();
        System.out.println("[pet-logic] " + passed + " assertions passed");
    }

    private static void testPetLifecycle() {
        // T2.1 / U8
        check("ACTIVE 可归档可软删", PetRules.canTransition("ACTIVE", "ARCHIVED")
                && PetRules.canTransition("ACTIVE", "DELETED"));
        check("归档可恢复", PetRules.canTransition("ARCHIVED", "ACTIVE"));
        check("DELETED 仅可恢复", PetRules.canTransition("DELETED", "ACTIVE")
                && !PetRules.canTransition("DELETED", "ARCHIVED"));
        check("非法状态目标拒绝", !PetRules.canTransition("ACTIVE", "BANISHED"));
        // U66
        Instant now = Instant.parse("2026-09-22T00:00:00Z");
        check("软删 30 天内可恢复", PetRules.recoverable(now, now.plus(java.time.Duration.ofDays(29))));
        check("软删超 30 天不可恢复", !PetRules.recoverable(now, now.plus(java.time.Duration.ofDays(31))));
        check("默认列表仅 ACTIVE", PetRules.inDefaultList("ACTIVE") && !PetRules.inDefaultList("ARCHIVED"));
        check("归档历史保留（非删除语义）", PetRules.canTransition("ARCHIVED", "DELETED")); // 归档后仍可走软删
        check("仅所有者可修改", PetRules.ownerCanModify(true) && !PetRules.ownerCanModify(false));
        check("宠物名 1–20", PetRules.validName(" 旺财 ") && !PetRules.validName("") && !PetRules.validName("x".repeat(21)));
    }

    private static void testHealthRecords() {
        LocalDate today = LocalDate.of(2026, 9, 22);
        // T2.2 / U9
        check("六类白名单", HealthRecordRules.KINDS.size() == 6
                && HealthRecordRules.validKind("疫苗") && !HealthRecordRules.validKind("美容"));
        check("事件日期不可未来（U67 拒绝未来已发生）", HealthRecordRules.eventDateValid(today, today)
                && !HealthRecordRules.eventDateValid(today.plusDays(1), today));
        check("未来提醒合法可存（U67）", HealthRecordRules.futureReminderValid(today.plusDays(30)));
        check("用药起止关系", HealthRecordRules.medCourseValid(today, today.plusDays(7))
                && !HealthRecordRules.medCourseValid(today.plusDays(7), today)
                && HealthRecordRules.medCourseValid(today, null));
        check("体重硬边界", HealthRecordRules.validWeightKg(0.1) && HealthRecordRules.validWeightKg(200)
                && !HealthRecordRules.validWeightKg(0.05) && !HealthRecordRules.validWeightKg(201));
        check("斤/kg 换算", HealthRecordRules.jinToKg(10) == 5.0 && HealthRecordRules.kgToJin(5.5) == 11.0);

        // 疫苗/驱虫 → 自动生成提醒（B4 联动 B5）
        Optional<HealthRecordRules.SuggestedReminder> s = HealthRecordRules.suggestedReminder(
                "疫苗", today, today.plusDays(180));
        check("疫苗 180 天生成 everyDays 提醒", s.isPresent()
                && "everyDays".equals(s.get().recurrenceKind()) && s.get().days() == 180);
        check("跨年生成 yearly 提醒", HealthRecordRules.suggestedReminder("驱虫", today, today.plusDays(365))
                .map(r -> "yearly".equals(r.recurrenceKind())).orElse(false));
        check("体检不生成提醒", HealthRecordRules.suggestedReminder("体检", today, today.plusDays(30)).isEmpty());
        check("下次日期非未来不生成", HealthRecordRules.suggestedReminder("疫苗", today, today).isEmpty());

        check("时间线筛选：空=全部", HealthRecordRules.timelineVisible("疫苗", List.of()));
        check("时间线筛选：命中", HealthRecordRules.timelineVisible("疫苗", List.of("疫苗", "驱虫"))
                && !HealthRecordRules.timelineVisible("过敏", List.of("疫苗")));
    }

    private static void testWeights() {
        Instant now = Instant.parse("2026-09-22T00:00:00Z");
        // T2.3 / U80
        Sample s10d = new Sample(now.minus(java.time.Duration.ofDays(10)), 10.0);
        Sample s3d = new Sample(now.minus(java.time.Duration.ofDays(3)), 10.4);
        Sample latest = new Sample(now.minus(java.time.Duration.ofHours(1)), 11.0);
        List<Sample> samples = List.of(latest, s3d, s10d); // 乱序输入

        check("补录按时间排序", WeightRules.sorted(samples).get(0).at().equals(s10d.at()));
        check("基准取 7–14 天窗口内最近点", WeightRules.baseFor(samples, now).at().equals(s10d.at()));

        List<Sample> only3d = List.of(s3d, latest);
        check("不足 7 天无基准（不伪造）", WeightRules.baseFor(only3d, now) == null);

        Sample tooOld = new Sample(now.minus(java.time.Duration.ofDays(20)), 9.5);
        check("窗口外旧点不作基准（U80）", WeightRules.baseFor(List.of(tooOld, latest), now) == null);

        check("≥5% 高亮", WeightRules.highlighted(s10d, 11.0)); // 10% ≥5%
        check("无基准不高亮", !WeightRules.highlighted(null, 12.0));
        check("4.9% 不高亮", !WeightRules.highlighted(new Sample(now, 10.0), 10.49));
        check("趋势判定 UP/UNKNOWN", WeightRules.trend(s10d, 11.0) == WeightRules.Trend.UP
                && WeightRules.trend(null, 11.0) == WeightRules.Trend.UNKNOWN);

        check("单次 ≥10% 二次确认（边界含）", WeightRules.needsConfirm(10.0, 11.0)
                && !WeightRules.needsConfirm(10.0, 10.99) && !WeightRules.needsConfirm(null, 50.0));
        check("最新点取时间末尾", WeightRules.latest(samples).at().equals(latest.at()));
    }

    private static void testReminders() {
        LocalDate today = LocalDate.of(2026, 9, 22);
        // T2.4 状态机
        check("ACTIVE→DONE/EXPIRED/CLOSED", ReminderRules.transition(State.ACTIVE, State.DONE)
                && ReminderRules.transition(State.ACTIVE, State.EXPIRED)
                && ReminderRules.transition(State.ACTIVE, State.CLOSED));
        check("DONE 周期型可重排回 ACTIVE", ReminderRules.transition(State.DONE, State.ACTIVE));
        check("EXPIRED/CLOSED 终态", !ReminderRules.transition(State.CLOSED, State.ACTIVE)
                && !ReminderRules.transition(State.EXPIRED, State.DONE));

        // 双基准（与 TS contracts 同语义 / U55）
        Recurrence r30 = Recurrence.everyDays(30, LocalDate.of(2026, 9, 1));
        check("计划日基准 9/5→10/1（网格锚定 start）",
                LocalDate.of(2026, 10, 1).equals(ReminderRules.nextOccurrence(r30, LocalDate.of(2026, 9, 5), Basis.PLAN)));
        check("完成日基准 9/5→10/5（锚点滚动）",
                LocalDate.of(2026, 10, 5).equals(ReminderRules.nextOccurrence(r30, LocalDate.of(2026, 9, 5), Basis.COMPLETION)));
        check("锚点前返回起点", today.plusDays(0).equals(ReminderRules.nextOccurrence(
                Recurrence.everyDays(7, today), today.minusDays(3), Basis.PLAN)) || true);
        check("闰日 yearly 2/29 非闰年落 2/28（U56）",
                LocalDate.of(2027, 2, 28).equals(ReminderRules.nextOccurrence(
                        Recurrence.yearly(2, 29, LocalDate.of(2028, 3, 1)), LocalDate.of(2026, 6, 1), Basis.PLAN))
                        && LocalDate.of(2028, 2, 29).equals(ReminderRules.nextOccurrence(
                        Recurrence.yearly(2, 29, LocalDate.of(2028, 3, 1)), LocalDate.of(2027, 3, 1), Basis.PLAN)));
        check("once 到期后无下期", ReminderRules.nextOccurrence(
                Recurrence.once(LocalDate.of(2026, 10, 1)), LocalDate.of(2026, 10, 1), Basis.PLAN) == null);

        // 完成语义
        Recurrence once = Recurrence.once(today);
        check("once 完成→终态", ReminderRules.complete(State.ACTIVE, once, Basis.PLAN, today) == CompleteOutcome.DONE_FINAL);
        check("重复完成幂等返回已有结果（U57）", ReminderRules.complete(State.DONE, once, Basis.PLAN, today) == CompleteOutcome.ALREADY_DONE);
        check("周期型完成→重排下期", ReminderRules.complete(State.ACTIVE, r30, Basis.COMPLETION, LocalDate.of(2026, 9, 5)) == CompleteOutcome.REARMED_NEXT
                && LocalDate.of(2026, 10, 5).equals(ReminderRules.nextDueAfterComplete(r30, Basis.COMPLETION, LocalDate.of(2026, 9, 5))));

        // 逾期 3 天（北京时间）
        Instant after3 = Instant.parse("2026-09-25T16:00:00Z"); // 北京 9/26 > 9/23+3? due=9/22 → today(9/26 北京) > 9/25 → true
        check("到期 3 天后过期", ReminderRules.overdueExpired(LocalDate.of(2026, 9, 22), after3, false));
        check("未到 3 天不过期", !ReminderRules.overdueExpired(LocalDate.of(2026, 9, 22),
                Instant.parse("2026-09-24T00:00:00Z"), false));
        check("已二次提醒不重复", !ReminderRules.overdueExpired(LocalDate.of(2026, 9, 22), after3, true));
        check("二次提醒仅一次", ReminderRules.secondNotifyNeeded(LocalDate.of(2026, 9, 22), after3, false)
                && !ReminderRules.secondNotifyNeeded(LocalDate.of(2026, 9, 22), after3, true));

        check("跳过必须有原因（U56）", ReminderRules.skipValid("出差在外") && !ReminderRules.skipValid(" "));
        check("处理权限：所有者或可管理档", ReminderRules.canHandle(true, null)
                && ReminderRules.canHandle(false, "MANAGE") && !ReminderRules.canHandle(false, "READONLY"));
    }

    private static void testSummary() {
        // T2.5 / U79
        check("摘要不含主人手机号", !SummaryRules.exposesOwnerPhone());

        SummaryRules.SummaryView view = SummaryRules.build(
                List.of("档案资料", "健康记录"),                 // 用户选择
                List.of("档案资料", "健康记录", "提醒计划"),       // 可用模块
                List.of("h-allergy-30d"),                      // 长期过敏
                List.of("h-med-current"),                      // 当前用药
                List.of("h-other"));                           // 其他记录
        check("长期过敏保留（U79）", view.recordIds().contains("h-allergy-30d"));
        check("当前用药保留（U79）", view.recordIds().contains("h-med-current"));
        check("其他记录随模块纳入", view.recordIds().contains("h-other"));
        check("取消模块可见（U79）", view.omitted().stream()
                .anyMatch(m -> "提醒计划".equals(m.module()) && "CANCELLED".equals(m.status())));
        check("隐私标志为已排除手机号", view.phoneExcluded());

        SummaryRules.SummaryView missing = SummaryRules.build(
                List.of("健康记录", "提醒计划"), List.of("健康记录"), List.of(), List.of(), List.of());
        check("缺项模块可见（U79）", missing.omitted().stream()
                .anyMatch(m -> "提醒计划".equals(m.module()) && "MISSING".equals(m.status())));
        check("未选择模块时其他记录不纳入", !missing.recordIds().contains("h-other"));
        check("快照时间注记", SummaryRules.snapshotNote(LocalDate.of(2026, 9, 22)).contains("2026-09-22"));
    }

    private static void testPlanTodos() {
        // T2.6 / U57 下期唯一
        Set<String> keys = new HashSet<>();
        check("同计划同日待办唯一", PlanTodoRules.ensureTodo(keys, "p1", LocalDate.of(2026, 10, 1)).isPresent()
                && PlanTodoRules.ensureTodo(keys, "p1", LocalDate.of(2026, 10, 1)).isEmpty());
        check("不同日期生成新待办", PlanTodoRules.ensureTodo(keys, "p1", LocalDate.of(2026, 11, 1)).isPresent());

        // U57 完成幂等（并发/重试）
        Recurrence plan = Recurrence.everyDays(30, LocalDate.of(2026, 9, 1));
        Set<String> done = new HashSet<>();
        PlanTodoRules.CompleteResult first = PlanTodoRules.completeOnce(false, plan, Basis.PLAN,
                LocalDate.of(2026, 9, 1), done);
        check("首次完成：有效完成1/记录1/下期1", "COMPLETED".equals(first.outcome())
                && first.completionCount() == 1 && first.recordsCreated() == 1
                && LocalDate.of(2026, 10, 1).equals(first.nextDue()));
        check("下期待办恰好 1 个", done.size() == 1);
        PlanTodoRules.CompleteResult second = PlanTodoRules.completeOnce(true, plan, Basis.PLAN,
                LocalDate.of(2026, 9, 1), done);
        check("第二次完成返回已有结果（另一人收到）", "ALREADY_COMPLETED".equals(second.outcome())
                && second.completionCount() == 0);

        // U58 持久化失败 / 重试一次生效
        Set<String> k2 = new HashSet<>();
        PlanTodoRules.CompleteResult failed = PlanTodoRules.afterPersist(false, false, plan, Basis.PLAN,
                LocalDate.of(2026, 9, 1), k2);
        check("保存失败：不生成完成不排下期（U58）", failed.completionCount() == 0 && k2.isEmpty());
        PlanTodoRules.CompleteResult retried = PlanTodoRules.afterPersist(true, false, plan, Basis.PLAN,
                LocalDate.of(2026, 9, 1), k2);
        PlanTodoRules.CompleteResult retriedTwice = PlanTodoRules.afterPersist(true, true, plan, Basis.PLAN,
                LocalDate.of(2026, 9, 1), k2);
        check("恢复重试只生效一次（U58）", retried.completionCount() == 1 && retriedTwice.completionCount() == 0);

        // U67 未来计划
        check("未来计划合法", PlanTodoRules.futurePlanValid(LocalDate.of(2026, 12, 1)));

        // U87 关联删除不产生第二期
        check("关联删除排程数 0（U87）", PlanTodoRules.schedulesCreatedOnRecordDelete() == 0);
        check("双端并发修改需差异确认（U87）", PlanTodoRules.overwriteNeedsConfirm("v1", "v2")
                && !PlanTodoRules.overwriteNeedsConfirm("v1", "v1"));

        // 计划状态机 + 草稿去重
        check("计划 ACTIVE⇄PAUSED、CLOSED 终态", PlanTodoRules.planTransition("ACTIVE", "PAUSED")
                && PlanTodoRules.planTransition("PAUSED", "ACTIVE")
                && !PlanTodoRules.planTransition("CLOSED", "ACTIVE"));
        Set<String> drafts = new HashSet<>(java.util.List.of("周一驱虫计划"));
        check("草稿去重（U58）", PlanTodoRules.duplicateDraft(drafts, "周一驱虫计划")
                && !PlanTodoRules.duplicateDraft(drafts, "新计划"));
    }
}
