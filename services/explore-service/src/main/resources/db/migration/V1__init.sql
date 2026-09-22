-- cutePet explore-service · owner schema: cutepet_explore
CREATE TABLE pois (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  type VARCHAR(16) NOT NULL,                         -- 店/医院/公园/餐厅/寄养/美容/训练
  city VARCHAR(16) NOT NULL,
  lng DECIMAL(10,6) NOT NULL,
  lat DECIMAL(10,6) NOT NULL,
  address VARCHAR(128) NOT NULL DEFAULT '',
  phone VARCHAR(20) NOT NULL DEFAULT '',
  open_hours VARCHAR(128) NOT NULL DEFAULT '',
  attrs VARCHAR(255) NOT NULL DEFAULT '',            -- 可带宠/有围栏/有水碗
  state VARCHAR(16) NOT NULL DEFAULT 'NORMAL',       -- NORMAL/CLOSED
  avg_score DECIMAL(3,2) NOT NULL DEFAULT 0,         -- 有效评价均值（决议）
  review_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_city_type (city, type),
  KEY idx_geo (lng, lat)
);
CREATE TABLE poi_photos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  poi_id BIGINT NOT NULL,
  url VARCHAR(256) NOT NULL,
  ord INT NOT NULL DEFAULT 0
);
CREATE TABLE poi_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  poi_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  score_friendly TINYINT NOT NULL,
  score_env TINYINT NOT NULL,
  score_service TINYINT NOT NULL,
  content VARCHAR(500) NOT NULL DEFAULT '',
  pics TEXT NULL,
  state VARCHAR(24) NOT NULL DEFAULT 'VISIBLE',      -- VISIBLE/PENDING/SELF_VISIBLE/HIDDEN
  device_key VARCHAR(64) NULL,                       -- 防刷：设备维度
  day_key DATE NOT NULL,                             -- 防刷：同场所同人同日唯一
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_poi_day (user_id, poi_id, day_key),
  KEY idx_poi_state (poi_id, state)
);
CREATE TABLE corrections (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  poi_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  field VARCHAR(24) NOT NULL,
  proposed VARCHAR(255) NOT NULL,
  evidence VARCHAR(256) NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'OPEN',         -- OPEN/ACCEPTED/REJECTED
  merged_into BIGINT NULL,                           -- 同类合并
  handler_id BIGINT NULL,
  note VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE ugc_routes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  city VARCHAR(16) NOT NULL,
  name VARCHAR(64) NOT NULL,
  points TEXT NOT NULL,                              -- 选点连线坐标（一期非真实轨迹）
  distance_m INT NOT NULL DEFAULT 0,
  duration_min INT NOT NULL DEFAULT 0,
  difficulty VARCHAR(8) NOT NULL DEFAULT '简单',
  note VARCHAR(500) NOT NULL DEFAULT '',
  pics TEXT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/PUBLISHED/REJECTED（先审后发）
  reviewer_id BIGINT NULL,
  review_note VARCHAR(200) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_user_id BIGINT NOT NULL,
  city VARCHAR(16) NOT NULL,
  title VARCHAR(64) NOT NULL,
  type VARCHAR(16) NOT NULL,                         -- 集市/领养日/课程/其他
  begins_at DATETIME NOT NULL,
  ends_at DATETIME NOT NULL,
  address VARCHAR(128) NOT NULL,
  lng DECIMAL(10,6) NULL,
  lat DECIMAL(10,6) NULL,
  quota INT NOT NULL DEFAULT 0,                      -- 0=不限
  signup_deadline DATETIME NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/PUBLISHED/CHANGED/ENDED/CANCELLED
  change_note VARCHAR(200) NULL,                     -- 修改核实中（U70）
  contact VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_city_state (city, state)
);
CREATE TABLE activity_signups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  name VARCHAR(32) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',       -- ACTIVE/CANCELLED（重报名=新历史行）
  consent_version VARCHAR(16) NOT NULL,              -- 告知同意凭证（U69/U86）
  consent_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_activity_user (activity_id, user_id)       -- 同账号仅一条 ACTIVE 由业务层保证（U68）
);
CREATE TABLE adoptions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_user_id BIGINT NOT NULL,
  city VARCHAR(16) NOT NULL,
  title VARCHAR(64) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  contact VARCHAR(64) NOT NULL,
  pics TEXT NULL,
  expire_on DATE NOT NULL,                           -- 过 30 天不确认停止公开（U90）
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/PUBLISHED/ENDED/REMOVED
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE favorite_pois (
  user_id BIGINT NOT NULL,
  poi_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, poi_id)
);
CREATE TABLE favorite_routes (
  user_id BIGINT NOT NULL,
  route_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, route_id)
);
