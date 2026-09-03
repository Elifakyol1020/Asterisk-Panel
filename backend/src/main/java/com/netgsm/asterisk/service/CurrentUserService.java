package com.netgsm.asterisk.service;
import com.netgsm.asterisk.security.CurrentUser;

import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.enums.TenantStatus;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CurrentUserService {
    private final TenantRepository tenants;
    public CurrentUser current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser user))
            throw new InvalidCredentialsException();
        return user;
    }
    public boolean isSuperAdmin() { return current().role() == Role.SUPER_ADMIN; }
    public Long getCurrentUserId() { return current().userId(); }
    public Long getCurrentTenantId() { return current().tenantId(); }
    public Role getCurrentRole() { return current().role(); }
    public void requireSuperAdmin() {
        if (!isSuperAdmin()) throw new TenantAccessDeniedException();
    }
    public Long tenantForList(Long requested) {
        return isSuperAdmin() ? requested : current().tenantId();
    }
    public Long tenantForCreate(Long requested) {
        Long tenantId = isSuperAdmin() ? requested : current().tenantId();
        if (tenantId == null) throw new BusinessRuleException("tenantId is required for SUPER_ADMIN");
        // Serialize mutations within a tenant so polymorphic reference checks cannot race deletes.
        var tenant = tenants.findLockedById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant"));
        if (tenant.getStatus() != TenantStatus.ACTIVE) throw new BusinessRuleException("Tenant is inactive");
        return tenantId;
    }
    public String context(Long tenantId, String purpose) { return "tenant_" + tenantId + "_" + purpose; }
}
