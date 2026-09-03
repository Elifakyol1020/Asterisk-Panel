package com.netgsm.asterisk.dto;
import java.time.Instant;
public record QueueResponse(Long id, Long tenantId, String name, String strategy, Integer timeout, Integer retry, Integer wrapupTime, Integer maxLength, String musicOnHold, Boolean enabled, Instant createdAt, Instant updatedAt) {
}
