-- =============================================================================
-- Ground Station Resource Management System (GSRM)
-- Database Schema DDL Script
-- 
-- @author Jeff
-- @since 2026-03-06
-- =============================================================================

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    locked BOOLEAN NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER
);

-- Ground Stations Table
CREATE TABLE IF NOT EXISTS ground_stations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20),
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    altitude DOUBLE,
    setup_time INTEGER NOT NULL DEFAULT 300,
    teardown_time INTEGER NOT NULL DEFAULT 300,
    frequency_band VARCHAR(10) NOT NULL,
    min_elevation DOUBLE DEFAULT 5.0,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    description VARCHAR(500),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Station Unavailabilities Table
CREATE TABLE IF NOT EXISTS station_unavailabilities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ground_station_id INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    maintenance_type VARCHAR(50),
    is_recurring BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    FOREIGN KEY (ground_station_id) REFERENCES ground_stations(id)
);

-- Satellites Table
CREATE TABLE IF NOT EXISTS satellites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20),
    company VARCHAR(100),
    frequency_band VARCHAR(10) NOT NULL,
    min_daily_passes INTEGER NOT NULL DEFAULT 1,
    min_pass_duration INTEGER NOT NULL DEFAULT 60,
    priority_weight INTEGER NOT NULL DEFAULT 50,
    is_emergency BOOLEAN NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    description VARCHAR(500),
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ground Station Preferences Table
CREATE TABLE IF NOT EXISTS ground_station_preferences (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    satellite_id INTEGER NOT NULL,
    ground_station_id INTEGER NOT NULL,
    preference_order INTEGER NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT 0,
    notes VARCHAR(255),
    FOREIGN KEY (satellite_id) REFERENCES satellites(id),
    FOREIGN KEY (ground_station_id) REFERENCES ground_stations(id),
    UNIQUE(satellite_id, ground_station_id),
    UNIQUE(satellite_id, preference_order)
);

-- Schedule Sessions Table
CREATE TABLE IF NOT EXISTS schedule_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    schedule_start_time TIMESTAMP NOT NULL,
    schedule_end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    shortening_strategy VARCHAR(50) DEFAULT 'PROPORTIONAL',
    executed_at TIMESTAMP,
    executed_by INTEGER,
    total_requests INTEGER DEFAULT 0,
    scheduled_count INTEGER DEFAULT 0,
    rejected_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER
);

-- Session-Satellites Junction Table
CREATE TABLE IF NOT EXISTS session_satellites (
    session_id INTEGER NOT NULL,
    satellite_id INTEGER NOT NULL,
    PRIMARY KEY (session_id, satellite_id),
    FOREIGN KEY (session_id) REFERENCES schedule_sessions(id),
    FOREIGN KEY (satellite_id) REFERENCES satellites(id)
);

-- Session-Ground Stations Junction Table
CREATE TABLE IF NOT EXISTS session_ground_stations (
    session_id INTEGER NOT NULL,
    ground_station_id INTEGER NOT NULL,
    PRIMARY KEY (session_id, ground_station_id),
    FOREIGN KEY (session_id) REFERENCES schedule_sessions(id),
    FOREIGN KEY (ground_station_id) REFERENCES ground_stations(id)
);

-- Satellite Requests Table
CREATE TABLE IF NOT EXISTS satellite_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    external_request_id VARCHAR(100),
    schedule_session_id INTEGER NOT NULL,
    satellite_id INTEGER NOT NULL,
    ground_station_id INTEGER NOT NULL,
    frequency_band VARCHAR(10) NOT NULL,
    aos TIMESTAMP NOT NULL,
    los TIMESTAMP NOT NULL,
    max_elevation DOUBLE,
    priority INTEGER DEFAULT 5,
    is_emergency BOOLEAN NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    import_batch_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    FOREIGN KEY (schedule_session_id) REFERENCES schedule_sessions(id),
    FOREIGN KEY (satellite_id) REFERENCES satellites(id),
    FOREIGN KEY (ground_station_id) REFERENCES ground_stations(id)
);

-- Scheduled Passes Table
CREATE TABLE IF NOT EXISTS scheduled_passes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_session_id INTEGER NOT NULL,
    satellite_request_id INTEGER,
    satellite_id INTEGER NOT NULL,
    ground_station_id INTEGER NOT NULL,
    frequency_band VARCHAR(10) NOT NULL,
    original_aos TIMESTAMP NOT NULL,
    original_los TIMESTAMP NOT NULL,
    scheduled_aos TIMESTAMP,
    scheduled_los TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_allowed BOOLEAN NOT NULL DEFAULT 0,
    shortened_seconds INTEGER DEFAULT 0,
    rejection_reason VARCHAR(500),
    conflict_with_pass_id INTEGER,
    is_forced BOOLEAN NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    FOREIGN KEY (schedule_session_id) REFERENCES schedule_sessions(id),
    FOREIGN KEY (satellite_request_id) REFERENCES satellite_requests(id),
    FOREIGN KEY (satellite_id) REFERENCES satellites(id),
    FOREIGN KEY (ground_station_id) REFERENCES ground_stations(id)
);

-- Create Indexes
CREATE INDEX IF NOT EXISTS idx_unavail_station ON station_unavailabilities(ground_station_id);
CREATE INDEX IF NOT EXISTS idx_unavail_time ON station_unavailabilities(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_pref_satellite ON ground_station_preferences(satellite_id);
CREATE INDEX IF NOT EXISTS idx_session_status ON schedule_sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_time ON schedule_sessions(schedule_start_time, schedule_end_time);
CREATE INDEX IF NOT EXISTS idx_request_session ON satellite_requests(schedule_session_id);
CREATE INDEX IF NOT EXISTS idx_request_satellite ON satellite_requests(satellite_id);
CREATE INDEX IF NOT EXISTS idx_request_station ON satellite_requests(ground_station_id);
CREATE INDEX IF NOT EXISTS idx_request_aos ON satellite_requests(aos);
CREATE INDEX IF NOT EXISTS idx_request_status ON satellite_requests(status);
CREATE INDEX IF NOT EXISTS idx_pass_session ON scheduled_passes(schedule_session_id);
CREATE INDEX IF NOT EXISTS idx_pass_satellite ON scheduled_passes(satellite_id);
CREATE INDEX IF NOT EXISTS idx_pass_station ON scheduled_passes(ground_station_id);
CREATE INDEX IF NOT EXISTS idx_pass_aos ON scheduled_passes(scheduled_aos);
CREATE INDEX IF NOT EXISTS idx_pass_status ON scheduled_passes(status);
