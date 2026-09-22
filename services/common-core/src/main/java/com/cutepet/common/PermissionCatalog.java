package com.cutepet.common;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 32 权限点目录（与 packages/contracts/src/permissions.ts 对齐的 Java 侧镜像）。
 * 纯 Java，可 javac 逻辑测试；M6 角色管理以本目录校验输入合法性。
 */
public final class PermissionCatalog {

    public static final List<String> ALL = List.of(
            "article.create", "article.publish.direct", "article.edit.any", "article.takedown",
            "article.pinned.schedule", "review.article", "comment.manage", "report.handle",
            "taxonomy.manage", "banner.manage", "product.create.edit", "product.takedown",
            "product.import.csv", "list.manage", "review.bind.product", "poi.create.edit",
            "poi.close", "correction.handle", "ugv.review.hide", "route.approve",
            "activity.manage", "activity.export", "user.view", "user.ban",
            "user.post.right.cancel", "family.dispute", "appeal.handle", "dashboard.view.all",
            "notify.template", "audit.view", "rbac.manage", "system.settings");

    private PermissionCatalog() {
    }

    public static boolean isValid(String permission) {
        return ALL.contains(permission);
    }

    /** 从请求头解析授权集合（dev shim：X-Permissions 逗号分隔；正式为 JWT 声明，本地测试阶段切换） */
    public static Set<String> parseHeader(String header) {
        if (header == null || header.isBlank()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(Arrays.stream(header.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
    }

    public static boolean has(Set<String> granted, String required) {
        return granted.contains(required);
    }

    public static List<String> invalidOf(List<String> candidates) {
        return candidates.stream().filter(p -> !isValid(p)).toList();
    }
}
