package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "extensions", schema = "platform", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "extension_number"}))
public class Extension extends TenantEntity {
    @Column(nullable = false)
    private String extensionNumber;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String targetType;
    @Column(nullable = false)
    private Long targetId;
    @Column(nullable = false)
    private Boolean enabled;
    @Column(nullable = false)
    private String context;
}
