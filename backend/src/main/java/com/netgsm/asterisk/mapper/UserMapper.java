package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateUserRequest;
import com.netgsm.asterisk.dto.UpdateUserRequest;
import com.netgsm.asterisk.dto.UserResponse;
import com.netgsm.asterisk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(request.enabled());
        return user;
    }

    public void update(UpdateUserRequest request, User user, String username, String email) {
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(request.enabled());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getTenantId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
