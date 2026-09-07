package com.netgsm.asterisk;

import com.netgsm.asterisk.config.CdrIndex;
import com.netgsm.asterisk.dto.request.*;
import com.netgsm.asterisk.entity.CdrDocument;
import com.netgsm.asterisk.mapper.CdrMapper;
import com.netgsm.asterisk.repository.*;
import com.netgsm.asterisk.service.*;
import com.netgsm.asterisk.exception.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CdrTests {
    final CdrElasticsearchRepository repository = mock(CdrElasticsearchRepository.class);
    final CurrentUserService current = mock(CurrentUserService.class);
    final CdrMapper mapper = new CdrMapper();
    final CdrService service = new CdrService(current, repository, mapper);
    final CdrElasticsearchRepository queryRepository = new CdrElasticsearchRepository(null, new CdrIndex("test-cdr"));

    @Test void tenantOneCanReadOwnRecordsAndCannotOverrideTenant() {
        when(current.getCurrentTenantId()).thenReturn(1L);
        var request = new CdrSearchRequest(); request.setTenantId(2L);
        var document = mapper.toDocument(input("tenant_1_internal", "1003", "1002"), 1L);
        when(repository.search(1L, request)).thenReturn(new PageImpl<>(List.of(document)));
        assertThat(service.list(request).getContent()).extracting(r -> r.tenantId()).containsExactly(1L);
        verify(repository).search(1L, request);
        verify(repository, never()).search(eq(2L), any());
    }
    @Test void tenantOneCannotReadTenantTwoDetail() {
        when(current.getCurrentTenantId()).thenReturn(1L);
        when(repository.find("tenant-two-id", 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("tenant-two-id")).isInstanceOf(ResourceNotFoundException.class);
        verify(repository).find("tenant-two-id", 1L);
    }
    @Test void superAdminCanSearchAllOrChooseTenant() {
        when(current.isSuperAdmin()).thenReturn(true);
        var request = new CdrSearchRequest();
        when(repository.search(null, request)).thenReturn(Page.empty());
        service.list(request); verify(repository).search(null, request);
        request.setTenantId(2L);
        when(repository.search(2L, request)).thenReturn(Page.empty());
        service.list(request); verify(repository).search(2L, request);
    }
    @Test void unscopedUserFailsClosed() {
        assertThatThrownBy(() -> service.list(new CdrSearchRequest())).isInstanceOf(TenantAccessDeniedException.class);
        verifyNoInteractions(repository);
    }
    @Test void srcExactAndPrefixUseTermAndPrefixWithoutWildcard() {
        var r = new CdrSearchRequest(); r.setSrc("100");
        var filters = queryRepository.query(1L, r).getQuery().bool().filter();
        assertThat(filters).anyMatch(q -> q.isTerm() && q.term().field().equals("src") && q.term().value().stringValue().equals("100"));
        r.setPrefix(true);
        assertThat(queryRepository.query(1L, r).getQuery().bool().filter()).anyMatch(q -> q.isPrefix() && q.prefix().field().equals("src"));
        assertThat(queryRepository.query(1L, r).getQuery().toString()).doesNotContain("wildcard");
    }
    @Test void dstFilterIsExact() {
        var r = new CdrSearchRequest(); r.setDst("1002");
        assertThat(queryRepository.query(1L, r).getQuery().bool().filter()).anyMatch(q -> q.isTerm() && q.term().field().equals("dst") && q.term().value().stringValue().equals("1002"));
    }
    @Test void dispositionFilterIsExact() {
        var r = new CdrSearchRequest(); r.setDisposition("ANSWERED");
        assertThat(queryRepository.query(1L, r).getQuery().bool().filter()).anyMatch(q -> q.isTerm() && q.term().field().equals("disposition") && q.term().value().stringValue().equals("ANSWERED"));
    }
    @Test void dateRangeIncludesStartExcludesEnd() {
        var r = new CdrSearchRequest(); r.setStartDate(Instant.parse("2026-09-07T00:00:00Z")); r.setEndDate(Instant.parse("2026-09-08T00:00:00Z"));
        var range = queryRepository.query(1L, r).getQuery().bool().filter().stream().filter(q -> q.isRange()).findFirst().orElseThrow().range().date();
        assertThat(range.gte()).isEqualTo(r.getStartDate().toString()); assertThat(range.lt()).isEqualTo(r.getEndDate().toString());
    }
    @Test void paginationAndStableSortingAreApplied() {
        var r = new CdrSearchRequest(); r.setPage(2); r.setSize(20);
        var q = queryRepository.query(1L, r);
        assertThat(q.getPageable().getOffset()).isEqualTo(40);
        assertThat(q.getPageable().getPageSize()).isEqualTo(20);
        assertThat(q.getSort().getOrderFor("startTime").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(q.getSort().getOrderFor("sortId")).isNotNull();
        r.setPage(500); assertThatThrownBy(r::validateRange).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void everyTenantQueryHasMandatoryTenantFilter() {
        assertThat(queryRepository.query(1L, new CdrSearchRequest()).getQuery().bool().filter())
            .anyMatch(q -> q.isTerm() && q.term().field().equals("tenantId") && q.term().value().longValue() == 1L);
    }
    @Test void unresolvedCdrIsNeverSavedAndPublishesRejection() {
        var tenants = mock(TenantRepository.class);
        var events = mock(ApplicationEventPublisher.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var ingestion = new CdrIngestionService(new ContextTenantResolver(tenants), mapper, repository, factory.getValidator(), events);
            assertThatThrownBy(() -> ingestion.ingest(input("unknown", "1003", "1002"))).isInstanceOf(PlatformException.class);
            verifyNoInteractions(repository); verify(events).publishEvent(any(CdrRejectedEvent.class));
        }
    }
    @Test void resolverChecksExistingTenantAndRejectsConflictingChannels() {
        var tenants = mock(TenantRepository.class); when(tenants.existsById(1L)).thenReturn(true);
        var resolver = new ContextTenantResolver(tenants);
        assertThat(resolver.resolveTenant(input("tenant_1_internal", "1003", "1002"))).isEqualTo(1L);
        assertThatThrownBy(() -> resolver.resolveTenant(input("tenant_2_internal", "1003", "1002"))).isInstanceOf(PlatformException.class);
        var i = input("tenant_1_internal", "1003", "1002");
        var conflict = new CdrInput(i.uniqueId(), i.linkedId(), 0, i.src(), i.dst(), null, null, i.context(), "PJSIP/tenant2_1003-0001", null, i.startTime(), null, i.endTime(), 60, 0, "NO ANSWER", null);
        assertThatThrownBy(() -> resolver.resolveTenant(conflict)).isInstanceOf(PlatformException.class);
    }
    @Test void repeatedIngestionHasStableIdAndSequenceSeparatesLegs() {
        var i = input("tenant_1_internal", "1003", "1002");
        assertThat(mapper.toDocument(i, 1L).getId()).isEqualTo(mapper.toDocument(i, 1L).getId()).isNotEqualTo(mapper.toDocument(i, 2L).getId());
        var next = new CdrInput(i.uniqueId(), i.linkedId(), 1, i.src(), i.dst(), null, null, i.context(), null, null, i.startTime(), i.answerTime(), i.endTime(), i.duration(), i.billsec(), i.disposition(), null);
        assertThat(mapper.toDocument(next, 1L).getId()).isNotEqualTo(mapper.toDocument(i, 1L).getId());
    }
    static CdrInput input(String context, String src, String dst) {
        return new CdrInput("1788756112.43", "1788756112.43", 0, src, dst, "Elif", "Eren", context, null, null,
            Instant.parse("2026-09-07T07:22:14Z"), Instant.parse("2026-09-07T07:22:18Z"), Instant.parse("2026-09-07T07:23:32Z"), 78, 74, "ANSWERED", null);
    }
}

