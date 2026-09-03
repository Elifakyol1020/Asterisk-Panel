package com.netgsm.asterisk.dto;
import java.time.Instant;
public record IvrResponse(Long id, Long tenantId, String name, String description, String audioFile, Integer timeout, Integer maxAttempts, Boolean enabled, Instant createdAt, Instant updatedAt) {
}
