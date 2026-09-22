package com.cutepet.adminops;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.adminops.domain.GovernanceRules;
import com.cutepet.adminops.domain.ModerationRules;
import com.cutepet.adminops.domain.ModerationRules.Level;
import com.cutepet.adminops.domain.TrackingRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** admin-ops 应用服务：举报/版权/申诉/处置/角色/埋点（判定全部委托 domain）。 */
@Service
class ReportService {

    private final Repos.ReportRepo reports;

    ReportService(Repos.ReportRepo reports) {
        this.reports = reports;
    }

    @Transactional
    public Map<String, Object> create(long reporterId, String targetType, Long targetId, String reason, String content) {
        if (!GovernanceRules.validReportTarget(targetType)) {
            throw new IllegalArgumentException("举报对象类型不合法");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("举报理由必填");
        }
        ReportTicketEntity row = new ReportTicketEntity();
        row.reporterUserId = reporterId;
        row.targetType = targetType;
        row.targetId = targetId;
        row.reasonCode = reason;
        row.content = content == null ? "" : content;
        row.createdAt = LocalDateTime.now();
        reports.save(row);
        return Map.of("id", row.id, "state", "OPEN", "slaHours", GovernanceRules.REPORT_SLA_HOURS);
    }

    public List<ReportTicketEntity> list(String state) {
        return "OPEN".equals(state) ? reports.findByStateOrderByCreatedAtAsc("OPEN") : reports.findAll();
    }

    public List<ReportTicketEntity> mine(long reporterId) {
        return reports.findByReporterUserIdOrderByCreatedAtDesc(reporterId);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "report.resolve", targetType = "REPORT")
    public Map<String, Object> resolve(long id, Long handlerId, String resolution) {
        if (!GovernanceRules.canResolveReport(resolution)) {
            throw new IllegalArgumentException("结案必须填写处理说明");
        }
        ReportTicketEntity row = reports.findById(id).orElseThrow(() -> new NotFoundException("举报不存在"));
        row.state = "RESOLVED";
        row.resolution = resolution;
        row.handlerId = handlerId;
        row.resolvedAt = LocalDateTime.now();
        reports.save(row);
        boolean wasOverdue = GovernanceRules.reportOverdue(row.createdAt.toInstant(), null, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        return Map.of("state", "RESOLVED", "wasOverdue", wasOverdue);
    }
}

@Service
class GovernanceService {

    private final Repos.CopyrightRepo copyrights;
    private final Repos.CounterRepo counters;
    private final Repos.AppealRepo appeals;
    private final Repos.ModerationRepo moderations;

    GovernanceService(Repos.CopyrightRepo copyrights, Repos.CounterRepo counters,
                      Repos.AppealRepo appeals, Repos.ModerationRepo moderations) {
        this.copyrights = copyrights;
        this.counters = counters;
        this.appeals = appeals;
        this.moderations = moderations;
    }

    // ---------- 版权（避风港） ----------

    @Transactional
    @com.cutepet.common.web.Audited(action = "copyright.receive", targetType = "COPYRIGHT")
    public Map<String, Object> receiveNotice(String claimant, String contact, String links,
                                             String targetType, Long targetId) {
        if (claimant == null || claimant.isBlank() || contact == null || contact.isBlank()
                || links == null || links.isBlank()) {
            throw new IllegalArgumentException("权利证明信息不完整");
        }
        CopyrightNoticeEntity row = new CopyrightNoticeEntity();
        row.claimant = claimant;
        row.contact = contact;
        row.links = links;
        row.targetType = targetType;
        row.targetId = targetId;
        row.state = GovernanceRules.copyrightAfterReceived(); // 收到即下架
        row.createdAt = LocalDateTime.now();
        copyrights.save(row);
        return Map.of("id", row.id, "state", row.state, "contentTakenDown", true);
    }

    public List<CopyrightNoticeEntity> listCopyrights(String state) {
        return state == null || state.isBlank() ? copyrights.findAll()
                : copyrights.findByStateOrderByCreatedAtAsc(state);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "copyright.takedown", targetType = "COPYRIGHT")
    public Map<String, Object> takedown(long id, Long handlerId) {
        CopyrightNoticeEntity row = copyrights.findById(id).orElseThrow(() -> new NotFoundException("版权工单不存在"));
        row.state = "TAKEDOWN";
        row.handlerId = handlerId;
        row.handledAt = LocalDateTime.now();
        copyrights.save(row);
        return Map.of("state", row.state, "contentTakenDown", true);
    }

    @Transactional
    public Map<String, Object> counter(long copyrightId, long userId, String statement) {
        CopyrightNoticeEntity notice = copyrights.findById(copyrightId)
                .orElseThrow(() -> new NotFoundException("版权工单不存在"));
        if (!GovernanceRules.canCounterNotice(notice.state)) {
            throw new IllegalStateException("当前状态不可提交反通知: " + notice.state);
        }
        CounterNoticeEntity counter = new CounterNoticeEntity();
        counter.copyrightId = copyrightId;
        counter.userId = userId;
        counter.statement = statement;
        counter.createdAt = LocalDateTime.now();
        counters.save(counter);
        notice.state = GovernanceRules.copyrightAfterCounter(notice.state);
        copyrights.save(notice);
        return Map.of("counterId", counter.id, "state", notice.state);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "copyright.counter.resolve", targetType = "COPYRIGHT")
    public Map<String, Object> resolveCounter(long copyrightId, boolean accepted, Long handlerId) {
        CopyrightNoticeEntity notice = copyrights.findById(copyrightId)
                .orElseThrow(() -> new NotFoundException("版权工单不存在"));
        String state = GovernanceRules.copyrightResolveCounter(accepted);
        boolean restore = GovernanceRules.restoreContentAfterCounter(state);
        notice.state = state;
        notice.handlerId = handlerId;
        notice.handledAt = LocalDateTime.now();
        copyrights.save(notice);
        return Map.of("state", state, "contentRestored", restore, "closed", true);
    }

    // ---------- 申诉与客服工单（U82） ----------

    @Transactional
    public Map<String, Object> createAppeal(long userId, String actionType, Long actionId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("申诉理由必填");
        }
        AppealEntity row = new AppealEntity();
        row.userId = userId;
        row.actionType = actionType;
        row.actionId = actionId;
        row.reason = reason;
        row.createdAt = LocalDateTime.now();
        appeals.save(row);
        return Map.of("id", row.id, "state", "OPEN", "feedbackHours", GovernanceRules.APPEAL_FEEDBACK_HOURS);
    }

    public List<AppealEntity> listAppeals(String state) {
        return state == null || state.isBlank() ? appeals.findAll()
                : appeals.findByStateOrderByCreatedAtAsc(state);
    }

    public List<AppealEntity> myTickets(long userId) {
        return appeals.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "appeal.resolve", targetType = "APPEAL")
    public Map<String, Object> resolveAppeal(long id, boolean accepted, Long handlerId, String resolution) {
        AppealEntity row = appeals.findById(id).orElseThrow(() -> new NotFoundException("申诉不存在"));
        row.state = GovernanceRules.appealDecision(accepted);
        row.resolution = resolution;
        row.handlerId = handlerId;
        row.resolvedAt = LocalDateTime.now();
        appeals.save(row);

        boolean revoked = false;
        if (accepted && row.actionId != null && "MODERATION".equals(row.actionType)) {
            moderations.findById(row.actionId).ifPresent(m -> {
                m.state = GovernanceRules.moderationStateAfterAppealAccepted(); // 撤销：剔除计数
                moderations.save(m);
            });
            revoked = true;
        }
        return Map.of("state", row.state, "moderationRevoked", revoked);
    }
}

@Service
class ModerationService {

    private final Repos.ModerationRepo moderations;

    ModerationService(Repos.ModerationRepo moderations) {
        this.moderations = moderations;
    }

    private int effectiveCount(long userId) {
        int confirmed = moderations.findByUserIdAndState(userId, "ACTIVE").size()
                + moderations.findByUserIdAndState(userId, "EXPIRED").size();
        int revoked = moderations.findByUserIdAndState(userId, "REVOKED").size();
        return ModerationRules.effectiveViolationCount(confirmed, revoked);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "user.ban", targetType = "USER")
    public Map<String, Object> ban(long userId, String levelName, String reason, Long handlerId) {
        Level level = Level.valueOf(levelName);
        int l1Count = moderations.findByUserIdAndState(userId, "ACTIVE").stream()
                .filter(m -> "L1".equals(m.level)).count() > 0
                ? Math.max(1, effectiveCount(userId)) : 1;
        Level effective = ModerationRules.escalate(level, l1Count);

        ModerationActionEntity row = new ModerationActionEntity();
        row.userId = userId;
        row.level = effective.name();
        row.action = ModerationRules.actionForLevel(effective).name();
        row.reason = reason == null ? "" : reason;
        row.windowStart = LocalDate.now();
        row.l1Count = l1Count;
        row.handlerId = handlerId;
        row.createdAt = LocalDateTime.now();
        moderations.save(row);
        // 注：封禁对登录/业务的强制拦截点在网关过滤器（本地测试阶段联调 iam 用户状态同步）
        return Map.of("id", row.id, "level", effective.name(), "action", row.action,
                "effectiveViolations", effectiveCount(userId));
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "user.unban", targetType = "USER")
    public Map<String, Object> unban(long userId, Long handlerId) {
        for (ModerationActionEntity m : moderations.findByUserIdAndState(userId, "ACTIVE")) {
            m.state = "EXPIRED";
            m.handlerId = handlerId;
            moderations.save(m);
        }
        return Map.of("unbanned", true);
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "user.post.right.cancel", targetType = "USER")
    public Map<String, Object> postRight(long userId, boolean restore, String reason, Long handlerId) {
        ModerationActionEntity row = new ModerationActionEntity();
        row.userId = userId;
        row.level = "L1";
        row.action = restore ? "POST_RIGHT_RESTORE" : "POST_RIGHT_SUSPEND";
        row.reason = reason == null ? "" : reason;
        row.windowStart = LocalDate.now();
        row.handlerId = handlerId;
        row.createdAt = LocalDateTime.now();
        moderations.save(row);
        return Map.of("restored", restore, "note", "与 iam.users.post_right 同步在本地测试阶段联调");
    }

    public List<FamilyDisputeEntity> disputes(com.cutepet.adminops.Repos.DisputeRepo repo, String state) {
        return "OPEN".equals(state) ? repo.findByStateOrderByCreatedAtAsc("OPEN") : repo.findAll();
    }
}

@Service
class RoleService {

    private final Repos.RoleRepo roles;
    private final Repos.RolePermissionRepo rolePermissions;

    RoleService(Repos.RoleRepo roles, Repos.RolePermissionRepo rolePermissions) {
        this.roles = roles;
        this.rolePermissions = rolePermissions;
    }

    public List<Map<String, Object>> list() {
        return roles.findAll().stream().map(r -> {
            List<String> perms = rolePermissions.findByRoleId(r.id).stream().map(p -> p.permission).toList();
            return Map.<String, Object>of("id", r.id, "name", r.name, "builtin", r.builtin == 1, "permissions", perms);
        }).toList();
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "rbac.role.create", targetType = "ROLE")
    public Map<String, Object> create(String name, List<String> permissions) {
        List<String> invalid = PermissionCatalog.invalidOf(permissions == null ? List.of() : permissions);
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("含非 32 权限点内容: " + invalid);
        }
        RoleEntity role = new RoleEntity();
        role.name = name;
        role.createdAt = LocalDateTime.now();
        role = roles.save(role);
        for (String p : permissions) {
            RolePermissionEntity rp = new RolePermissionEntity();
            rp.roleId = role.id;
            rp.permission = p;
            rolePermissions.save(rp);
        }
        return Map.of("id", role.id, "name", role.name, "count", permissions.size());
    }

    @Transactional
    @com.cutepet.common.web.Audited(action = "rbac.role.update", targetType = "ROLE")
    public Map<String, Object> update(long id, List<String> permissions) {
        RoleEntity role = roles.findById(id).orElseThrow(() -> new NotFoundException("角色不存在"));
        if (role.builtin == 1) {
            throw new ForbiddenException("内置角色不可修改（管理端功能设计 §6.2）");
        }
        List<String> invalid = PermissionCatalog.invalidOf(permissions == null ? List.of() : permissions);
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("含非 32 权限点内容: " + invalid);
        }
        rolePermissions.deleteByRoleId(id);
        for (String p : permissions) {
            RolePermissionEntity rp = new RolePermissionEntity();
            rp.roleId = id;
            rp.permission = p;
            rolePermissions.save(rp);
        }
        return Map.of("id", role.id, "count", permissions.size());
    }
}

@Service
class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    @Transactional
    public Map<String, Object> track(String event, Long timestamp, String sessionId, String deviceId,
                                     Map<String, Object> props) {
        Map<String, String> errors = TrackingRules.validate(event, timestamp, sessionId, deviceId, props);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("埋点校验失败: " + errors);
        }
        log.info("[track] event={} session={} device={} props={}", event, sessionId, deviceId, props);
        return Map.of("accepted", true);
    }
}

/** 权限校验 dev shim（正式为 JWT 声明 + 网关预校验，本地测试阶段切换） */
final class PermissionGuard {

    private PermissionGuard() {
    }

    static void require(String header, String permission) {
        Set<String> granted = PermissionCatalog.parseHeader(header);
        if (!PermissionCatalog.has(granted, permission)) {
            throw new ForbiddenException("缺少权限: " + permission);
        }
    }
}
