package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateEndpointRequest;
import com.netgsm.asterisk.dto.EndpointResponse;
import com.netgsm.asterisk.dto.UpdateEndpointRequest;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.exception.AsteriskConfigurationException;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class EndpointService {
    private final EndpointRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    private final org.springframework.security.crypto.password.PasswordEncoder passwords;
    private final com.netgsm.asterisk.service.AsteriskRealtimeService realtime;
    @Transactional(readOnly = true)
    public Page<EndpointResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(EndpointResponse::from);
    }
    @Transactional(readOnly = true)
    public EndpointResponse get(Long id) { return EndpointResponse.from(find(id)); }
    public EndpointResponse create(CreateEndpointRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtension(tenantId, request.extension())) throw new DuplicateResourceException("Endpoint");

        Endpoint entity = new Endpoint(); entity.setTenantId(tenantId);
        entity.setExtension(request.extension());
        entity.setDisplayName(request.displayName());
        entity.setTransport(request.transport());
        entity.setCodecs(request.codecs());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        entity.setPasswordHash(hash(request.password()));
        repository.saveAndFlush(entity);
        realtime.save(entity, request.password());
        log.info("Endpoint created id={} tenantId={}", entity.getId(), tenantId);
        return EndpointResponse.from(entity);
    }
    public EndpointResponse update(Long id, UpdateEndpointRequest request) {
        Endpoint entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionAndIdNot(tenantId, request.extension(), id)) throw new DuplicateResourceException("Endpoint");

        entity.setExtension(request.extension());
        entity.setDisplayName(request.displayName());
        entity.setTransport(request.transport());
        entity.setCodecs(request.codecs());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        if (request.password() != null) entity.setPasswordHash(hash(request.password()));
        repository.flush();
        realtime.save(entity, request.password());
        log.info("Endpoint updated id={} tenantId={}", id, tenantId);
        return EndpointResponse.from(entity);
    }
    public void delete(Long id) {
        Endpoint entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("ENDPOINT", id);
        realtime.delete(entity);
        repository.delete(entity); repository.flush();
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
