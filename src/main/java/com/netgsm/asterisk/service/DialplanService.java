package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateDialplanRequest;
import com.netgsm.asterisk.dto.DialplanResponse;
import com.netgsm.asterisk.dto.UpdateDialplanRequest;
import com.netgsm.asterisk.entity.Dialplan;
import com.netgsm.asterisk.repository.DialplanRepository;
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
public class DialplanService {
    private final DialplanRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    private final com.netgsm.asterisk.service.AsteriskRealtimeService realtime;
    @Transactional(readOnly = true)
    public Page<DialplanResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(DialplanResponse::from);
    }
    @Transactional(readOnly = true)
    public DialplanResponse get(Long id) { return DialplanResponse.from(find(id)); }
    public DialplanResponse create(CreateDialplanRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndExtensionAndPriority(tenantId, request.extension(), request.priority())) throw new DuplicateResourceException("Dialplan");
        references.validateDialplan(request.application(), request.applicationData());
        Dialplan entity = new Dialplan(); entity.setTenantId(tenantId);
        entity.setExtension(request.extension());
        entity.setPriority(request.priority());
        entity.setApplication(request.application());
        entity.setApplicationData(request.applicationData());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        repository.saveAndFlush(entity);
        realtime.save(entity);
        log.info("Dialplan created id={} tenantId={}", entity.getId(), tenantId);
        return DialplanResponse.from(entity);
    }
    public DialplanResponse update(Long id, UpdateDialplanRequest request) {
        Dialplan entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndExtensionAndPriorityAndIdNot(tenantId, request.extension(), request.priority(), id)) throw new DuplicateResourceException("Dialplan");
        references.validateDialplan(request.application(), request.applicationData());
        entity.setExtension(request.extension());
        entity.setPriority(request.priority());
        entity.setApplication(request.application());
        entity.setApplicationData(request.applicationData());
        entity.setEnabled(request.enabled());

        entity.setContext(current.context(tenantId, "internal"));
        repository.flush();
        realtime.save(entity);
        log.info("Dialplan updated id={} tenantId={}", id, tenantId);
        return DialplanResponse.from(entity);
    }
    public void delete(Long id) {
        Dialplan entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("DIALPLAN", id);
        realtime.delete(entity);
        repository.delete(entity); repository.flush();
        log.info("Dialplan deleted id={} tenantId={}", id, entity.getTenantId());
    }
    private Dialplan find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Dialplan"));
    }

}
