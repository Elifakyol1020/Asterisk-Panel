package com.netgsm.asterisk.dto;
import java.time.Instant;
public record EndpointResponse(Long id, Long tenantId, String extension, String displayName, String transport, String codecs, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
}
