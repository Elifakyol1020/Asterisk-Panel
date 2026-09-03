package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "ivrs", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
public class Ivr extends TenantEntity {
    @Column(nullable = false)
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false)
    private String audioFile;
    @Column(nullable = false)
    private Integer timeout;
    @Column(nullable = false)
    private Integer maxAttempts;
    @Column(nullable = false)
    private Boolean enabled;
}
