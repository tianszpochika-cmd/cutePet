package com.cutepet.iam.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 注销与隐私纯规则（T1.4 · U85）。
 * 追溯：待定项决议（冷静期 15 天）、需求-账号 A4、功能设计-账号 §5。
 */
public final class DeletionRules {

    public static final int COOLDOWN_DAYS = 15;

    private DeletionRules() {
    }

    public static Instant cooldownExpiry(Instant requestedAt) {
        return requestedAt.plus(Duration.ofDays(COOLDOWN_DAYS));
    }

    /** 冷静期内可撤销；到期执行 */
    public static boolean canCancel(String state, Instant now, Instant expireAt) {
        return "PENDING".equals(state) && now.isBefore(expireAt);
    }

    public static String stateAt(String state, Instant now, Instant expireAt) {
        if (!"PENDING".equals(state)) {
            return state;
        }
        return now.isAfter(expireAt) || now.equals(expireAt) ? "EXECUTED" : "PENDING";
    }

    /** U85：登录不自动撤销注销——必须显式调用撤销 */
    public static boolean loginCancelsDeletion() {
        return false;
    }

    /** U85：撤销后不自动重建此前的共享/报名（调用方只恢复账号状态，不恢复业务关系） */
    public static List<String> autoRestoredRelationsOnCancel() {
        return new ArrayList<>();
    }

    /** 阻断项：拥有共享中的宠物（所有制在己）必须先转移或解除共享 */
    public static List<String> blockReasons(boolean ownsSharedPets) {
        List<String> reasons = new ArrayList<>();
        if (ownsSharedPets) {
            reasons.add("OWNED_SHARED_PETS_MUST_TRANSFER_OR_UNSHARE");
        }
        return reasons;
    }

    /** UGC 处理策略（决议）：匿名化保留，非删除 */
    public static String ugcPolicyOnExecute() {
        return "ANONYMIZE_AND_KEEP";
    }

    /** U66 关联：软删 30 天窗口（宠物侧规则，归档在 pet 域复用） */
    public static final int PET_SOFT_DELETE_DAYS = 30;

    public static boolean petRecoverable(Instant deletedAt, Instant now) {
        return now.isBefore(deletedAt.plus(Duration.ofDays(PET_SOFT_DELETE_DAYS)));
    }
}
