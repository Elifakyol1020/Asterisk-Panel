package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrOptionRequest;
import com.netgsm.asterisk.dto.IvrOptionResponse;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.entity.Ivr;
import com.netgsm.asterisk.entity.IvrOption;
import com.netgsm.asterisk.repository.IvrOptionRepository;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.service.CurrentUserService;
import com.netgsm.asterisk.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j @Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class IvrOptionService {
    private final IvrRepository ivrs;
    private final IvrOptionRepository options;
    private final CurrentUserService current;
    private final ReferenceService references;
    @Transactional(readOnly = true)
    public Page<IvrOptionResponse> list(Long ivrId, Pageable page) {
        Ivr ivr = find(ivrId); return options.findAllByIvrIdAndTenantId(ivrId, ivr.getTenantId(), page).map(IvrOptionResponse::from);
    }
    public IvrOptionResponse create(Long ivrId, IvrOptionRequest request) {
        Ivr ivr = find(ivrId); current.tenantForCreate(ivr.getTenantId());
        if (options.existsByIvrIdAndDigit(ivrId, request.digit())) throw new DuplicateResourceException("IVR digit");
        IvrOption option = new IvrOption(); option.setIvrId(ivrId); option.setTenantId(ivr.getTenantId());
        apply(option, request); options.saveAndFlush(option);
        log.info("IVR option created id={} tenantId={}", option.getId(), option.getTenantId()); return IvrOptionResponse.from(option);
    }
    public IvrOptionResponse update(Long ivrId, Long id, IvrOptionRequest request) {
        Ivr ivr = find(ivrId); current.tenantForCreate(ivr.getTenantId());
        var option = options.findByIdAndIvrIdAndTenantId(id, ivrId, ivr.getTenantId()).orElseThrow(() -> new ResourceNotFoundException("IVR option"));
        if (options.existsByIvrIdAndDigitAndIdNot(ivrId, request.digit(), id)) throw new DuplicateResourceException("IVR digit");
        apply(option, request); options.flush(); log.info("IVR option updated id={}", id); return IvrOptionResponse.from(option);
    }
    public void delete(Long ivrId, Long id) {
        Ivr ivr = find(ivrId);
        current.tenantForCreate(ivr.getTenantId());
        options.delete(options.findByIdAndIvrIdAndTenantId(id, ivrId, ivr.getTenantId()).orElseThrow(() -> new ResourceNotFoundException("IVR option")));
        log.info("IVR option deleted id={}", id);
    }
    private void apply(IvrOption option, IvrOptionRequest request) {
        if ("IVR".equals(request.actionType()) && option.getIvrId().equals(request.targetId()))
            throw new BusinessRuleException("IVR cannot target itself");
        references.requireTarget(option.getTenantId(), request.actionType(), request.targetId());
        option.setDigit(request.digit()); option.setActionType(request.actionType()); option.setTargetId(request.targetId());
    }
    private Ivr find(Long id) {
        return (current.isSuperAdmin() ? ivrs.findById(id) : ivrs.findByIdAndTenantId(id, current.getCurrentTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("IVR"));
    }
}
