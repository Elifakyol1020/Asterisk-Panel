package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.entity.QueueMember;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j @Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class QueueMemberService {
    private final QueueRepository queues;
    private final QueueMemberRepository members;
    private final EndpointRepository endpoints;
    private final CurrentUserService current;
    @Transactional(readOnly = true)
    public Page<QueueMemberResponse> list(Long queueId, Pageable page) {
        Queue queue = find(queueId);
        return members.findAllByQueueIdAndTenantId(queueId, queue.getTenantId(), page).map(QueueMemberResponse::from);
    }
    public QueueMemberResponse create(Long queueId, QueueMemberRequest request) {
        Queue queue = find(queueId); Long tenantId = queue.getTenantId(); current.tenantForCreate(tenantId);
        if (!endpoints.existsByIdAndTenantId(request.endpointId(), tenantId)) throw new ResourceNotFoundException("Endpoint");
        if (members.existsByQueueIdAndEndpointId(queueId, request.endpointId())) throw new DuplicateResourceException("Queue member");
        QueueMember member = new QueueMember(); member.setTenantId(tenantId); member.setQueueId(queueId);
        member.setEndpointId(request.endpointId()); member.setPenalty(request.penalty()); member.setPaused(request.paused());
        members.saveAndFlush(member);
        log.info("Queue member added id={} tenantId={}", member.getId(), tenantId); return QueueMemberResponse.from(member);
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
