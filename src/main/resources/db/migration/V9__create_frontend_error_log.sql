CREATE TABLE IF NOT EXISTS frontend_error_log (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(1024) NOT NULL,
    error_type VARCHAR(255),
    severity VARCHAR(16) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    release VARCHAR(128),
    environment VARCHAR(64),
    user_id VARCHAR(64),
    user_email VARCHAR(255),
    session_id VARCHAR(128),
    component VARCHAR(255),
    url VARCHAR(2048),
    ip_address VARCHAR(64),
    browser VARCHAR(255),
    user_agent VARCHAR(512),
    stack_trace TEXT,
    metadata_json TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_frontend_error_severity_time
    ON frontend_error_log (severity, received_at);

CREATE INDEX IF NOT EXISTS idx_frontend_error_component
    ON frontend_error_log (component);
