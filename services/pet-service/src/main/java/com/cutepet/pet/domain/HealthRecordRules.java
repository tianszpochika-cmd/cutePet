package com.cutepet.pet.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** 健康记录纯规则（T2.2 · 需求 B4 · BR-07 · U9/U67/U79 关联）。 */
public final class HealthRecordRules {

    public static final List<String> KINDS = List.of("疫苗", "驱虫", "体检", "就医", "用药", "过敏");

    public static final double WEIGHT_MIN_KG = 0.1;
    public static final double WEIGHT_MAX_KG = 200.0;

    public record SuggestedReminder(String type, String recurrenceKind, int days, int month, int day) {
    }

    private HealthRecordRules() {
    }

    public static boolean validKind(String kind) {
        return KINDS.contains(kind);
    }

    /** 事件日期不得晚于今天（U67：未来“已发生事件”拒绝） */
    public static boolean eventDateValid(LocalDate eventDate, LocalDate today) {
        return eventDate != null && !eventDate.isAfter(today);
    }

    /** U67：合法未来计划（提醒日期）始终可保存 */
    public static boolean futureReminderValid(LocalDate reminderDate) {
        return reminderDate != null;
    }

    /** 用药起止关系：valid_to 不得早于 valid_from */
    public static boolean medCourseValid(LocalDate from, LocalDate to) {
        return to == null || (from != null && !to.isBefore(from));
    }

    /** 体重硬边界（contracts.validate 镜像） */
    public static boolean validWeightKg(double kg) {
        return !Double.isNaN(kg) && kg >= WEIGHT_MIN_KG && kg <= WEIGHT_MAX_KG;
    }

    /** 斤/kg（决议：存储统一 kg） */
    public static double jinToKg(double jin) {
        return Math.round(jin * 0.5 * 1000.0) / 1000.0;
    }

    public static double kgToJin(double kg) {
        return Math.round(kg * 2 * 1000.0) / 1000.0;
    }

    /**
     * 疫苗/驱虫记录填写“下次日期”→ 自动生成周期提醒（需求 B4 联动 B5）。
     * 间隔 <300 天按 everyDays；否则按 yearly（与录入日期同月日）。
     */
    public static Optional<SuggestedReminder> suggestedReminder(String kind, LocalDate eventDate, LocalDate nextDate) {
        if (!"疫苗".equals(kind) && !"驱虫".equals(kind)) {
            return Optional.empty();
        }
        if (nextDate == null || eventDate == null || !nextDate.isAfter(eventDate)) {
            return Optional.empty();
        }
        long gap = java.time.temporal.ChronoUnit.DAYS.between(eventDate, nextDate); // 总天数（非 Period 余数）
        if (gap < 300) {
            return Optional.of(new SuggestedReminder(kind, "everyDays", (int) gap, 0, 0));
        }
        return Optional.of(new SuggestedReminder(kind, "yearly", 0, nextDate.getMonthValue(), nextDate.getDayOfMonth()));
    }

    /** 时间线筛选：空集合=全部 */
    public static boolean timelineVisible(String kind, List<String> filter) {
        return filter == null || filter.isEmpty() || filter.contains(kind);
    }
}
