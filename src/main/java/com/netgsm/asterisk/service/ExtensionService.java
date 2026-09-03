package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateExtensionRequest;
import com.netgsm.asterisk.dto.ExtensionResponse;
import com.netgsm.asterisk.dto.UpdateExtensionRequest;
import com.netgsm.asterisk.entity.Extension;
import com.netgsm.asterisk.repository.ExtensionRepository;
import com.netgsm.asterisk.exception.AsteriskConfigurationException;
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
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ExtensionService {
    private final ExtensionRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    @Transactional(readOnly = true)
    public Page<ExtensionResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(ExtensionResponse::from);
    }
    @Transactional(readOnly = true)
    public ExtensionResponse get(Long id) { return ExtensionResponse.from(find(id)); }
    public ExtensionResponse create(CreateExtensionRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtensionNumber(tenantId, request.extensionNumber())) throw new DuplicateResourceException("Extension");
        references.requireTarget(tenantId, request.targetType(), request.targetId());
        Extension entity = new Extension(); entity.setTenantId(tenantId);
        entity.setExtensionNumber(request.extensionNumber());
        entity.setName(request.name());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        repository.saveAndFlush(entity);

        log.info("Extension created id={} tenantId={}", entity.getId(), tenantId);
        return ExtensionResponse.from(entity);
    }
    public ExtensionResponse update(Long id, UpdateExtensionRequest request) {
        Extension entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionNumberAndIdNot(tenantId, request.extensionNumber(), id)) throw new DuplicateResourceException("Extension");
        references.requireTarget(tenantId, request.targetType(), request.targetId());
        entity.setExtensionNumber(request.extensionNumber());
        entity.setName(request.name());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        repository.flush();

        log.info("Extension updated id={} tenantId={}", id, tenantId);
        return ExtensionResponse.from(entity);
    }
    public void delete(Long id) {
        Extension entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("EXTENSION", id);

        repository.delete(entity); repository.flush();
        log.info("Extension deleted id={} tenantId={}", id, entity.getTenantId());
    }
    private Extension find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Extension"));
    }

}
