package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.QueueMember;
public record QueueMemberResponse(Long id, Long tenantId, Long queueId, Long endpointId, Integer penalty, Boolean paused) {
    public static QueueMemberResponse from(QueueMember member) {
        return new QueueMemberResponse(member.getId(), member.getTenantId(), member.getQueueId(), member.getEndpointId(), member.getPenalty(), member.getPaused());
    }
}
