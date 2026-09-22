-- cutePet notification-service · owner schema: cutepet_notification
CREATE TABLE messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category VARCHAR(16) NOT NULL,                     -- REMINDER/INTERACTION/REVIEW/FAMILY/SYSTEM
  title VARCHAR(64) NOT NULL,
  body VARCHAR(500) NOT NULL DEFAULT '',
  deep_link VARCHAR(255) NULL,                       -- 点击跳转（深链路由表）
  state VARCHAR(16) NOT NULL DEFAULT 'UNREAD',       -- UNREAD/READ
  read_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_state (user_id, state)
);
CREATE TABLE notification_templates (
  tpl_key VARCHAR(48) PRIMARY KEY,
  channel VARCHAR(16) NOT NULL,                      -- STATION/PUSH
  title_tpl VARCHAR(64) NOT NULL,
  body_tpl VARCHAR(500) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE delivery_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  channel VARCHAR(16) NOT NULL,                      -- STATION/PUSH_STUB
  state VARCHAR(16) NOT NULL,                        -- SENT/FAILED/RETRYING
  retries INT NOT NULL DEFAULT 0,
  error VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE push_channels (                         -- 一期 stub（E01 真实通道属本地/上线阶段）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id VARCHAR(64) NOT NULL,
  token VARCHAR(128) NOT NULL DEFAULT '',
  platform VARCHAR(16) NOT NULL DEFAULT 'WEB_H5',
  stub TINYINT NOT NULL DEFAULT 1,
  enabled TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_device (device_id)
);
CREATE TABLE reminder_dispatches (                    -- 调度留痕（幂等/U59 补推判断）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reminder_id BIGINT NOT NULL,
  due_date DATE NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',       -- PENDING/DONE/FAILED
  attempts INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_reminder_due (reminder_id, due_date)
);
