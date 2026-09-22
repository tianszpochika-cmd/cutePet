package com.cutepet.adminops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** admin-ops 持久化实体（映射 cutepet_admin_ops V1）。 */
@Entity
@Table(name = "admin_users", schema = "cutepet_admin_ops")
class AdminUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String username;
    @Column(name = "display_name")
    public String displayName = "";
    public String state = "ACTIVE";
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "roles", schema = "cutepet_admin_ops")
class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String name;
    public int builtin = 0;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "role_permissions", schema = "cutepet_admin_ops")
class RolePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "role_id", nullable = false)
    public Long roleId;
    @Column(nullable = false)
    public String permission;
}

@Entity
@Table(name = "admin_user_roles", schema = "cutepet_admin_ops")
class AdminUserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "admin_user_id", nullable = false)
    public Long adminUserId;
    @Column(name = "role_id", nullable = false)
    public Long roleId;
}

@Entity
@Table(name = "report_tickets", schema = "cutepet_admin_ops")
class ReportTicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "reporter_user_id", nullable = false)
    public Long reporterUserId;
    @Column(name = "target_type", nullable = false)
    public String targetType;
    @Column(name = "target_id", nullable = false)
    public Long targetId;
    @Column(name = "reason_code", nullable = false)
    public String reasonCode;
    public String content = "";
    public String state = "OPEN";
    @Column(name = "handler_id")
    public Long handlerId;
    public String resolution;
    public LocalDateTime createdAt;
    public LocalDateTime resolvedAt;
}

@Entity
@Table(name = "copyright_notices", schema = "cutepet_admin_ops")
class CopyrightNoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String claimant;
    public String contact;
    public String links;
    public String proof;
    @Column(name = "target_type", nullable = false)
    public String targetType;
    @Column(name = "target_id", nullable = false)
    public Long targetId;
    public String state = "RECEIVED";
    @Column(name = "handler_id")
    public Long handlerId;
    public LocalDateTime createdAt;
    public LocalDateTime handledAt;
}

@Entity
@Table(name = "counter_notices", schema = "cutepet_admin_ops")
class CounterNoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "copyright_id", nullable = false)
    public Long copyrightId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String statement;
    public String state = "RECEIVED";
    @Column(name = "handler_id")
    public Long handlerId;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "appeals", schema = "cutepet_admin_ops")
class AppealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "action_type", nullable = false)
    public String actionType; // MODERATION / CUSTOMER_SERVICE
    @Column(name = "action_id")
    public Long actionId;
    public String reason;
    public String state = "OPEN";
    @Column(name = "handler_id")
    public Long handlerId;
    public String resolution;
    public LocalDateTime createdAt;
    public LocalDateTime resolvedAt;
}

@Entity
@Table(name = "moderation_actions", schema = "cutepet_admin_ops")
class ModerationActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String level; // L1..L4
    public String action;
    public String reason = "";
    @Column(name = "window_start")
    public LocalDate windowStart;
    @Column(name = "l1_count")
    public int l1Count = 1;
    public String state = "ACTIVE"; // ACTIVE/EXPIRED/REVOKED
    @Column(name = "handler_id")
    public Long handlerId;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "family_disputes", schema = "cutepet_admin_ops")
class FamilyDisputeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "family_id", nullable = false)
    public Long familyId;
    @Column(name = "reporter_user_id", nullable = false)
    public Long reporterUserId;
    public String content;
    public String state = "OPEN";
    @Column(name = "handler_id")
    public Long handlerId;
    public String resolution;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "system_settings", schema = "cutepet_admin_ops")
class SystemSettingEntity {
    @Id
    @Column(name = "cfg_key")
    public String cfgKey;
    @Column(name = "cfg_value", nullable = false)
    public String cfgValue;
    @Column(name = "updated_by")
    public Long updatedBy;
    public LocalDateTime updatedAt;
}
