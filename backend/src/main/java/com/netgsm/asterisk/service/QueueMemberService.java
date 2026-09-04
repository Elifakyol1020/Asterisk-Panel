package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.entity.QueueMember;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.QueueMemberMapper;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.service.provisioning.AsteriskQueueProvisioningService;
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
    private final AsteriskQueueProvisioningService provisioning;

    @Transactional(readOnly = true)
    public Page<QueueMemberResponse> list(Long queueId, Pageable page) {
        Queue queue = find(queueId);
        return members.findAllByQueueIdAndTenantId(queueId, queue.getTenantId(), page).map(mapper::toResponse);
    }

    public QueueMemberResponse create(Long queueId, QueueMemberRequest request) {
        Queue queue = find(queueId);
        Long tenantId = queue.getTenantId();
        current.tenantForCreate(tenantId);
        Endpoint endpoint = endpoints.findByIdAndTenantId(request.endpointId(), tenantId).orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
        if (members.existsByQueueIdAndEndpointId(queueId, request.endpointId())) throw new DuplicateResourceException("Queue member");
        QueueMember member = mapper.toEntity(request, queueId, tenantId);
        members.saveAndFlush(member);
        provisioning.upsertMember(queue, member, endpoint);
        log.info("Queue member added id={} tenantId={}", member.getId(), tenantId);
        return mapper.toResponse(member);
    }

    public QueueMemberResponse update(Long queueId, Long memberId, QueueMemberRequest request) {
        Queue queue = find(queueId);
        Long tenantId = queue.getTenantId();
        current.tenantForCreate(tenantId);
        var member = members.findByIdAndQueueIdAndTenantId(memberId, queueId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue member"));
        Endpoint oldEndpoint = endpoints.findByIdAndTenantId(member.getEndpointId(), tenantId).orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
        Endpoint endpoint = endpoints.findByIdAndTenantId(request.endpointId(), tenantId).orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
        if (members.existsByQueueIdAndEndpointIdAndIdNot(queueId, request.endpointId(), memberId)) throw new DuplicateResourceException("Queue member");
        mapper.update(request, member);
        members.flush();
        if (!oldEndpoint.getId().equals(endpoint.getId())) provisioning.deleteMember(queue, oldEndpoint);
        provisioning.upsertMember(queue, member, endpoint);
        log.info("Queue member updated id={} tenantId={}", memberId, tenantId);
        return mapper.toResponse(member);
    }

    public void delete(Long queueId, Long memberId) {
        Queue queue = find(queueId);
        current.tenantForCreate(queue.getTenantId());
        var member = members.findByIdAndQueueIdAndTenantId(memberId, queueId, queue.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue member"));
        Endpoint endpoint = endpoints.findByIdAndTenantId(member.getEndpointId(), queue.getTenantId()).orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
        provisioning.deleteMember(queue, endpoint);
        members.delete(member);
        members.flush();
        log.info("Queue member deleted id={} tenantId={}", memberId, queue.getTenantId());
    }

    private Queue find(Long id) {
        return (current.isSuperAdmin() ? queues.findById(id) : queues.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Queue"));
    }
}
