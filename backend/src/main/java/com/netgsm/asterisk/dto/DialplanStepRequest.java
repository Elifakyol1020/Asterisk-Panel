package com.netgsm.asterisk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DialplanStepRequest(
        @NotBlank @Pattern(regexp = "Answer|Hangup|Playback|Wait") String application,
        @NotNull @Size(max = 120) String applicationData) { }
