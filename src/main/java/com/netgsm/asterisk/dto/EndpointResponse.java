package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Endpoint;
import java.time.Instant;
public record EndpointResponse(Long id, Long tenantId, String extension, String displayName, String transport, String codecs, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
    public static EndpointResponse from(Endpoint entity) {
        return new EndpointResponse(entity.getId(), entity.getTenantId(), entity.getExtension(), entity.getDisplayName(), entity.getTransport(), entity.getCodecs(), entity.getEnabled(), entity.getContext(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
