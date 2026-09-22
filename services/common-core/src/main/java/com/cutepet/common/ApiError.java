package com.cutepet.common;

/**
 * 统一错误体（纯 Java，无 Spring 依赖 —— 设计机可用 javac 直接逻辑测试）。
 * 网关与各服务的错误响应必须收敛到本结构（规划 T0.3 验收：错误统一格式）。
 */
public record ApiError(String code, String message, String requestId) {

    public ApiError {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code 不可为空");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message 不可为空");
        }
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public ApiError withRequestId(String id) {
        return new ApiError(code, message, id);
    }

    /** HTTP 状态映射：未知错误按 500 归一 */
    public int httpStatus() {
        return switch (code) {
            case "UNAUTHORIZED" -> 401;
            case "FORBIDDEN" -> 403;
            case "NOT_FOUND" -> 404;
            case "CONFLICT" -> 409;
            case "VALIDATION_ERROR" -> 422;
            case "RATE_LIMITED" -> 429;
            default -> 500;
        };
    }
}
