package com.cutepet.audit;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.common.web.ForbiddenException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * audit 控制器（T6.3）：内部写入通道 + /admin/audit-logs 查询（audit.view 权限）。
 * 内部通道不经网关暴露（网关未路由 /internal/**），仅服务间调用。
 */
@RestController
class AuditControllers {

    private final AuditService auditService;

    AuditControllers(AuditService auditService) {
        this.auditService = auditService;
    }

    public record IngestReq(String operator, String operatorRole, String ip, String action,
                            String targetType, String targetId, String before, String after) {
    }

    @PostMapping("/internal/audits")
    public Map<String, Object> ingest(@RequestBody IngestReq req) {
        return auditService.ingest(req.operator(), req.operatorRole(), req.ip(), req.action(),
                req.targetType(), req.targetId(), req.before(), req.after());
    }

    @GetMapping("/admin/audit-logs")
    public List<Map<String, Object>> query(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        if (!PermissionCatalog.has(PermissionCatalog.parseHeader(perms), "audit.view")) {
            throw new ForbiddenException("缺少权限: audit.view");
        }
        return auditService.query(action, operator, from, to);
    }
}
