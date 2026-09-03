package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "endpoints", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "extension"}))
public class Endpoint extends TenantEntity {
    @Column(nullable = false)
    private String extension;
    @Column(nullable = false)
    private String displayName;
    @Column(nullable = false)
    private String transport;
    @Column(nullable = false)
    private String codecs;
    @Column(nullable = false)
    private Boolean enabled;
    @Column(nullable = false)
    private String context;
    @Column(nullable = false, length = 100)
    private String passwordHash;
}
