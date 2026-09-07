package com.netgsm.asterisk.dto.request;
import jakarta.validation.constraints.*;
public record QueueMemberRequest(@NotNull @Positive Long endpointId,
        @NotNull @Min(0) @Max(1000) Integer penalty, @NotNull Boolean paused) { }
