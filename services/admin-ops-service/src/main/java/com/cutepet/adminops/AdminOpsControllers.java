package com.cutepet.adminops;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * admin-ops 控制器（契约对齐）。
 * dev shim：用户身份取 X-User-Id、权限取 X-Permissions（网关 JWT 预校验在本地测试阶段接通）。
 */
@RestController
class AdminOpsControllers {

    private final ReportService reports;
    private final GovernanceService governance;
    private final ModerationService moderation;
    private final RoleService roles;
    private final TrackingService tracking;
    private final Repos.DisputeRepo disputes;
    private final Repos.ModerationRepo moderationRepo;

    AdminOpsControllers(ReportService reports, GovernanceService governance, ModerationService moderation,
                        RoleService roles, TrackingService tracking,
                        Repos.DisputeRepo disputes, Repos.ModerationRepo moderationRepo) {
        this.reports = reports;
        this.governance = governance;
        this.moderation = moderation;
        this.roles = roles;
        this.tracking = tracking;
        this.disputes = disputes;
        this.moderationRepo = moderationRepo;
    }

    private long userOf(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("缺少用户身份（X-User-Id）");
        }
        return Long.parseLong(userIdHeader.trim());
    }

    // ---------- 用户侧 ----------

    public record ReportReq(String targetType, Long targetId, String reason, String content) {
    }

    public record TicketReq(String actionType, Long actionId, String reason) {
    }

    public record CounterReq(Long copyrightId, String statement) {
    }

    public record TrackReq(String event, Long timestamp, String sessionId, String deviceId, Map<String, Object> props) {
    }

    @PostMapping("/reports")
    public Map<String, Object> createReport(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestBody ReportReq req) {
        return reports.create(userOf(userId), req.targetType(), req.targetId(), req.reason(), req.content());
    }

    @GetMapping("/me/reports")
    public List<ReportTicketEntity> myReports(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return reports.mine(userOf(userId));
    }

    /** 客服/申诉统一工单入口（封禁用户可达 — U82） */
    @PostMapping("/tickets")
    public Map<String, Object> createTicket(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestBody TicketReq req) {
        return governance.createAppeal(userOf(userId), req.actionType() == null ? "CUSTOMER_SERVICE" : req.actionType(),
                req.actionId(), req.reason());
    }

    @GetMapping("/me/tickets")
    public List<AppealEntity> myTickets(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return governance.myTickets(userOf(userId));
    }

    @PostMapping("/counters")
    public Map<String, Object> counter(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                       @RequestBody CounterReq req) {
        if (req.copyrightId() == null) {
            throw new IllegalArgumentException("缺少版权工单 id");
        }
        return governance.counter(req.copyrightId(), userOf(userId), req.statement());
    }

    @PostMapping("/track")
    public Map<String, Object> track(@RequestBody TrackReq req) {
        return tracking.track(req.event(), req.timestamp(), req.sessionId(), req.deviceId(), req.props());
    }

    // ---------- 管理侧（权限 dev shim） ----------

    public record ResolveReq(Long handlerId, String resolution) {
    }

    public record BanReq(String level, String reason, Long handlerId) {
    }

    public record PostRightReq(boolean restore, String reason, Long handlerId) {
    }

    public record DisputeResolveReq(Long handlerId, String resolution) {
    }

    public record RoleReq(String name, List<String> permissions) {
    }

    public record RoleUpdateReq(List<String> permissions) {
    }

    public record CopyrightReceiveReq(String claimant, String contact, String links, String targetType, Long targetId) {
    }

    public record CounterResolveReq(boolean accepted, Long handlerId) {
    }

    public record AppealResolveReq(boolean accepted, Long handlerId, String resolution) {
    }

    @GetMapping("/admin/reports")
    public List<ReportTicketEntity> adminReports(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false) String state) {
        PermissionGuard.require(perms, "report.handle");
        return reports.list(state == null ? "OPEN" : state);
    }

    @PostMapping("/admin/reports/{id}/resolve")
    public Map<String, Object> adminResolveReport(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody ResolveReq req) {
        PermissionGuard.require(perms, "report.handle");
        return reports.resolve(id, req.handlerId(), req.resolution());
    }

    @GetMapping("/admin/copyrights")
    public List<CopyrightNoticeEntity> adminCopyrights(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false) String state) {
        PermissionGuard.require(perms, "report.handle");
        return governance.listCopyrights(state);
    }

    @PostMapping("/admin/copyrights")
    public Map<String, Object> adminReceiveCopyright(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody CopyrightReceiveReq req) {
        PermissionGuard.require(perms, "report.handle");
        return governance.receiveNotice(req.claimant(), req.contact(), req.links(), req.targetType(), req.targetId());
    }

    @PostMapping("/admin/copyrights/{id}/takedown")
    public Map<String, Object> adminCopyrightTakedown(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody ResolveReq req) {
        PermissionGuard.require(perms, "report.handle");
        return governance.takedown(id, req.handlerId());
    }

    @PostMapping("/admin/copyrights/{id}/counter")
    public Map<String, Object> adminCopyrightCounter(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody CounterResolveReq req) {
        PermissionGuard.require(perms, "report.handle");
        return governance.resolveCounter(id, req.accepted(), req.handlerId());
    }

    @GetMapping("/admin/appeals")
    public List<AppealEntity> adminAppeals(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false) String state) {
        PermissionGuard.require(perms, "appeal.handle");
        return governance.listAppeals(state);
    }

    @PostMapping("/admin/appeals/{id}/resolve")
    public Map<String, Object> adminResolveAppeal(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody AppealResolveReq req) {
        PermissionGuard.require(perms, "appeal.handle");
        return governance.resolveAppeal(id, req.accepted(), req.handlerId(), req.resolution());
    }

    @GetMapping("/admin/users")
    public Map<String, Object> adminUsers(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false) String q) {
        PermissionGuard.require(perms, "user.view");
        // 用户明细来自 iam 服务（跨域查询在本地测试阶段经网关聚合）；此处返回处置摘要
        return Map.of("query", q == null ? "" : q, "note", "用户明细经 iam 聚合（本地阶段）",
                "activeModerations", moderationRepo.findByState("ACTIVE").size());
    }

    @PostMapping("/admin/users/{id}/ban")
    public Map<String, Object> adminBan(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody BanReq req) {
        PermissionGuard.require(perms, "user.ban");
        return moderation.ban(id, req.level(), req.reason(), req.handlerId());
    }

    @PostMapping("/admin/users/{id}/unban")
    public Map<String, Object> adminUnban(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody BanReq req) {
        PermissionGuard.require(perms, "user.ban");
        return moderation.unban(id, req.handlerId());
    }

    @PostMapping("/admin/users/{id}/post-right")
    public Map<String, Object> adminPostRight(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody PostRightReq req) {
        PermissionGuard.require(perms, "user.post.right.cancel");
        return moderation.postRight(id, req.restore(), req.reason(), req.handlerId());
    }

    @PostMapping("/admin/families/disputes/{id}/resolve")
    public Map<String, Object> adminResolveDispute(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody DisputeResolveReq req) {
        PermissionGuard.require(perms, "family.dispute");
        FamilyDisputeEntity row = disputes.findById(id)
                .orElseThrow(() -> new com.cutepet.common.web.NotFoundException("纠纷工单不存在"));
        if (req.resolution() == null || req.resolution().isBlank()) {
            throw new IllegalArgumentException("处理结论必填");
        }
        row.state = "RESOLVED";
        row.handlerId = req.handlerId();
        row.resolution = req.resolution();
        disputes.save(row);
        return Map.of("state", row.state);
    }

    @GetMapping("/admin/roles")
    public List<Map<String, Object>> adminRoles(
            @RequestHeader(value = "X-Permissions", required = false) String perms) {
        PermissionGuard.require(perms, "rbac.manage");
        return roles.list();
    }

    @PostMapping("/admin/roles")
    public Map<String, Object> adminCreateRole(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody RoleReq req) {
        PermissionGuard.require(perms, "rbac.manage");
        return roles.create(req.name(), req.permissions());
    }

    @PatchMapping("/admin/roles/{id}")
    public Map<String, Object> adminUpdateRole(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody RoleUpdateReq req) {
        PermissionGuard.require(perms, "rbac.manage");
        return roles.update(id, req.permissions());
    }
}
