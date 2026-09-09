package com.netgsm.asterisk.dto.request;
import com.netgsm.asterisk.enums.TenantStatus;
import jakarta.validation.constraints.*;
public record TenantRequest(@NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "[0-9]{4,6}", message = "Santral numarası 4–6 rakamdan oluşmalıdır") String code, @NotNull TenantStatus status) { }
