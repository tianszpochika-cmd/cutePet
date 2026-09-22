package com.cutepet.common.web;

/** 无权限（映射 403 / FORBIDDEN）——RBAC 判定失败出口。 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
