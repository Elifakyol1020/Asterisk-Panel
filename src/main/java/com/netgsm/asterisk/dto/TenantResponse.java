package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.entity.TenantStatus;
import java.time.Instant;
public record TenantResponse(Long id, String name, String code, TenantStatus status, Instant createdAt, Instant updatedAt) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getCode(), tenant.getStatus(), tenant.getCreatedAt(), tenant.getUpdatedAt());
    }
}
