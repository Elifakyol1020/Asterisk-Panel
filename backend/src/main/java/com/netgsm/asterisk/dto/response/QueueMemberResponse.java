package com.netgsm.asterisk.dto.response;
public record QueueMemberResponse(Long id, Long tenantId, Long queueId, Long endpointId, Integer penalty, Boolean paused) {
}
