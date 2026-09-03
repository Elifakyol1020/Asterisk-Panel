package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.mapper.QueueMapper;
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
public class QueueService {
    private final QueueMapper mapper;
    private final QueueRepository repository;
    private final CurrentUserService current;
    private final ReferenceService references;

    @Transactional(readOnly = true)
    public Page<QueueResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public QueueResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public QueueResponse create(CreateQueueRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Queue");

        Queue entity = mapper.toEntity(request, tenantId);

        repository.saveAndFlush(entity);
        log.info("Queue created id={} tenantId={}", entity.getId(), tenantId);
        return mapper.toResponse(entity);
    }

    public QueueResponse update(Long id, UpdateQueueRequest request) {
        Queue entity = find(id);
        Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Queue");

        mapper.update(request, entity);

        repository.flush();
        log.info("Queue updated id={} tenantId={}", id, tenantId);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        Queue entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("QUEUE", id);
        repository.delete(entity);
        repository.flush();
        log.info("Queue deleted id={} tenantId={}", id, entity.getTenantId());
    }

    private Queue find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Queue"));
    }

}
