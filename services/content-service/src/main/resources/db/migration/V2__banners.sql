-- cutePet content-service · owner schema: cutepet_content（V1 追加：推荐位）
CREATE TABLE IF NOT EXISTS banners (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slot VARCHAR(32) NOT NULL,                         -- home_top/product_hero 等
  title VARCHAR(128) NOT NULL DEFAULT '',
  target_url VARCHAR(255) NULL,
  starts_at DATETIME NULL,
  ends_at DATETIME NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  ord INT NOT NULL DEFAULT 0,
  KEY idx_slot_state (slot, state)
);
