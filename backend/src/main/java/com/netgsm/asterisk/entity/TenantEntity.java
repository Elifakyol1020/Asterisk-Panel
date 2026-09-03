package com.netgsm.asterisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @MappedSuperclass
public abstract class TenantEntity extends BaseEntity {
    @Column(nullable = false, updatable = false)
    private Long tenantId;
}
