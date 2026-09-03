package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record UpdateUserRequest(@NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,80}") String username,
        @NotBlank @Email @Size(max = 254) String email, @Size(min = 12, max = 72) String password,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "UpdateUserRequest[REDACTED]"; }
}
