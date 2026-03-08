-- =============================================================================
-- Ground Station Resource Management System (GSRM)
-- Initial Test Data DML Script
-- 
-- @author Jeff
-- @since 2026-03-06
-- =============================================================================

-- =============================================================================
-- Demo Passwords (BCrypt cost=10, all share same demo password: gsrm@2026)
--   admin    / gsrm@2026
--   operator / gsrm@2026
--   viewer   / gsrm@2026
--
-- To regenerate a hash in Java:
--   new BCryptPasswordEncoder().encode("gsrm@2026")
-- =============================================================================

-- 開發用明文密碼（{noop} 前綴），密碼皆為 admin123 / operator123 / viewer123
INSERT OR IGNORE INTO users (username, password, email, display_name, role, enabled, locked)
VALUES
  ('admin',    '{noop}admin123',    'admin@gsrm.com',    'System Administrator', 'ADMIN',    1, 0),
  ('operator', '{noop}operator123', 'operator@gsrm.com', 'Schedule Operator',    'OPERATOR', 1, 0),
  ('viewer',   '{noop}viewer123',   'viewer@gsrm.com',   'Read-only Viewer',     'VIEWER',   1, 0);

-- Insert Ground Stations (Taiwan and Asia-Pacific Region)
INSERT OR IGNORE INTO ground_stations (name, code, longitude, latitude, altitude, setup_time, teardown_time, frequency_band, min_elevation, enabled, description)
VALUES 
    ('Taiwan Taipei', 'TPE', 121.5654, 25.0330, 10.0, 300, 300, 'XS', 5.0, 1, 'Main ground station in Taipei, Taiwan'),
    ('Taiwan Hsinchu', 'HSZ', 120.9647, 24.8066, 50.0, 300, 300, 'X', 5.0, 1, 'Space center ground station in Hsinchu'),
    ('Taiwan Pingtung', 'PIF', 120.4879, 22.6727, 20.0, 300, 300, 'S', 5.0, 1, 'Southern ground station in Pingtung'),
    ('Japan Tokyo', 'TYO', 139.6917, 35.6895, 40.0, 360, 360, 'XS', 5.0, 1, 'Ground station in Tokyo, Japan'),
    ('Japan Okinawa', 'OKA', 127.6809, 26.2124, 15.0, 300, 300, 'X', 5.0, 1, 'Ground station in Okinawa, Japan'),
    ('Korea Seoul', 'SEL', 126.9780, 37.5665, 30.0, 300, 300, 'XS', 5.0, 1, 'Ground station in Seoul, Korea'),
    ('Australia Sydney', 'SYD', 151.2093, -33.8688, 58.0, 360, 360, 'XS', 5.0, 1, 'Ground station in Sydney, Australia'),
    ('Singapore', 'SIN', 103.8198, 1.3521, 15.0, 300, 300, 'S', 5.0, 1, 'Ground station in Singapore'),
    ('India Bangalore', 'BLR', 77.5946, 12.9716, 920.0, 300, 300, 'XS', 5.0, 1, 'ISRO ground station in Bangalore'),
    ('USA California', 'LAX', -118.2437, 34.0522, 71.0, 360, 360, 'XS', 5.0, 1, 'NASA DSN station in California');

-- Insert Satellites
INSERT OR IGNORE INTO satellites (name, code, company, frequency_band, min_daily_passes, min_pass_duration, priority_weight, is_emergency, enabled, description)
VALUES 
    ('FormoSat-7A', 'FS7A', 'NSPO Taiwan', 'X', 2, 120, 80, 0, 1, 'Weather observation satellite'),
    ('FormoSat-7B', 'FS7B', 'NSPO Taiwan', 'X', 2, 120, 80, 0, 1, 'Weather observation satellite'),
    ('FormoSat-7C', 'FS7C', 'NSPO Taiwan', 'X', 2, 120, 80, 0, 1, 'Weather observation satellite'),
    ('FormoSat-8', 'FS8', 'NSPO Taiwan', 'S', 3, 90, 90, 0, 1, 'Remote sensing satellite'),
    ('TRITON', 'TRI', 'NSPO Taiwan', 'X', 2, 60, 70, 0, 1, 'Technology demonstration satellite'),
    ('Himawari-9', 'HIM9', 'JMA Japan', 'XS', 4, 180, 95, 0, 1, 'Geostationary weather satellite'),
    ('KOMPSAT-7', 'KMP7', 'KARI Korea', 'X', 2, 90, 75, 0, 1, 'Earth observation satellite'),
    ('LANDSAT-9', 'LS9', 'NASA', 'S', 3, 120, 85, 0, 1, 'Earth observation satellite'),
    ('Sentinel-2A', 'S2A', 'ESA', 'X', 2, 90, 80, 0, 1, 'Copernicus earth observation'),
    ('Sentinel-2B', 'S2B', 'ESA', 'X', 2, 90, 80, 0, 1, 'Copernicus earth observation'),
    ('EMERGENCY-SAT', 'EMRG', 'UN COPUOS', 'XS', 6, 60, 100, 1, 1, 'Emergency response satellite');

-- Insert Ground Station Preferences for Satellites
INSERT OR IGNORE INTO ground_station_preferences (satellite_id, ground_station_id, preference_order, is_mandatory)
SELECT s.id, g.id, 1, 0
FROM satellites s, ground_stations g
WHERE s.name = 'FormoSat-7A' AND g.name = 'Taiwan Hsinchu';

INSERT OR IGNORE INTO ground_station_preferences (satellite_id, ground_station_id, preference_order, is_mandatory)
SELECT s.id, g.id, 2, 0
FROM satellites s, ground_stations g
WHERE s.name = 'FormoSat-7A' AND g.name = 'Taiwan Taipei';

INSERT OR IGNORE INTO ground_station_preferences (satellite_id, ground_station_id, preference_order, is_mandatory)
SELECT s.id, g.id, 1, 0
FROM satellites s, ground_stations g
WHERE s.name = 'FormoSat-8' AND g.name = 'Taiwan Hsinchu';

INSERT OR IGNORE INTO ground_station_preferences (satellite_id, ground_station_id, preference_order, is_mandatory)
SELECT s.id, g.id, 1, 0
FROM satellites s, ground_stations g
WHERE s.name = 'Himawari-9' AND g.name = 'Japan Tokyo';

INSERT OR IGNORE INTO ground_station_preferences (satellite_id, ground_station_id, preference_order, is_mandatory)
SELECT s.id, g.id, 2, 0
FROM satellites s, ground_stations g
WHERE s.name = 'Himawari-9' AND g.name = 'Japan Okinawa';

-- Insert Sample Maintenance Windows
INSERT OR IGNORE INTO station_unavailabilities (ground_station_id, start_time, end_time, reason, maintenance_type, is_recurring)
SELECT id, datetime('now', '+7 days', 'start of day', '+2 hours'), 
       datetime('now', '+7 days', 'start of day', '+6 hours'),
       'Scheduled weekly maintenance', 'SCHEDULED', 0
FROM ground_stations WHERE name = 'Taiwan Hsinchu';

INSERT OR IGNORE INTO station_unavailabilities (ground_station_id, start_time, end_time, reason, maintenance_type, is_recurring)
SELECT id, datetime('now', '+14 days', 'start of day', '+1 hours'), 
       datetime('now', '+14 days', 'start of day', '+3 hours'),
       'Equipment calibration', 'CALIBRATION', 0
FROM ground_stations WHERE name = 'Taiwan Taipei';
