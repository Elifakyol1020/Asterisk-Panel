package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Queue;
import java.time.Instant;
public record QueueResponse(Long id, Long tenantId, String name, String strategy, Integer timeout, Integer retry, Integer wrapupTime, Integer maxLength, String musicOnHold, Boolean enabled, Instant createdAt, Instant updatedAt) {
    public static QueueResponse from(Queue entity) {
        return new QueueResponse(entity.getId(), entity.getTenantId(), entity.getName(), entity.getStrategy(), entity.getTimeout(), entity.getRetry(), entity.getWrapupTime(), entity.getMaxLength(), entity.getMusicOnHold(), entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
