package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record UpdateIvrRequest(Long tenantId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,120}") String audioFile,
        @NotNull @Min(1) @Max(3600) Integer timeout,
        @NotNull @Min(1) @Max(20) Integer maxAttempts,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "UpdateIvrRequest[REDACTED]"; }
}
