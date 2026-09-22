package com.cutepet.adminops.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 看板聚合纯规则（T6.2 · 状态表 U26 口径 + U91 指标复算前置）。
 */
public final class DashboardRules {

    public static final int BACKLOG_ALERT_THRESHOLD = 50;
    public static final int SLA_BREACH_HOURS = 24;
    public static final int SLA_ESCALATE_HOURS = 48;

    private DashboardRules() {
    }

    /** 队列等待时长 → SLA 状态（24h 标红 / 48h 升级） */
    public static String slaStatus(double waitHours) {
        if (waitHours >= SLA_ESCALATE_HOURS) {
            return "ESCALATE";
        }
        if (waitHours >= SLA_BREACH_HOURS) {
            return "BREACH";
        }
        return "OK";
    }

    public static boolean backlogAlert(int pendingCount) {
        return pendingCount > BACKLOG_ALERT_THRESHOLD;
    }

    /** 比率（U91 零分母正确）：total=0 → null（展示层显示 "—"，不产生 0% 假象） */
    public static Double rate(long part, long total) {
        if (total <= 0) {
            return null;
        }
        return Math.round(part * 1000.0 / total) / 10.0; // 百分比，1 位小数
    }

    /** 分位数（审核时长 P50/P90）：空列表 → null；奇偶均按近似插值下取整 */
    public static Double percentile(List<Double> sortedAscending, double p) {
        if (sortedAscending == null || sortedAscending.isEmpty()) {
            return null;
        }
        List<Double> list = new ArrayList<>(sortedAscending);
        Collections.sort(list);
        int idx = (int) Math.ceil(p / 100.0 * list.size()) - 1;
        idx = Math.max(0, Math.min(list.size() - 1, idx));
        return list.get(idx);
    }

    /** 投稿通过率（U91 样例口径） */
    public static Double approvalRate(long approved, long submitted) {
        return rate(approved, submitted);
    }

    /** 提醒完成率 */
    public static Double reminderCompletionRate(long done, long triggered) {
        return rate(done, triggered);
    }

    /** 举报按时率（48h 内结案占比）；未结案全量分母含在 total 内 */
    public static Double reportOnTimeRate(long onTimeResolved, long total) {
        return rate(onTimeResolved, total);
    }

    /** 跨日口径：按自然日分桶（入参为当日计数集合）——供聚合层使用 */
    public static long sumDaily(List<Long> dailyCounts) {
        if (dailyCounts == null || dailyCounts.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (Long c : dailyCounts) {
            if (c != null) {
                sum += c;
            }
        }
        return sum;
    }

    /** 指标校验：出现次数为负或分母负 → 指标无效（U91 防脏数据） */
    public static boolean metricValid(long part, long total) {
        return part >= 0 && total >= 0 && part <= total;
    }
}
