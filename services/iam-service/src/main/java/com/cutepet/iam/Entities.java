package com.cutepet.iam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * iam 持久化实体（映射 db/migration/V1__init.sql · cutepet_iam）。
 * 字段与迁移一一对应；hibernate ddl-auto=validate 在本地测试阶段校验一致性。
 */
@Entity
@Table(name = "users", schema = "cutepet_iam")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String phone;
    public String nickname = "";
    public String avatar = "";
    public String status = "ACTIVE";
    @Column(name = "age_band")
    public String ageBand = "ADULT";
    @Column(name = "post_right")
    public int postRight = 1;
    @Column(name = "pet_tags")
    public String petTags = "";
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

@Entity
@Table(name = "user_agreements", schema = "cutepet_iam")
class UserAgreementEntity {
    @Id
    public Long userId;
    @Column(name = "terms_version", nullable = false)
    public String termsVersion;
    @Column(name = "privacy_version", nullable = false)
    public String privacyVersion;
    public LocalDateTime agreedAt;
}

@Entity
@Table(name = "guardian_consents", schema = "cutepet_iam")
class GuardianConsentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "minor_user_id", nullable = false, unique = true)
    public Long minorUserId;
    @Column(name = "guardian_phone", nullable = false)
    public String guardianPhone;
    public String state = "PENDING";
    public LocalDateTime verifiedAt;
    public LocalDateTime revokedAt;
}

@Entity
@Table(name = "families", schema = "cutepet_iam")
class FamilyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(name = "owner_user_id", nullable = false)
    public Long ownerUserId;
    public String state = "ACTIVE";
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "family_members", schema = "cutepet_iam")
class FamilyMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "family_id", nullable = false)
    public Long familyId;
    @Column(name = "user_id", nullable = false, unique = true)
    public Long userId;
    public String level = "MEMBER"; // OWNER/ADMIN/MEMBER
    public LocalDateTime joinedAt;
}

@Entity
@Table(name = "family_invites", schema = "cutepet_iam")
class FamilyInviteEntity {
    @Id
    public String code;
    @Column(name = "family_id", nullable = false)
    public Long familyId;
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;
    public int revoked = 0;
}

@Entity
@Table(name = "family_shares", schema = "cutepet_iam")
class FamilyShareEntity {
    @Id
    @Column(name = "pet_id")
    public Long petId;
    @Column(name = "family_id", nullable = false)
    public Long familyId;
    public String level = "READONLY"; // MANAGE/READONLY
}

@Entity
@Table(name = "ownership_transfers", schema = "cutepet_iam")
class OwnershipTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String kind; // PET_OWNER/FAMILY_ADMIN
    @Column(name = "from_user", nullable = false)
    public Long fromUser;
    @Column(name = "to_user", nullable = false)
    public Long toUser;
    @Column(name = "ref_id", nullable = false)
    public Long refId;
    public String state = "PENDING";
    public LocalDateTime createdAt;
    public LocalDateTime expiresAt;
}

@Entity
@Table(name = "account_deletions", schema = "cutepet_iam")
class AccountDeletionEntity {
    @Id
    public Long userId;
    @Column(name = "requested_at", nullable = false)
    public LocalDateTime requestedAt;
    @Column(name = "expire_at", nullable = false)
    public LocalDateTime expireAt;
    public String state = "PENDING";
}

@Entity
@Table(name = "phone_changes", schema = "cutepet_iam")
class PhoneChangeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "old_phone_masked", nullable = false)
    public String oldPhoneMasked;
    @Column(name = "new_phone", nullable = false)
    public String newPhone;
    public String state = "PENDING";
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "privacy_exports", schema = "cutepet_iam")
class PrivacyExportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String state = "PENDING";
    @Column(name = "file_path")
    public String filePath;
    public LocalDateTime createdAt;
}
