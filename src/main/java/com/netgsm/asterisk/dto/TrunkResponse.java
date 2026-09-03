package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Trunk;
import java.time.Instant;
public record TrunkResponse(Long id, Long tenantId, String name, String host, Integer port, String username, String transport, String fromUser, String fromDomain, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
    public static TrunkResponse from(Trunk entity) {
        return new TrunkResponse(entity.getId(), entity.getTenantId(), entity.getName(), entity.getHost(), entity.getPort(), entity.getUsername(), entity.getTransport(), entity.getFromUser(), entity.getFromDomain(), entity.getEnabled(), entity.getContext(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
