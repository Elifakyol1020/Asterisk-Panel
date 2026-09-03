package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Extension;
import java.time.Instant;
public record ExtensionResponse(Long id, Long tenantId, String extensionNumber, String name, String targetType, Long targetId, Boolean enabled, String context, Instant createdAt, Instant updatedAt) {
    public static ExtensionResponse from(Extension entity) {
        return new ExtensionResponse(entity.getId(), entity.getTenantId(), entity.getExtensionNumber(), entity.getName(), entity.getTargetType(), entity.getTargetId(), entity.getEnabled(), entity.getContext(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
