package com.cutepet.common;

import java.time.Instant;

/**
 * 审计条目（纯 Java）。追溯：需求-管理端 §2.7 / 功能设计-管理端 §8。
 * 字段口径：操作者、IP、动作、对象、前后值摘要（IP 为已决议必录项）。
 */
public record AuditEntry(
        String operator,
        String operatorRole,
        String ip,
        String action,
        String targetType,
        String targetId,
        String before,
        String after,
        Instant timestamp) {

    public AuditEntry {
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator 不可为空");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action 不可为空");
        }
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP 必录（决议 2026-09-22）");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static AuditEntry of(String operator, String role, String ip, String action,
                                String targetType, String targetId, String before, String after) {
        return new AuditEntry(operator, role, ip, action, targetType, targetId, before, after, Instant.now());
    }

    /** 高危操作（封禁/权限变更/物理删除）需二次确认 —— 这里提供清单供调用方统一判定 */
    public static boolean isHighRisk(String action) {
        return action != null && (action.startsWith("user.ban")
                || action.startsWith("rbac.")
                || action.startsWith("system.")
                || action.equals("physical.delete"));
    }
}
