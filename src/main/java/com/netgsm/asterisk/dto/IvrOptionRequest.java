package com.netgsm.asterisk.dto;
import jakarta.validation.constraints.*;
public record IvrOptionRequest(@NotBlank @Pattern(regexp = "[0-9*#]") String digit,
        @NotBlank @Pattern(regexp = "QUEUE|EXTENSION|IVR|HANGUP") String actionType, @Positive Long targetId) { }
