package com.cutepet.notification;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.common.web.ForbiddenException;
import com.cutepet.notification.domain.MessageRules.Category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** notification 控制器（契约 /messages/** 与 /admin/notify/**；dev shim 身份/权限头同 admin-ops）。 */
@RestController
class NotificationControllers {

    private final MessageService messages;
    private final TemplateService templates;

    NotificationControllers(MessageService messages, TemplateService templates) {
        this.messages = messages;
        this.templates = templates;
    }

    private long userOf(String header) {
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("缺少用户身份（X-User-Id）");
        }
        return Long.parseLong(header.trim());
    }

    public record TemplateReq(String key, String channel, String titleTpl, String bodyTpl) {
    }

    @GetMapping("/messages")
    public Map<String, Object> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state) {
        return messages.list(userOf(userId), category, state);
    }

    @PostMapping("/messages/{id}/read")
    public Map<String, Object> read(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @org.springframework.web.bind.annotation.PathVariable long id) {
        return messages.read(userOf(userId), id);
    }

    @PostMapping("/messages/read-all")
    public Map<String, Object> readAll(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return messages.readAll(userOf(userId));
    }

    @PostMapping("/admin/notify/templates")
    public Map<String, Object> upsertTemplate(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody TemplateReq req) {
        if (!PermissionCatalog.parseHeader(perms).contains("notify.template")) {
            throw new ForbiddenException("缺少权限: notify.template");
        }
        return templates.upsert(req.key(), req.channel(), req.titleTpl(), req.bodyTpl());
    }

    @GetMapping("/admin/notify/templates")
    public List<NotificationTemplateEntity> listTemplates(
            @RequestHeader(value = "X-Permissions", required = false) String perms) {
        if (!PermissionCatalog.parseHeader(perms).contains("notify.template")) {
            throw new ForbiddenException("缺少权限: notify.template");
        }
        return templates.list();
    }

    /** 内部发布入口（供 pet/iam 联通后调用的 HTTP 等价物；当前由调度器与服务内直接使用） */
    static Category parseCategory(String value) {
        return Category.valueOf(value == null ? "SYSTEM" : value);
    }
}
