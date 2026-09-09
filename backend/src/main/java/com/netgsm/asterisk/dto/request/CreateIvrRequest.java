package com.netgsm.asterisk.dto.request;
import jakarta.validation.constraints.*;
public record CreateIvrRequest(Long tenantId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,120}") String audioFile,
        @NotNull @Min(1) @Max(3600) Integer timeout,
        @NotNull @Min(1) @Max(20) Integer maxAttempts,
        @NotNull Boolean enabled,
        @Size(max = 12) java.util.List<@jakarta.validation.Valid @NotNull IvrOptionRequest> options) {
    @Override public String toString() { return "CreateIvrRequest[REDACTED]"; }
}
