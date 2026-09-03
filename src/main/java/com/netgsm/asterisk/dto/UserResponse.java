package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Role;
import com.netgsm.asterisk.entity.User;
import java.time.Instant;
public record UserResponse(Long id, Long tenantId, String username, String email, Role role, boolean enabled,
                           Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getTenantId(), user.getUsername(), user.getEmail(), user.getRole(),
                user.isEnabled(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
