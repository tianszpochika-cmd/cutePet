-- cutePet content-service · owner schema: cutepet_content
CREATE TABLE channels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(16) NOT NULL UNIQUE,                  -- 犬/猫/异宠/营养/疾病健康/训练行为/行业快讯
  slug VARCHAR(24) NOT NULL UNIQUE,
  ord INT NOT NULL DEFAULT 0
);
CREATE TABLE tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(24) NOT NULL UNIQUE
);
CREATE TABLE topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(24) NOT NULL UNIQUE,
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
);
CREATE TABLE articles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slug VARCHAR(96) NOT NULL UNIQUE,
  channel_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  author_kind VARCHAR(8) NOT NULL DEFAULT 'UGC',     -- PGC/UGC
  state VARCHAR(16) NOT NULL DEFAULT 'DRAFT',        -- 见 contracts 状态机
  live_version_id BIGINT NULL,
  is_top TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_state_channel (state, channel_id)
);
CREATE TABLE article_versions (                       -- T3.6 线上/修改版分离
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  title VARCHAR(120) NOT NULL,
  cover VARCHAR(256) NULL,
  body MEDIUMTEXT NOT NULL,
  tags VARCHAR(255) NOT NULL DEFAULT '',
  state VARCHAR(16) NOT NULL,                        -- DRAFT/PENDING/REVIEWING/REJECTED/PUBLISHED/TAKEDOWN/WITHDRAWN
  reject_count INT NOT NULL DEFAULT 0,
  review_note VARCHAR(500) NULL,
  reviewer_id BIGINT NULL,
  claimed_at DATETIME NULL,                          -- 处理中锁
  submitted_at DATETIME NULL,
  reviewed_at DATETIME NULL,
  UNIQUE KEY uk_article_version (article_id, version_no)
);
CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  content VARCHAR(500) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'VISIBLE',      -- VISIBLE/HIDDEN/DELETED
  machine_state VARCHAR(16) NOT NULL DEFAULT 'PASS', -- PASS/SUSPECT(仅自己可见)/REJECT
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_article (article_id, created_at)
);
CREATE TABLE likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(16) NOT NULL,                  -- ARTICLE/COMMENT/ROUTE/LIST
  target_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_target (user_id, target_type, target_id)
);
CREATE TABLE favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  tab VARCHAR(16) NOT NULL,                          -- 内容/商品/清单/场所/路线
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  group_name VARCHAR(32) NOT NULL DEFAULT '默认',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_target (user_id, target_type, target_id)
);
CREATE TABLE follows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  UNIQUE KEY uk_follow (user_id, author_id)
);
CREATE TABLE specials (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(64) NOT NULL,
  intro VARCHAR(500) NOT NULL DEFAULT '',
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
);
CREATE TABLE special_items (
  special_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  ord INT NOT NULL DEFAULT 0,
  PRIMARY KEY (special_id, article_id)
);
