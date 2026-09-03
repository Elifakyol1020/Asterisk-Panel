package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.TenantRequest;
import com.netgsm.asterisk.dto.TenantResponse;
import com.netgsm.asterisk.entity.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public Tenant toEntity(TenantRequest request) {
        Tenant tenant = new Tenant();
        update(request, tenant);
        return tenant;
    }

    public void update(TenantRequest request, Tenant tenant) {
        tenant.setName(request.name().trim());
        tenant.setCode(request.code());
        tenant.setStatus(request.status());
    }

    public TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCode(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt());
    }
}
