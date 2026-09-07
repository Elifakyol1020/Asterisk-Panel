package com.netgsm.asterisk.dto.response;
import java.time.Instant;
public record TrunkResponse(Long id, Long tenantId, String name, String host, Integer port, String username, String transport, String fromUser, String fromDomain, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
}
