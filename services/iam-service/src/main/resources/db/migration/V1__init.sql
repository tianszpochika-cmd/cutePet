-- cutePet iam-service · owner schema: cutepet_iam（跨 schema 不建 FK，数据归属见数据设计）
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  nickname VARCHAR(32) NOT NULL DEFAULT '',
  avatar VARCHAR(256) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',      -- ACTIVE/MUTED/BANNED/DELETING/DELETED
  age_band VARCHAR(8) NOT NULL DEFAULT 'ADULT',      -- ADULT/MINOR(<14 走监护人)
  post_right TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE user_agreements (
  user_id BIGINT PRIMARY KEY,
  terms_version VARCHAR(16) NOT NULL,
  privacy_version VARCHAR(16) NOT NULL,
  agreed_at DATETIME NOT NULL
);
CREATE TABLE guardian_consents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  minor_user_id BIGINT NOT NULL UNIQUE,
  guardian_phone VARCHAR(20) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/CONSENTED/REVOKED
  verified_at DATETIME NULL,
  revoked_at DATETIME NULL
);
CREATE TABLE families (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  owner_user_id BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE family_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL UNIQUE,                    -- BR-03 一人限一个家庭
  level VARCHAR(16) NOT NULL,                        -- OWNER/MANAGE/READONLY
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_family_user (family_id, user_id)
);
CREATE TABLE family_invites (
  code VARCHAR(24) PRIMARY KEY,
  family_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,                      -- 决议：7 天
  revoked TINYINT NOT NULL DEFAULT 0
);
CREATE TABLE family_shares (
  pet_id BIGINT PRIMARY KEY,                         -- 宠物属 pet 域，无跨 schema FK
  family_id BIGINT NOT NULL,
  level VARCHAR(16) NOT NULL                         -- MANAGE/READONLY（两档）
);
CREATE TABLE ownership_transfers (                    -- T1.9 双向转移（所有权/管理员独立）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  kind VARCHAR(16) NOT NULL,                         -- PET_OWNER/FAMILY_ADMIN
  from_user BIGINT NOT NULL,
  to_user BIGINT NOT NULL,
  ref_id BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/ACCEPTED/REJECTED/EXPIRED
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL                       -- 超 24h 失效（U62）
);
CREATE TABLE account_deletions (
  user_id BIGINT PRIMARY KEY,
  requested_at DATETIME NOT NULL,
  expire_at DATETIME NOT NULL,                       -- 冷静期 15 天
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING'       -- PENDING/CANCELLED/EXECUTED
);
CREATE TABLE phone_changes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  old_phone_masked VARCHAR(20) NOT NULL,
  new_phone VARCHAR(20) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE privacy_exports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/READY/FAILED
  file_path VARCHAR(256) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
