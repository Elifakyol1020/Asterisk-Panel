package com.netgsm.asterisk.dto.response;
import java.time.Instant;
public record CdrResponse(String id, Long tenantId, Integer sequence,
    String uniqueId,
    String linkedId,
    String src,
    String dst,
    String srcName,
    String dstName,
    String context,
    String channel,
    String dstChannel,
    Instant startTime,
    Instant answerTime,
    Instant endTime,
    Integer duration,
    Integer billsec,
    String disposition,
    String recordingPath) {}

