package com.netgsm.asterisk.entity;

import com.netgsm.asterisk.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "tenants", schema = "platform")
public class Tenant extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 48)
    private String code;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TenantStatus status;
}
