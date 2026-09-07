-- Legacy CSV checkpoint migration, retained for migration history only.
-- Direct ODBC CDR does not use this table. Do not apply for new installations.
CREATE TABLE IF NOT EXISTS platform.cdr_tail_state (
    id VARCHAR(64) PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    byte_offset BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
