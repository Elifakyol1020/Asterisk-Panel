package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.TenantStatus;
import jakarta.validation.constraints.*;
public record TenantRequest(@NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9_]{1,47}") String code, @NotNull TenantStatus status) { }
