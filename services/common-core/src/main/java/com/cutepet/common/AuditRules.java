package com.cutepet.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * 审计查询与脱敏纯规则（T6.3 · 需求-管理端 §2.7 · 决议 IP 必录/隐私脱敏）。
 */
public final class AuditRules {

    /** 查询时间窗上限（防越权大范围拖库） */
    public static final int MAX_QUERY_RANGE_DAYS = 90;

    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(1[3-9]\\d)(\\d{4})(\\d{4})(?!\\d)");
    private static final Pattern EMAIL = Pattern.compile("([A-Za-z0-9._%+-])[A-Za-z0-9._%+-]*@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})");

    private AuditRules() {
    }

    /** 查询区间校验：起 ≤ 止，跨度 ≤90 天 */
    public static boolean queryRangeValid(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return false;
        }
        return ChronoUnit.DAYS.between(from, to) <= MAX_QUERY_RANGE_DAYS;
    }

    /** before/after 值脱敏：手机号保留前3后4、邮箱保留首字符与域名（读写双向适用） */
    public static String redact(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String out = PHONE.matcher(value).replaceAll("$1****$3"); // 分组: 前3 | 中4 | 后4 → 3 组
        out = EMAIL.matcher(out).replaceAll("$1***@$2");
        return out;
    }

    /** IP 存储为必录字段（决议）——空值由调用方拒绝 */
    public static boolean ipRecorded(String ip) {
        return ip != null && !ip.isBlank();
    }

    /** 高危动作判定与 common-core AuditEntry.isHighRisk 同源 */
    public static boolean highRisk(String action) {
        return AuditEntry.isHighRisk(action);
    }

    /** 审计条目可读性输出：operator/action 必填 + 时间已生成 */
    public static boolean readable(AuditEntry entry) {
        return entry != null
                && entry.operator() != null && !entry.operator().isBlank()
                && entry.action() != null && !entry.action().isBlank()
                && entry.timestamp() != null;
    }
}
