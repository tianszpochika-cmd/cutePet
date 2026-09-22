package com.cutepet.common;

import java.time.Instant;

/**
 * common-core 逻辑测试（纯 Java main，无 JUnit 依赖 —— 设计机 javac 即可运行）。
 * 覆盖：统一错误体构造/状态映射、审计条目必录字段与高危判定。
 */
public class CommonLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("✔ " + name);
    }

    public static void main(String[] args) {
        // ApiError
        ApiError e1 = ApiError.of("NOT_FOUND", "资源不存在");
        check("错误体构造", e1.code().equals("NOT_FOUND") && e1.requestId() == null);
        check("状态映射 404", e1.httpStatus() == 404);
        check("requestId 追加", e1.withRequestId("req-1").requestId().equals("req-1"));
        check("未知错误归一 500", ApiError.of("SOMETHING", "x").httpStatus() == 500);
        check("校验错误 422", ApiError.of("VALIDATION_ERROR", "字段错").httpStatus() == 422);

        boolean threw = false;
        try {
            ApiError.of(" ", "x");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("空 code 被拒绝", threw);

        threw = false;
        try {
            ApiError.of("OK", " ");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("空 message 被拒绝", threw);

        // AuditEntry
        AuditEntry a = AuditEntry.of("op1", "ADMIN", "127.0.0.1", "article.takedown",
                "ARTICLE", "a1", "PUBLISHED", "TAKEDOWN");
        check("审计条目可构造", a.timestamp() instanceof Instant);
        check("高危：封禁", AuditEntry.isHighRisk("user.ban"));
        check("高危：权限变更", AuditEntry.isHighRisk("rbac.manage"));
        check("非高危：普通下架", !AuditEntry.isHighRisk("article.takedown"));

        threw = false;
        try {
            AuditEntry.of("op1", "ADMIN", " ", "x", "T", "id", "", "");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("IP 必录（决议）", threw);

        threw = false;
        try {
            AuditEntry.of("", "ADMIN", "1.1.1.1", "x", "T", "id", "", "");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("操作者必录", threw);

        System.out.println("[common-core] " + passed + " assertions passed");
    }
}
