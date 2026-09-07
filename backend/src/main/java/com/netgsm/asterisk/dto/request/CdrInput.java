package com.netgsm.asterisk.dto.request;
import java.time.Instant;
import jakarta.validation.constraints.*;
public record CdrInput(
    @NotBlank @Size(max=150) String uniqueId,
    @Size(max=150) String linkedId,
    @NotNull @Min(0) Integer sequence,
    @NotBlank @Size(max=255) String src,
    @NotBlank @Size(max=255) String dst,
    @Size(max=255) String srcName,
    @Size(max=255) String dstName,
    @Size(max=255) String context,
    @Size(max=255) String channel,
    @Size(max=255) String dstChannel,
    @NotNull Instant startTime, Instant answerTime, @NotNull Instant endTime,
    @NotNull @Min(0) Integer duration, @NotNull @Min(0) Integer billsec,
    @NotBlank @Size(max=32) String disposition,
    @Size(max=1000) String recordingPath) {}

