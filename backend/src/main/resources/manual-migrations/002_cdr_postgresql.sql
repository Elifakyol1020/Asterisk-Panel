-- Apply before starting the updated backend on an existing database.
CREATE TABLE IF NOT EXISTS platform.cdr (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES platform.tenants(id),
    cdr_sequence INTEGER NOT NULL CHECK (cdr_sequence >= 0),
    unique_id VARCHAR(150) NOT NULL,
    linked_id VARCHAR(150),
    src VARCHAR(255) NOT NULL,
    dst VARCHAR(255) NOT NULL,
    src_name VARCHAR(255),
    dst_name VARCHAR(255),
    context VARCHAR(255),
    channel VARCHAR(255),
    dst_channel VARCHAR(255),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    answer_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration >= 0),
    billsec INTEGER NOT NULL CHECK (billsec >= 0 AND billsec <= duration),
    disposition VARCHAR(32) NOT NULL,
    recording_path VARCHAR(1000),
    UNIQUE (tenant_id, unique_id, cdr_sequence),
    CHECK (end_time >= start_time)
);
CREATE INDEX IF NOT EXISTS cdr_tenant_start_idx ON platform.cdr (tenant_id, start_time DESC, id);
CREATE INDEX IF NOT EXISTS cdr_start_idx ON platform.cdr (start_time DESC, id);
CREATE INDEX IF NOT EXISTS cdr_tenant_src_idx ON platform.cdr (tenant_id, src);
CREATE INDEX IF NOT EXISTS cdr_tenant_dst_idx ON platform.cdr (tenant_id, dst);
CREATE INDEX IF NOT EXISTS cdr_linked_idx ON platform.cdr (tenant_id, linked_id);
