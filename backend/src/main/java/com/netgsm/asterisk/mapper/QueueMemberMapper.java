package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.entity.QueueMember;
import org.springframework.stereotype.Component;

@Component
public class QueueMemberMapper {

    public QueueMember toEntity(QueueMemberRequest request, Long queueId, Long tenantId) {
        QueueMember member = new QueueMember();
        member.setTenantId(tenantId);
        member.setQueueId(queueId);
        member.setEndpointId(request.endpointId());
        member.setPenalty(request.penalty());
        member.setPaused(request.paused());
        return member;
    }

    public void update(QueueMemberRequest request, QueueMember member) {
        member.setEndpointId(request.endpointId());
        member.setPenalty(request.penalty());
        member.setPaused(request.paused());
    }

    public QueueMemberResponse toResponse(QueueMember member) {
        return new QueueMemberResponse(
                member.getId(),
                member.getTenantId(),
                member.getQueueId(),
                member.getEndpointId(),
                member.getPenalty(),
                member.getPaused());
    }
}
