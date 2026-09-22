package com.cutepet.pet.domain;

import java.time.Duration;
import java.time.Instant;

/** 宠物档案纯规则（T2.1 · 需求-宠物管理 B1 · U8/U66）。 */
public final class PetRules {

    public static final int SOFT_DELETE_DAYS = 30; // 决议：软删 30 天可恢复

    private PetRules() {
    }

    // 状态：ACTIVE / ARCHIVED / DELETED(软删)
    public static boolean canTransition(String from, String to) {
        return switch (from) {
            case "ACTIVE" -> "ARCHIVED".equals(to) || "DELETED".equals(to);
            case "ARCHIVED" -> "ACTIVE".equals(to) || "DELETED".equals(to);
            case "DELETED" -> "ACTIVE".equals(to); // 仅恢复（30 天内）
            default -> false;
        };
    }

    /** U66：软删 30 天内可恢复，超期不可普通恢复 */
    public static boolean recoverable(Instant deletedAt, Instant now) {
        return deletedAt != null && now.isBefore(deletedAt.plus(Duration.ofDays(SOFT_DELETE_DAYS)));
    }

    /** 归档不等于删除：历史保留、默认列表隐藏 */
    public static boolean inDefaultList(String state) {
        return "ACTIVE".equals(state);
    }

    /** 归档/删除/编辑仅所有者（家庭档位联动在本地测试阶段接 iam 共享表） */
    public static boolean ownerCanModify(boolean actorIsOwner) {
        return actorIsOwner;
    }

    /** 删除/归档需二次确认由 UI 层执行；此处校验目标状态合法 */
    public static boolean validStateTarget(String state) {
        return "ACTIVE".equals(state) || "ARCHIVED".equals(state) || "DELETED".equals(state);
    }

    /** 宠物名：去空白 1–20（与 contracts.validate 一致） */
    public static boolean validName(String name) {
        if (name == null) {
            return false;
        }
        String t = name.trim();
        return t.length() >= 1 && t.length() <= 20;
    }
}
