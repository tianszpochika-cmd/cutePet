package com.cutepet.audit;

import com.cutepet.common.AuditRules;
import com.cutepet.common.web.ForbiddenException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * audit 应用服务（T6.3）：写入强制 IP 必录 + 前后值脱敏；查询限窗 ≤90 天 + 读出再脱敏。
 */
@Service
class AuditService {

    private final Repos.AuditLogRepo logs;

    AuditService(Repos.AuditLogRepo logs) {
        this.logs = logs;
    }

    @Transactional
    public Map<String, Object> ingest(String operator, String operatorRole, String ip, String action,
                                      String targetType, String targetId, String before, String after) {
        if (!AuditRules.ipRecorded(ip)) {
            throw new IllegalArgumentException("IP 必录（决议 2026-09-22）");
        }
        if (operator == null || operator.isBlank() || action == null || action.isBlank()) {
            throw new IllegalArgumentException("operator/action 必填");
        }
        AuditLogEntity row = new AuditLogEntity();
        row.operator = operator;
        row.operatorRole = operatorRole == null ? "" : operatorRole;
        row.ip = ip;
        row.action = action;
        row.targetType = targetType;
        row.targetId = targetId;
        row.beforeValue = AuditRules.redact(before);   // 存储即脱敏
        row.afterValue = AuditRules.redact(after);
        row.createdAt = LocalDateTime.now();
        logs.save(row);
        return Map.of("id", row.id, "highRisk", AuditRules.highRisk(action));
    }

    public List<Map<String, Object>> query(String action, String operator,
                                           LocalDate from, LocalDate to) {
        if ((from != null || to != null) && !AuditRules.queryRangeValid(from, to)) {
            throw new ForbiddenException("查询区间非法（起≤止且跨度≤90 天）");
        }
        List<AuditLogEntity> rows;
        if (action != null && !action.isBlank()) {
            rows = logs.findByActionOrderByCreatedAtDesc(action);
        } else if (operator != null && !operator.isBlank()) {
            rows = logs.findByOperatorOrderByCreatedAtDesc(operator);
        } else if (from != null && to != null) {
            rows = logs.findByCreatedAtBetweenOrderByCreatedAtDesc(from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay());
        } else {
            rows = logs.findAllByOrderByCreatedAtDesc();
        }
        return rows.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.id);
            item.put("operator", r.operator);
            item.put("operatorRole", r.operatorRole);
            item.put("ip", r.ip);
            item.put("action", r.action);
            item.put("highRisk", AuditRules.highRisk(r.action));
            item.put("targetType", r.targetType);
            item.put("targetId", r.targetId);
            item.put("before", AuditRules.redact(r.beforeValue)); // 读出再脱敏（双保险）
            item.put("after", AuditRules.redact(r.afterValue));
            item.put("createdAt", r.createdAt == null ? "" : r.createdAt.toString());
            return item;
        }).toList();
    }
}
