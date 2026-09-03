package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record CreateEndpointRequest(Long tenantId,
        @NotBlank @Pattern(regexp = "[0-9]{1,20}") String extension,
        @NotBlank @Size(max = 120) String displayName,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String transport,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_,]{1,120}") String codecs,
        @NotNull Boolean enabled,
        @NotBlank @Size(min = 12, max = 72) String password) {
    @Override public String toString() { return "CreateEndpointRequest[REDACTED]"; }
}
