package com.netgsm.asterisk.repository;

import com.netgsm.asterisk.dto.request.CdrSearchRequest;
import com.netgsm.asterisk.entity.CdrDocument;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CdrRepository {
    private final CdrJpaRepository records;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.transaction.annotation.Transactional
    public CdrDocument save(CdrDocument document) {
        if (document.getTenantId() == null || document.getTenantId() <= 0)
            throw new IllegalArgumentException("Tenant required");
        var saved = records.saveAndFlush(document);
        entityManager.refresh(saved);
        return saved;
    }

    public Optional<CdrDocument> find(String id, Long tenantId) {
        return records.findOne((root, query, cb) -> tenantId == null
                ? cb.equal(root.get("id"), id)
                : cb.and(cb.equal(root.get("id"), id), cb.equal(root.get("tenantId"), tenantId)));
    }

    public Page<CdrDocument> search(Long tenantId, CdrSearchRequest r) {
        return records.findAll(filters(tenantId, r), PageRequest.of(r.getPage(), r.getSize(),
                Sort.by(Sort.Order.desc("startTime"), Sort.Order.asc("id"))));
    }

    private Specification<CdrDocument> filters(Long tenantId, CdrSearchRequest r) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (tenantId != null) predicates.add(cb.equal(root.get("tenantId"), tenantId));
            String[] fields = {"src", "dst", "disposition", "uniqueId", "linkedId"};
            String[] values = {r.getSrc(), r.getDst(), r.getDisposition(), r.getUniqueId(), r.getLinkedId()};
            for (int i = 0; i < fields.length; i++) {
                if (values[i] == null || values[i].isBlank()) continue;
                String value = values[i].trim();
                predicates.add(i < 2 && r.isPrefix()
                        ? cb.like(root.get(fields[i]), escape(value) + "%", '!')
                        : cb.equal(root.get(fields[i]), value));
            }
            if (r.getStartDate() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), r.getStartDate()));
            if (r.getEndDate() != null) predicates.add(cb.lessThan(root.get("startTime"), r.getEndDate()));
            if (r.getMinDuration() != null) predicates.add(cb.ge(root.get("duration"), r.getMinDuration()));
            if (r.getMaxDuration() != null) predicates.add(cb.le(root.get("duration"), r.getMaxDuration()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String escape(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
