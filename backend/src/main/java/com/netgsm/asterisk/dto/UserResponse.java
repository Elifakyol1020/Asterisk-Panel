package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.enums.Role;
import java.time.Instant;
public record UserResponse(Long id, Long tenantId, String username, String email, Role role, boolean enabled,
                           Instant createdAt, Instant updatedAt) {
}
