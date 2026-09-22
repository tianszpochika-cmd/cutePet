package com.cutepet.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** audit 持久化实体（映射 cutepet_audit V1 · IP 必录/前后值脱敏存储）。 */
@Entity
@Table(name = "audit_logs", schema = "cutepet_audit")
class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String operator;
    @Column(name = "operator_role")
    public String operatorRole = "";
    @Column(nullable = false)
    public String ip;
    @Column(nullable = false)
    public String action;
    @Column(name = "target_type")
    public String targetType;
    @Column(name = "target_id")
    public String targetId;
    @Column(name = "before_value")
    public String beforeValue;
    @Column(name = "after_value")
    public String afterValue;
    public LocalDateTime createdAt;
}
