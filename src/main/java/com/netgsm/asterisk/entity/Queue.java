package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "queues", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
public class Queue extends TenantEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String strategy;
    @Column(nullable = false)
    private Integer timeout;
    @Column(nullable = false)
    private Integer retry;
    @Column(nullable = false)
    private Integer wrapupTime;
    @Column(nullable = false)
    private Integer maxLength;
    @Column(nullable = false)
    private String musicOnHold;
    @Column(nullable = false)
    private Boolean enabled;
}
