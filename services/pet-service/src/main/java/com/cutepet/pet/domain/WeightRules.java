package com.cutepet.pet.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * 体重曲线纯规则（T2.3 · 需求 B3 · 决议 10%/5% · U80 不伪造趋势）。
 */
public final class WeightRules {

    public static final double CONFIRM_RATIO = 0.10;
    public static final double TREND_RATIO = 0.05;
    public static final int BASE_MIN_AGE_DAYS = 7;
    public static final int BASE_MAX_AGE_DAYS = 14;

    /** 曲线数据点（决议：时间戳+数值，存储统一 kg） */
    public record Sample(Instant at, double kg) {
    }

    public enum Trend { UP, DOWN, FLAT, UNKNOWN }

    private WeightRules() {
    }

    /** 补录后按时间重排（U80：补录排序重算） */
    public static List<Sample> sorted(List<Sample> samples) {
        return samples.stream().sorted(Comparator.comparing(Sample::at)).toList();
    }

    /** 单次 ≥10% 二次确认（边界含 10%） */
    public static boolean needsConfirm(Double prevKg, double nextKg) {
        if (prevKg == null || prevKg <= 0) {
            return false;
        }
        return Math.abs(nextKg - prevKg) / prevKg >= CONFIRM_RATIO;
    }

    /**
     * U80 趋势基准：取“距今 7–14 天窗口”内最近的样本；窗口外或不足 7 天 → 无基准（不伪造）。
     */
    public static Sample baseFor(List<Sample> samples, Instant now) {
        Sample latestEligible = null;
        for (Sample s : sorted(samples)) {
            if (s.at().isAfter(now)) {
                continue; // 未来点不参与（时钟异常防御）
            }
            long age = Duration.between(s.at(), now).toDays();
            if (age >= BASE_MIN_AGE_DAYS && age <= BASE_MAX_AGE_DAYS) {
                latestEligible = s; // 排序后取窗口内最靠近 7 天的最近点
            }
        }
        return latestEligible;
    }

    /** ≥5% 高亮；无基准不标记（U80） */
    public static boolean highlighted(Sample base, double currentKg) {
        if (base == null || base.kg() <= 0) {
            return false;
        }
        return Math.abs(currentKg - base.kg()) / base.kg() >= TREND_RATIO;
    }

    public static Trend trend(Sample base, double currentKg) {
        if (base == null) {
            return Trend.UNKNOWN;
        }
        double delta = currentKg - base.kg();
        if (Math.abs(delta) / base.kg() >= TREND_RATIO) {
            return delta > 0 ? Trend.UP : Trend.DOWN;
        }
        return Trend.FLAT;
    }

    /** 最新数据点（按时间末尾） */
    public static Sample latest(List<Sample> samples) {
        return sorted(samples).stream().reduce((a, b) -> b).orElse(null);
    }
}
