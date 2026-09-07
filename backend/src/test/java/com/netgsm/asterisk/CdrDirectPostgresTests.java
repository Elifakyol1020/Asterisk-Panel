package com.netgsm.asterisk;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.assertj.core.api.Assertions.*;

/** Real PostgreSQL table/default test. All objects live in a random schema and are rolled back. */
@EnabledIfEnvironmentVariable(named = "CDR_PG_TEST_URL", matches = ".+")
class CdrDirectPostgresTests {
    @Test void directOdbcInsertGeneratesIdWithoutTrigger() throws Exception {
        try (var db = DriverManager.getConnection(System.getenv("CDR_PG_TEST_URL"),
                System.getenv("CDR_PG_TEST_USER"), System.getenv("CDR_PG_TEST_PASSWORD"))) {
            db.setAutoCommit(false);
            String schema = "cdr_test_" + UUID.randomUUID().toString().replace("-", "");
            try (var stmt = db.createStatement()) {
                stmt.execute("CREATE SCHEMA " + schema);
                stmt.execute("CREATE TABLE " + schema + ".tenants (id BIGINT PRIMARY KEY)");
                stmt.execute("INSERT INTO " + schema + ".tenants VALUES (1), (2)");
                stmt.execute(resource("002_cdr_postgresql.sql").replace("platform.", schema + "."));
                String migration = resource("003_cdr_direct_odbc.sql")
                        .replace("platform.", schema + ".").replace("BEGIN;", "").replace("COMMIT;", "");
                stmt.execute(migration);
                // Reapplying the migration is safe.
                stmt.execute(migration);
                stmt.execute("SET TIME ZONE 'UTC'");
                String insert = "INSERT INTO " + schema + ".cdr "
                        + "(tenant_id, unique_id, cdr_sequence, src, dst, context, channel, dst_channel, "
                        + "start_time, answer_time, end_time, duration, billsec, disposition) "
                        + "VALUES (NULL, ?, ?, ' 1003 ', '1002', ?, ?, ?, "
                        + "'2026-09-07 07:00:00', '1970-01-01 00:00:00', '2026-09-07 07:01:00', 60, 0, 'NO ANSWER') "
                        + "RETURNING id, tenant_id, src, answer_time, start_time";
                try (var write = db.prepareStatement(insert)) {
                    check(write, "known", 0, "tenant_1_internal", "PJSIP/tenant1_1003-001", null, 1L);
                    check(write, "known", 1, "tenant_1_internal", null, null, 1L);
                    check(write, "channel", 0, "from-trunk", null, "PJSIP/tenant2_1002-001", 2L);
                    check(write, "conflict", 0, "tenant_1_internal", "PJSIP/tenant2_1003-001", null, null);
                    check(write, "unknown", 0, "from-trunk", null, null, null);
                    check(write, "missing", 0, "tenant_3_internal", null, null, null);
                    check(write, "overflow", 0, "tenant_999999999999999999999999_internal", null, null, null);
                }
            } finally { db.rollback(); }
        }
    }

    private void check(PreparedStatement write, String unique, int sequence, String context,
                       String channel, String dstChannel, Long tenant) throws SQLException {
        write.setString(1, unique); write.setInt(2, sequence); write.setString(3, context);
        write.setString(4, channel); write.setString(5, dstChannel);
        try (var row = write.executeQuery()) {
            assertThat(row.next()).isTrue();
            assertThat(row.getString("id")).isNotBlank();
            assertThat(row.getObject("tenant_id", Long.class)).isNull();
            assertThat(row.getString("src")).isEqualTo(" 1003 ");
            assertThat(row.getTimestamp("answer_time").toInstant()).isEqualTo(java.time.Instant.EPOCH);
            assertThat(row.getTimestamp("start_time").toInstant()).isEqualTo("2026-09-07T07:00:00Z");
        }
    }

    private String resource(String name) throws Exception {
        try (var stream = getClass().getResourceAsStream("/manual-migrations/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing migration " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
