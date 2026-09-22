package com.cutepet.common.web;

/** 业务资源不存在（映射 404 / NOT_FOUND）。 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
