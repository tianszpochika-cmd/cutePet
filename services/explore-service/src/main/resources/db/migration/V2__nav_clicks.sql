-- cutePet explore-service · V2：导航点击计数（转化指标）
ALTER TABLE pois ADD COLUMN nav_clicks INT NOT NULL DEFAULT 0;
