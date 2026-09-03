package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "queue_members", schema = "platform",
        uniqueConstraints = @UniqueConstraint(columnNames = {"queue_id", "endpoint_id"}))
public class QueueMember extends TenantEntity {
    @Column(nullable = false) private Long queueId;
    @Column(nullable = false) private Long endpointId;
    @Column(nullable = false) private Integer penalty;
    @Column(nullable = false) private Boolean paused;
}
