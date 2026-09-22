package com.cutepet.explore.domain;

import java.util.List;
import java.util.Optional;

/**
 * 纠错工单纯规则（T4.3 · 总纲状态机③ · U20 关联）。
 */
public final class CorrectionRules {

    public enum State { OPEN, ACCEPTED, REJECTED }

    public static final List<String> FIELDS = List.of("address", "phone", "open_hours", "closed", "attrs");

    private CorrectionRules() {
    }

    public static boolean validField(String field) {
        return FIELDS.contains(field);
    }

    public static boolean transition(State from, State to) {
        return from == State.OPEN && (to == State.ACCEPTED || to == State.REJECTED);
    }

    public static void assertTransition(State from, State to) {
        if (!transition(from, to)) {
            throw new IllegalStateException("纠错状态不可流转: " + from + " → " + to);
        }
    }

    /** 同 POI 同字段已有 OPEN → 合并到该工单（同类合并，处理时一次回写） */
    public static Optional<Long> mergeTarget(List<Long> openIdsByPoiAndField) {
        if (openIdsByPoiAndField == null || openIdsByPoiAndField.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(openIdsByPoiAndField.get(0));
    }

    /** 采纳 → 回写值 = 用户 proposed（写回 POI 字段由服务执行） */
    public static String valueToWriteBack(String proposed) {
        if (proposed == null || proposed.isBlank()) {
            throw new IllegalArgumentException("采纳需有有效修正值");
        }
        return proposed.trim();
    }

    /** 驳回必附理由（双向通知的驳回侧） */
    public static boolean canReject(String note) {
        return note != null && !note.isBlank();
    }

    /** 采纳/驳回均通知贡献者（工单结果回执标识） */
    public static String notificationFor(State result) {
        return switch (result) {
            case ACCEPTED -> "CORRECTION_ACCEPTED_POI_UPDATED";
            case REJECTED -> "CORRECTION_REJECTED_WITH_NOTE";
            default -> "";
        };
    }
}
