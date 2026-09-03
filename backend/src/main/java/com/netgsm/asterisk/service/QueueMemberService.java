package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.entity.QueueMember;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.QueueMemberMapper;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class QueueMemberService {
    private final QueueMemberMapper mapper;
    private final QueueRepository queues;
    private final QueueMemberRepository members;
    private final EndpointRepository endpoints;
    private final CurrentUserService current;

    @Transactional(readOnly = true)
    public Page<QueueMemberResponse> list(Long queueId, Pageable page) {
        Queue queue = find(queueId);
        return members.findAllByQueueIdAndTenantId(queueId, queue.getTenantId(), page).map(mapper::toResponse);
    }

    public QueueMemberResponse create(Long queueId, QueueMemberRequest request) {
        Queue queue = find(queueId);
        Long tenantId = queue.getTenantId();
        current.tenantForCreate(tenantId);
        if (!endpoints.existsByIdAndTenantId(request.endpointId(), tenantId)) throw new ResourceNotFoundException("Endpoint");
        if (members.existsByQueueIdAndEndpointId(queueId, request.endpointId())) throw new DuplicateResourceException("Queue member");
        QueueMember member = mapper.toEntity(request, queueId, tenantId);
        members.saveAndFlush(member);
        log.info("Queue member added id={} tenantId={}", member.getId(), tenantId);
        return mapper.toResponse(member);
    }

    public void delete(Long queueId, Long memberId) {
        Queue queue = find(queueId);
        current.tenantForCreate(queue.getTenantId());
        var member = members.findByIdAndQueueIdAndTenantId(memberId, queueId, queue.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue member"));
        members.delete(member);
        log.info("Queue member deleted id={} tenantId={}", memberId, queue.getTenantId());
    }

    private Queue find(Long id) {
        return (current.isSuperAdmin() ? queues.findById(id) : queues.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Queue"));
    }
}
