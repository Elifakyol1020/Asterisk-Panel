package com.netgsm.asterisk.mapper;

import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.entity.Queue;
import org.springframework.stereotype.Component;

@Component
public class QueueMapper {

    public Queue toEntity(CreateQueueRequest request, Long tenantId) {
        Queue entity = new Queue();
        entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setStrategy(request.strategy());
        entity.setTimeout(request.timeout());
        entity.setRetry(request.retry());
        entity.setWrapupTime(request.wrapupTime());
        entity.setMaxLength(request.maxLength());
        entity.setMusicOnHold(request.musicOnHold());
        entity.setEnabled(request.enabled());
        return entity;
    }

    public void update(UpdateQueueRequest request, Queue entity) {
        entity.setName(request.name());
        entity.setStrategy(request.strategy());
        entity.setTimeout(request.timeout());
        entity.setRetry(request.retry());
        entity.setWrapupTime(request.wrapupTime());
        entity.setMaxLength(request.maxLength());
        entity.setMusicOnHold(request.musicOnHold());
        entity.setEnabled(request.enabled());
    }

    public QueueResponse toResponse(Queue entity) {
        return new QueueResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getStrategy(),
                entity.getTimeout(),
                entity.getRetry(),
                entity.getWrapupTime(),
                entity.getMaxLength(),
                entity.getMusicOnHold(),
                entity.getEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
