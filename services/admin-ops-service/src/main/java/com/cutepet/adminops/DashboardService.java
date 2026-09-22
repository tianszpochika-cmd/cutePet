package com.cutepet.adminops;

import com.cutepet.adminops.domain.DashboardRules;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 看板聚合服务（T6.2/U26）：admin-ops 本地指标 + 规则口径；
 * 跨服务（内容队列/留存）聚合在本地阶段经网关汇总扩展。
 */
@Service
class DashboardService {

    private final Repos.ReportRepo reports;
    private final Repos.AppealRepo appeals;
    private final Repos.ModerationRepo moderations;

    DashboardService(Repos.ReportRepo reports, Repos.AppealRepo appeals, Repos.ModerationRepo moderations) {
        this.reports = reports;
        this.appeals = appeals;
        this.moderations = moderations;
    }

    public Map<String, Object> overview() {
        List<ReportTicketEntity> open = reports.findByStateOrderByCreatedAtAsc("OPEN");
        List<AppealEntity> openAppeals = appeals.findByStateOrderByCreatedAtAsc("OPEN");
        List<ReportTicketEntity> resolved = reports.findByStateOrderByCreatedAtAsc("RESOLVED");

        double oldestWaitHours = 0;
        if (!open.isEmpty() && open.get(0).createdAt != null) {
            oldestWaitHours = java.time.Duration.between(open.get(0).createdAt,
                    java.time.LocalDateTime.now()).toHours();
        }
        long onTime = resolved.stream().filter(r -> r.createdAt != null && r.resolvedAt != null
                && java.time.Duration.between(r.createdAt, r.resolvedAt).toHours()
                        <= DashboardRules.SLA_BREACH_HOURS).count();

        int pending = open.size();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("openReports", pending);
        out.put("openAppeals", openAppeals.size());
        out.put("activeModerations", moderations.findByState("ACTIVE").size());
        out.put("oldestReportWaitHours", oldestWaitHours);
        out.put("slaStatus", DashboardRules.slaStatus(oldestWaitHours));   // OK/BREACH/ESCALATE（24/48 决议）
        out.put("backlogAlert", DashboardRules.backlogAlert(pending));     // >50 告警
        out.put("reportOnTimeRate", DashboardRules.reportOnTimeRate(onTime, resolved.size())); // 零分母→null
        out.put("queueP50Note", "内容队列 P50/P90 随跨服务聚合接入（本地阶段）");
        out.put("zone", ZoneId.of("Asia/Shanghai").getId());
        return out;
    }
}
