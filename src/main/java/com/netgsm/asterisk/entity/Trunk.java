package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "trunks", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
public class Trunk extends TenantEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private Integer port;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String transport;
    @Column
    private String fromUser;
    @Column
    private String fromDomain;
    @Column(nullable = false)
    private Boolean enabled;
    @Column(nullable = false)
    private String context;
    @Column(nullable = false, length = 100)
    private String passwordHash;
}
