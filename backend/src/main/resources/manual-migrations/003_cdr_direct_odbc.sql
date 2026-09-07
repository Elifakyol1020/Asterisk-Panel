-- PostgreSQL 15+. Apply 002 first. Safe to run again; preserves existing rows.
BEGIN;
ALTER TABLE platform.cdr ALTER COLUMN id SET DEFAULT gen_random_uuid()::text;
ALTER TABLE platform.cdr ALTER COLUMN tenant_id DROP NOT NULL;
-- Remove the previously proposed trigger, if it was installed.
DROP TRIGGER IF EXISTS cdr_prepare_direct ON platform.cdr;
DROP FUNCTION IF EXISTS platform.prepare_direct_cdr();
COMMIT;
