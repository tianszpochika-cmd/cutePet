-- cutePet admin-ops-service · owner schema: cutepet_admin_ops
CREATE TABLE admin_users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL UNIQUE,
  display_name VARCHAR(32) NOT NULL DEFAULT '',
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL UNIQUE,
  builtin TINYINT NOT NULL DEFAULT 0,                -- 管理员角色不可改删
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission VARCHAR(64) NOT NULL,                   -- 32 权限点（contracts 单一事实源）
  PRIMARY KEY (role_id, permission)
);
CREATE TABLE admin_user_roles (
  admin_user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (admin_user_id, role_id)
);
CREATE TABLE report_tickets (                        -- 统一举报（48h 时效）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_user_id BIGINT NOT NULL,
  target_type VARCHAR(16) NOT NULL,                  -- ARTICLE/COMMENT/REVIEW/USER/ROUTE/ACTIVITY/POI
  target_id BIGINT NOT NULL,
  reason_code VARCHAR(16) NOT NULL,
  content VARCHAR(500) NOT NULL DEFAULT '',
  state VARCHAR(16) NOT NULL DEFAULT 'OPEN',         -- OPEN/RESOLVED
  handler_id BIGINT NULL,
  resolution VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME NULL
);
CREATE TABLE copyright_notices (                      -- 通知-删除（避风港）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  claimant VARCHAR(64) NOT NULL,
  contact VARCHAR(64) NOT NULL,
  links TEXT NOT NULL,
  proof VARCHAR(255) NULL,
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'RECEIVED',     -- RECEIVED/TAKEDOWN/DISPUTED/COUNTER_OK/COUNTER_REJECTED/CLOSED
  handler_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  handled_at DATETIME NULL
);
CREATE TABLE counter_notices (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  copyright_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  statement VARCHAR(500) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'RECEIVED',     -- RECEIVED/RESTORED/REJECTED
  handler_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE appeals (                               -- 处置申诉（48h 反馈）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  action_type VARCHAR(24) NOT NULL,
  action_id BIGINT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'OPEN',         -- OPEN/ACCEPTED/REJECTED
  handler_id BIGINT NULL,
  resolution VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME NULL
);
CREATE TABLE moderation_actions (                     -- L1–L4 处置 + 计数窗口（U76）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  level CHAR(2) NOT NULL,                            -- L1/L2/L3/L4
  action VARCHAR(24) NOT NULL,                       -- DELETE_CONTENT/MUTE_7D/MUTE_30D/PERMANENT_BAN
  reason VARCHAR(200) NOT NULL DEFAULT '',
  window_start DATE NOT NULL,
  l1_count INT NOT NULL DEFAULT 1,
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',       -- ACTIVE/EXPIRED/REVOKED(申诉撤销)
  handler_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE family_disputes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  reporter_user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  handler_id BIGINT NULL,
  resolution VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE system_settings (
  cfg_key VARCHAR(64) PRIMARY KEY,
  cfg_value VARCHAR(500) NOT NULL,
  updated_by BIGINT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
