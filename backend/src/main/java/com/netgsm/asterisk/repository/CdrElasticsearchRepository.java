package com.netgsm.asterisk.repository;

import com.netgsm.asterisk.config.CdrIndex;
import com.netgsm.asterisk.dto.request.CdrSearchRequest;
import com.netgsm.asterisk.entity.CdrDocument;
import com.netgsm.asterisk.exception.PlatformException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import java.util.ArrayList;
import java.util.List;

@Repository @RequiredArgsConstructor
public class CdrElasticsearchRepository {
    private final ElasticsearchOperations operations;
    private final CdrIndex index;

    public Page<CdrDocument> search(Long tenantId, CdrSearchRequest request) {
        try {
            if (!operations.indexOps(index.coordinates()).exists()) return Page.empty(PageRequest.of(request.getPage(), request.getSize()));
            var query = query(tenantId, request);
            var hits = operations.search(query, CdrDocument.class, index.coordinates());
            return new PageImpl<>(hits.getSearchHits().stream().map(hit -> hit.getContent()).toList(),
                    query.getPageable(), hits.getTotalHits());
        } catch (org.springframework.dao.DataAccessException ex) { throw unavailable(); }
    }
    public Optional<CdrDocument> find(String id, Long tenantId) {
        try {
            if (!operations.indexOps(index.coordinates()).exists()) return Optional.empty();
            var criteria = new Criteria("sortId").is(id);
            if (tenantId != null) criteria = criteria.and("tenantId").is(tenantId);
            var hit = operations.searchOne(new CriteriaQuery(criteria), CdrDocument.class, index.coordinates());
            return Optional.ofNullable(hit).map(value -> value.getContent());
        } catch (org.springframework.dao.DataAccessException ex) { throw unavailable(); }
    }
    public CdrDocument save(CdrDocument document) {
        if (document.getTenantId() == null || document.getTenantId() <= 0) throw new IllegalArgumentException("Tenant required");
        try {
            ensureIndex();
            return operations.save(document, index.coordinates());
        } catch (org.springframework.dao.DataAccessException ex) { throw unavailable(); }
    }
    private synchronized void ensureIndex() {
        var ops = operations.indexOps(index.coordinates());
        if (!ops.exists()) {
            try {
                ops.create(java.util.Map.of("number_of_shards", 1, "number_of_replicas", 0),
                        operations.indexOps(CdrDocument.class).createMapping());
            } catch (org.springframework.dao.DataAccessException ex) {
                if (!ops.exists()) throw ex;
            }
        }
    }
    public NativeQuery query(Long tenantId, CdrSearchRequest r) {
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(q -> q.exists(e -> e.field("tenantId"))));
        if (tenantId != null) filters.add(Query.of(q -> q.term(t -> t.field("tenantId").value(tenantId))));
        if (present(r.getSrc())) filters.add(number("src", r.getSrc(), r.isPrefix()));
        if (present(r.getDst())) filters.add(number("dst", r.getDst(), r.isPrefix()));
        if (present(r.getDisposition())) filters.add(number("disposition", r.getDisposition(), false));
        if (present(r.getUniqueId())) filters.add(number("uniqueId", r.getUniqueId(), false));
        if (present(r.getLinkedId())) filters.add(number("linkedId", r.getLinkedId(), false));
        if (r.getStartDate() != null || r.getEndDate() != null) filters.add(Query.of(q -> q.range(range -> range.date(d -> {
            d.field("startTime");
            if (r.getStartDate() != null) d.gte(r.getStartDate().toString());
            if (r.getEndDate() != null) d.lt(r.getEndDate().toString());
            return d;
        }))));
        if (r.getMinDuration() != null || r.getMaxDuration() != null) filters.add(Query.of(q -> q.range(range -> range.number(n -> {
            n.field("duration");
            if (r.getMinDuration() != null) n.gte(r.getMinDuration().doubleValue());
            if (r.getMaxDuration() != null) n.lte(r.getMaxDuration().doubleValue());
            return n;
        }))));
        var query = NativeQuery.builder().withQuery(q -> q.bool(b -> b.filter(filters))).build();
        query.setPageable(PageRequest.of(r.getPage(), r.getSize(), Sort.by(Sort.Order.desc("startTime"), Sort.Order.asc("sortId"))));
        query.setTrackTotalHits(true);
        return query;
    }
    private Query number(String field, String value, boolean prefix) {
        return prefix ? Query.of(q -> q.prefix(p -> p.field(field).value(value.trim())))
                : Query.of(q -> q.term(t -> t.field(field).value(value.trim())));
    }
    private boolean present(String value) { return value != null && !value.isBlank(); }
    private PlatformException unavailable() { return new PlatformException(503, "CDR_UNAVAILABLE", "CDR search is temporarily unavailable"); }
}
