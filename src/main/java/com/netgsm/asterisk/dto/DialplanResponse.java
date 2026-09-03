package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Dialplan;
import java.time.Instant;
public record DialplanResponse(Long id, Long tenantId, String extension, Integer priority, String application, String applicationData, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
    public static DialplanResponse from(Dialplan entity) {
        return new DialplanResponse(entity.getId(), entity.getTenantId(), entity.getExtension(), entity.getPriority(), entity.getApplication(), entity.getApplicationData(), entity.getEnabled(), entity.getContext(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
