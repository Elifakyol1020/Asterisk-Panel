package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.enums.Role;

import com.netgsm.asterisk.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "users", schema = "platform")
public class User extends BaseEntity {
    private Long tenantId;
    @Column(nullable = false, unique = true, length = 80)
    private String username;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private Role role;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false)
    private long authVersion;
}
