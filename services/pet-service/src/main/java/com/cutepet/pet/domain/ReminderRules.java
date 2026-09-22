package com.cutepet.pet.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 提醒纯规则（T2.4 · 需求 B5 · 决议：3 天过期二次提醒 / 北京时间 / 双基准 · U11/U55/U56/U59 关联）。
 * 周期计算与 contracts（TS）nextOccurrence 同语义的 Java 移植。
 */
public final class ReminderRules {

    public static final int OVERDUE_SECOND_NOTIFY_DAYS = 3;

    // 状态：ACTIVE(启用) / DONE / EXPIRED / CLOSED
    public enum State { ACTIVE, DONE, EXPIRED, CLOSED }

    public record Recurrence(String kind, int days, int month, int day, LocalDate start) {
        public static Recurrence once(LocalDate date) {
            return new Recurrence("once", 0, 0, 0, date);
        }

        public static Recurrence everyDays(int days, LocalDate start) {
            return new Recurrence("everyDays", days, 0, 0, start);
        }

        public static Recurrence yearly(int month, int day, LocalDate start) {
            return new Recurrence("yearly", 0, month, day, start);
        }
    }

    public enum Basis { PLAN, COMPLETION }

    public enum CompleteOutcome { DONE_FINAL, REARMED_NEXT, ALREADY_DONE }

    private ReminderRules() {
    }

    public static boolean transition(State from, State to) {
        return switch (from) {
            case ACTIVE -> to == State.DONE || to == State.EXPIRED || to == State.CLOSED;
            case DONE -> to == State.ACTIVE; // 周期型完成→排下期重新启用
            default -> false; // EXPIRED/CLOSED 终态
        };
    }

    public static boolean isLeap(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /** after（不含）之后的下一次触发日；同语义见 contracts.nextOccurrence（PLAN/COMPLETION 双基准）。 */
    public static LocalDate nextOccurrence(Recurrence r, LocalDate after, Basis basis) {
        if ("once".equals(r.kind())) {
            return r.start().isAfter(after) ? r.start() : null;
        }
        if ("everyDays".equals(r.kind())) {
            if (r.days() < 1) {
                throw new IllegalArgumentException("周期天数必须为正整数");
            }
            if (after.isBefore(r.start())) {
                return r.start();
            }
            if (basis == Basis.COMPLETION) {
                return after.plusDays(r.days()); // U55 完成日锚点滚动
            }
            long elapsed = r.start().until(after).getDays();
            long k = Math.floorDiv(elapsed, r.days()) + 1;
            LocalDate next = r.start().plusDays(k * r.days());
            return next.isAfter(after) ? next : next.plusDays(r.days());
        }
        // yearly：2/29 非闰年落 2/28（U56），其余保持月日
        int y = after.getYear();
        LocalDate candidate = inYear(y, r);
        return candidate.isAfter(after) ? candidate : inYear(y + 1, r);
    }

    private static LocalDate inYear(int year, Recurrence r) {
        if (r.month() == 2 && r.day() == 29 && !isLeap(year)) {
            return LocalDate.of(year, 2, 28);
        }
        return LocalDate.of(year, r.month(), r.day());
    }

    /** 完成动作：once→终态 DONE；周期→按基准排下期并 REARM（防止重复完成由存储层唯一键保证）。 */
    public static CompleteOutcome complete(State current, Recurrence r, Basis basis, LocalDate completedOn) {
        if (current != State.ACTIVE) {
            return CompleteOutcome.ALREADY_DONE; // 幂等：重复完成返回已有结果（U57）
        }
        if ("once".equals(r.kind())) {
            return CompleteOutcome.DONE_FINAL;
        }
        return CompleteOutcome.REARMED_NEXT; // 下期 = nextOccurrence(r, completedOn, basis)
    }

    public static LocalDate nextDueAfterComplete(Recurrence r, Basis basis, LocalDate completedOn) {
        return nextOccurrence(r, completedOn, basis);
    }

    /** 逾期：到期 +3 天未处理 → 过期并触发第二次提醒（决议）；已完成不补推由 notification U59 承担 */
    public static boolean overdueExpired(LocalDate due, Instant now, boolean overdueReminded) {
        if (due == null || overdueReminded) {
            return false;
        }
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.ofHours(8)); // 北京时间（决议）
        return today.isAfter(due.plusDays(OVERDUE_SECOND_NOTIFY_DAYS));
    }

    public static boolean secondNotifyNeeded(LocalDate due, Instant now, boolean overdueReminded) {
        return overdueExpired(due, now, overdueReminded) && !overdueReminded;
    }

    /** 跳过必须带原因（U56 跳过有原因） */
    public static boolean skipValid(String reason) {
        return reason != null && !reason.isBlank();
    }

    /** 处理提醒需“可管理”档（所有者或共享 MANAGE；档位数据在 iam，本地阶段联动） */
    public static boolean canHandle(boolean isOwner, String shareLevel) {
        return isOwner || "MANAGE".equals(shareLevel);
    }
}
