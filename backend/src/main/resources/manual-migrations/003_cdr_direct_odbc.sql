-- PostgreSQL only. Apply 002 first on an existing installation.
-- Also mounted after schema.sql for fresh Docker volumes.
BEGIN;
ALTER TABLE platform.cdr ALTER COLUMN id SET DEFAULT gen_random_uuid()::text;
ALTER TABLE platform.cdr ALTER COLUMN tenant_id DROP NOT NULL;

-- Never trust a caller-supplied tenant_id. Match the existing provisioning names.
-- Ambiguous/unknown tenants remain NULL: retained for SUPER_ADMIN, invisible to tenants.
CREATE OR REPLACE FUNCTION platform.prepare_direct_cdr()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    candidates TEXT[];
    resolved BIGINT;
BEGIN
    NEW.id := COALESCE(NULLIF(btrim(NEW.id), ''), gen_random_uuid()::text);
    NEW.src := btrim(NEW.src);
    NEW.dst := btrim(NEW.dst);
    NEW.unique_id := btrim(NEW.unique_id);
    NEW.disposition := upper(btrim(NEW.disposition));
    -- cdr_adaptive_odbc usegmtime=yes can render an unanswered timestamp as epoch.
    IF NEW.answer_time <= TIMESTAMPTZ '1970-01-01 00:00:00+00' THEN
        NEW.answer_time := NULL;
    END IF;
    SELECT array_agg(DISTINCT value) INTO candidates
    FROM unnest(ARRAY[
        substring(btrim(NEW.context) from '^tenant_([1-9][0-9]*)_.+$'),
        substring(btrim(NEW.channel) from '^(?:PJSIP|SIP)/tenant([1-9][0-9]*)_.+$'),
        substring(btrim(NEW.dst_channel) from '^(?:PJSIP|SIP)/tenant([1-9][0-9]*)_.+$')
    ]) AS names(value) WHERE value IS NOT NULL;
    NEW.tenant_id := NULL;
    IF cardinality(candidates) = 1 THEN
        -- Compare as text so malformed oversized numbers cannot overflow BIGINT.
        SELECT id INTO resolved FROM platform.tenants WHERE id::text = candidates[1];
        NEW.tenant_id := resolved;
    END IF;
    RETURN NEW;
END;
$$;
DROP TRIGGER IF EXISTS cdr_prepare_direct ON platform.cdr;
CREATE TRIGGER cdr_prepare_direct
BEFORE INSERT OR UPDATE ON platform.cdr
FOR EACH ROW EXECUTE FUNCTION platform.prepare_direct_cdr();
COMMIT;
