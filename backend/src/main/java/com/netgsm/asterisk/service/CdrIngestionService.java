package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.CdrInput;
import com.netgsm.asterisk.dto.response.CdrResponse;
import com.netgsm.asterisk.mapper.CdrMapper;
import com.netgsm.asterisk.repository.CdrRepository;
import com.netgsm.asterisk.exception.PlatformException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor @Slf4j
public class CdrIngestionService {
    private final TenantResolver resolver;
    private final CdrMapper mapper;
    private final CdrRepository repository;
    private final Validator validator;
    private final ApplicationEventPublisher events;
    public CdrResponse ingest(CdrInput input) {
        if (input == null || !validator.validate(input).isEmpty()) throw new IllegalArgumentException("Invalid CDR input");
        if (input.endTime().isBefore(input.startTime()) || input.billsec() > input.duration()
                || (input.answerTime() != null && (input.answerTime().isBefore(input.startTime()) || input.answerTime().isAfter(input.endTime()))))
            throw new IllegalArgumentException("Inconsistent CDR times");
        if (!Set.of("ANSWERED","NO ANSWER","BUSY","FAILED","CONGESTION").contains(input.disposition().trim().toUpperCase(java.util.Locale.ROOT)))
            throw new IllegalArgumentException("Invalid CDR disposition");
        Long tenantId;
        try { tenantId = resolver.resolveTenant(input); }
        catch (PlatformException ex) {
            log.warn("CDR rejected: tenant resolution failed, code={}", ex.code());
            events.publishEvent(new CdrRejectedEvent(input, ex.code()));
            throw ex;
        }
        if (tenantId == null || tenantId <= 0) throw new PlatformException(422, "CDR_TENANT_UNRESOLVED", "Tenant required");
        return mapper.toResponse(repository.save(mapper.toDocument(input, tenantId)));
    }
}
