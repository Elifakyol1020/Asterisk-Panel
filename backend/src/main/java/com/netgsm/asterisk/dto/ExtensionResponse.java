package com.netgsm.asterisk.dto;
import java.time.Instant;
public record ExtensionResponse(Long id, Long tenantId, String extensionNumber, String name, String targetType, Long targetId, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
}
