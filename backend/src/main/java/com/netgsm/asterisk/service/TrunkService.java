package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.CreateTrunkRequest;
import com.netgsm.asterisk.dto.TrunkResponse;
import com.netgsm.asterisk.dto.UpdateTrunkRequest;
import com.netgsm.asterisk.entity.Trunk;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.TrunkMapper;
import com.netgsm.asterisk.repository.TrunkRepository;
import com.netgsm.asterisk.service.provisioning.AsteriskTrunkProvisioningService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class TrunkService {
    private final TrunkMapper mapper;
    private final TrunkRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;
    private final PasswordEncoder passwords;
    private final AsteriskTrunkProvisioningService provisioning;

    @Transactional(readOnly = true)
    public Page<TrunkResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TrunkResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public TrunkResponse create(CreateTrunkRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Trunk");

        Trunk entity = mapper.toEntity(request, tenantId);

        entity.setContext(current.context(tenantId, "internal"));
        entity.setPasswordHash(hash(request.password()));
        repository.saveAndFlush(entity);
        provisioning.upsert(entity, request.password());
        log.info("Trunk created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public TrunkResponse update(Long id, UpdateTrunkRequest request) {
        Trunk entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Trunk");
        String oldName = entity.getName();
        if (!oldName.equals(request.name()) && request.password() == null) {
            throw new BusinessRuleException("SIP password is required when changing a trunk name");
        }

        mapper.update(request, entity);

        entity.setContext(current.context(tenantId, "internal"));
        if (request.password() != null) entity.setPasswordHash(hash(request.password()));
        repository.flush();
        if (!oldName.equals(entity.getName())) provisioning.renameOrDelete(tenantId, oldName);
        provisioning.upsert(entity, request.password());
        log.info("Trunk updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Trunk entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced(entity.getTenantId(), "TRUNK", id);
        provisioning.renameOrDelete(entity.getTenantId(), entity.getName());
        repository.delete(entity);
        repository.flush();
        log.info("Trunk deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Trunk find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trunk"));
    }

    private String hash(String password) {
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new BusinessRuleException("Password exceeds 72 UTF-8 bytes");
        return passwords.encode(password);
    }
}
