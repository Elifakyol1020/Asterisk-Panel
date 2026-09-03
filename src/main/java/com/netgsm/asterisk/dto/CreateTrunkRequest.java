package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record CreateTrunkRequest(Long tenantId,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9.:-]{1,253}") String host,
        @NotNull @Min(1) @Max(65535) Integer port,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{1,80}") String username,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String transport,
        @Size(max = 80) String fromUser,
        @Size(max = 253) String fromDomain,
        @NotNull Boolean enabled,
        @NotBlank @Size(min = 12, max = 72) String password) {
    @Override public String toString() { return "CreateTrunkRequest[REDACTED]"; }
}
