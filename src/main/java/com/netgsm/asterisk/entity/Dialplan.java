package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "dialplans", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "extension", "priority"}))
public class Dialplan extends TenantEntity {
    @Column(nullable = false)
    private String extension;
    @Column(nullable = false)
    private Integer priority;
    @Column(nullable = false)
    private String application;
    @Column(nullable = false)
    private String applicationData;
    @Column(nullable = false)
    private Boolean enabled;
    @Column(nullable = false)
    private String context;
}
