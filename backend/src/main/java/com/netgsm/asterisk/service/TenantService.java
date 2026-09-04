package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.TenantRequest;
import com.netgsm.asterisk.dto.TenantResponse;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.enums.TenantStatus;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.TenantMapper;
import com.netgsm.asterisk.repository.TenantRepository;
import java.text.Normalizer;
import java.util.Locale;
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
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantService {
    private final TenantMapper mapper;
    private final TenantRepository repository;

    @Transactional(readOnly = true)
    public Page<TenantResponse> list(Pageable page) {
        var allowed = java.util.Set.of("id", "name", "code", "status", "createdAt", "updatedAt");
        if (page.getSort().stream().anyMatch(order -> !allowed.contains(order.getProperty()))) {
            throw new PlatformException(400, "INVALID_SORT",
                    "Invalid sort. Use name,asc or id,desc without JSON brackets. Allowed fields: id, name, code, status, createdAt, updatedAt");
        }
        return repository.findAll(page).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TenantResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public TenantResponse create(TenantRequest request) {
        String code = normalizeCode(request.code());
        if (repository.existsByCode(code)) throw new DuplicateResourceException("Tenant code");
        Tenant tenant = new Tenant();
        mapper.update(request, tenant, code);
        repository.saveAndFlush(tenant);
        log.info("Tenant created id={}", tenant.getId());
        return mapper.toResponse(tenant);
    }

    public TenantResponse update(Long id, TenantRequest request) {
        Tenant tenant = find(id);
        String code = normalizeCode(request.code());
        if (repository.existsByCodeAndIdNot(code, id)) throw new DuplicateResourceException("Tenant code");
        mapper.update(request, tenant, code);
        repository.flush();
        log.info("Tenant updated id={}", id);
        return mapper.toResponse(tenant);
    }

    public void delete(Long id) {
        find(id).setStatus(TenantStatus.INACTIVE);
        log.info("Tenant deactivated id={}", id);
    }

    private Tenant find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tenant"));
    }

    private String normalizeCode(String value) {
        String ascii = value.trim().toLowerCase(Locale.ROOT)
                .replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u')
                .replace('ş', 's').replace('ö', 'o').replace('ç', 'c');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (normalized.length() > 48) normalized = normalized.substring(0, 48).replaceAll("_+$", "");
        if (normalized.length() < 2) {
            throw new PlatformException(400, "INVALID_TENANT_CODE", "Kısa kod en az iki harf veya rakam içermeli");
        }
        return normalized;
    }
}
