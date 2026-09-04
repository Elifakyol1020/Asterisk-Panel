package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "ps_auths", schema = "public")
public class PsAuth {
    @Id
    @Column(length = 255)
    private String id;
    @Column(name = "auth_type")
    private String authType;
    @Column(length = 40)
    private String username;
    @Column(length = 80)
    private String password;
}
