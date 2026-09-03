package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.enums.TenantStatus;
import java.time.Instant;
public record TenantResponse(Long id, String name, String code, TenantStatus status, Instant createdAt, Instant updatedAt) {
}
