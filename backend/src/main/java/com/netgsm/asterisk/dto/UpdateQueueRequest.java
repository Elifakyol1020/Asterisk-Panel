package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record UpdateQueueRequest(Long tenantId,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String name,
        @NotBlank @Pattern(regexp = "ringall|leastrecent|fewestcalls|random|rrmemory|linear|wrandom") String strategy,
        @NotNull @Min(1) @Max(3600) Integer timeout,
        @NotNull @Min(1) @Max(3600) Integer retry,
        @NotNull @Min(0) @Max(3600) Integer wrapupTime,
        @NotNull @Min(0) @Max(100000) Integer maxLength,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String musicOnHold,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "UpdateQueueRequest[REDACTED]"; }
}
