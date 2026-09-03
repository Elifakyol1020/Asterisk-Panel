package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateDialplanRequest;
import com.netgsm.asterisk.dto.DialplanResponse;
import com.netgsm.asterisk.dto.UpdateDialplanRequest;
import com.netgsm.asterisk.entity.Dialplan;
import org.springframework.stereotype.Component;

@Component
public class DialplanMapper {

    public Dialplan toEntity(CreateDialplanRequest request, Long tenantId) {
        Dialplan entity = new Dialplan();
        entity.setTenantId(tenantId);
        entity.setExtension(request.extension());
        entity.setPriority(request.priority());
        entity.setApplication(request.application());
        entity.setApplicationData(request.applicationData());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateDialplanRequest request, Dialplan entity) {
        entity.setExtension(request.extension());
        entity.setPriority(request.priority());
        entity.setApplication(request.application());
        entity.setApplicationData(request.applicationData());
        entity.setEnabled(request.enabled());
    }

    public DialplanResponse toResponse(Dialplan entity) {
        return new DialplanResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getExtension(),
                entity.getPriority(),
                entity.getApplication(),
                entity.getApplicationData(),
                entity.getEnabled(),
                entity.getContext(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
