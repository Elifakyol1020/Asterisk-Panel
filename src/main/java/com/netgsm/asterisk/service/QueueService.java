package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.repository.QueueRepository;
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
public class QueueService {
    private final QueueRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    @Transactional(readOnly = true)
    public Page<QueueResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(QueueResponse::from);
    }
    @Transactional(readOnly = true)
    public QueueResponse get(Long id) { return QueueResponse.from(find(id)); }
    public QueueResponse create(CreateQueueRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Queue");

        Queue entity = new Queue(); entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setStrategy(request.strategy());
        entity.setTimeout(request.timeout());
        entity.setRetry(request.retry());
        entity.setWrapupTime(request.wrapupTime());
        entity.setMaxLength(request.maxLength());
        entity.setMusicOnHold(request.musicOnHold());
        entity.setEnabled(request.enabled());

        repository.saveAndFlush(entity);
        log.info("Queue created id={} tenantId={}", entity.getId(), tenantId);
        return QueueResponse.from(entity);
    }
    public QueueResponse update(Long id, UpdateQueueRequest request) {
        Queue entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Queue");

        entity.setName(request.name());
        entity.setStrategy(request.strategy());
        entity.setTimeout(request.timeout());
        entity.setRetry(request.retry());
        entity.setWrapupTime(request.wrapupTime());
        entity.setMaxLength(request.maxLength());
        entity.setMusicOnHold(request.musicOnHold());
        entity.setEnabled(request.enabled());

        repository.flush();
        log.info("Queue updated id={} tenantId={}", id, tenantId);
        return QueueResponse.from(entity);
    }
    public void delete(Long id) {
        Queue entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("QUEUE", id);
        repository.delete(entity); repository.flush();
        log.info("Queue deleted id={} tenantId={}", id, entity.getTenantId());
    }
    private Queue find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Queue"));
    }

}
