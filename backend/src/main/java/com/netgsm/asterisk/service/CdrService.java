package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.CdrSearchRequest;
import com.netgsm.asterisk.dto.response.CdrResponse;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.mapper.CdrMapper;
import com.netgsm.asterisk.repository.CdrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class CdrService {
    private final CurrentUserService current;
    private final CdrRepository repository;
    private final CdrMapper mapper;
    public Page<CdrResponse> list(CdrSearchRequest request) {
        request.validateRange();
        return repository.search(scope(request.getTenantId()), request).map(mapper::toResponse);
    }
    public CdrResponse get(String id) {
        return repository.find(id, scope(null)).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("CDR"));
    }
    private Long scope(Long requested) {
        if (current.isSuperAdmin()) return requested;
        Long id = current.getCurrentTenantId();
        if (id == null || id <= 0) throw new TenantAccessDeniedException();
        return id;
    }
}
