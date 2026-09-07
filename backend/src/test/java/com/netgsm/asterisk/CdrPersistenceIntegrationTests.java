package com.netgsm.asterisk;

import com.netgsm.asterisk.dto.request.CdrInput;
import com.netgsm.asterisk.dto.request.CdrSearchRequest;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.enums.*;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.security.CurrentUser;
import com.netgsm.asterisk.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={
    "spring.config.import=", "spring.datasource.url=jdbc:h2:mem:cdr-live;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS platform",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.jwt.secret=test-only-secret-that-is-at-least-32-bytes",
    "app.jwt.expiration=60000", "app.cors.allowed-origins=https://test.invalid"
})
class CdrPersistenceIntegrationTests {
    @Autowired CdrIngestionService ingestion;
    @Autowired CdrService service;
    @Autowired TenantRepository tenants;
    @Autowired com.netgsm.asterisk.repository.CdrJpaRepository records;
    @AfterEach void cleanup() {
        SecurityContextHolder.clearContext();
    }
    @Test void unassignedDirectRecordsAreVisibleOnlyToSuperAdmin() {
        var record = new com.netgsm.asterisk.mapper.CdrMapper().toDocument(
                input(1L, "unassigned", "1003", "1002", "ANSWERED", "2026-09-07T07:00:00Z"), 1L);
        record.setTenantId(null);
        records.saveAndFlush(record);
        try {
            var request = new CdrSearchRequest(); request.setUniqueId("unassigned");
            login(Role.TENANT_ADMIN, 1L);
            assertThat(service.list(request).getTotalElements()).isZero();
            assertThatThrownBy(() -> service.get(record.getId()))
                    .isInstanceOf(com.netgsm.asterisk.exception.ResourceNotFoundException.class);
            login(Role.SUPER_ADMIN, null);
            assertThat(service.list(request).getTotalElements()).isEqualTo(1);
            assertThat(service.get(record.getId()).tenantId()).isNull();
        } finally { records.deleteById(record.getId()); }
    }
    @Test void databaseEnforcesTenantFiltersPaginationAndDetailAccess() {
        Tenant one = tenant("cdr-one"), two = tenant("cdr-two");
        var a = ingestion.ingest(input(one.getId(), "a", "1003", "1002", "ANSWERED", "2026-09-07T07:00:00Z"));
        ingestion.ingest(input(one.getId(), "b", "1004", "2002", "BUSY", "2026-09-08T07:00:00Z"));
        var hidden = ingestion.ingest(input(two.getId(), "c", "1003", "1002", "ANSWERED", "2026-09-07T07:00:00Z"));
        login(Role.TENANT_ADMIN, one.getId());
        var r = new CdrSearchRequest(); r.setTenantId(two.getId());
        assertThat(service.list(r).getTotalElements()).isEqualTo(2);
        assertThat(service.get(a.id()).tenantId()).isEqualTo(one.getId());
        assertThatThrownBy(() -> service.get(hidden.id())).isInstanceOf(com.netgsm.asterisk.exception.ResourceNotFoundException.class);
        r.setSrc("1003"); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        r.setSrc(null); r.setDst("2002"); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        r.setDst(null); r.setDisposition("ANSWERED"); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        r.setDisposition(null); r.setStartDate(Instant.parse("2026-09-07T00:00:00Z")); r.setEndDate(Instant.parse("2026-09-08T00:00:00Z"));
        assertThat(service.list(r).getContent()).extracting(v -> v.uniqueId()).containsExactly("a");
        r.setStartDate(null); r.setEndDate(null); r.setSize(1);
        assertThat(service.list(r).getContent()).extracting(v -> v.uniqueId()).containsExactly("b");
        r.setPage(1); assertThat(service.list(r).getContent()).extracting(v -> v.uniqueId()).containsExactly("a");
        r.setPage(0); r.setSize(20); r.setSrc("100"); r.setPrefix(true);
        assertThat(service.list(r).getTotalElements()).isEqualTo(2);
        r.setSrc(null); r.setTenantId(null); login(Role.SUPER_ADMIN, null);
        assertThat(service.list(r).getTotalElements()).isEqualTo(3);
        r.setMinDuration(61); assertThat(service.list(r).getTotalElements()).isZero();
        r.setMinDuration(60); r.setMaxDuration(60); assertThat(service.list(r).getTotalElements()).isEqualTo(3);
        r.setMinDuration(null); r.setMaxDuration(null);
        r.setUniqueId("a"); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        r.setUniqueId(null); r.setLinkedId("c"); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        r.setLinkedId(null);
        r.setSrc("100%"); assertThat(service.list(r).getTotalElements()).isZero();
        r.setSrc("100_"); assertThat(service.list(r).getTotalElements()).isZero();
        r.setSrc("100"); r.setPrefix(false); assertThat(service.list(r).getTotalElements()).isZero();
        r.setSrc(null);
        r.setTenantId(two.getId()); assertThat(service.list(r).getTotalElements()).isEqualTo(1);
        ingestion.ingest(input(one.getId(), "a", "1003", "1002", "ANSWERED", "2026-09-07T07:00:00Z"));
        r.setTenantId(null); assertThat(service.list(r).getTotalElements()).isEqualTo(3);
    }
    Tenant tenant(String code) { var t = new Tenant(); t.setCode(code); t.setName(code); t.setStatus(TenantStatus.ACTIVE); return tenants.save(t); }
    void login(Role role, Long tenant) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new CurrentUser(1L, tenant, role, "test"), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    CdrInput input(Long tenant, String unique, String src, String dst, String status, String start) {
        var at = Instant.parse(start);
        return new CdrInput(unique, unique, 0, src, dst, null, null, "tenant_" + tenant + "_internal", null, null,
            at, status.equals("ANSWERED") ? at.plusSeconds(4) : null, at.plusSeconds(60), 60, status.equals("ANSWERED") ? 56 : 0, status, null);
    }
}
