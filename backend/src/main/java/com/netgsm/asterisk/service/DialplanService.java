package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.CreateDialplanRequest;
import com.netgsm.asterisk.dto.DialplanResponse;
import com.netgsm.asterisk.dto.UpdateDialplanRequest;
import com.netgsm.asterisk.entity.Dialplan;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.DialplanMapper;
import com.netgsm.asterisk.repository.DialplanRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class DialplanService {
    private final DialplanMapper mapper;
    private final DialplanRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;

    @Transactional(readOnly = true)
    public Page<DialplanResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DialplanResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public DialplanResponse create(CreateDialplanRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtensionAndPriority(tenantId, request.extension(), request.priority())) throw new DuplicateResourceException("Dialplan");
        references.validateDialplan(request.application(), request.applicationData());
        Dialplan entity = mapper.toEntity(request, tenantId);

        entity.setContext(current.context(tenantId, "internal"));
        repository.saveAndFlush(entity);
        log.info("Dialplan created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public DialplanResponse update(Long id, UpdateDialplanRequest request) {
        Dialplan entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionAndPriorityAndIdNot(tenantId, request.extension(), request.priority(), id)) throw new DuplicateResourceException("Dialplan");
        references.validateDialplan(request.application(), request.applicationData());
        mapper.update(request, entity);

        entity.setContext(current.context(tenantId, "internal"));
        repository.flush();
        log.info("Dialplan updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Dialplan entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("DIALPLAN", id);
        repository.delete(entity);
        repository.flush();
        log.info("Dialplan deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Dialplan find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Dialplan"));
    }

}
