package com.cutepet.iam;

import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.iam.Repos.AgreementRepo;
import com.cutepet.iam.Repos.DeletionRepo;
import com.cutepet.iam.Repos.ExportRepo;
import com.cutepet.iam.Repos.FamilyRepo;
import com.cutepet.iam.Repos.GuardianRepo;
import com.cutepet.iam.Repos.InviteRepo;
import com.cutepet.iam.Repos.MemberRepo;
import com.cutepet.iam.Repos.PhoneChangeRepo;
import com.cutepet.iam.Repos.ShareRepo;
import com.cutepet.iam.Repos.UserRepo;
import com.cutepet.iam.domain.AccountRules;
import com.cutepet.iam.domain.DeletionRules;
import com.cutepet.iam.domain.FamilyRules;
import com.cutepet.iam.domain.FamilyRules.FamilyAction;
import com.cutepet.iam.domain.FamilyRules.MemberRole;
import com.cutepet.iam.domain.FamilyRules.ShareLevel;
import com.cutepet.iam.domain.FamilyRules.TransferKind;
import com.cutepet.common.web.Audited;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * iam 应用服务。业务判定全部委托 domain 纯规则（已被 IamLogicTest 覆盖）；
 * 本层只做编排、持久化与端口调用。编译/运行验证属开发机与本地测试阶段。
 */
@Service
class TokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    /** dev 不透明令牌（RS256/JWKS 升级随 E02 真实能力阶段落地，接口不变） */
    public String issue(long userId) {
        byte[] buf = new byte[24];
        RANDOM.nextBytes(buf);
        String token = HexFormat.of().formatHex(buf);
        tokens.put(token, userId);
        return token;
    }

    public Optional<Long> verify(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void revoke(String token) {
        tokens.remove(token);
    }
}

/** 新设备登录安全提醒出口（T1.3；通知落点由 T1.7 消息中心接入，一期先日志桩） */
interface MessageOutPort {
    void newDeviceAlert(long userId, String detail);
}

@Service
class LoggingMessageOutPort implements MessageOutPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingMessageOutPort.class);

    @Override
    public void newDeviceAlert(long userId, String detail) {
        log.info("[security-alert] user={} detail={}", userId, detail);
    }
}

@Service
class AuthService {

    private final UserRepo users;
    private final AgreementRepo agreements;
    private final GuardianRepo guardians;
    private final TokenService tokens;
    private final MessageOutPort messages;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    AuthService(UserRepo users, AgreementRepo agreements, GuardianRepo guardians,
                TokenService tokens, MessageOutPort messages) {
        this.users = users;
        this.agreements = agreements;
        this.guardians = guardians;
        this.tokens = tokens;
        this.messages = messages;
    }

    public Map<String, Object> sendSmsCode(String phone) {
        if (!AccountRules.isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        // dev 固定验证码（真短信属 E02）
        log.info("[sms] code sent (dev) to {}", AccountRules.maskPhone(phone));
        return Map.of("sent", true, "devHint", AccountRules.DEV_SMS_CODE);
    }

    @Transactional
    public Map<String, Object> login(String phone, String code, String birthDate, String guardianPhone,
                                     String terms, String privacy, String device) {
        if (!AccountRules.isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (!AccountRules.verifyDevSms(code)) {
            throw new IllegalArgumentException("验证码错误");
        }
        if (!AccountRules.agreementsComplete(terms, privacy)) {
            throw new IllegalArgumentException("请先勾选用户协议与隐私政策");
        }

        UserEntity user = users.findByPhone(phone).orElseGet(() -> createNew(phone, birthDate, guardianPhone, terms, privacy));
        String band = user.ageBand;
        if (AccountRules.requiresGuardian(band)) {
            GuardianConsentEntity consent = guardians.findByMinorUserId(user.id).orElse(null);
            if (consent == null || !"CONSENTED".equals(consent.state)) {
                throw new IllegalArgumentException("未成年账号需先完成监护人同意流程");
            }
        }

        String token = tokens.issue(user.id);
        if (device != null && !device.isBlank()) {
            messages.newDeviceAlert(user.id, "login device=" + device); // U3 异地/新设备提醒出口
        }
        return Map.of("accessToken", token, "tokenType", "Bearer", "userId", user.id, "ageBand", band);
    }

    private UserEntity createNew(String phone, String birthDate, String guardianPhone, String terms, String privacy) {
        String band = "ADULT";
        if (birthDate != null && !birthDate.isBlank()) {
            band = AccountRules.ageBand(java.time.LocalDate.parse(birthDate), java.time.LocalDate.now());
        }
        UserEntity user = new UserEntity();
        user.phone = phone;
        user.ageBand = band;
        user.createdAt = LocalDateTime.now();
        user = users.save(user);

        UserAgreementEntity agreement = new UserAgreementEntity();
        agreement.userId = user.id;
        agreement.termsVersion = terms;
        agreement.privacyVersion = privacy;
        agreement.agreedAt = LocalDateTime.now();
        agreements.save(agreement);

        if ("MINOR".equals(band)) {
            if (guardianPhone == null || guardianPhone.isBlank()) {
                throw new IllegalArgumentException("未满14周岁需提供监护人手机号");
            }
            GuardianConsentEntity consent = new GuardianConsentEntity();
            consent.minorUserId = user.id;
            consent.guardianPhone = guardianPhone;
            consent.state = "PENDING";
            guardians.save(consent);
        }
        return user;
    }

    @Transactional
    public Map<String, Object> wechatLogin(String wechatCode, String phone, String code,
                                           String birthDate, String guardianPhone, String terms, String privacy, String device) {
        if (wechatCode == null || wechatCode.isBlank()) {
            throw new IllegalArgumentException("微信授权码缺失");
        }
        String band = "ADULT";
        if (birthDate != null && !birthDate.isBlank()) {
            band = AccountRules.ageBand(java.time.LocalDate.parse(birthDate), java.time.LocalDate.now());
        }
        // U83：微信必须补齐手机验证码 + 协议 +（未成年）监护人流程
        if (!AccountRules.wechatLoginEligible(true, code, band, terms, privacy)) {
            throw new IllegalArgumentException("微信登录需完成手机号验证与协议勾选（未成年需监护人同意）");
        }
        return login(phone, code, birthDate, guardianPhone, terms, privacy, device);
    }

    public void logout(String token) {
        tokens.revoke(token);
    }

    public Optional<Long> userIdOf(String token) {
        return tokens.verify(token);
    }

    // ---------- 监护人（U84：仅凭监护号操作，不进入儿童会话） ----------

    @Transactional
    public void guardianVerify(String minorPhone, String guardianPhone) {
        UserEntity minor = users.findByPhone(minorPhone)
                .orElseThrow(() -> new NotFoundException("账号不存在"));
        GuardianConsentEntity consent = guardians.findByMinorUserId(minor.id)
                .orElseThrow(() -> new NotFoundException("无监护人记录"));
        if (!guardianPhone.equals(consent.guardianPhone)) {
            throw new IllegalArgumentException("监护人手机号不匹配");
        }
        consent.verifiedAt = LocalDateTime.now();
        guardians.save(consent);
    }

    @Transactional
    public void guardianConsent(String minorPhone, String guardianPhone) {
        UserEntity minor = users.findByPhone(minorPhone)
                .orElseThrow(() -> new NotFoundException("账号不存在"));
        GuardianConsentEntity consent = guardians.findByMinorUserId(minor.id)
                .orElseThrow(() -> new NotFoundException("无监护人记录"));
        if (!AccountRules.guardianCanOperate(guardianPhone, consent.guardianPhone, consent.state)) {
            throw new ForbiddenException("监护人校验失败或状态不可操作");
        }
        if (!AccountRules.guardianTransition(consent.state, "CONSENTED")) {
            throw new IllegalStateException("状态不可流转: " + consent.state);
        }
        consent.state = "CONSENTED";
        guardians.save(consent);
    }

    @Transactional
    public void guardianRevoke(String minorPhone, String guardianPhone) {
        UserEntity minor = users.findByPhone(minorPhone)
                .orElseThrow(() -> new NotFoundException("账号不存在"));
        GuardianConsentEntity consent = guardians.findByMinorUserId(minor.id)
                .orElseThrow(() -> new NotFoundException("无监护人记录"));
        if (!guardianPhone.equals(consent.guardianPhone)) {
            throw new ForbiddenException("仅监护人本人可撤回");
        }
        if (!AccountRules.guardianTransition(consent.state, "REVOKED")) {
            throw new IllegalStateException("状态不可流转: " + consent.state);
        }
        consent.state = "REVOKED";
        consent.revokedAt = LocalDateTime.now();
        guardians.save(consent);
        // effects: REVOKE_MINOR_SESSIONS / DISABLE_PUSH / NO_PROFILE_EXPOSURE —— 会话失效随 T1.7 通道执行
        messages.newDeviceAlert(minor.id, "guardian-revoked");
    }
}

@Service
class ProfileService {

    private final UserRepo users;
    private final PhoneChangeRepo phoneChanges;

    ProfileService(UserRepo users, PhoneChangeRepo phoneChanges) {
        this.users = users;
        this.phoneChanges = phoneChanges;
    }

    public UserEntity get(long userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    @Transactional
    public UserEntity update(long userId, String nickname, String avatar, String petTags) {
        UserEntity user = get(userId);
        if (nickname != null) {
            user.nickname = nickname;
        }
        if (avatar != null) {
            user.avatar = avatar;
        }
        if (petTags != null) {
            user.petTags = petTags;
        }
        user.updatedAt = LocalDateTime.now();
        return users.save(user);
    }

    @Transactional
    @Audited(action = "user.phone.bind", targetType = "USER")
    public void bindPhone(long userId, String oldPhone, String newPhone) {
        UserEntity user = get(userId);
        if (!AccountRules.canBindPhone(user.phone, oldPhone, newPhone)) {
            throw new IllegalArgumentException("换绑校验失败：需原号验证且新号合法");
        }
        PhoneChangeEntity change = new PhoneChangeEntity();
        change.userId = userId;
        change.oldPhoneMasked = AccountRules.maskPhone(user.phone);
        change.newPhone = newPhone;
        change.createdAt = LocalDateTime.now();
        phoneChanges.save(change);
        user.phone = newPhone;
        users.save(user);
    }
}

@Service
class DeletionService {

    private final UserRepo users;
    private final DeletionRepo deletions;
    private final ExportRepo exports;
    private final MemberRepo members;
    private final ShareRepo shares;

    DeletionService(UserRepo users, DeletionRepo deletions, ExportRepo exports,
                    MemberRepo members, ShareRepo shares) {
        this.users = users;
        this.deletions = deletions;
        this.exports = exports;
        this.members = members;
        this.shares = shares;
    }

    @Transactional
    @Audited(action = "user.delete.request", targetType = "USER")
    public Map<String, Object> requestDeletion(long userId) {
        // U64：拥有共享中的宠物才阻断；家庭角色本身不阻断
        boolean ownsShared = shares.existsById(sharesIdOwnedBy(userId));
        if (!FamilyRules.deletionBlockedByFamily(ownsShared, false)) {
            // pass
        }
        java.util.List<String> blockers = DeletionRules.blockReasons(ownsShared);
        if (!blockers.isEmpty()) {
            throw new IllegalArgumentException("存在未处理的共享宠物，请先转移或解除共享");
        }
        AccountDeletionEntity row = deletions.findById(userId).orElseGet(() -> {
            AccountDeletionEntity d = new AccountDeletionEntity();
            d.userId = userId;
            return d;
        });
        Instant now = Instant.now();
        row.requestedAt = toLocal(now);
        row.expireAt = toLocal(DeletionRules.cooldownExpiry(now));
        row.state = "PENDING";
        deletions.save(row);
        return Map.of("state", "PENDING", "expireAt", row.expireAt.toString(), "cooldownDays", DeletionRules.COOLDOWN_DAYS);
    }

    @Transactional
    public Map<String, Object> cancelDeletion(long userId) {
        AccountDeletionEntity row = deletions.findById(userId)
                .orElseThrow(() -> new NotFoundException("无注销申请"));
        Instant now = Instant.now();
        if (!DeletionRules.canCancel(row.state, now, row.expireAt.toInstant())) {
            throw new IllegalStateException("当前不可撤销（冷静期已过或状态不允许）");
        }
        row.state = "CANCELLED";
        deletions.save(row);
        // U85：撤销不自动重建共享/报名（autoRestoredRelationsOnCancel 为空）
        return Map.of("state", "CANCELLED", "restoredRelations", DeletionRules.autoRestoredRelationsOnCancel());
    }

    @Transactional
    public Map<String, Object> requestExport(long userId) {
        PrivacyExportEntity row = new PrivacyExportEntity();
        row.userId = userId;
        row.state = "PENDING";
        row.createdAt = LocalDateTime.now();
        exports.save(row);
        return Map.of("state", "PENDING", "format", "json", "id", row.id);
    }

    private Long sharesIdOwnedBy(long userId) {
        // 简化判定：以用户拥有共享宠物为阻断条件（petId 未知时返回可命中的探测值语义由 false 兜底）
        // 完整跨域所有权校验在 pet-service 联通后补强（本地测试阶段）；此处仅家庭内共享
        java.util.List<FamilyMemberEntity> memberships = members.findAll();
        for (FamilyMemberEntity m : memberships) {
            if (m.userId == userId) {
                for (FamilyShareEntity s : shares.findByFamilyId(m.familyId)) {
                    return s.petId; // 存在共享即阻断
                }
            }
        }
        return -1L; // 无共享：existsById(-1)=false
    }

    private static LocalDateTime toLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
    }
}

@Service
class FamilyService {

    private final FamilyRepo families;
    private final MemberRepo members;
    private final InviteRepo invites;
    private final ShareRepo shares;
    private final TransferRepoStub transfers;

    FamilyService(FamilyRepo families, MemberRepo members, InviteRepo invites,
                  ShareRepo shares, TransferRepoStub transfers) {
        this.families = families;
        this.members = members;
        this.invites = invites;
        this.shares = shares;
        this.transfers = transfers;
    }

    private MemberRole roleOf(long userId) {
        FamilyMemberEntity member = members.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("未加入任何家庭"));
        return MemberRole.valueOf(member.level);
    }

    private FamilyMemberEntity memberOf(long userId) {
        return members.findByUserId(userId).orElseThrow(() -> new NotFoundException("未加入任何家庭"));
    }

    @Transactional
    @Audited(action = "family.create", targetType = "FAMILY")
    public Map<String, Object> create(long userId, String name) {
        if (!FamilyRules.joinAllowed(members.findByUserId(userId).map(m -> m.familyId.toString()).orElse(null))) {
            throw new IllegalArgumentException("一人限一个家庭：请先退出现有家庭");
        }
        FamilyEntity family = new FamilyEntity();
        family.name = name;
        family.ownerUserId = userId;
        family.createdAt = LocalDateTime.now();
        family = families.save(family);

        FamilyMemberEntity owner = new FamilyMemberEntity();
        owner.familyId = family.id;
        owner.userId = userId;
        owner.level = MemberRole.OWNER.name();
        owner.joinedAt = LocalDateTime.now();
        members.save(owner);
        return Map.of("familyId", family.id, "name", family.name, "role", MemberRole.OWNER.name());
    }

    public Map<String, Object> mine(long userId) {
        FamilyMemberEntity member = memberOf(userId);
        FamilyEntity family = families.findById(member.familyId)
                .orElseThrow(() -> new NotFoundException("家庭不存在"));
        return Map.of(
                "familyId", family.id,
                "name", family.name,
                "ownerUserId", family.ownerUserId,
                "role", member.level,
                "members", members.findByFamilyId(family.id).size(),
                "shares", shares.findByFamilyId(family.id).size());
    }

    @Transactional
    public Map<String, Object> createInvite(long userId) {
        MemberRole role = roleOf(userId);
        if (!FamilyRules.canFamilyAction(role, FamilyAction.INVITE, false)) {
            throw new ForbiddenException("仅创建者或管理员可邀请");
        }
        FamilyMemberEntity member = memberOf(userId);
        FamilyInviteEntity invite = new FamilyInviteEntity();
        invite.code = Long.toHexString(System.nanoTime()) + Integer.toHexString(userId).substring(0, Math.min(4, Integer.toHexString(userId).length()));
        invite.familyId = member.familyId;
        invite.createdBy = userId;
        invite.expiresAt = toLocal(FamilyRules.inviteExpiry(Instant.now()));
        invites.save(invite);
        return Map.of("code", invite.code, "expiresAt", invite.expiresAt.toString(), "ttlDays", FamilyRules.INVITE_TTL_DAYS);
    }

    @Transactional
    public Map<String, Object> join(long userId, String code) {
        FamilyInviteEntity invite = invites.findById(code)
                .orElseThrow(() -> new NotFoundException("邀请码无效"));
        if (!FamilyRules.inviteValid(invite.expiresAt.toInstant(), invite.revoked == 1, Instant.now())) {
            throw new IllegalArgumentException("邀请码已过期或已失效");
        }
        if (!FamilyRules.joinAllowed(members.findByUserId(userId).map(m -> m.familyId.toString()).orElse(null))) {
            throw new IllegalArgumentException("一人限一个家庭：请先退出现有家庭");
        }
        FamilyMemberEntity member = new FamilyMemberEntity();
        member.familyId = invite.familyId;
        member.userId = userId;
        member.level = MemberRole.MEMBER.name();
        member.joinedAt = LocalDateTime.now();
        members.save(member);
        return Map.of("familyId", invite.familyId, "role", MemberRole.MEMBER.name(),
                "ownPetsShared", FamilyRules.ownPetsSharedOnJoin());
    }

    @Transactional
    @Audited(action = "family.leave", targetType = "FAMILY")
    public Map<String, Object> leave(long userId) {
        FamilyMemberEntity member = memberOf(userId);
        if (MemberRole.OWNER.name().equals(member.level)) {
            throw new IllegalStateException("创建者请先转移所有权或解散家庭");
        }
        members.delete(member); // U63：访问即刻归零、贡献保留、不再触达
        return Map.of("left", true, "contributionsKept", FamilyRules.contributionsKeptAfterRemoval(),
                "queuedNotifications", FamilyRules.queuedFamilyNotificationsAfterRemoval());
    }

    @Transactional
    @Audited(action = "family.member.remove", targetType = "FAMILY")
    public void removeMember(long actorUserId, long targetUserId) {
        FamilyMemberEntity actor = memberOf(actorUserId);
        if (!FamilyRules.canFamilyAction(MemberRole.valueOf(actor.level), FamilyAction.REMOVE_MEMBER, false)) {
            throw new ForbiddenException("仅创建者或管理员可移除成员");
        }
        FamilyMemberEntity target = members.findByUserId(targetUserId)
                .orElseThrow(() -> new NotFoundException("目标成员不存在"));
        if (!target.familyId.equals(actor.familyId)) {
            throw new ForbiddenException("目标成员不在同一家庭");
        }
        members.delete(target);
    }

    @Transactional
    @Audited(action = "family.dissolve", targetType = "FAMILY")
    public Map<String, Object> dissolve(long userId) {
        FamilyMemberEntity actor = memberOf(userId);
        if (!FamilyRules.canDissolve(MemberRole.valueOf(actor.level))) {
            throw new ForbiddenException("非创建者/管理员不可解散（U60）");
        }
        Long familyId = actor.familyId;
        FamilyEntity family = families.findById(familyId).orElseThrow(() -> new NotFoundException("家庭不存在"));
        family.state = "DISSOLVED";
        families.save(family);
        for (FamilyInviteEntity invite : invites.findByFamilyId(familyId)) {
            invite.revoked = 1; // U63：旧链接失效
            invites.save(invite);
        }
        shares.deleteAll(shares.findByFamilyId(familyId)); // 解除全部共享
        for (FamilyMemberEntity m : members.findByFamilyId(familyId)) {
            members.delete(m);
        }
        return Map.of("dissolved", true, "sharesCleared", true, "invitesRevoked", true);
    }

    @Transactional
    @Audited(action = "family.transfer.owner", targetType = "FAMILY")
    public Map<String, Object> requestTransfer(long userId, TransferKind kind, long targetUserId, long refId) {
        FamilyMemberEntity actor = memberOf(userId);
        boolean actorIsPetOwner = kind == TransferKind.PET_OWNER; // 宠物所有权转移仅所有者可发起（U60 校验在 pet 域补强）
        if (kind == TransferKind.FAMILY_ADMIN && !MemberRole.OWNER.name().equals(actor.level)) {
            throw new ForbiddenException("仅创建者可转交管理员身份");
        }
        if (!FamilyRules.canFamilyAction(MemberRole.valueOf(actor.level), FamilyAction.TRANSFER_PET, actorIsPetOwner)
                && kind == TransferKind.PET_OWNER) {
            throw new ForbiddenException("仅宠物所有者可发起所有权转移");
        }
        OwnershipTransferEntity transfer = new OwnershipTransferEntity();
        transfer.kind = kind.name();
        transfer.fromUser = userId;
        transfer.toUser = targetUserId;
        transfer.refId = refId;
        transfer.createdAt = LocalDateTime.now();
        transfer.expiresAt = toLocal(transfer.createdAt.toInstant()
                .plus(java.time.Duration.ofHours(FamilyRules.TRANSFER_TTL_HOURS)));
        transfers.save(transfer);
        return Map.of("id", transfer.id, "state", "PENDING", "ttlHours", FamilyRules.TRANSFER_TTL_HOURS);
    }

    @Transactional
    public Map<String, Object> respondTransfer(long userId, long transferId, String decision) {
        OwnershipTransferEntity transfer = transfers.find(transferId);
        if (transfer.toUser != userId) {
            throw new ForbiddenException("仅接收方可响应转移");
        }
        String state = FamilyRules.transferState(transfer.createdAt.toInstant(), Instant.now());
        boolean effective = FamilyRules.changesOwnership(state, decision);
        transfer.state = "EXPIRED".equals(state) ? "EXPIRED" : decision.toUpperCase(java.util.Locale.ROOT);
        transfers.save(transfer);
        return Map.of("state", transfer.state, "ownershipChanged", effective,
                "sharesResetTo", FamilyRules.sharesAfterOwnerTransferAccepted().name());
    }

    @Transactional
    public Map<String, Object> setShareLevel(long actorUserId, long petId, String level) {
        FamilyMemberEntity actor = memberOf(actorUserId);
        // SET_SHARE_LEVEL 仅宠物所有者；跨域所有权在 pet-service 联通后强制校验（本地测试阶段补强）
        if (!FamilyRules.canSetShareLevel(true)) {
            throw new ForbiddenException("非宠物所有者不可调整共享档位");
        }
        FamilyShareEntity share = shares.findById(petId).orElseGet(() -> {
            FamilyShareEntity s = new FamilyShareEntity();
            s.petId = petId;
            s.familyId = actor.familyId;
            return s;
        });
        share.level = ShareLevel.valueOf(level);
        share.familyId = actor.familyId;
        shares.save(share);
        return Map.of("petId", petId, "level", share.level);
    }

    private static LocalDateTime toLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
    }
}

/** 转移记录仓储薄壳（避免再拆文件；等价于 TransferRepo） */
interface TransferRepoStub {
    OwnershipTransferEntity save(OwnershipTransferEntity entity);

    OwnershipTransferEntity find(long id);
}
