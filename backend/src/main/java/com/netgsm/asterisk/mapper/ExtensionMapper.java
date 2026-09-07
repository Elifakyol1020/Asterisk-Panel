package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.request.CreateExtensionRequest;
import com.netgsm.asterisk.dto.response.ExtensionResponse;
import com.netgsm.asterisk.dto.request.UpdateExtensionRequest;
import com.netgsm.asterisk.entity.Extension;
import org.springframework.stereotype.Component;

@Component
public class ExtensionMapper {

    public Extension toEntity(CreateExtensionRequest request, Long tenantId) {
        Extension entity = new Extension();
        entity.setTenantId(tenantId);
        entity.setExtensionNumber(request.extensionNumber());
        entity.setName(request.name());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateExtensionRequest request, Extension entity) {
        entity.setExtensionNumber(request.extensionNumber());
        entity.setName(request.name());
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setEnabled(request.enabled());
    }

    public ExtensionResponse toResponse(Extension entity) {
        return new ExtensionResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getExtensionNumber(),
                entity.getName(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getEnabled(),
                entity.getContext(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
