package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.IvrOption;
public record IvrOptionResponse(Long id, Long tenantId, Long ivrId, String digit, String actionType, Long targetId) {
    public static IvrOptionResponse from(IvrOption option) {
        return new IvrOptionResponse(option.getId(), option.getTenantId(), option.getIvrId(), option.getDigit(), option.getActionType(), option.getTargetId());
    }
}
