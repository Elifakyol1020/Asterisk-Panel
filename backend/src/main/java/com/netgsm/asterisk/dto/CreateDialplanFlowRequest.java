package com.netgsm.asterisk.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateDialplanFlowRequest(
        Long tenantId,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "[0-9]{1,20}") String extension,
        @NotNull Boolean enabled,
        @NotEmpty @Size(max = 50) List<@Valid DialplanStepRequest> steps) { }
