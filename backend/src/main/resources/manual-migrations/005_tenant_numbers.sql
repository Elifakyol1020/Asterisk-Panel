-- Run with psql -v ON_ERROR_STOP=1 while the backend is stopped, before deploying.
BEGIN;
LOCK TABLE platform.tenants IN ACCESS EXCLUSIVE MODE;
CREATE TABLE IF NOT EXISTS platform.tenant_number_migration (
    tenant_id BIGINT PRIMARY KEY,
    old_code VARCHAR(48) NOT NULL,
    new_code VARCHAR(6) NOT NULL UNIQUE
);
-- Preserve valid numbers; allocate unused six-digit numbers to legacy codes.
DO $$
DECLARE item RECORD; candidate INTEGER := 100000;
BEGIN
    FOR item IN SELECT id, code FROM platform.tenants WHERE code !~ '^[0-9]{4,6}$' ORDER BY id LOOP
        WHILE EXISTS (SELECT 1 FROM platform.tenants WHERE code = candidate::text) LOOP
            candidate := candidate + 1;
        END LOOP;
        IF candidate > 999999 THEN RAISE EXCEPTION 'Tenant number range exhausted'; END IF;
        INSERT INTO platform.tenant_number_migration VALUES (item.id, item.code, candidate::text);
        UPDATE platform.tenants SET code = candidate::text, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE id = item.id;
        candidate := candidate + 1;
    END LOOP;
END $$;
ALTER TABLE platform.tenants DROP CONSTRAINT IF EXISTS tenants_number_format;
ALTER TABLE platform.tenants ADD CONSTRAINT tenants_number_format CHECK (code ~ '^[0-9]{4,6}$');
COMMIT;
SELECT id, name, code FROM platform.tenants ORDER BY id;
