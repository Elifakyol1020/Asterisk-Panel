package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.CreateExtensionRequest;
import com.netgsm.asterisk.dto.ExtensionResponse;
import com.netgsm.asterisk.dto.UpdateExtensionRequest;
import com.netgsm.asterisk.entity.Extension;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.ExtensionMapper;
import com.netgsm.asterisk.repository.ExtensionRepository;
import com.netgsm.asterisk.repository.DialplanRepository;
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
public class ExtensionService {
    private final ExtensionMapper mapper;
    private final ExtensionRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;
    private final AsteriskDialplanProvisioningService provisioning;
    private final DialplanRepository dialplans;

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ExtensionResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public ExtensionResponse create(CreateExtensionRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtensionNumber(tenantId, request.extensionNumber())) throw new DuplicateResourceException("Extension");
        if (dialplans.existsByTenantIdAndExtension(tenantId, request.extensionNumber())) throw new DuplicateResourceException("Extension number");
        references.requireTarget(tenantId, request.targetType(), request.targetId());
        Extension entity = mapper.toEntity(request, tenantId);

        entity.setContext(current.context(tenantId, "internal"));
        repository.saveAndFlush(entity);
        provisioning.upsertExtensionRoute(entity);

        log.info("Extension created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public ExtensionResponse update(Long id, UpdateExtensionRequest request) {
        Extension entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionNumberAndIdNot(tenantId, request.extensionNumber(), id)) throw new DuplicateResourceException("Extension");
        references.requireTarget(tenantId, request.targetType(), request.targetId());
        provisioning.deleteExtensionRoute(entity);
        mapper.update(request, entity);

        entity.setContext(current.context(tenantId, "internal"));
        repository.flush();
        provisioning.upsertExtensionRoute(entity);

        log.info("Extension updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Extension entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced(entity.getTenantId(), "EXTENSION", id);

        if ("DIALPLAN".equals(entity.getTargetType())) {
            var rows = dialplans.findAllByTenantIdAndExtensionOrderByPriorityAsc(entity.getTenantId(), entity.getExtensionNumber());
            rows.forEach(provisioning::deleteDialplan);
            dialplans.deleteAll(rows);
            dialplans.flush();
        } else {
            provisioning.deleteExtensionRoute(entity);
        }
        repository.delete(entity);
        repository.flush();
        log.info("Extension deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Extension find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Extension"));
    }

}
