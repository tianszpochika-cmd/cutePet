package com.cutepet.adminops.domain;

import com.cutepet.common.PermissionCatalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RBAC 引擎纯规则（T6.1 + T6.4 ★ · 功能设计-管理端 §6 · U25/U82 关联）。
 * 预置 6 角色与 contracts/permissions.ts 镜像（SQL 种子 V2 同步）。
 */
public final class RbacEngine {

    /** 高危动作（§6.2 双保险：二次确认 + 审计；#31/#32 仅 administrator） */
    public static final List<String> HIGH_RISK_ACTIONS = List.of("user.ban", "rbac.manage", "system.settings");

    private static final Map<String, List<String>> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("editor", List.of(
                "article.create", "article.edit.any", "article.pinned.schedule",
                "taxonomy.manage", "banner.manage", "list.manage", "review.bind.product"));
        PRESETS.put("reviewer", List.of(
                "article.takedown", "review.article", "comment.manage", "report.handle",
                "ugv.review.hide", "route.approve", "user.view", "user.post.right.cancel",
                "dashboard.view.all"));
        PRESETS.put("poiOperator", List.of(
                "poi.create.edit", "poi.close", "correction.handle", "ugv.review.hide",
                "route.approve", "activity.manage", "activity.export"));
        PRESETS.put("productOperator", List.of(
                "product.create.edit", "product.takedown", "product.import.csv",
                "list.manage", "review.bind.product"));
        PRESETS.put("supervisor", PermissionCatalog.ALL.stream()
                .filter(p -> !"rbac.manage".equals(p) && !"system.settings".equals(p)).toList());
        PRESETS.put("administrator", List.copyOf(PermissionCatalog.ALL));
    }

    private RbacEngine() {
    }

    public static Map<String, List<String>> presets() {
        return Map.copyOf(PRESETS);
    }

    public static boolean presetRole(String roleName) {
        return PRESETS.containsKey(roleName);
    }

    public static boolean can(String roleName, String permission) {
        List<String> perms = PRESETS.get(roleName);
        return perms != null && perms.contains(permission);
    }

    /** 自定义角色校验：权限点合法（32 白名单）+ 非空 */
    public static void assertCustomRole(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("自定义角色至少 1 个权限点");
        }
        List<String> invalid = PermissionCatalog.invalidOf(permissions);
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("含非 32 权限点内容: " + invalid);
        }
        // 高危权限允许授予自定义角色由负责人审定；rbac.manage/system.settings 建议仅 administrator（§6.2）
    }

    /** 内置角色不可修改/删除（§6.2） */
    public static boolean editableRole(String roleName, boolean builtin) {
        if ("administrator".equals(roleName)) {
            return false; // 管理员角色永不可改
        }
        return !builtin;
    }

    /** 高危动作需二次确认（UI 层依据） */
    public static boolean requiresDoubleConfirm(String action) {
        return HIGH_RISK_ACTIONS.contains(action);
    }

    /**
     * T6.4 对象级权限：
     * - 封禁类：不得作用于自己（自封禁防护）
     * - 编辑他人内容需 article.edit.any；编辑自己内容 article.create 即可
     * - 投稿处理走 review.article，与对象无关
     */
    public static boolean canActOnTarget(Set<String> granted, String action, long actorId, long targetOwnerId) {
        if ("user.ban".equals(action) && actorId == targetOwnerId) {
            return false; // 不能封禁自己
        }
        if ("article.edit.any".equals(action)) {
            if (actorId == targetOwnerId) {
                return granted.contains("article.create") || granted.contains("article.edit.any");
            }
            return granted.contains("article.edit.any");
        }
        return granted.contains(action);
    }

    /** 看板访问（§6.1 注）：全量权限或任一业务域权限 */
    public static boolean canViewDashboard(Set<String> granted) {
        if (granted.contains("dashboard.view.all")) {
            return true;
        }
        List<String> domain = List.of("article.create", "product.create.edit", "poi.create.edit",
                "review.article", "activity.manage");
        return granted.stream().anyMatch(domain::contains);
    }

    /** T6.4 认证复核：机构认证审核须 activity.manage（与 explore 侧一致） */
    public static boolean canReviewCredential(Set<String> granted) {
        return granted.contains("activity.manage");
    }

    /** T6.4 客服/版权独立工单：分别走 appeal.handle / report.handle（不可互串） */
    public static boolean canHandleTicket(Set<String> granted, String ticketKind) {
        return switch (ticketKind) {
            case "APPEAL", "CUSTOMER_SERVICE" -> granted.contains("appeal.handle");
            case "COPYRIGHT", "REPORT" -> granted.contains("report.handle");
            default -> false;
        };
    }
}
