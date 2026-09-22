-- cutePet audit-service · owner schema: cutepet_audit（IP 必录——决议 2026-09-22）
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator VARCHAR(32) NOT NULL,
  operator_role VARCHAR(32) NOT NULL DEFAULT '',
  ip VARCHAR(45) NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(32) NULL,
  target_id VARCHAR(64) NULL,
  before_value TEXT NULL,
  after_value TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_action_time (action, created_at),
  KEY idx_operator_time (operator, created_at)
);
