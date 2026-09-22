-- cutePet pet-service · owner schema: cutepet_pet
CREATE TABLE pets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  name VARCHAR(20) NOT NULL,
  species VARCHAR(8) NOT NULL,                       -- 犬/猫/异宠
  breed VARCHAR(32) NOT NULL DEFAULT '',
  sex VARCHAR(8) NULL,
  neutered TINYINT NULL,
  birth_date DATE NULL,
  photo VARCHAR(256) NOT NULL DEFAULT '',
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',       -- ACTIVE/ARCHIVED/DELETED(软删30天)
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE health_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  kind VARCHAR(8) NOT NULL,                          -- 疫苗/驱虫/体检/就医/用药/过敏
  event_date DATE NOT NULL,
  valid_from DATE NULL,                              -- BR-07 用药起止
  valid_to DATE NULL,
  org VARCHAR(64) NULL,
  doctor VARCHAR(32) NULL,
  note VARCHAR(500) NOT NULL DEFAULT '',
  photo VARCHAR(256) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_pet_date (pet_id, event_date)
);
CREATE TABLE weight_samples (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  measured_at DATETIME NOT NULL,
  weight_kg DECIMAL(6,2) NOT NULL,                   -- 统一 kg（决议）
  note VARCHAR(200) NOT NULL DEFAULT '',
  created_by BIGINT NOT NULL,
  KEY idx_pet_time (pet_id, measured_at)
);
CREATE TABLE reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  type VARCHAR(16) NOT NULL,                         -- 疫苗/驱虫/喂药/体检/复诊/自定义
  title VARCHAR(64) NOT NULL DEFAULT '',
  recurrence_kind VARCHAR(12) NOT NULL,              -- once/everyDays/yearly
  recurrence_rule VARCHAR(120) NOT NULL,
  basis VARCHAR(12) NOT NULL DEFAULT 'plan',         -- plan/completion（U55 双基准）
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',       -- ACTIVE/DONE/EXPIRED/CLOSED
  next_due DATE NULL,
  overdue_reminded TINYINT NOT NULL DEFAULT 0,       -- 3 天过期二次提醒（决议）
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE plans (                                  -- T2.6 计划/待办分离
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  kind VARCHAR(16) NOT NULL,                         -- PLAN
  title VARCHAR(64) NOT NULL,
  recurrence_kind VARCHAR(12) NOT NULL,
  recurrence_rule VARCHAR(120) NOT NULL,
  basis VARCHAR(12) NOT NULL DEFAULT 'plan',
  state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE plan_todos (                            -- 待办实例（U57 同期唯一）
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  due_date DATE NOT NULL,
  state VARCHAR(16) NOT NULL DEFAULT 'PENDING',      -- PENDING/DONE/SKIPPED
  completed_at DATETIME NULL,
  completed_by BIGINT NULL,
  skip_reason VARCHAR(200) NULL,
  UNIQUE KEY uk_plan_due (plan_id, due_date)
);
