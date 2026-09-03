package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Ivr;
import java.time.Instant;
public record IvrResponse(Long id, Long tenantId, String name, String description, String audioFile, Integer timeout, Integer maxAttempts, Boolean enabled, Instant createdAt, Instant updatedAt) {
    public static IvrResponse from(Ivr entity) {
        return new IvrResponse(entity.getId(), entity.getTenantId(), entity.getName(), entity.getDescription(), entity.getAudioFile(), entity.getTimeout(), entity.getMaxAttempts(), entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
