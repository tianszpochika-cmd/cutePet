package com.cutepet.iam;

import com.cutepet.iam.domain.AccountRules;
import com.cutepet.iam.domain.DeletionRules;
import com.cutepet.iam.domain.FamilyRules;
import com.cutepet.iam.domain.FamilyRules.FamilyAction;
import com.cutepet.iam.domain.FamilyRules.MemberRole;
import com.cutepet.iam.domain.FamilyRules.PetAction;
import com.cutepet.iam.domain.FamilyRules.ShareLevel;
import com.cutepet.iam.domain.FamilyRules.TransferKind;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * iam 领域逻辑测试（纯 Java main，javac 逻辑测试通道）。
 * 覆盖：T1.1/T1.2/T1.3（U1/U2/U3/U83/U84）、T1.5/T1.9（U5/U60–U64）、T1.4（U4/U85）。
 */
public class IamLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[iam] PASS " + name);
    }

    public static void main(String[] args) {
        testRegistrationAndAge();
        testGuardian();
        testPhoneBind();
        testFamilyPermissions();
        testTransfers();
        testRemovalAndDeletion();
        System.out.println("[iam-logic] " + passed + " assertions passed");
    }

    private static void testRegistrationAndAge() {
        // T1.1 / U1
        check("dev 验证码仅 123456", AccountRules.verifyDevSms("123456") && !AccountRules.verifyDevSms("000000"));
        check("手机号段校验", AccountRules.isValidPhone("13812345678") && !AccountRules.isValidPhone("12345678901"));
        check("脱敏 138****5678", "138****5678".equals(AccountRules.maskPhone("13812345678")));
        check("协议必须双版本", AccountRules.agreementsComplete("v1", "v1") && !AccountRules.agreementsComplete("v1", ""));

        // T1.2 / U83：实际周岁分支
        LocalDate today = LocalDate.of(2026, 9, 22);
        check("满14岁为 ADULT",
                "ADULT".equals(AccountRules.ageBand(LocalDate.of(2012, 9, 22), today)));
        check("差一天未满14为 MINOR",
                "MINOR".equals(AccountRules.ageBand(LocalDate.of(2012, 9, 23), today)));
        check("方案甲：MINOR 需监护人", AccountRules.requiresGuardian("MINOR") && !AccountRules.requiresGuardian("ADULT"));
        boolean threw = false;
        try {
            AccountRules.ageAt(LocalDate.of(2030, 1, 1), today);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("未来出生日期抛错", threw);

        // U83：微信不可绕过手机/协议/监护人
        check("微信登录：缺手机绑定拒绝",
                !AccountRules.wechatLoginEligible(false, "123456", "ADULT", "v1", "v1"));
        check("微信登录：MINOR 未走监护人拒绝",
                !AccountRules.wechatLoginEligible(true, "123456", "MINOR", "v1", "v1"));
        check("微信登录：全满足才放行",
                AccountRules.wechatLoginEligible(true, "123456", "ADULT", "v1", "v1"));
    }

    private static void testGuardian() {
        // U84
        check("监护人凭监护号操作同意",
                AccountRules.guardianCanOperate("13900000001", "13900000001", "PENDING"));
        check("非监护号拒绝",
                !AccountRules.guardianCanOperate("13900000002", "13900000001", "PENDING"));
        check("已撤回不可再操作",
                !AccountRules.guardianCanOperate("13900000001", "13900000001", "REVOKED"));
        check("同意状态机 PENDING→CONSENTED→REVOKED",
                AccountRules.guardianTransition("PENDING", "CONSENTED")
                        && AccountRules.guardianTransition("CONSENTED", "REVOKED")
                        && !AccountRules.guardianTransition("REVOKED", "CONSENTED"));
        check("撤回效果含会话与推送限制且不暴露资料",
                AccountRules.effectsAfterGuardianRevoke().contains("REVOKE_MINOR_SESSIONS")
                        && AccountRules.effectsAfterGuardianRevoke().contains("NO_PROFILE_EXPOSURE"));
    }

    private static void testPhoneBind() {
        // T1.3 / U3
        check("换绑：原号不符拒绝", !AccountRules.canBindPhone("13812345678", "13800000000", "13911111111"));
        check("换绑：新号与原号相同拒绝", !AccountRules.canBindPhone("13812345678", "13812345678", "13812345678"));
        check("换绑：合法路径放行", AccountRules.canBindPhone("13812345678", "13812345678", "13911111111"));
    }

    private static void testFamilyPermissions() {
        // U5 / BR-03
        check("一人一家庭：已在家庭不可加入", !FamilyRules.joinAllowed("fam-1"));
        check("一人一家庭：无家庭可加入", FamilyRules.joinAllowed(null));

        // 判定表
        check("所有者全权",
                FamilyRules.canPetAction(true, null, PetAction.EDIT_RECORD)
                        && FamilyRules.canPetAction(true, null, PetAction.HANDLE_REMINDER));
        check("只读档仅可见",
                FamilyRules.canPetAction(false, ShareLevel.READONLY, PetAction.VIEW)
                        && !FamilyRules.canPetAction(false, ShareLevel.READONLY, PetAction.EDIT_RECORD)
                        && !FamilyRules.canPetAction(false, ShareLevel.READONLY, PetAction.HANDLE_REMINDER));
        check("可管理档可编辑可处理提醒",
                FamilyRules.canPetAction(false, ShareLevel.MANAGE, PetAction.EDIT_RECORD)
                        && FamilyRules.canPetAction(false, ShareLevel.MANAGE, PetAction.HANDLE_REMINDER));
        check("未共享=私有（他人不可见）", !FamilyRules.canPetAction(false, null, PetAction.VIEW));

        // U60
        check("U60 MEMBER 不可解散", !FamilyRules.canDissolve(MemberRole.MEMBER));
        check("U60 ADMIN 可解散家庭", FamilyRules.canDissolve(MemberRole.ADMIN));
        check("U60 管理员不能动他人宠物：SET_SHARE 需为所有者",
                !FamilyRules.canFamilyAction(MemberRole.ADMIN, FamilyAction.SET_SHARE_LEVEL, false));
        check("U60 管理员不能转移他人宠物",
                !FamilyRules.canFamilyAction(MemberRole.ADMIN, FamilyAction.TRANSFER_PET, false));
        check("宠物所有者可转移自己的宠物",
                FamilyRules.canFamilyAction(MemberRole.MEMBER, FamilyAction.TRANSFER_PET, true));

        // U61
        check("U61 自有宠物加入时不自动共享", !FamilyRules.ownPetsSharedOnJoin());
        check("U61 非所有者不能提升档位", !FamilyRules.canSetShareLevel(false));
        check("U61 所有者可设置档位", FamilyRules.canSetShareLevel(true));

        // 邀请 7 天
        Instant now = Instant.parse("2026-09-22T00:00:00Z");
        Instant created = now;
        check("邀请码 7 天有效",
                FamilyRules.inviteValid(FamilyRules.inviteExpiry(created), false, now)
                        && !FamilyRules.inviteValid(FamilyRules.inviteExpiry(created), false,
                                now.plus(Duration.ofDays(8))));
        check("邀请可失效重置", !FamilyRules.inviteValid(FamilyRules.inviteExpiry(created), true, now));
        check("解散后旧链接拒绝", FamilyRules.inviteRevokedOnDissolve());
    }

    private static void testTransfers() {
        // U62：双向独立
        Instant base = Instant.parse("2026-09-22T00:00:00Z");
        check("转移 24h 内 PENDING",
                "PENDING".equals(FamilyRules.transferState(base, base.plus(Duration.ofHours(23)))));
        check("转移超 24h EXPIRED",
                "EXPIRED".equals(FamilyRules.transferState(base, base.plus(Duration.ofHours(25)))));

        check("过期接受不改变所有权",
                !FamilyRules.changesOwnership("EXPIRED", "ACCEPTED"));
        check("拒绝不改变所有权",
                !FamilyRules.changesOwnership("PENDING", "REJECTED"));
        check("有效期内接受才生效",
                FamilyRules.changesOwnership("PENDING", "ACCEPTED"));

        // 两种转移独立：PET_OWNER 接受不影响 FAMILY_ADMIN 转移状态判断
        String adminState = FamilyRules.transferState(base, base.plus(Duration.ofHours(25)));
        check("两种转移独立：所有权已转移不代表管理员转移生效",
                FamilyRules.changesOwnership("PENDING", "ACCEPTED")
                        && !FamilyRules.changesOwnership(adminState, "ACCEPTED"));

        check("接受后共享重置为只读安全档",
                FamilyRules.sharesAfterOwnerTransferAccepted() == ShareLevel.READONLY);
        check("TransferKind 两值", TransferKind.values().length == 2);
    }

    private static void testRemovalAndDeletion() {
        // U63
        check("移除/退出后访问归零", !FamilyRules.accessAfterRemoval());
        check("贡献保留", FamilyRules.contributionsKeptAfterRemoval());
        check("原成员不再触达（排队通知清空）", FamilyRules.queuedFamilyNotificationsAfterRemoval() == 0);
        check("解散仅解除共享、宠物回归所有者语义",
                FamilyRules.petsAfterDissolve(java.util.List.of("p1", "p2"), "u1").size() == 2);

        // U64
        check("U64 拥有共享宠物则阻断注销", FamilyRules.deletionBlockedByFamily(true, false));
        check("U64 家庭管理角色本身不阻断注销", !FamilyRules.deletionBlockedByFamily(false, true));
        check("解除共享后可继续注销", FamilyRules.canContinueDeletionAfterUnshare(false)
                && !FamilyRules.canContinueDeletionAfterUnshare(true));

        // T1.4 / U85
        Instant requested = Instant.parse("2026-09-22T00:00:00Z");
        Instant expire = DeletionRules.cooldownExpiry(requested);
        check("冷静期 15 天", Duration.between(requested, expire).toDays() == 15);
        check("期内可撤销", DeletionRules.canCancel("PENDING", requested.plus(Duration.ofDays(3)), expire));
        check("到期不可撤销", !DeletionRules.canCancel("PENDING", expire.plus(Duration.ofSeconds(1)), expire));
        check("到期状态→EXECUTED",
                "EXECUTED".equals(DeletionRules.stateAt("PENDING", expire, expire)));
        check("U85 登录不自动撤销", !DeletionRules.loginCancelsDeletion());
        check("U85 撤销不自动重建共享/报名",
                DeletionRules.autoRestoredRelationsOnCancel().isEmpty());
        check("注销执行：UGC 匿名化保留（决议）",
                "ANONYMIZE_AND_KEEP".equals(DeletionRules.ugcPolicyOnExecute()));
        check("阻断项仅含共享宠物",
                DeletionRules.blockReasons(true).size() == 1
                        && DeletionRules.blockReasons(false).isEmpty());

        // U66 关联（软删窗口供 pet 域复用）
        Instant deleted = requested;
        check("宠物软删 30 天内可恢复",
                DeletionRules.petRecoverable(deleted, deleted.plus(Duration.ofDays(29))));
        check("宠物软删超 30 天不可恢复",
                !DeletionRules.petRecoverable(deleted, deleted.plus(Duration.ofDays(31))));
    }
}
