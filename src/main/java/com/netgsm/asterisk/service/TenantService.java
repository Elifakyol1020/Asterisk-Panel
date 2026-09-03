package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.TenantRequest;
import com.netgsm.asterisk.dto.TenantResponse;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.enums.TenantStatus;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.exception.AsteriskConfigurationException;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional @PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantService {
    private final TenantRepository repository;
    @Transactional(readOnly = true)
    public Page<TenantResponse> list(Pageable page) {
        var allowed = java.util.Set.of("id", "name", "code", "status", "createdAt", "updatedAt");
        if (page.getSort().stream().anyMatch(order -> !allowed.contains(order.getProperty()))) {
            throw new PlatformException(400, "INVALID_SORT",
                    "Invalid sort. Use name,asc or id,desc without JSON brackets. Allowed fields: id, name, code, status, createdAt, updatedAt");
        }
        return repository.findAll(page).map(TenantResponse::from);
    }
    @Transactional(readOnly = true)
    public TenantResponse get(Long id) { return TenantResponse.from(find(id)); }
    public TenantResponse create(TenantRequest request) {
        if (repository.existsByCode(request.code())) throw new DuplicateResourceException("Tenant code");
        Tenant tenant = new Tenant(); apply(tenant, request);
        repository.saveAndFlush(tenant); log.info("Tenant created id={}", tenant.getId());
        return TenantResponse.from(tenant);
    }
    public TenantResponse update(Long id, TenantRequest request) {
        Tenant tenant = find(id);
        if (repository.existsByCodeAndIdNot(request.code(), id)) throw new DuplicateResourceException("Tenant code");
        apply(tenant, request); repository.flush(); log.info("Tenant updated id={}", id);
        return TenantResponse.from(tenant);
    }
    public void delete(Long id) {
        find(id).setStatus(TenantStatus.INACTIVE); log.info("Tenant deactivated id={}", id);
    }
    private Tenant find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant")); }
    private void apply(Tenant tenant, TenantRequest request) {
        tenant.setName(request.name().trim()); tenant.setCode(request.code()); tenant.setStatus(request.status());
    }
}
