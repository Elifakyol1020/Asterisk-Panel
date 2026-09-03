package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.IvrOptionRequest;
import com.netgsm.asterisk.dto.IvrOptionResponse;
import com.netgsm.asterisk.entity.IvrOption;
import org.springframework.stereotype.Component;

@Component
public class IvrOptionMapper {

    public IvrOption toEntity(IvrOptionRequest request, Long ivrId, Long tenantId) {
        IvrOption option = new IvrOption();
        option.setIvrId(ivrId);
        option.setTenantId(tenantId);
        update(request, option);
        return option;
    }

    public void update(IvrOptionRequest request, IvrOption option) {
        option.setDigit(request.digit());
        option.setActionType(request.actionType());
        option.setTargetId(request.targetId());
    }

    public IvrOptionResponse toResponse(IvrOption option) {
        return new IvrOptionResponse(
                option.getId(),
                option.getTenantId(),
                option.getIvrId(),
                option.getDigit(),
                option.getActionType(),
                option.getTargetId());
    }
}
