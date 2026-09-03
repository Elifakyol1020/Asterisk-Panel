package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateTrunkRequest;
import com.netgsm.asterisk.dto.TrunkResponse;
import com.netgsm.asterisk.dto.UpdateTrunkRequest;
import com.netgsm.asterisk.entity.Trunk;
import com.netgsm.asterisk.repository.TrunkRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j @Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class TrunkService {
    private final TrunkRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    private final org.springframework.security.crypto.password.PasswordEncoder passwords;
    @Transactional(readOnly = true)
    public Page<TrunkResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(TrunkResponse::from);
    }
    @Transactional(readOnly = true)
    public TrunkResponse get(Long id) { return TrunkResponse.from(find(id)); }
    public TrunkResponse create(CreateTrunkRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Trunk");

        Trunk entity = new Trunk(); entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setHost(request.host());
        entity.setPort(request.port());
        entity.setUsername(request.username());
        entity.setTransport(request.transport());
        entity.setFromUser(request.fromUser());
        entity.setFromDomain(request.fromDomain());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        entity.setPasswordHash(hash(request.password()));
        repository.saveAndFlush(entity);
        log.info("Trunk created id={} tenantId={}", entity.getId(), tenantId);
        return TrunkResponse.from(entity);
    }
    public TrunkResponse update(Long id, UpdateTrunkRequest request) {
        Trunk entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Trunk");

        entity.setName(request.name());
        entity.setHost(request.host());
        entity.setPort(request.port());
        entity.setUsername(request.username());
        entity.setTransport(request.transport());
        entity.setFromUser(request.fromUser());
        entity.setFromDomain(request.fromDomain());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        if (request.password() != null) entity.setPasswordHash(hash(request.password()));
        repository.flush();
        log.info("Trunk updated id={} tenantId={}", id, tenantId);
        return TrunkResponse.from(entity);
    }
    public void delete(Long id) {
        Trunk entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("TRUNK", id);
        repository.delete(entity); repository.flush();
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
