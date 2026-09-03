package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrOptionRequest;
import com.netgsm.asterisk.dto.IvrOptionResponse;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.entity.Ivr;
import com.netgsm.asterisk.repository.IvrRepository;
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
public class IvrService {
    private final IvrRepository repository;
    private final CurrentUserService current;
    private final com.netgsm.asterisk.service.ReferenceService references;
    @Transactional(readOnly = true)
    public Page<IvrResponse> list(Long requestedTenantId, Pageable page) {
        Long tenantId = current.tenantForList(requestedTenantId);
        return (tenantId == null ? repository.findAll(page) : repository.findAllByTenantId(tenantId, page)).map(IvrResponse::from);
    }
    @Transactional(readOnly = true)
    public IvrResponse get(Long id) { return IvrResponse.from(find(id)); }
    public IvrResponse create(CreateIvrRequest request) {
        Long tenantId = current.tenantForCreate(request.tenantId());
        if (repository.existsByTenantIdAndName(tenantId, request.name())) throw new DuplicateResourceException("Ivr");

        Ivr entity = new Ivr(); entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setAudioFile(request.audioFile());
        entity.setTimeout(request.timeout());
        entity.setMaxAttempts(request.maxAttempts());
        entity.setEnabled(request.enabled());

        repository.saveAndFlush(entity);

        log.info("Ivr created id={} tenantId={}", entity.getId(), tenantId);
        return IvrResponse.from(entity);
    }
    public IvrResponse update(Long id, UpdateIvrRequest request) {
        Ivr entity = find(id); Long tenantId = entity.getTenantId();
        current.tenantForCreate(tenantId);
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.name(), id)) throw new DuplicateResourceException("Ivr");

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setAudioFile(request.audioFile());
        entity.setTimeout(request.timeout());
        entity.setMaxAttempts(request.maxAttempts());
        entity.setEnabled(request.enabled());

        repository.flush();

        log.info("Ivr updated id={} tenantId={}", id, tenantId);
        return IvrResponse.from(entity);
    }
    public void delete(Long id) {
        Ivr entity = find(id);
        current.tenantForCreate(entity.getTenantId());
        references.requireUnreferenced("IVR", id);

        repository.delete(entity); repository.flush();
        log.info("Ivr deleted id={} tenantId={}", id, entity.getTenantId());
    }
    private Ivr find(Long id) {
        return (current.isSuperAdmin() ? repository.findById(id) : repository.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Ivr"));
    }

}
