package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.entity.Ivr;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.IvrMapper;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.service.provisioning.AsteriskDialplanProvisioningService;
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
public class IvrService {
    private final IvrMapper mapper;
    private final IvrRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;
    private final AsteriskDialplanProvisioningService provisioning;

    @Transactional(readOnly = true)
    public Page<IvrResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public IvrResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public IvrResponse create(CreateIvrRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Ivr");

        Ivr entity = mapper.toEntity(request, tenantId);

        repository.saveAndFlush(entity);
        provisioning.recompileIvr(entity);

        log.info("Ivr created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public IvrResponse update(Long id, UpdateIvrRequest request) {
        Ivr entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Ivr");

        provisioning.deleteIvr(entity);
        mapper.update(request, entity);

        repository.flush();
        provisioning.recompileIvr(entity);

        log.info("Ivr updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Ivr entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced(entity.getTenantId(), "IVR", id);

        provisioning.deleteIvr(entity);
        repository.delete(entity);
        repository.flush();
        log.info("Ivr deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Ivr find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Ivr"));
    }

}
