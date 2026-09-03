package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.entity.Ivr;
import org.springframework.stereotype.Component;

@Component
public class IvrMapper {

    public Ivr toEntity(CreateIvrRequest request, Long tenantId) {
        Ivr entity = new Ivr();
        entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setAudioFile(request.audioFile());
        entity.setTimeout(request.timeout());
        entity.setMaxAttempts(request.maxAttempts());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateIvrRequest request, Ivr entity) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setAudioFile(request.audioFile());
        entity.setTimeout(request.timeout());
        entity.setMaxAttempts(request.maxAttempts());
        entity.setEnabled(request.enabled());
    }

    public IvrResponse toResponse(Ivr entity) {
        return new IvrResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAudioFile(),
                entity.getTimeout(),
                entity.getMaxAttempts(),
                entity.getEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
