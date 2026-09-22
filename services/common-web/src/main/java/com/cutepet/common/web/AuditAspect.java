package com.cutepet.common.web;

import com.cutepet.common.AuditEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计切面：@Audited 方法调用前记录 AuditEntry（IP 必录——决议 2026-09-22）。
 * 一期输出到本地日志；audit-service 落库通道在 M6 接入（本地测试阶段联调）。
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @Before("@annotation(audited)")
    public void record(Audited audited) {
        String ip = resolveIp();
        String operator = resolveOperator();
        AuditEntry entry = AuditEntry.of(operator, "SYSTEM", ip, audited.action(),
                audited.targetType(), "", "", "");
        audit.info("action={} operator={} ip={} target={}:{}",
                entry.action(), entry.operator(), entry.ip(), entry.targetType(), entry.targetId());
    }

    private String resolveIp() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            HttpServletRequest request = sra.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "internal";
    }

    private String resolveOperator() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            String operator = sra.getRequest().getHeader("X-Operator");
            if (operator != null && !operator.isBlank()) {
                return operator;
            }
        }
        return "system";
    }
}
