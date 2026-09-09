package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.request.CreateEndpointRequest;
import com.netgsm.asterisk.dto.response.EndpointResponse;
import com.netgsm.asterisk.dto.request.UpdateEndpointRequest;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.EndpointMapper;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.service.provisioning.AsteriskEndpointProvisioningService;
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
public class EndpointService {
    private final EndpointMapper mapper;
    private final AsteriskRegistrationService registrations;
    private final com.netgsm.asterisk.service.provisioning.AsteriskNaming naming;
    private final EndpointRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;
    private final com.netgsm.asterisk.service.provisioning.AsteriskDialplanProvisioningService inboundProvisioning;
    private final PasswordEncoder passwords;
    private final AsteriskEndpointProvisioningService provisioning;

    @Transactional(readOnly = true)
    public Page<EndpointResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EndpointResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long,String> registrationStatus(java.util.List<Long> ids) {
        if(ids == null || ids.isEmpty() || ids.size() > 100) throw new BusinessRuleException("Select between 1 and 100 endpoints");
        var selected = ids.stream().distinct().map(this::find).toList();
        var contacts = registrations.registeredContacts();
        java.util.Map<Long,String> result = new java.util.LinkedHashMap<>();
        for(var endpoint:selected) {
            Long expires = contacts == null ? null : contacts.get(naming.endpoint(endpoint.getTenantId(),endpoint.getExtension()));
            result.put(endpoint.getId(), contacts == null ? "UNKNOWN" : expires != null && expires > java.time.Instant.now().getEpochSecond() ? "REGISTERED" : "UNREGISTERED");
        }
        return result;
    }

    public EndpointResponse create(CreateEndpointRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtension(tenantId, request.extension())) throw new DuplicateResourceException("Endpoint");

        Endpoint entity = mapper.toEntity(request, tenantId);

        entity.setContext(current.context(tenantId, "internal"));
        entity.setPasswordHash(hash(request.password()));
        repository.saveAndFlush(entity);
        provisioning.upsert(entity, request.password());
        log.info("Endpoint created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public EndpointResponse update(Long id, UpdateEndpointRequest request) {
        Endpoint entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionAndIdNot(tenantId, request.extension(), id)) throw new DuplicateResourceException("Endpoint");
        String oldExtension = entity.getExtension();
        String oldContext = entity.getContext();
        if (!oldExtension.equals(request.extension()) && request.password() == null) {
            throw new BusinessRuleException("SIP password is required when changing an endpoint number");
        }

        mapper.update(request, entity);

        entity.setContext(current.context(tenantId, "internal"));
        if (request.password() != null) entity.setPasswordHash(hash(request.password()));
        repository.flush();
        if (!oldExtension.equals(entity.getExtension()) || !oldContext.equals(entity.getContext())) {
            provisioning.renameOrDelete(tenantId, oldExtension, oldContext);
        }
        provisioning.upsert(entity, request.password());
        inboundProvisioning.refreshInboundTarget(tenantId, "ENDPOINT", entity.getId());
        log.info("Endpoint updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Endpoint entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced(entity.getTenantId(), "ENDPOINT", id);
        provisioning.renameOrDelete(entity.getTenantId(), entity.getExtension(), entity.getContext());
        repository.delete(entity);
        repository.flush();
        log.info("Endpoint deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Endpoint find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
    }

    private String hash(String password) {
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new BusinessRuleException("Password exceeds 72 UTF-8 bytes");
        return passwords.encode(password);
    }
}
