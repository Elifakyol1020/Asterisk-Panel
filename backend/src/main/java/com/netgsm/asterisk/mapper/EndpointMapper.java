package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateEndpointRequest;
import com.netgsm.asterisk.dto.EndpointResponse;
import com.netgsm.asterisk.dto.UpdateEndpointRequest;
import com.netgsm.asterisk.entity.Endpoint;
import org.springframework.stereotype.Component;

@Component
public class EndpointMapper {

    public Endpoint toEntity(CreateEndpointRequest request, Long tenantId) {
        Endpoint entity = new Endpoint();
        entity.setTenantId(tenantId);
        entity.setExtension(request.extension());
        entity.setDisplayName(request.displayName());
        entity.setTransport(request.transport());
        entity.setCodecs(request.codecs());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateEndpointRequest request, Endpoint entity) {
        entity.setExtension(request.extension());
        entity.setDisplayName(request.displayName());
        entity.setTransport(request.transport());
        entity.setCodecs(request.codecs());
        entity.setEnabled(request.enabled());
    }

    public EndpointResponse toResponse(Endpoint entity) {
        return new EndpointResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getExtension(),
                entity.getDisplayName(),
                entity.getTransport(),
                entity.getCodecs(),
                entity.getEnabled(),
                entity.getContext(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
