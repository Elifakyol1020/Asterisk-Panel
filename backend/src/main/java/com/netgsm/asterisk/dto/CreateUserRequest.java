package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record CreateUserRequest(@NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,80}") String username,
        @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "CreateUserRequest[REDACTED]"; }
}
