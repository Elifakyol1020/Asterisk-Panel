package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record UpdateExtensionRequest(Long tenantId,
        @NotBlank @Pattern(regexp = "[0-9]{1,20}") String extensionNumber,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "ENDPOINT|QUEUE|IVR|TRUNK|CUSTOM") String targetType,
        @NotNull @Positive Long targetId,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "UpdateExtensionRequest[REDACTED]"; }
}
