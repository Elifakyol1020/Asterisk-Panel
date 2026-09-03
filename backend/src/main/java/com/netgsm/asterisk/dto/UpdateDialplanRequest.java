package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record UpdateDialplanRequest(Long tenantId,
        @NotBlank @Pattern(regexp = "[0-9]{1,20}") String extension,
        @NotNull @Min(1) @Max(1000) Integer priority,
        @NotBlank @Pattern(regexp = "Answer|Hangup|Playback|Wait") String application,
        @NotNull @Size(max = 120) String applicationData,
        @NotNull Boolean enabled) {
    @Override public String toString() { return "UpdateDialplanRequest[REDACTED]"; }
}
