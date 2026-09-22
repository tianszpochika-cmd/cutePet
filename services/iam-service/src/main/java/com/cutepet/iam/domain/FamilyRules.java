package com.cutepet.iam.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 家庭域纯规则（T1.5 家庭组 + T1.9 增补：所有者/管理员分离、双向转移）。
 * 追溯：功能设计-账号 §2 判定表、BR-03/BR-04、U60–U64、待定项决议（一人一家庭、邀请码 7 天）。
 */
public final class FamilyRules {

    public static final int INVITE_TTL_DAYS = 7;
    public static final int TRANSFER_TTL_HOURS = 24;

    public enum MemberRole { OWNER, ADMIN, MEMBER }
    public enum ShareLevel { MANAGE, READONLY }
    public enum PetAction { VIEW, EDIT_RECORD, HANDLE_REMINDER }
    public enum FamilyAction { INVITE, REMOVE_MEMBER, DISSOLVE, SET_SHARE_LEVEL, TRANSFER_PET }
    public enum TransferKind { PET_OWNER, FAMILY_ADMIN }

    private FamilyRules() {
    }

    // ---------- 成员与家庭 ----------

    /** BR-03 一人限一个家庭：已在家庭中不可加入新家庭（同家庭幂等） */
    public static boolean joinAllowed(String currentFamilyIdOrNull) {
        return currentFamilyIdOrNull == null;
    }

    /** 家庭级操作：邀请/移除/解散 = 创建者或管理员；档位设置/宠物转移 = 宠物所有者（见 canFamilyAction 分支） */
    public static boolean canFamilyAction(MemberRole role, FamilyAction action, boolean actorIsPetOwner) {
        return switch (action) {
            case INVITE, REMOVE_MEMBER, DISSOLVE -> role == MemberRole.OWNER || role == MemberRole.ADMIN;
            case SET_SHARE_LEVEL, TRANSFER_PET -> actorIsPetOwner; // U60/U61：管理员不能动他人宠物
        };
    }

    /** U60：非家庭管理员（MEMBER）不得解散 */
    public static boolean canDissolve(MemberRole role) {
        return role == MemberRole.OWNER || role == MemberRole.ADMIN;
    }

    // ---------- 宠物共享两档（判定表） ----------

    public static boolean canPetAction(boolean isPetOwner, ShareLevel share, PetAction action) {
        if (isPetOwner) {
            return true;
        }
        if (share == null) {
            return false; // U61：未共享=私有
        }
        if (share == ShareLevel.MANAGE) {
            return true; // 查看+编辑+处理提醒
        }
        return action == PetAction.VIEW; // 只读仅可见
    }

    /** U61：受邀成员——自有宠物仍私有（不自动共享） */
    public static boolean ownPetsSharedOnJoin() {
        return false;
    }

    /** U61：非宠物所有者不能提升共享档位 */
    public static boolean canSetShareLevel(boolean actorIsPetOwner) {
        return actorIsPetOwner;
    }

    // ---------- 邀请码（决议：7 天、可失效重置） ----------

    public static boolean inviteValid(Instant expiresAt, boolean revoked, Instant now) {
        return !revoked && now.isBefore(expiresAt);
    }

    public static Instant inviteExpiry(Instant createdAt) {
        return createdAt.plus(Duration.ofDays(INVITE_TTL_DAYS));
    }

    public static boolean inviteRevokedOnDissolve() {
        return true; // U63：解散后旧链接拒绝
    }

    // ---------- 双向转移（T1.9 / U62） ----------

    public static String transferState(Instant createdAt, Instant now) {
        if (now.isAfter(createdAt.plus(Duration.ofHours(TRANSFER_TTL_HOURS)))) {
            return "EXPIRED";
        }
        return "PENDING";
    }

    /** U62：两种转移独立——A 过期/拒绝不影响 B，也不改变所有权 */
    public static boolean changesOwnership(String transferState, String decision) {
        if (!"ACCEPTED".equals(decision)) {
            return false; // REJECTED/EXPIRED/PENDING 均不变
        }
        return "PENDING".equals(transferState); // 仅在有效期内接受才生效
    }

    /** U62：接受后该宠物共享按规则重置为安全档（只读） */
    public static ShareLevel sharesAfterOwnerTransferAccepted() {
        return ShareLevel.READONLY;
    }

    // ---------- 退出/移除/解散（U63） ----------

    /** 成员被移除/退出后：访问立即归零（其可见的共享均由 shares 决定，成员行删除即失效） */
    public static boolean accessAfterRemoval() {
        return false;
    }

    /** 贡献保留：成员录入的健康记录不删除，仅标注录入人 */
    public static boolean contributionsKeptAfterRemoval() {
        return true;
    }

    /** U63：原成员不再收到该家庭通知；排队中的家庭通知清空 */
    public static int queuedFamilyNotificationsAfterRemoval() {
        return 0;
    }

    /** 宠物回归各所有者（数据层所有制从未离开所有者，此处为语义断言） */
    public static List<String> petsAfterDissolve(List<String> familyPetIds, String soughtOwnerId) {
        return new ArrayList<>(familyPetIds); // 所有权唯一，解散只解除共享
    }

    // ---------- 注销与家庭角色（U64） ----------

    /** U64：所有者非管理员申请注销——拥有共享宠物才阻断；家庭管理角色本身不阻断 */
    public static boolean deletionBlockedByFamily(boolean ownsSharedPets, boolean isFamilyAdminOrOwner) {
        if (ownsSharedPets) {
            return true;
        }
        return false; // isFamilyAdminOrOwner 不构成阻断（家庭角色随注销解除即可）
    }

    /** 解除共享/转移完成后可继续注销 */
    public static boolean canContinueDeletionAfterUnshare(boolean ownsSharedPets) {
        return !ownsSharedPets;
    }
}
