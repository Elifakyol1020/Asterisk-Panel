package com.netgsm.asterisk.dto.request;
import com.netgsm.asterisk.enums.TenantStatus;
import jakarta.validation.constraints.*;
public record TenantRequest(@NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String code, @NotNull TenantStatus status) { }
