package com.netgsm.asterisk.dto;
public record QueueMemberResponse(Long id, Long tenantId, Long queueId, Long endpointId, Integer penalty, Boolean paused) {
}
