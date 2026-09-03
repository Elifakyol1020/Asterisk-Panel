package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateTrunkRequest;
import com.netgsm.asterisk.dto.TrunkResponse;
import com.netgsm.asterisk.dto.UpdateTrunkRequest;
import com.netgsm.asterisk.entity.Trunk;
import org.springframework.stereotype.Component;

@Component
public class TrunkMapper {

    public Trunk toEntity(CreateTrunkRequest request, Long tenantId) {
        Trunk entity = new Trunk();
        entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setHost(request.host());
        entity.setPort(request.port());
        entity.setUsername(request.username());
        entity.setTransport(request.transport());
        entity.setFromUser(request.fromUser());
        entity.setFromDomain(request.fromDomain());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateTrunkRequest request, Trunk entity) {
        entity.setName(request.name());
        entity.setHost(request.host());
        entity.setPort(request.port());
        entity.setUsername(request.username());
        entity.setTransport(request.transport());
        entity.setFromUser(request.fromUser());
        entity.setFromDomain(request.fromDomain());
        entity.setEnabled(request.enabled());
    }

    public TrunkResponse toResponse(Trunk entity) {
        return new TrunkResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getHost(),
                entity.getPort(),
                entity.getUsername(),
                entity.getTransport(),
                entity.getFromUser(),
                entity.getFromDomain(),
                entity.getEnabled(),
                entity.getContext(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
