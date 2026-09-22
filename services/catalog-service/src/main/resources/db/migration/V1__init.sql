-- cutePet catalog-service · owner schema: cutepet_catalog（方案 A 无交易）
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  brand VARCHAR(32) NOT NULL DEFAULT '',
  category VARCHAR(16) NOT NULL,                     -- 7 类；兽药/处方类由校验层拒绝（BR-06/U77）
  specs VARCHAR(255) NOT NULL DEFAULT '',
  price_min DECIMAL(10,2) NULL,
  price_max DECIMAL(10,2) NULL,
  cover VARCHAR(256) NOT NULL DEFAULT '',
  tags VARCHAR(255) NOT NULL DEFAULT '',
  source_type VARCHAR(16) NOT NULL DEFAULT 'EDITORIAL', -- 来源字段（T5.3）
  source_ref VARCHAR(255) NOT NULL DEFAULT '',
  editor_test_only TINYINT NOT NULL DEFAULT 0,       -- 编辑部测试标识（强制字段）
  state VARCHAR(16) NOT NULL DEFAULT 'ON_SHELF',     -- ON_SHELF/OFF_SHELF/CLEARED(治理清除)
  hot_score INT NOT NULL DEFAULT 0,                  -- 近30天 浏览+收藏*3（决议）
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_category_state (category, state)
);
CREATE TABLE product_favorites (
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, product_id)
);
CREATE TABLE product_lists (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NULL,                         -- NULL=运营官方清单
  title VARCHAR(64) NOT NULL,
  intro VARCHAR(500) NOT NULL DEFAULT '',
  kind VARCHAR(16) NOT NULL DEFAULT 'OFFICIAL',      -- OFFICIAL/AUTHOR（清单类型 T3.6/T5.3）
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE list_items (
  list_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  ord INT NOT NULL DEFAULT 0,
  reason VARCHAR(200) NOT NULL DEFAULT '',
  PRIMARY KEY (list_id, product_id)
);
CREATE TABLE article_product_bindings (
  article_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  ord INT NOT NULL DEFAULT 0,
  PRIMARY KEY (article_id, product_id)
);
CREATE TABLE product_import_jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_by BIGINT NOT NULL,
  filename VARCHAR(128) NOT NULL,
  total INT NOT NULL DEFAULT 0,
  accepted INT NOT NULL DEFAULT 0,
  rejected INT NOT NULL DEFAULT 0,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
