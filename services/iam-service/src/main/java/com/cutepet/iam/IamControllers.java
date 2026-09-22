package com.cutepet.iam;

import com.cutepet.iam.domain.FamilyRules.TransferKind;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * iam 控制器（路径对齐 packages/contracts/src/routes.ts 契约）。
 * DTO 用 record；错误出口统一走 common-web ApiExceptionHandler。
 */
@RestController
@RequestMapping("/")
class IamControllers {

    private final AuthService auth;
    private final ProfileService profile;
    private final DeletionService deletion;
    private final FamilyService family;
    private final TokenService tokens;

    IamControllers(AuthService auth, ProfileService profile, DeletionService deletion,
                   FamilyService family, TokenService tokens) {
        this.auth = auth;
        this.profile = profile;
        this.deletion = deletion;
        this.family = family;
        this.tokens = tokens;
    }

    private long requireUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("缺少访问令牌");
        }
        return auth.userIdOf(authorization.substring(7))
                .orElseThrow(() -> new IllegalArgumentException("令牌无效"));
    }

    // ---------- 认证 ----------

    public record SmsSendReq(String phone) {
    }

    public record LoginReq(String phone, String code, String birthDate, String guardianPhone,
                           String termsVersion, String privacyVersion, String device) {
    }

    public record WechatLoginReq(String wechatCode, String phone, String code, String birthDate,
                                 String guardianPhone, String termsVersion, String privacyVersion, String device) {
    }

    @PostMapping("/auth/sms/send")
    public Map<String, Object> sendSms(@RequestBody SmsSendReq req) {
        return auth.sendSmsCode(req.phone());
    }

    @PostMapping("/auth/sms/login")
    public Map<String, Object> smsLogin(@RequestBody LoginReq req) {
        return auth.login(req.phone(), req.code(), req.birthDate(), req.guardianPhone(),
                req.termsVersion(), req.privacyVersion(), req.device());
    }

    @PostMapping("/auth/wechat/login")
    public Map<String, Object> wechatLogin(@RequestBody WechatLoginReq req) {
        return auth.wechatLogin(req.wechatCode(), req.phone(), req.code(), req.birthDate(),
                req.guardianPhone(), req.termsVersion(), req.privacyVersion(), req.device());
    }

    @PostMapping("/auth/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            auth.logout(authorization.substring(7));
        }
        return Map.of("loggedOut", true);
    }

    // ---------- 监护人（方案甲） ----------

    public record GuardianReq(String minorPhone, String guardianPhone) {
    }

    @PostMapping("/auth/minor/guardian/verify")
    public Map<String, Object> guardianVerify(@RequestBody GuardianReq req) {
        auth.guardianVerify(req.minorPhone(), req.guardianPhone());
        return Map.of("verified", true);
    }

    @PostMapping("/auth/minor/guardian/consent")
    public Map<String, Object> guardianConsent(@RequestBody GuardianReq req) {
        auth.guardianConsent(req.minorPhone(), req.guardianPhone());
        return Map.of("state", "CONSENTED");
    }

    @PostMapping("/auth/minor/guardian/revoke")
    public Map<String, Object> guardianRevoke(@RequestBody GuardianReq req) {
        auth.guardianRevoke(req.minorPhone(), req.guardianPhone());
        return Map.of("state", "REVOKED");
    }

    // ---------- 个人 ----------

    public record ProfilePatch(String nickname, String avatar, String petTags) {
    }

    public record PhoneBindReq(String oldPhone, String newPhone) {
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = requireUser(authorization);
        UserEntity user = profile.get(userId);
        return Map.of("id", user.id, "phone", user.phone, "nickname", user.nickname,
                "avatar", user.avatar, "petTags", user.petTags, "ageBand", user.ageBand, "status", user.status);
    }

    @PatchMapping("/me")
    public Map<String, Object> patchMe(@RequestHeader(value = "Authorization", required = false) String authorization,
                                       @RequestBody ProfilePatch patch) {
        long userId = requireUser(authorization);
        UserEntity user = profile.update(userId, patch.nickname(), patch.avatar(), patch.petTags());
        return Map.of("id", user.id, "nickname", user.nickname, "petTags", user.petTags);
    }

    @PostMapping("/me/phone/bind")
    public Map<String, Object> bindPhone(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestBody PhoneBindReq req) {
        long userId = requireUser(authorization);
        profile.bindPhone(userId, req.oldPhone(), req.newPhone());
        return Map.of("bound", true);
    }

    @PostMapping("/me/delete")
    public Map<String, Object> requestDelete(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = requireUser(authorization);
        return deletion.requestDeletion(userId);
    }

    @PostMapping("/me/cancel-delete")
    public Map<String, Object> cancelDelete(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = requireUser(authorization);
        return deletion.cancelDeletion(userId);
    }

    @PostMapping("/me/export")
    public Map<String, Object> exportData(@RequestHeader(value = "Authorization", required = false) String authorization) {
        long userId = requireUser(authorization);
        return deletion.requestExport(userId);
    }

    // ---------- 家庭 ----------

    public record FamilyCreateReq(String name) {
    }

    public record JoinReq(String code) {
    }

    public record MemberReq(long userId) {
    }

    public record TransferReq(String kind, long toUserId, long refId) {
    }

    public record TransferRespReq(long transferId, String decision) {
    }

    public record ShareReq(String level) {
    }

    @PostMapping("/families")
    public Map<String, Object> createFamily(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @RequestBody FamilyCreateReq req) {
        return family.create(requireUser(authorization), req.name());
    }

    @GetMapping("/families/mine")
    public Map<String, Object> myFamily(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return family.mine(requireUser(authorization));
    }

    @PostMapping("/families/invite")
    public Map<String, Object> invite(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return family.createInvite(requireUser(authorization));
    }

    @PostMapping("/families/join")
    public Map<String, Object> join(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @RequestBody JoinReq req) {
        return family.join(requireUser(authorization), req.code());
    }

    @PostMapping("/families/leave")
    public Map<String, Object> leave(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return family.leave(requireUser(authorization));
    }

    @DeleteMapping("/families/members/{userId}")
    public Map<String, Object> removeMember(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @org.springframework.web.bind.annotation.PathVariable long userId) {
        family.removeMember(requireUser(authorization), userId);
        return Map.of("removed", true);
    }

    @PostMapping("/families/dissolve")
    public Map<String, Object> dissolve(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return family.dissolve(requireUser(authorization));
    }

    @PostMapping("/families/transfer-owner")
    public Map<String, Object> transferOwner(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestBody TransferReq req) {
        return family.requestTransfer(requireUser(authorization), TransferKind.valueOf(req.kind()),
                req.toUserId(), req.refId());
    }

    @PostMapping("/families/transfer-admin")
    public Map<String, Object> transferAdmin(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @RequestBody TransferReq req) {
        return family.requestTransfer(requireUser(authorization), TransferKind.FAMILY_ADMIN,
                req.toUserId(), req.refId());
    }

    /** 转移响应（接收方接受/拒绝）——契约补充入口，映射到 owner/admin 转移单 */
    @PutMapping("/families/transfers/respond")
    public Map<String, Object> respondTransfer(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @RequestBody TransferRespReq req) {
        return family.respondTransfer(requireUser(authorization), req.transferId(), req.decision());
    }

    @PatchMapping("/families/share/{petId}")
    public Map<String, Object> sharePet(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @org.springframework.web.bind.annotation.PathVariable long petId,
                                        @RequestBody ShareReq req) {
        return family.setShareLevel(requireUser(authorization), petId, req.level());
    }
}
