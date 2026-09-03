package com.netgsm.asterisk.dto;
import java.time.Instant;
public record DialplanResponse(Long id, Long tenantId, String extension, Integer priority, String application, String applicationData, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
}
