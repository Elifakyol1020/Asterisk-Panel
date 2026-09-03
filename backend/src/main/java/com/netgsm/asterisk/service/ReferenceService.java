package com.netgsm.asterisk.service;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.repository.TrunkRepository;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.repository.IvrOptionRepository;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.repository.ExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class ReferenceService {
    private final EndpointRepository endpoints;
    private final TrunkRepository trunks;
    private final QueueRepository queues;
    private final QueueMemberRepository members;
    private final IvrRepository ivrs;
    private final IvrOptionRepository options;
    private final ExtensionRepository extensions;
    public void requireTarget(Long tenantId, String type, Long id) {
        if ("HANGUP".equals(type)) {
            if (id != null) throw new BusinessRuleException("HANGUP must not have a target");
            return;
        }
        if ("CUSTOM".equals(type)) throw new BusinessRuleException("CUSTOM requires a reviewed tenant-safe dialplan integration");
        if (id == null) throw new BusinessRuleException("Target is required");
        boolean exists = switch (type) {
            case "ENDPOINT" -> endpoints.existsByIdAndTenantId(id, tenantId);
            case "TRUNK" -> trunks.existsByIdAndTenantId(id, tenantId);
            case "QUEUE" -> queues.existsByIdAndTenantId(id, tenantId);
            case "IVR" -> ivrs.existsByIdAndTenantId(id, tenantId);
            case "EXTENSION" -> extensions.existsByIdAndTenantId(id, tenantId);
            default -> throw new BusinessRuleException("Unsupported target type");
        };
        if (!exists) throw new ResourceNotFoundException("Target");
    }
    public void requireUnreferenced(String type, Long id) {
        boolean referenced = extensions.existsByTargetTypeAndTargetId(type, id)
                || options.existsByActionTypeAndTargetId(type, id)
                || ("ENDPOINT".equals(type) && members.existsByEndpointId(id))
                || ("QUEUE".equals(type) && members.existsByQueueId(id))
                || ("IVR".equals(type) && options.existsByIvrId(id));
        if (referenced) throw new DuplicateResourceException("Referenced resource; remove its references first");
    }
    public void validateDialplan(String application, String data) {
        boolean valid = switch (application) {
            case "Answer", "Hangup" -> data.isEmpty();
            case "Playback" -> data.matches("[a-zA-Z0-9_-]{1,120}");
            case "Wait" -> data.matches("[0-9]{1,3}") && Integer.parseInt(data) <= 300;
            default -> false;
        };
        if (!valid) throw new BusinessRuleException("Unsupported application or unsafe dialplan arguments");
    }
}
