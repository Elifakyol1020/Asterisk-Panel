package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "extensions", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {"context", "exten", "priority"}))
public class AsteriskExtension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40)
    private String context;
    @Column(nullable = false, length = 40)
    private String exten;
    @Column(nullable = false)
    private Integer priority;
    @Column(nullable = false, length = 40)
    private String app;
    @Column(nullable = false, length = 256)
    private String appdata;
}
